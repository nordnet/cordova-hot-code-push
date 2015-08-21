package com.nordnetab.chcp.updater;

import android.content.Context;

import com.nordnetab.chcp.config.ApplicationConfig;
import com.nordnetab.chcp.config.ContentManifest;
import com.nordnetab.chcp.model.ManifestDiff;
import com.nordnetab.chcp.model.ManifestFile;
import com.nordnetab.chcp.storage.ApplicationConfigStorage;
import com.nordnetab.chcp.storage.ContentManifestStorage;
import com.nordnetab.chcp.utils.FilesUtility;

import java.io.File;
import java.io.IOException;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by Nikolay Demyankov on 28.07.15.
 */
class InstallationWorker implements Runnable {

    private File downloadFolder;
    private File wwwFolder;
    private File backupFolder;
    private ManifestDiff manifestDiff;
    private ApplicationConfig appConfig;
    private ApplicationConfigStorage appConfigStorage;
    private ContentManifest manifest;
    private ContentManifestStorage manifestStorage;

    public InstallationWorker(Context context, String wwwFolderPath, String downloadFolderPath, String backupFolderPath) {
        manifestStorage = new ContentManifestStorage(context, wwwFolderPath);
        ContentManifest oldManifest = manifestStorage.loadFromFS();
        manifest = manifestStorage.loadFromPreference();
        appConfigStorage = new ApplicationConfigStorage(context, wwwFolderPath);

        downloadFolder = new File(downloadFolderPath);
        wwwFolder = new File(wwwFolderPath);
        backupFolder = new File(backupFolderPath);
        appConfig = appConfigStorage.loadFromPreference();
        manifestDiff = oldManifest.calculateDifference(manifest);
    }

    @Override
    public void run() {
        // validate update
        if (!isUpdateValid(downloadFolder, manifestDiff)) {
            EventBus.getDefault().post(new UpdatesInstaller.InstallationErrorEvent(appConfig, UpdatesInstaller.Error.UPDATE_IS_INVALID));
            return;
        }

        // backup before installing
        if (!backupCurrentFiles(wwwFolder, backupFolder)) {
            EventBus.getDefault().post(new UpdatesInstaller.InstallationErrorEvent(appConfig, UpdatesInstaller.Error.FAILED_TO_CREATE_BACKUP));
            return;
        }

        // remove old manifest files
        deleteUnusedFiles(wwwFolder, manifestDiff.deletedFiles());

        // install the update
        boolean isInstalled = moveFilesToNewDirectory(downloadFolder, wwwFolder);
        if (!isInstalled) {
            rollback(backupFolder, wwwFolder);
            cleanUp(downloadFolder, backupFolder);

            EventBus.getDefault().post(new UpdatesInstaller.InstallationErrorEvent(appConfig, UpdatesInstaller.Error.FAILED_TO_COPY_NEW_CONTENT_FILES));
            return;
        }

        // save new configuration in www folder
        appConfigStorage.storeOnFS(appConfig);
        manifestStorage.storeOnFS(manifest);

        // perform cleaning
        cleanUp(downloadFolder, backupFolder);

        // send notification, that we finished
        EventBus.getDefault().post(new UpdatesInstaller.UpdateInstalledEvent(appConfig));
    }

    private boolean backupCurrentFiles(File wwwFolder, File backupFolder) {
        boolean result = true;
        try {
            FilesUtility.copy(wwwFolder, backupFolder);
        } catch (Exception e) {
            e.printStackTrace();
            result = false;
        }

        return result;
    }

    private void cleanUp(File downloadFolder, File backupFolder) {
        // clear preferences
        appConfigStorage.clearPreference();
        manifestStorage.clearPreference();

        // remove temporary folders
        FilesUtility.delete(downloadFolder);
        FilesUtility.delete(backupFolder);
    }

    private void rollback(File backupFolder, File wwwFolder) {

    }

    private void deleteUnusedFiles(File wwwFolder, List<ManifestFile> files) {
        for (ManifestFile file : files) {
            File fileToDelete = new File(wwwFolder, file.name);
            FilesUtility.delete(fileToDelete);
        }
    }

    private boolean moveFilesToNewDirectory(File downloadFolder, File wwwFolder) {
        try {
            FilesUtility.copy(downloadFolder, wwwFolder);

            return true;
        } catch (IOException e) {
            e.printStackTrace();

            return false;
        }
    }

    private boolean isUpdateValid(File downloadFolder, ManifestDiff manifestDiff) {
        if (!downloadFolder.exists()) {
            return false;
        }

        boolean isValid = true;
        List<ManifestFile> updateFileList = manifestDiff.getUpdateFiles();

        for (ManifestFile updatedFile : updateFileList) {
            File file = new File(downloadFolder, updatedFile.name);

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


