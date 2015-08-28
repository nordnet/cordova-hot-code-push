package com.nordnetab.chcp.updater;

import com.nordnetab.chcp.config.ApplicationConfig;
import com.nordnetab.chcp.config.ContentManifest;
import com.nordnetab.chcp.events.NothingToInstallEvent;
import com.nordnetab.chcp.events.UpdateInstallationErrorEvent;
import com.nordnetab.chcp.events.UpdateInstalledEvent;
import com.nordnetab.chcp.model.ChcpError;
import com.nordnetab.chcp.model.ManifestDiff;
import com.nordnetab.chcp.model.ManifestFile;
import com.nordnetab.chcp.model.IPluginFilesStructure;
import com.nordnetab.chcp.storage.ApplicationConfigStorage;
import com.nordnetab.chcp.storage.ContentManifestStorage;
import com.nordnetab.chcp.storage.IObjectFileStorage;
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
        // try to initialize before run
        if (!init()) {
            return;
        }

        // validate update
        if (!isUpdateValid(installationFolder, manifestDiff)) {
            dispatchErrorEvent(ChcpError.UPDATE_IS_INVALID);
            return;
        }

        // backup before installing
        if (!backupCurrentFiles()) {
            dispatchErrorEvent(ChcpError.FAILED_TO_CREATE_BACKUP);
            return;
        }

        // remove old manifest files
        deleteUnusedFiles();

        // install the update
        boolean isInstalled = moveFilesFromInstallationFolderToWwwFodler();
        if (!isInstalled) {
            rollback();
            cleanUp();
            dispatchErrorEvent(ChcpError.FAILED_TO_COPY_NEW_CONTENT_FILES);
            return;
        }

        // perform cleaning
        cleanUp();

        // send notification, that we finished
        dispatchSuccessEvent();
    }

    private boolean init() {
        installationFolder = new File(filesStructure.installationFolder());
        wwwFolder = new File(filesStructure.wwwFolder());
        backupFolder = new File(filesStructure.backupFolder());

        IObjectFileStorage<ApplicationConfig> appConfigStorage = new ApplicationConfigStorage(filesStructure);
        newAppConfig = appConfigStorage.loadFromFolder(filesStructure.installationFolder());
        if (newAppConfig == null) {
            dispatchErrorEvent(ChcpError.LOADED_VERSION_OF_APPLICATION_CONFIG_NOT_FOUND);
            return false;
        }

        IObjectFileStorage<ContentManifest> manifestStorage = new ContentManifestStorage(filesStructure);
        ContentManifest oldManifest = manifestStorage.loadFromFolder(filesStructure.wwwFolder());
        if (oldManifest == null) {
            dispatchErrorEvent(ChcpError.LOCAL_VERSION_OF_MANIFEST_NOT_FOUND);
            return false;
        }

        ContentManifest newManifest = manifestStorage.loadFromFolder(filesStructure.installationFolder());
        if (newManifest == null) {
            dispatchErrorEvent(ChcpError.LOADED_VERSION_OF_MANIFEST_NOT_FOUND);
            return false;
        }

        manifestDiff = oldManifest.calculateDifference(newManifest);

        return true;
    }

    private void dispatchErrorEvent(ChcpError error) {
        EventBus.getDefault().post(new UpdateInstallationErrorEvent(error, newAppConfig));
    }

    private void dispatchSuccessEvent() {
        EventBus.getDefault().post(new UpdateInstalledEvent(newAppConfig));
    }

    private void dispatchNothingToInstallEvent() {
        EventBus.getDefault().post(new NothingToInstallEvent(newAppConfig));
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


