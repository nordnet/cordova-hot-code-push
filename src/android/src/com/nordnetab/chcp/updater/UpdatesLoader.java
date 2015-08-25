package com.nordnetab.chcp.updater;

import android.content.Context;

import com.nordnetab.chcp.config.ApplicationConfig;
import com.nordnetab.chcp.model.IPluginFilesStructure;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by Nikolay Demyankov on 24.07.15.
 *
 *
 */
public class UpdatesLoader {

    // region Events

    public enum ErrorType {
        FAILED_TO_DOWNLOAD_APPLICATION_CONFIG(-1, "Failed to download application configuration file"),
        APPLICATION_BUILD_VERSION_TOO_LOW(-2, "Application build version is too low for this update"),
        FAILED_TO_DOWNLOAD_CONTENT_MANIFEST(-3, "Failed to download content manifest file"),
        FAILED_TO_DOWNLOAD_UPDATE_FILES(-4, "Failed to download update files"),
        FAILED_TO_MOVE_LOADED_FILES_TO_INSTALLATION_FOLDER(-5, "Failed to move downloaded files to the installation folder");

        private String errorDescription;
        private int errorCode;

        ErrorType(int errorCode, String errorDescription) {
            this.errorCode = errorCode;
            this.errorDescription = errorDescription;
        }

        public String getErrorDescription() {
            return errorDescription;
        }

        public int getErrorCode() {
            return errorCode;
        }
    }

    private static class UpdateProgressEvent {
        public final ApplicationConfig config;
        public final String taskId;

        public UpdateProgressEvent(ApplicationConfig config, String taskId) {
            this.config = config;
            this.taskId = taskId;
        }
    }

    public static class UpdateIsReadyToInstallEvent extends UpdateProgressEvent {

        public UpdateIsReadyToInstallEvent(ApplicationConfig config, String taskId) {
            super(config, taskId);
        }
    }

    public static class NothingToUpdateEvent extends UpdateProgressEvent {

        public NothingToUpdateEvent(ApplicationConfig config, String taskId) {
            super(config, taskId);
        }
    }

    public static class UpdateErrorEvent extends UpdateProgressEvent {
        public final ErrorType error;

        public UpdateErrorEvent(ApplicationConfig config, String taskId, ErrorType error) {
            super(config, taskId);
            this.error = error;
        }
    }

    // endregion

    //TODO: add key for tasks to remove duplicates

    private static ConcurrentLinkedQueue<Runnable> queue;
    private static boolean isExecuting;

    public static String addUpdateTaskToQueue(Context context, final String configURL, final IPluginFilesStructure filesStructure) {
        UpdateLoaderWorker task = new UpdateLoaderWorker(context, configURL, filesStructure);
        getQueue().add(task);

        if (!isExecuting) {
            executeTaskFromQueue();
        }

        return task.getWorkerId();
    }

    public static boolean isExecuting() {
        return isExecuting;
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
}
