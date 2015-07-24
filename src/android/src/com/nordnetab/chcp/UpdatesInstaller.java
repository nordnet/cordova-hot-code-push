package com.nordnetab.chcp;

import android.content.Context;

import com.nordnetab.chcp.config.ApplicationConfig;
import com.nordnetab.chcp.config.ContentManifest;
import com.nordnetab.chcp.storage.ApplicationConfigStorage;
import com.nordnetab.chcp.storage.ContentManifestStorage;
import com.nordnetab.chcp.utils.FilesUtility;
import com.nordnetab.chcp.utils.Paths;

import java.io.File;
import java.io.IOException;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by Nikolay Demyankov on 22.07.15.
 */
public class UpdatesInstaller {

    // region Events

    public enum Error {
        UPDATE_IS_INVALID,
        FAILED_TO_COPY_NEW_CONTENT_FILES
    }

    private static class InstallProgressEvent {
        public final ApplicationConfig config;

        public InstallProgressEvent(ApplicationConfig config) {
            this.config = config;
        }
    }

    public static class UpdateInstalledEvent extends InstallProgressEvent {

        public UpdateInstalledEvent(ApplicationConfig config) {
            super(config);
        }
    }

    public static class InstallationErrorEvent extends InstallProgressEvent {

        public final Error error;

        public InstallationErrorEvent(ApplicationConfig config, Error error) {
            super(config);
            this.error = error;
        }
    }

    // endregion

    public static void install(Context context, final String downloadFolder, final String wwwFolder) {
        ContentManifestStorage manifestStorage = new ContentManifestStorage(context, wwwFolder);
        ContentManifest oldManifest = manifestStorage.loadFromFS();
        ContentManifest newManifest = manifestStorage.loadFromPreference();

        ApplicationConfigStorage appConfigStorage = new ApplicationConfigStorage(context, wwwFolder);
        ApplicationConfig newConfig = appConfigStorage.loadFromPreference();

        List<ContentManifest.DiffFile> updatedFiles = oldManifest.calculateDifference(newManifest);

        install(downloadFolder, wwwFolder, updatedFiles, newConfig, appConfigStorage, newManifest, manifestStorage);
    }

    public static void install(final String downloadFolder, final String wwwFolder, final List<ContentManifest.DiffFile> updatedFiles,
                               final ApplicationConfig appConfig, final ApplicationConfigStorage appConfigStorage,
                               final ContentManifest manifest, final ContentManifestStorage manifestStorage) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (!isUpdateValid(downloadFolder, updatedFiles)) {
                    EventBus.getDefault().post(new InstallationErrorEvent(appConfig, Error.UPDATE_IS_INVALID));
                    return;
                }

                deleteUnusedFiles(wwwFolder, updatedFiles);

                boolean isInstalled = moveFilesToNewDirectory(downloadFolder, wwwFolder);
                if (!isInstalled) {
                    EventBus.getDefault().post(new InstallationErrorEvent(appConfig, Error.FAILED_TO_COPY_NEW_CONTENT_FILES));
                    return;
                }

                // save new configuration in www folder
                appConfigStorage.storeOnFS(appConfig);
                manifestStorage.storeOnFS(manifest);

                // clear preferences
                appConfigStorage.clearPreference();
                manifestStorage.clearPreference();

                EventBus.getDefault().post(new UpdateInstalledEvent(appConfig));

            }
        }).start();
    }

    private static void deleteUnusedFiles(final String wwwFolder, List<ContentManifest.DiffFile> files) {
        for (ContentManifest.DiffFile file : files) {
            if (!file.isRemoved) {
                continue;
            }

            String path = Paths.get(wwwFolder, file.name);
            FilesUtility.delete(new File(path));
        }
    }

    private static boolean moveFilesToNewDirectory(final String updateDownloadFolderPath, final String wwwFolderPath) {
        File wwwFolder = new File(wwwFolderPath);
        File downloadFolder = new File(updateDownloadFolderPath);

        try {
            FilesUtility.copy(downloadFolder, wwwFolder);
            FilesUtility.delete(downloadFolder);

            return true;
        } catch (IOException e) {
            e.printStackTrace();

            return false;
        }
    }

    private static boolean isUpdateValid(final String downloadFolder, List<ContentManifest.DiffFile> filesList) {
        if (!new File(downloadFolder).exists()) {
            return false;
        }

        boolean isValid = true;
        for (ContentManifest.DiffFile updatedFile : filesList) {
            if (updatedFile.isRemoved) {
                continue;
            }

            File file = new File(Paths.get(downloadFolder, updatedFile.name));

            try {
                if (!file.exists() ||
                        !FilesUtility.calculateFileHash(file).equals(updatedFile.hash)) {
                    isValid = false;
                    break;
                }
            } catch (Exception e) {
                e.printStackTrace();
                isValid = false;
                break;
            }
        }

        return isValid;
    }
}
