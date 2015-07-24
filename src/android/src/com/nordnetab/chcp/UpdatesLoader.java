package com.nordnetab.chcp;

import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

import com.nordnetab.chcp.config.ApplicationConfig;
import com.nordnetab.chcp.config.ContentManifest;
import com.nordnetab.chcp.network.ApplicationConfigDownloader;
import com.nordnetab.chcp.network.ContentManifestDownloader;
import com.nordnetab.chcp.network.FileDownloader;
import com.nordnetab.chcp.storage.ApplicationConfigStorage;
import com.nordnetab.chcp.storage.ContentManifestStorage;
import com.nordnetab.chcp.utils.AssetsHelper;
import com.nordnetab.chcp.utils.FilesUtility;
import com.nordnetab.chcp.utils.URLUtility;

import java.io.File;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import de.greenrobot.event.EventBus;

/**
 * Created by Nikolay Demyankov on 24.07.15.
 *
 *
 */
public class UpdatesLoader {

    // region Events

    public enum Error {
        FAILED_TO_DOWNLOAD_APPLICATION_CONFIG,
        APPLICATION_BUILD_VERSION_TOO_LOW,
        FAILED_TO_DOWNLOAD_CONTENT_MANIFEST,
        FAILED_TO_DOWNLOAD_UPDATE_FILES
    }

    private static class UpdateProgressEvent {
        public final ApplicationConfig config;

        public UpdateProgressEvent(ApplicationConfig config) {
            this.config = config;
        }
    }

    public static class UpdateIsReadyToInstallEvent extends UpdateProgressEvent {

        public UpdateIsReadyToInstallEvent(ApplicationConfig config) {
            super(config);
        }
    }

    public static class NothingToUpdateEvent extends UpdateProgressEvent {

        public NothingToUpdateEvent(ApplicationConfig config) {
            super(config);
        }
    }

    public static class UpdateErrorEvent extends UpdateProgressEvent {
        public final Error error;

        public UpdateErrorEvent(ApplicationConfig config, Error error) {
            super(config);
            this.error = error;
        }
    }

    // endregion

    //TODO: add key for tasks to remove duplicates

    private static ConcurrentLinkedQueue<Runnable> queue;
    private static boolean isExecuting;

    public static void addUpdateTaskToQueue(Context context, String wwwFolder, String downloadFolder, String configUrl) {
        UpdateWorker task = new UpdateWorker(context, wwwFolder, downloadFolder, configUrl);
        queue.add(task);

        if (!isExecuting) {
            executeTaskFromQueue();
        }
    }

    private static void executeTaskFromQueue() {
        final Runnable task = getQueue().poll();
        if (task == null) {
            isExecuting = false;
            return;
        }
        isExecuting = true;

        new Thread(new Runnable() {
            @Override
            public void run() {
                task.run();

                executeTaskFromQueue();
            }
        }).start();
    }

    private static ConcurrentLinkedQueue<Runnable> getQueue() {
        if (queue == null) {
            queue = new ConcurrentLinkedQueue<Runnable>();
        }

        return queue;
    }

    private static class UpdateWorker implements Runnable {

        private final ApplicationConfigStorage appConfigStorage;
        private final ContentManifestStorage manifestStorage;
        private final String wwwFolder;
        private final String downloadFolder;
        private final String applicationConfigUrl;
        private final Context context;
        private final int appBuildVersion;

        private ApplicationConfig newAppConfig;
        private ApplicationConfig oldAppConfig;

        private ContentManifest newContentManifest;
        private ContentManifest oldContentManifest;

        public UpdateWorker(Context context, String wwwFolder, String downloadFolder, String configUrl) {
            this.context = context;
            this.wwwFolder = wwwFolder;
            this.downloadFolder = downloadFolder;
            applicationConfigUrl = configUrl;
            manifestStorage = new ContentManifestStorage(context, wwwFolder);
            appConfigStorage = new ApplicationConfigStorage(context, wwwFolder);

            // get version code of the app
            int versionCode = 0;
            try {
                versionCode = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            appBuildVersion = versionCode;
        }

        @Override
        public void run() {
            ensureWwwFolderExists();
            loadConfigs();

            // download new application config
            newAppConfig = downloadApplicationConfig();
            if (newAppConfig == null) {
                EventBus.getDefault().post(new UpdateErrorEvent(null, Error.FAILED_TO_DOWNLOAD_APPLICATION_CONFIG));
                return;
            }

            // check if there is a new content version available
            if (newAppConfig.getContentConfig().getReleaseVersion().equals(oldAppConfig.getContentConfig().getReleaseVersion())) {
                EventBus.getDefault().post(new NothingToUpdateEvent(newAppConfig));
                return;
            }

            // check if current native version supports new content
            if (newAppConfig.getContentConfig().getMinimumNativeVersion() > appBuildVersion) {
                EventBus.getDefault().post(new UpdateErrorEvent(newAppConfig, Error.APPLICATION_BUILD_VERSION_TOO_LOW));
                return;
            }

            appConfigStorage.storeInPreference(newAppConfig);

            // download new content manifest
            newContentManifest = downloadContentManifest(newAppConfig);
            if (newContentManifest == null) {
                EventBus.getDefault().post(new UpdateErrorEvent(newAppConfig, Error.FAILED_TO_DOWNLOAD_CONTENT_MANIFEST));
                return;
            }
            manifestStorage.storeInPreference(newContentManifest);

            // find files that were updated
            List<ContentManifest.DiffFile> updatedFiles = oldContentManifest.calculateDifference(newContentManifest);

            // download new content
            FilesUtility.delete(downloadFolder);
            FileDownloader.downloadFiles(downloadFolder, newAppConfig.getContentConfig().getContentUrl(), updatedFiles);

            // notify that we are done
            EventBus.getDefault().post(new UpdateIsReadyToInstallEvent(newAppConfig));
        }

        private void ensureWwwFolderExists() {
            if (new File(wwwFolder).exists()) {
                return;
            }

            AssetsHelper.copyAssetDirectoryToAppDirectory(context.getAssets(), HotCodePushPlugin.WWW_FOLDER, wwwFolder);
        }

        private void loadConfigs() {
            oldAppConfig = appConfigStorage.loadFromFS();
            oldContentManifest = manifestStorage.loadFromFS();
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
    }

}
