package com.nordnetab.chcp.updater;

import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

import com.nordnetab.chcp.HotCodePushPlugin;
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
import java.io.IOException;
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

    public enum ErrorType {
        FAILED_TO_DOWNLOAD_APPLICATION_CONFIG,
        APPLICATION_BUILD_VERSION_TOO_LOW,
        FAILED_TO_DOWNLOAD_CONTENT_MANIFEST,
        FAILED_TO_DOWNLOAD_UPDATE_FILES
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

    public static String addUpdateTaskToQueue(Context context, String wwwFolder, String downloadFolder, String configUrl) {
        UpdateLoaderWorker task = new UpdateLoaderWorker(context, wwwFolder, downloadFolder, configUrl);
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
