package com.nordnetab.chcp.main.updater;

import com.nordnetab.chcp.main.config.ApplicationConfig;
import com.nordnetab.chcp.main.config.ContentManifest;
import com.nordnetab.chcp.main.events.NothingToInstallEvent;
import com.nordnetab.chcp.main.events.UpdateInstallationErrorEvent;
import com.nordnetab.chcp.main.events.UpdateInstalledEvent;
import com.nordnetab.chcp.main.model.ChcpError;
import com.nordnetab.chcp.main.model.ManifestDiff;
import com.nordnetab.chcp.main.model.ManifestFile;
import com.nordnetab.chcp.main.model.IPluginFilesStructure;
import com.nordnetab.chcp.main.storage.ApplicationConfigStorage;
import com.nordnetab.chcp.main.storage.ContentManifestStorage;
import com.nordnetab.chcp.main.storage.IObjectFileStorage;
import com.nordnetab.chcp.main.utils.FilesUtility;

import java.io.File;
import java.io.IOException;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by Nikolay Demyankov on 28.07.15.
 * <p/>
 * Worker, that implements installation logic.
 * During the installation process events are dispatched to notify the subscribers about the progress.
 * <p/>
 * Used internally.
 *
 * @see UpdatesInstaller
 * @see UpdateInstallationErrorEvent
 * @see UpdateInstalledEvent
 */
class InstallationWorker implements Runnable {

    private File wwwFolder;
    private File backupFolder;
    private File installationFolder;

    private ManifestDiff manifestDiff;
    private ApplicationConfig newAppConfig;

    private IPluginFilesStructure filesStructure;

    /**
     * Class constructor.
     *
     * @param filesStructure structure of plugin files
     * @see IPluginFilesStructure
     */
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

    /**
     * Initialize variables and other pre-work stuff.
     *
     * @return <code>true</code> if all initialized and ready; <code>false</code> - otherwise
     */
    private boolean init() {
        // working directories
        installationFolder = new File(filesStructure.installationFolder());
        wwwFolder = new File(filesStructure.wwwFolder());
        backupFolder = new File(filesStructure.backupFolder());

        // loaded application config
        IObjectFileStorage<ApplicationConfig> appConfigStorage = new ApplicationConfigStorage(filesStructure);
        newAppConfig = appConfigStorage.loadFromFolder(filesStructure.installationFolder());
        if (newAppConfig == null) {
            dispatchErrorEvent(ChcpError.LOADED_VERSION_OF_APPLICATION_CONFIG_NOT_FOUND);
            return false;
        }

        // old manifest file
        IObjectFileStorage<ContentManifest> manifestStorage = new ContentManifestStorage(filesStructure);
        ContentManifest oldManifest = manifestStorage.loadFromFolder(filesStructure.wwwFolder());
        if (oldManifest == null) {
            dispatchErrorEvent(ChcpError.LOCAL_VERSION_OF_MANIFEST_NOT_FOUND);
            return false;
        }

        // loaded manifest file
        ContentManifest newManifest = manifestStorage.loadFromFolder(filesStructure.installationFolder());
        if (newManifest == null) {
            dispatchErrorEvent(ChcpError.LOADED_VERSION_OF_MANIFEST_NOT_FOUND);
            return false;
        }

        // difference between old and the new manifest files
        manifestDiff = oldManifest.calculateDifference(newManifest);

        return true;
    }

    /**
     * Create backup of the current www folder.
     * If something will go wrong - we will use it to rollback.
     *
     * @return <code>true</code> if backup created; <code>false</code> - otherwise
     */
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

    /**
     * Remove temporary folders
     */
    private void cleanUp() {
        FilesUtility.delete(installationFolder);
        FilesUtility.delete(backupFolder);
    }

    /**
     * Restore www folder from backup.
     */
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

    /**
     * Delete from project unused files
     */
    private void deleteUnusedFiles() {
        final List<ManifestFile> files = manifestDiff.deletedFiles();
        for (ManifestFile file : files) {
            File fileToDelete = new File(wwwFolder, file.name);
            FilesUtility.delete(fileToDelete);
        }
    }

    //

    /**
     * Copy downloaded files into www folder
     *
     * @return <code>true</code> if files are copied; <code>false</code> - otherwise
     */
    private boolean moveFilesFromInstallationFolderToWwwFodler() {
        try {
            FilesUtility.copy(installationFolder, wwwFolder);

            return true;
        } catch (IOException e) {
            e.printStackTrace();

            return false;
        }
    }

    /**
     * Check if update is ready for installation.
     * We will check, if all files are loaded and their hashes are correct.
     *
     * @param downloadFolder folder, where our files are situated
     * @param manifestDiff   difference between old and the new manifest. Holds information about updated files.
     * @return <code>true</code> update is valid and we are good to go; <code>false</code> - otherwise
     */
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

    // region Events

    private void dispatchErrorEvent(ChcpError error) {
        EventBus.getDefault().post(new UpdateInstallationErrorEvent(error, newAppConfig));
    }

    private void dispatchSuccessEvent() {
        EventBus.getDefault().post(new UpdateInstalledEvent(newAppConfig));
    }

    private void dispatchNothingToInstallEvent() {
        EventBus.getDefault().post(new NothingToInstallEvent(newAppConfig));
    }

    // endregion
}