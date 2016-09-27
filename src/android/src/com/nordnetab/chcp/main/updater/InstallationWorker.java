package com.nordnetab.chcp.main.updater;

import android.content.Context;

import com.nordnetab.chcp.main.config.ApplicationConfig;
import com.nordnetab.chcp.main.config.ContentManifest;
import com.nordnetab.chcp.main.events.UpdateInstallationErrorEvent;
import com.nordnetab.chcp.main.events.UpdateInstalledEvent;
import com.nordnetab.chcp.main.events.WorkerEvent;
import com.nordnetab.chcp.main.model.ChcpError;
import com.nordnetab.chcp.main.model.ManifestDiff;
import com.nordnetab.chcp.main.model.ManifestFile;
import com.nordnetab.chcp.main.model.PluginFilesStructure;
import com.nordnetab.chcp.main.storage.ApplicationConfigStorage;
import com.nordnetab.chcp.main.storage.ContentManifestStorage;
import com.nordnetab.chcp.main.storage.IObjectFileStorage;
import com.nordnetab.chcp.main.utils.FilesUtility;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Created by Nikolay Demyankov on 28.07.15.
 * <p/>
 * Worker, that implements installation logic.
 * During the installation process events are dispatched to notify the subscribers about the progress.
 * <p/>
 * Used internally.
 */
class InstallationWorker implements WorkerTask {

    private ManifestDiff manifestDiff;
    private ApplicationConfig newAppConfig;

    private PluginFilesStructure newReleaseFS;
    private PluginFilesStructure currentReleaseFS;

    private WorkerEvent resultEvent;

    /**
     * Constructor.
     *
     * @param context        application context
     * @param newVersion     version to install
     * @param currentVersion current content version
     */
    InstallationWorker(final Context context, final String newVersion, final String currentVersion) {
        newReleaseFS = new PluginFilesStructure(context, newVersion);
        currentReleaseFS = new PluginFilesStructure(context, currentVersion);
    }

    @Override
    public void run() {
        // try to initialize before run
        if (!init()) {
            return;
        }

        // validate update
        if (!isUpdateValid(newReleaseFS.getDownloadFolder(), manifestDiff)) {
            setResultForError(ChcpError.UPDATE_IS_INVALID);
            return;
        }

        // copy content from the current release to the new release folder
        if (!copyFilesFromCurrentReleaseToNewRelease()) {
            setResultForError(ChcpError.FAILED_TO_COPY_FILES_FROM_PREVIOUS_RELEASE);
            return;
        }

        // remove old manifest files
        deleteUnusedFiles();

        // install the update
        boolean isInstalled = moveFilesFromInstallationFolderToWwwFolder();
        if (!isInstalled) {
            cleanUpOnFailure();
            setResultForError(ChcpError.FAILED_TO_COPY_NEW_CONTENT_FILES);
            return;
        }

        // perform cleaning
        cleanUpOnSuccess();

        // send notification, that we finished
        setSuccessResult();
    }

    /**
     * Initialize variables and other pre-work stuff.
     *
     * @return <code>true</code> if all initialized and ready; <code>false</code> - otherwise
     */
    private boolean init() {
        // loaded application config
        IObjectFileStorage<ApplicationConfig> appConfigStorage = new ApplicationConfigStorage();
        newAppConfig = appConfigStorage.loadFromFolder(newReleaseFS.getDownloadFolder());
        if (newAppConfig == null) {
            setResultForError(ChcpError.LOADED_VERSION_OF_APPLICATION_CONFIG_NOT_FOUND);
            return false;
        }

        // old manifest file
        IObjectFileStorage<ContentManifest> manifestStorage = new ContentManifestStorage();
        ContentManifest oldManifest = manifestStorage.loadFromFolder(currentReleaseFS.getWwwFolder());
        if (oldManifest == null) {
            setResultForError(ChcpError.LOCAL_VERSION_OF_MANIFEST_NOT_FOUND);
            return false;
        }

        // loaded manifest file
        ContentManifest newManifest = manifestStorage.loadFromFolder(newReleaseFS.getDownloadFolder());
        if (newManifest == null) {
            setResultForError(ChcpError.LOADED_VERSION_OF_MANIFEST_NOT_FOUND);
            return false;
        }

        // difference between old and the new manifest files
        manifestDiff = oldManifest.calculateDifference(newManifest);

        return true;
    }

    /**
     * Copy all files from the previous release folder to the new release folder.
     *
     * @return <code>true</code> if files are copied; <code>false</code> - otherwise.
     */
    private boolean copyFilesFromCurrentReleaseToNewRelease() {
        boolean result = true;
        final File currentWwwFolder = new File(currentReleaseFS.getWwwFolder());
        final File newWwwFolder = new File(newReleaseFS.getWwwFolder());
        try {
            // just in case if www folder already exists - remove it
            if (newWwwFolder.exists()) {
                FilesUtility.delete(newWwwFolder);
            }

            FilesUtility.copy(currentWwwFolder, newWwwFolder);
        } catch (Exception e) {
            e.printStackTrace();
            result = false;
        }

        return result;
    }

    /**
     * Perform cleaning when we failed to install the update.
     */
    private void cleanUpOnFailure() {
        FilesUtility.delete(newReleaseFS.getContentFolder());
    }

    /**
     * Perform cleaning when update is installed.
     */
    private void cleanUpOnSuccess() {
        FilesUtility.delete(newReleaseFS.getDownloadFolder());
    }

    /**
     * Delete from project unused files
     */
    private void deleteUnusedFiles() {
        final List<ManifestFile> files = manifestDiff.deletedFiles();
        for (ManifestFile file : files) {
            File fileToDelete = new File(newReleaseFS.getWwwFolder(), file.name);
            FilesUtility.delete(fileToDelete);
        }
    }

    /**
     * Copy downloaded files into www folder
     *
     * @return <code>true</code> if files are copied; <code>false</code> - otherwise
     */
    private boolean moveFilesFromInstallationFolderToWwwFolder() {
        try {
            FilesUtility.copy(newReleaseFS.getDownloadFolder(), newReleaseFS.getWwwFolder());

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
     * @param downloadFolderPath folder, where our files are situated
     * @param manifestDiff       difference between old and the new manifest. Holds information about updated files.
     * @return <code>true</code> update is valid and we are good to go; <code>false</code> - otherwise
     */
    private boolean isUpdateValid(String downloadFolderPath, ManifestDiff manifestDiff) {
        File downloadFolder = new File(downloadFolderPath);
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

    private void setResultForError(final ChcpError error) {
        resultEvent = new UpdateInstallationErrorEvent(error, newAppConfig);
    }

    private void setSuccessResult() {
        resultEvent = new UpdateInstalledEvent(newAppConfig);
    }

    @Override
    public WorkerEvent result() {
        return resultEvent;
    }

    // endregion
}
