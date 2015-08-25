package com.nordnetab.chcp.updater;

import com.nordnetab.chcp.config.ApplicationConfig;
import com.nordnetab.chcp.model.IPluginFilesStructure;

import java.io.File;

import de.greenrobot.event.EventBus;

/**
 * Created by Nikolay Demyankov on 22.07.15.
 */
public class UpdatesInstaller {

    private static boolean isInstalling;

    // region Events

    public enum Error {
        UPDATE_IS_INVALID(-100, "Update package is broken"),
        FAILED_TO_CREATE_BACKUP(-101, "Could not create backup before the installation"),
        FAILED_TO_COPY_NEW_CONTENT_FILES(-102, "Failed to copy new files to content directory");

        private int errorCode;
        private String errorDescription;

        Error(int errorCode, String errorDescription) {
            this.errorCode = errorCode;
            this.errorDescription = errorDescription;
        }

        public int getErrorCode() {
            return errorCode;
        }

        public String getErrorDescription() {
            return errorDescription;
        }
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

    public static class NothingToInstallEvent extends InstallProgressEvent {

        public NothingToInstallEvent(ApplicationConfig config) {
            super(config);
        }
    }

    // endregion

    public static boolean install(final IPluginFilesStructure filesStructure) {
        if (isInstalling) {
            return false;
        }

        if (!new File(filesStructure.installationFolder()).exists()) {
            EventBus.getDefault().post(new NothingToInstallEvent(null));
            return false;
        }

        InstallationWorker task = new InstallationWorker(filesStructure);
        execute(task);

        return true;
    }

    private static void execute(final InstallationWorker task) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                isInstalling = true;
                task.run();
                isInstalling = false;
            }
        }).start();
    }

    public static boolean isInstalling() {
        return isInstalling;
    }
}
