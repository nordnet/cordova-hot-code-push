package com.nordnetab.chcp.updater;

import android.content.Context;
import android.util.Log;

import com.nordnetab.chcp.HotCodePushPlugin;
import com.nordnetab.chcp.config.ApplicationConfig;
import com.nordnetab.chcp.config.ContentManifest;
import com.nordnetab.chcp.model.ManifestDiff;
import com.nordnetab.chcp.model.ManifestFile;
import com.nordnetab.chcp.network.ApplicationConfigDownloader;
import com.nordnetab.chcp.network.ContentManifestDownloader;
import com.nordnetab.chcp.network.FileDownloader;
import com.nordnetab.chcp.storage.ApplicationConfigStorage;
import com.nordnetab.chcp.storage.ContentManifestStorage;
import com.nordnetab.chcp.utils.FilesUtility;
import com.nordnetab.chcp.utils.URLUtility;
import com.nordnetab.chcp.utils.VersionHelper;

import java.io.IOException;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by Nikolay Demyankov on 28.07.15.
 */
class UpdateLoaderWorker implements Runnable {

    private final ApplicationConfigStorage appConfigStorage;
    private final ContentManifestStorage manifestStorage;
    private final String downloadFolder;
    private final String applicationConfigUrl;
    private final int appBuildVersion;

    private ApplicationConfig newAppConfig;

    private String workerId;

    public UpdateLoaderWorker(Context context, String wwwFolder, String downloadFolder, String configUrl) {
        this.workerId = generateId();
        this.downloadFolder = downloadFolder;
        applicationConfigUrl = configUrl;
        manifestStorage = new ContentManifestStorage(context, wwwFolder);
        appConfigStorage = new ApplicationConfigStorage(context, wwwFolder);
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
        waitForInstallationToFinish();

        Log.d("CHCP", "Starting loader worker " + getWorkerId());

        // load configs
        ApplicationConfig oldAppConfig = appConfigStorage.loadFromFS();
        ContentManifest oldContentManifest = manifestStorage.loadFromFS();

        // download new application config
        newAppConfig = downloadApplicationConfig();
        if (newAppConfig == null) {
            EventBus.getDefault().post(new UpdatesLoader.UpdateErrorEvent(null, getWorkerId(), UpdatesLoader.ErrorType.FAILED_TO_DOWNLOAD_APPLICATION_CONFIG));
            return;
        }

        // check if there is a new content version available
        if (newAppConfig.getContentConfig().getReleaseVersion().equals(oldAppConfig.getContentConfig().getReleaseVersion())) {
            EventBus.getDefault().post(new UpdatesLoader.NothingToUpdateEvent(newAppConfig, getWorkerId()));
            return;
        }

        // check if current native version supports new content
        if (newAppConfig.getContentConfig().getMinimumNativeVersion() > appBuildVersion) {
            EventBus.getDefault().post(new UpdatesLoader.UpdateErrorEvent(newAppConfig, getWorkerId(), UpdatesLoader.ErrorType.APPLICATION_BUILD_VERSION_TOO_LOW));
            return;
        }

        // download new content manifest
        ContentManifest newContentManifest = downloadContentManifest(newAppConfig);
        if (newContentManifest == null) {
            EventBus.getDefault().post(new UpdatesLoader.UpdateErrorEvent(newAppConfig, getWorkerId(), UpdatesLoader.ErrorType.FAILED_TO_DOWNLOAD_CONTENT_MANIFEST));
            return;
        }

        // find files that were updated
        ManifestDiff diff = oldContentManifest.calculateDifference(newContentManifest);
        if (diff.isEmpty()) {
            EventBus.getDefault().post(new UpdatesLoader.NothingToUpdateEvent(newAppConfig, getWorkerId()));
            manifestStorage.storeOnFS(newContentManifest);
            appConfigStorage.storeOnFS(newAppConfig);
            return;
        }

        recreateDownloadFolder(downloadFolder);

        // download files
        boolean isDownloaded = downloadNewAndChagedFiles(diff);
        if (!isDownloaded) {
            FilesUtility.delete(downloadFolder);
            EventBus.getDefault().post(new UpdatesLoader.UpdateErrorEvent(newAppConfig, getWorkerId(), UpdatesLoader.ErrorType.FAILED_TO_DOWNLOAD_UPDATE_FILES));
            return;
        }

        // store configs
        manifestStorage.storeInPreference(newContentManifest);
        appConfigStorage.storeInPreference(newAppConfig);

        // notify that we are done
        EventBus.getDefault().post(new UpdatesLoader.UpdateIsReadyToInstallEvent(newAppConfig, getWorkerId()));

        Log.d("CHCP", "Loader worker " + getWorkerId() + " has finished");
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
        String url = URLUtility.construct(config.getContentConfig().getContentUrl(), HotCodePushPlugin.CONTENT_MANIFEST_FILE_NAME);

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
            FileDownloader.downloadFiles(downloadFolder, contentUrl, downloadFiles);
        } catch (IOException e) {
            e.printStackTrace();
            isFinishedWithSuccess = false;
        }

        return isFinishedWithSuccess;
    }

}
