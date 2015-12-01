package com.nordnetab.chcp.main.updater;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.nordnetab.chcp.main.config.ApplicationConfig;
import com.nordnetab.chcp.main.config.ContentManifest;
import com.nordnetab.chcp.main.events.NothingToUpdateEvent;
import com.nordnetab.chcp.main.events.UpdateDownloadErrorEvent;
import com.nordnetab.chcp.main.events.UpdateIsReadyToInstallEvent;
import com.nordnetab.chcp.main.model.ChcpError;
import com.nordnetab.chcp.main.model.ManifestDiff;
import com.nordnetab.chcp.main.model.ManifestFile;
import com.nordnetab.chcp.main.model.IPluginFilesStructure;
import com.nordnetab.chcp.main.network.ApplicationConfigDownloader;
import com.nordnetab.chcp.main.network.ContentManifestDownloader;
import com.nordnetab.chcp.main.network.DownloadResult;
import com.nordnetab.chcp.main.network.FileDownloader;
import com.nordnetab.chcp.main.storage.ApplicationConfigStorage;
import com.nordnetab.chcp.main.storage.ContentManifestStorage;
import com.nordnetab.chcp.main.storage.IObjectFileStorage;
import com.nordnetab.chcp.main.utils.FilesUtility;
import com.nordnetab.chcp.main.utils.URLUtility;
import com.nordnetab.chcp.main.utils.VersionHelper;

import java.io.IOException;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by Nikolay Demyankov on 28.07.15.
 * <p/>
 * Worker, that implements update download logic.
 * During the download process events are dispatched to notify the subscribers about the progress.
 * <p/>
 * Used internally.
 *
 * @see UpdatesLoader
 * @see UpdateDownloadErrorEvent
 * @see UpdateIsReadyToInstallEvent
 * @see NothingToUpdateEvent
 */
class UpdateLoaderWorker implements Runnable {

    private final String applicationConfigUrl;
    private final int appBuildVersion;
    private final IPluginFilesStructure filesStructure;
    private final String workerId;

    private IObjectFileStorage<ApplicationConfig> appConfigStorage;
    private IObjectFileStorage<ContentManifest> manifestStorage;

    private ApplicationConfig oldAppConfig;
    private ContentManifest oldManifest;

    public UpdateLoaderWorker(Context context, final String configUrl, final IPluginFilesStructure filesStructure) {
        this.workerId = generateId();

        this.filesStructure = filesStructure;
        applicationConfigUrl = configUrl;
        appBuildVersion = VersionHelper.applicationVersionCode(context);
    }

    private String generateId() {
        return System.currentTimeMillis() + "";
    }

    public String getWorkerId() {
        return workerId;
    }

    @Override
    public void run() {
        Log.d("CHCP", "Starting loader worker " + getWorkerId());
        // initialize before running
        if (!init()) {
            return;
        }

        // wait for possible installation process to end;
        // otherwise we can end up loading same stuff twice
        waitForInstallationToFinish();

        // download new application config
        ApplicationConfig newAppConfig = downloadApplicationConfig();
        if (newAppConfig == null) {
            dispatchErrorEvent(ChcpError.FAILED_TO_DOWNLOAD_APPLICATION_CONFIG, null);
            return;
        }

        // check if there is a new content version available
        if (newAppConfig.getContentConfig().getReleaseVersion().equals(oldAppConfig.getContentConfig().getReleaseVersion())) {
            dispatchNothingToUpdateEvent(newAppConfig);
            return;
        }

        // check if current native version supports new content
        if (newAppConfig.getContentConfig().getMinimumNativeVersion() > appBuildVersion) {
            dispatchErrorEvent(ChcpError.APPLICATION_BUILD_VERSION_TOO_LOW, newAppConfig);
            return;
        }

        // download new content manifest
        ContentManifest newContentManifest = downloadContentManifest(newAppConfig);
        if (newContentManifest == null) {
            dispatchErrorEvent(ChcpError.FAILED_TO_DOWNLOAD_CONTENT_MANIFEST, newAppConfig);
            return;
        }

        // find files that were updated
        ManifestDiff diff = oldManifest.calculateDifference(newContentManifest);
        if (diff.isEmpty()) {
            manifestStorage.storeInFolder(newContentManifest, filesStructure.wwwFolder());
            appConfigStorage.storeInFolder(newAppConfig, filesStructure.wwwFolder());
            dispatchNothingToUpdateEvent(newAppConfig);

            return;
        }

        recreateDownloadFolder(filesStructure.downloadFolder());

        // download files
        boolean isDownloaded = downloadNewAndChagedFiles(newAppConfig, diff);
        if (!isDownloaded) {
            cleanUp();
            dispatchErrorEvent(ChcpError.FAILED_TO_DOWNLOAD_UPDATE_FILES, newAppConfig);

            return;
        }

        // store configs
        manifestStorage.storeInFolder(newContentManifest, filesStructure.downloadFolder());
        appConfigStorage.storeInFolder(newAppConfig, filesStructure.downloadFolder());

        waitForInstallationToFinish();

        // copy all loaded content to installation folder
        boolean isReadyToInstall = moveDownloadedContentToInstallationFolder();
        if (!isReadyToInstall) {
            cleanUp();
            dispatchErrorEvent(ChcpError.FAILED_TO_MOVE_LOADED_FILES_TO_INSTALLATION_FOLDER, newAppConfig);
            return;
        }

        cleanUp();

        // notify that we are done
        dispatchSuccessEvent(newAppConfig);

        Log.d("CHCP", "Loader worker " + getWorkerId() + " has finished");
    }

    /**
     * Initialize all required variables before running the update.
     *
     * @return <code>true</code> if we are good to go, <code>false</code> - failed to initialize
     */
    private boolean init() {
        manifestStorage = new ContentManifestStorage(filesStructure);
        appConfigStorage = new ApplicationConfigStorage(filesStructure);

        // load current application config
        oldAppConfig = appConfigStorage.loadFromFolder(filesStructure.wwwFolder());
        if (oldAppConfig == null) {
            dispatchErrorEvent(ChcpError.LOCAL_VERSION_OF_APPLICATION_CONFIG_NOT_FOUND, null);
            return false;
        }

        // load current content manifest
        oldManifest = manifestStorage.loadFromFolder(filesStructure.wwwFolder());
        if (oldManifest == null) {
            dispatchErrorEvent(ChcpError.LOCAL_VERSION_OF_MANIFEST_NOT_FOUND, null);
            return false;
        }

        return true;
    }

    /**
     * Download application config from server.
     *
     * @return new application config
     */
    private ApplicationConfig downloadApplicationConfig() {
        DownloadResult<ApplicationConfig> downloadResult = new ApplicationConfigDownloader(applicationConfigUrl).download();
        if (downloadResult.error != null) {
            Log.d("CHCP", "Failed to download application config");

            return null;
        }

        return downloadResult.value;
    }

    /**
     * Download new content manifest from server.
     *
     * @param config new application config from which we will take content url
     * @return new content manifest
     */
    private ContentManifest downloadContentManifest(ApplicationConfig config) {
        final String contentUrl = config.getContentConfig().getContentUrl();
        if (TextUtils.isEmpty(contentUrl)) {
            Log.d("CHCP", "Content url is not set in your application config! Can't load updates.");
            return null;
        }

        final String url = URLUtility.construct(contentUrl, filesStructure.manifestFileName());
        DownloadResult<ContentManifest> downloadResult = new ContentManifestDownloader(url).download();
        if (downloadResult.error != null) {
            Log.d("CHCP", "Failed to download content manifest");
            return null;
        }

        return downloadResult.value;
    }

    /**
     * Remove old version of download folder and create a new one.
     *
     * @param folder absolute path to download folder
     */
    private void recreateDownloadFolder(final String folder) {
        FilesUtility.delete(folder);
        FilesUtility.ensureDirectoryExists(folder);
    }

    /**
     * Download from server new and update files.
     *
     * @param newAppConfig new application config, from which we will use content url
     * @param diff         manifest difference from which we will know, what files to download
     * @return <code>true</code> if files are loaded; <code>false</code> - otherwise
     */
    private boolean downloadNewAndChagedFiles(ApplicationConfig newAppConfig, ManifestDiff diff) {
        final String contentUrl = newAppConfig.getContentConfig().getContentUrl();
        if (TextUtils.isEmpty(contentUrl)) {
            Log.d("CHCP", "Content url is not set in your application config! Can't load updates.");
            return false;
        }

        List<ManifestFile> downloadFiles = diff.getUpdateFiles();

        boolean isFinishedWithSuccess = true;
        try {
            FileDownloader.downloadFiles(filesStructure.downloadFolder(), contentUrl, downloadFiles);
        } catch (IOException e) {
            e.printStackTrace();
            isFinishedWithSuccess = false;
        }

        return isFinishedWithSuccess;
    }

    /**
     * Copy all loaded files into installation folder.
     *
     * @return <code>true</code> if files are copied; <code>false</code> otherwise
     */
    private boolean moveDownloadedContentToInstallationFolder() {
        boolean isMoved = false;
        FilesUtility.ensureDirectoryExists(filesStructure.installationFolder());
        try {
            FilesUtility.copy(filesStructure.downloadFolder(), filesStructure.installationFolder());
            isMoved = true;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return isMoved;
    }

    /**
     * Remove temporary files
     */
    private void cleanUp() {
        FilesUtility.delete(filesStructure.downloadFolder());
    }

    /**
     * It is possible that download and installation are performed at the same time.
     * In that case we must wait for installation to finish before copying downloaded data into installation folder.
     */
    private void waitForInstallationToFinish() {
        while (UpdatesInstaller.isInstalling()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    // region Events

    private void dispatchErrorEvent(ChcpError error, ApplicationConfig newAppConfig) {
        EventBus.getDefault().post(new UpdateDownloadErrorEvent(getWorkerId(), error, newAppConfig));
    }

    private void dispatchSuccessEvent(ApplicationConfig newAppConfig) {
        EventBus.getDefault().post(new UpdateIsReadyToInstallEvent(getWorkerId(), newAppConfig));
    }

    private void dispatchNothingToUpdateEvent(ApplicationConfig newAppConfig) {
        EventBus.getDefault().post(new NothingToUpdateEvent(getWorkerId(), newAppConfig));
    }

    // endregion
}