package com.nordnetab.chcp.main.updater;

import android.text.TextUtils;
import android.util.Log;

import com.nordnetab.chcp.main.config.ApplicationConfig;
import com.nordnetab.chcp.main.config.ContentManifest;
import com.nordnetab.chcp.main.events.NothingToUpdateEvent;
import com.nordnetab.chcp.main.events.UpdateDownloadErrorEvent;
import com.nordnetab.chcp.main.events.UpdateIsReadyToInstallEvent;
import com.nordnetab.chcp.main.events.WorkerEvent;
import com.nordnetab.chcp.main.model.ChcpError;
import com.nordnetab.chcp.main.model.ManifestDiff;
import com.nordnetab.chcp.main.model.ManifestFile;
import com.nordnetab.chcp.main.model.PluginFilesStructure;
import com.nordnetab.chcp.main.network.ApplicationConfigDownloader;
import com.nordnetab.chcp.main.network.ContentManifestDownloader;
import com.nordnetab.chcp.main.network.DownloadResult;
import com.nordnetab.chcp.main.network.FileDownloader;
import com.nordnetab.chcp.main.storage.ApplicationConfigStorage;
import com.nordnetab.chcp.main.storage.ContentManifestStorage;
import com.nordnetab.chcp.main.storage.IObjectFileStorage;
import com.nordnetab.chcp.main.utils.FilesUtility;
import com.nordnetab.chcp.main.utils.URLUtility;

import java.io.IOException;
import java.util.List;

/**
 * Created by Nikolay Demyankov on 28.07.15.
 * <p/>
 * Worker, that implements update download logic.
 * During the download process events are dispatched to notify the subscribers about the progress.
 * <p/>
 * Used internally.
 */
class UpdateLoaderWorker implements WorkerTask {

    private final String applicationConfigUrl;
    private final int appNativeVersion;
    private final PluginFilesStructure filesStructure;

    private IObjectFileStorage<ApplicationConfig> appConfigStorage;
    private IObjectFileStorage<ContentManifest> manifestStorage;

    private ApplicationConfig oldAppConfig;
    private ContentManifest oldManifest;

    private WorkerEvent resultEvent;

    /**
     * Constructor.
     *
     * @param configUrl                   application config url
     * @param currentReleaseFileStructure files structure of the current release
     * @param currentNativeVersion        current native version of the
     */
    public UpdateLoaderWorker(final String configUrl, final PluginFilesStructure currentReleaseFileStructure, final int currentNativeVersion) {
        filesStructure = currentReleaseFileStructure;
        applicationConfigUrl = configUrl;
        appNativeVersion = currentNativeVersion;
    }

    @Override
    public void run() {
        Log.d("CHCP", "Starting loader worker ");
        // initialize before running
        if (!init()) {
            return;
        }

        // download new application config
        ApplicationConfig newAppConfig = downloadApplicationConfig();
        if (newAppConfig == null) {
            setErrorResult(ChcpError.FAILED_TO_DOWNLOAD_APPLICATION_CONFIG, null);
            return;
        }

        // check if there is a new content version available
        if (newAppConfig.getContentConfig().getReleaseVersion().equals(oldAppConfig.getContentConfig().getReleaseVersion())) {
            setNothingToUpdateResult(newAppConfig);
            return;
        }

        // check if current native version supports new content
        if (newAppConfig.getContentConfig().getMinimumNativeVersion() > appNativeVersion) {
            setErrorResult(ChcpError.APPLICATION_BUILD_VERSION_TOO_LOW, newAppConfig);
            return;
        }

        // download new content manifest
        ContentManifest newContentManifest = downloadContentManifest(newAppConfig);
        if (newContentManifest == null) {
            setErrorResult(ChcpError.FAILED_TO_DOWNLOAD_CONTENT_MANIFEST, newAppConfig);
            return;
        }

        // find files that were updated
        ManifestDiff diff = oldManifest.calculateDifference(newContentManifest);
        if (diff.isEmpty()) {
            manifestStorage.storeInFolder(newContentManifest, filesStructure.getWwwFolder());
            appConfigStorage.storeInFolder(newAppConfig, filesStructure.getWwwFolder());
            setNothingToUpdateResult(newAppConfig);

            return;
        }

        // switch file structure to new release
        filesStructure.switchToRelease(newAppConfig.getContentConfig().getReleaseVersion());

        recreateDownloadFolder(filesStructure.getDownloadFolder());

        // download files
        boolean isDownloaded = downloadNewAndChangedFiles(newAppConfig, diff);
        if (!isDownloaded) {
            cleanUp();
            setErrorResult(ChcpError.FAILED_TO_DOWNLOAD_UPDATE_FILES, newAppConfig);
            return;
        }

        // store configs
        manifestStorage.storeInFolder(newContentManifest, filesStructure.getDownloadFolder());
        appConfigStorage.storeInFolder(newAppConfig, filesStructure.getDownloadFolder());

        // notify that we are done
        setSuccessResult(newAppConfig);

        Log.d("CHCP", "Loader worker has finished");
    }

    /**
     * Initialize all required variables before running the update.
     *
     * @return <code>true</code> if we are good to go, <code>false</code> - failed to initialize
     */
    private boolean init() {
        manifestStorage = new ContentManifestStorage();
        appConfigStorage = new ApplicationConfigStorage();

        // load current application config
        oldAppConfig = appConfigStorage.loadFromFolder(filesStructure.getWwwFolder());
        if (oldAppConfig == null) {
            setErrorResult(ChcpError.LOCAL_VERSION_OF_APPLICATION_CONFIG_NOT_FOUND, null);
            return false;
        }

        // load current content manifest
        oldManifest = manifestStorage.loadFromFolder(filesStructure.getWwwFolder());
        if (oldManifest == null) {
            setErrorResult(ChcpError.LOCAL_VERSION_OF_MANIFEST_NOT_FOUND, null);
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

        final String url = URLUtility.construct(contentUrl, PluginFilesStructure.MANIFEST_FILE_NAME);
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
    private boolean downloadNewAndChangedFiles(ApplicationConfig newAppConfig, ManifestDiff diff) {
        final String contentUrl = newAppConfig.getContentConfig().getContentUrl();
        if (TextUtils.isEmpty(contentUrl)) {
            Log.d("CHCP", "Content url is not set in your application config! Can't load updates.");
            return false;
        }

        List<ManifestFile> downloadFiles = diff.getUpdateFiles();

        boolean isFinishedWithSuccess = true;
        try {
            FileDownloader.downloadFiles(filesStructure.getDownloadFolder(), contentUrl, downloadFiles);
        } catch (IOException e) {
            e.printStackTrace();
            isFinishedWithSuccess = false;
        }

        return isFinishedWithSuccess;
    }

    /**
     * Remove temporary files
     */
    private void cleanUp() {
        FilesUtility.delete(filesStructure.getContentFolder());
    }

    // region Events

    private void setErrorResult(ChcpError error, ApplicationConfig newAppConfig) {
        resultEvent = new UpdateDownloadErrorEvent(error, newAppConfig);
    }

    private void setSuccessResult(ApplicationConfig newAppConfig) {
        resultEvent = new UpdateIsReadyToInstallEvent(newAppConfig);
    }

    private void setNothingToUpdateResult(ApplicationConfig newAppConfig) {
        resultEvent = new NothingToUpdateEvent(newAppConfig);
    }

    @Override
    public WorkerEvent result() {
        return resultEvent;
    }

    // endregion
}