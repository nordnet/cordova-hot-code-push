package com.nordnetab.chcp.updater;

import android.content.Context;
import android.util.Log;

import com.nordnetab.chcp.config.ApplicationConfig;
import com.nordnetab.chcp.config.ContentManifest;
import com.nordnetab.chcp.events.NothingToUpdateEvent;
import com.nordnetab.chcp.events.UpdateDownloadErrorEvent;
import com.nordnetab.chcp.events.UpdateIsReadyToInstallEvent;
import com.nordnetab.chcp.model.ChcpError;
import com.nordnetab.chcp.model.ManifestDiff;
import com.nordnetab.chcp.model.ManifestFile;
import com.nordnetab.chcp.model.IPluginFilesStructure;
import com.nordnetab.chcp.network.ApplicationConfigDownloader;
import com.nordnetab.chcp.network.ContentManifestDownloader;
import com.nordnetab.chcp.network.FileDownloader;
import com.nordnetab.chcp.storage.ApplicationConfigStorage;
import com.nordnetab.chcp.storage.ContentManifestStorage;
import com.nordnetab.chcp.storage.IConfigFileStorage;
import com.nordnetab.chcp.utils.FilesUtility;
import com.nordnetab.chcp.utils.URLUtility;
import com.nordnetab.chcp.utils.VersionHelper;

import java.io.IOException;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by Nikolay Demyankov on 28.07.15.
 *
 *
 */
class UpdateLoaderWorker implements Runnable {

    private final IConfigFileStorage<ApplicationConfig> appConfigStorage;
    private final IConfigFileStorage<ContentManifest> manifestStorage;
    private final String applicationConfigUrl;
    private final int appBuildVersion;
    private final IPluginFilesStructure filesStructure;

    private ApplicationConfig newAppConfig;

    private String workerId;

    public UpdateLoaderWorker(Context context, String configUrl, final IPluginFilesStructure filesStructure) {
        this.workerId = generateId();

        this.filesStructure = filesStructure;
        applicationConfigUrl = configUrl;

        manifestStorage = new ContentManifestStorage(filesStructure);
        appConfigStorage = new ApplicationConfigStorage(filesStructure);

        appBuildVersion = VersionHelper.applicationVersionCode(context);
    }

    private String generateId() {
        return System.currentTimeMillis() + "";
    }

    public String getWorkerId() {
        return workerId;
    }

    private void waitForInstallationToFinish() {
        while (UpdatesInstaller.isInstalling()) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void run() {
        Log.d("CHCP", "Starting loader worker " + getWorkerId());

        // load configs
        ApplicationConfig oldAppConfig = appConfigStorage.loadFromFolder(filesStructure.wwwFolder());
        ContentManifest oldContentManifest = manifestStorage.loadFromFolder(filesStructure.wwwFolder());

        // download new application config
        newAppConfig = downloadApplicationConfig();
        if (newAppConfig == null) {
            dispatchErrorEvent(ChcpError.FAILED_TO_DOWNLOAD_APPLICATION_CONFIG);
            return;
        }

        // check if there is a new content version available
        if (newAppConfig.getContentConfig().getReleaseVersion().equals(oldAppConfig.getContentConfig().getReleaseVersion())) {
            dispatchNothingToUpdateEvent();
            return;
        }

        // check if current native version supports new content
        if (newAppConfig.getContentConfig().getMinimumNativeVersion() > appBuildVersion) {
            dispatchErrorEvent(ChcpError.APPLICATION_BUILD_VERSION_TOO_LOW);
            return;
        }

        // download new content manifest
        ContentManifest newContentManifest = downloadContentManifest(newAppConfig);
        if (newContentManifest == null) {
            dispatchErrorEvent(ChcpError.FAILED_TO_DOWNLOAD_CONTENT_MANIFEST);
            return;
        }

        // find files that were updated
        ManifestDiff diff = oldContentManifest.calculateDifference(newContentManifest);
        if (diff.isEmpty()) {
            manifestStorage.storeInFolder(newContentManifest, filesStructure.wwwFolder());
            appConfigStorage.storeInFolder(newAppConfig, filesStructure.wwwFolder());

            dispatchNothingToUpdateEvent();

            return;
        }

        recreateDownloadFolder(filesStructure.downloadFolder());

        // download files
        boolean isDownloaded = downloadNewAndChagedFiles(diff);
        if (!isDownloaded) {
            cleanUp();
            dispatchErrorEvent(ChcpError.FAILED_TO_DOWNLOAD_UPDATE_FILES);

            return;
        }

        // store configs
        manifestStorage.storeInFolder(newContentManifest, filesStructure.downloadFolder());
        appConfigStorage.storeInFolder(newAppConfig, filesStructure.downloadFolder());

        waitForInstallationToFinish();

        boolean isReadyToInstall = moveDownloadedContentToInstallationFolder();
        if (!isReadyToInstall) {
            cleanUp();
            dispatchErrorEvent(ChcpError.FAILED_TO_MOVE_LOADED_FILES_TO_INSTALLATION_FOLDER);
            return;
        }

        cleanUp();

        // notify that we are done
        dispatchSuccessEvent();

        Log.d("CHCP", "Loader worker " + getWorkerId() + " has finished");
    }

    private void dispatchErrorEvent(ChcpError error) {
        EventBus.getDefault().post(new UpdateDownloadErrorEvent(getWorkerId(), error, newAppConfig));
    }

    private void dispatchSuccessEvent() {
        EventBus.getDefault().post(new UpdateIsReadyToInstallEvent(getWorkerId(), newAppConfig));
    }

    private void dispatchNothingToUpdateEvent() {
        EventBus.getDefault().post(new NothingToUpdateEvent(getWorkerId(), newAppConfig));
    }

    private ApplicationConfig downloadApplicationConfig() {
        ApplicationConfigDownloader.Result downloadResult = new ApplicationConfigDownloader(applicationConfigUrl).download();
        if (downloadResult.error != null) {
            Log.d("CHCP", "Failed to download application config");

            return null;
        }

        return downloadResult.config;
    }

    private ContentManifest downloadContentManifest(ApplicationConfig config) {
        String url = URLUtility.construct(config.getContentConfig().getContentUrl(), filesStructure.manifestFileName());

        ContentManifestDownloader.Result downloadResult = new ContentManifestDownloader(url).download();
        if (downloadResult.error != null) {
            Log.d("CHCP", "Failed to download content manifest");

            return null;
        }

        return downloadResult.manifest;
    }

    private void recreateDownloadFolder(final String folder) {
        FilesUtility.delete(folder);
        FilesUtility.ensureDirectoryExists(folder);
    }

    private boolean downloadNewAndChagedFiles(ManifestDiff diff) {
        final String contentUrl = newAppConfig.getContentConfig().getContentUrl();
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

    private void cleanUp() {
        FilesUtility.delete(filesStructure.downloadFolder());
    }

}
