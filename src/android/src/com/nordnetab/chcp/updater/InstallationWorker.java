package com.nordnetab.chcp.updater;

import com.nordnetab.chcp.config.ApplicationConfig;
import com.nordnetab.chcp.config.ContentManifest;
import com.nordnetab.chcp.model.ManifestDiff;
import com.nordnetab.chcp.model.ManifestFile;
import com.nordnetab.chcp.model.IPluginFilesStructure;
import com.nordnetab.chcp.storage.ApplicationConfigStorage;
import com.nordnetab.chcp.storage.ContentManifestStorage;
import com.nordnetab.chcp.storage.IConfigFileStorage;
import com.nordnetab.chcp.utils.FilesUtility;

import java.io.File;
import java.io.IOException;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by Nikolay Demyankov on 28.07.15.
 */
class InstallationWorker implements Runnable {

    private File wwwFolder;
    private File backupFolder;
    private File installationFolder;

    private ManifestDiff manifestDiff;
    private ApplicationConfig newAppConfig;

    private IPluginFilesStructure filesStructure;

    public InstallationWorker(final IPluginFilesStructure filesStructure) {
        this.filesStructure = filesStructure;
    }

    @Override
    public void run() {
        init();

        // validate update
        if (!isUpdateValid(installationFolder, manifestDiff)) {
            EventBus.getDefault().post(new UpdatesInstaller.InstallationErrorEvent(newAppConfig, UpdatesInstaller.Error.UPDATE_IS_INVALID));
            return;
        }

        // backup before installing
        if (!backupCurrentFiles()) {
            EventBus.getDefault().post(new UpdatesInstaller.InstallationErrorEvent(newAppConfig, UpdatesInstaller.Error.FAILED_TO_CREATE_BACKUP));
            return;
        }

        // remove old manifest files
        deleteUnusedFiles();

        // install the update
        boolean isInstalled = moveFilesFromInstallationFolderToWwwFodler();
        if (!isInstalled) {
            rollback();
            cleanUp();

            EventBus.getDefault().post(new UpdatesInstaller.InstallationErrorEvent(newAppConfig, UpdatesInstaller.Error.FAILED_TO_COPY_NEW_CONTENT_FILES));
            return;
        }

        // perform cleaning
        cleanUp();

        // send notification, that we finished
        EventBus.getDefault().post(new UpdatesInstaller.UpdateInstalledEvent(newAppConfig));
    }

    private void init() {
        installationFolder = new File(filesStructure.installationFolder());
        wwwFolder = new File(filesStructure.wwwFolder());
        backupFolder = new File(filesStructure.backupFolder());

        IConfigFileStorage<ApplicationConfig> appConfigStorage = new ApplicationConfigStorage(filesStructure);
        newAppConfig = appConfigStorage.loadFromFolder(filesStructure.installationFolder());

        IConfigFileStorage<ContentManifest> manifestStorage = new ContentManifestStorage(filesStructure);
        ContentManifest oldManifest = manifestStorage.loadFromFolder(filesStructure.wwwFolder());
        ContentManifest newManifest = manifestStorage.loadFromFolder(filesStructure.installationFolder());

        manifestDiff = oldManifest.calculateDifference(newManifest);
    }

    private boolean backupCurrentFiles() {
        boolean result = true;
        try {
            FilesUtility.copy(wwwFolder, backupFolder);
        } catch (Exception e) {
            e.printStackTrace();
            result = false;
        }

        return result;
    }

    private void cleanUp() {
        FilesUtility.delete(installationFolder);
        FilesUtility.delete(backupFolder);
    }

    private void rollback() {
        FilesUtility.delete(wwwFolder);
        FilesUtility.ensureDirectoryExists(wwwFolder);
        try {
            FilesUtility.copy(backupFolder, wwwFolder);
        } catch (IOException e) {
            e.printStackTrace();
            // nothing we can do
        }
    }

    private void deleteUnusedFiles() {
        final List<ManifestFile> files = manifestDiff.deletedFiles();
        for (ManifestFile file : files) {
            File fileToDelete = new File(wwwFolder, file.name);
            FilesUtility.delete(fileToDelete);
        }
    }

    private boolean moveFilesFromInstallationFolderToWwwFodler() {
        try {
            FilesUtility.copy(installationFolder, wwwFolder);

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


