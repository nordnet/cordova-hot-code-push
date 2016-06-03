package com.nordnetab.chcp.main.updater;

import com.nordnetab.chcp.main.model.ChcpError;
import com.nordnetab.chcp.main.model.PluginFilesStructure;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by Nikolay Demyankov on 24.07.15.
 * <p/>
 * Utility class to perform update download.
 * It only schedules the download and executes it as soon as possible.
 * <p/>
 */
public class UpdatesLoader {

    private static boolean isExecuting;

    /**
     * Check if download currently in progress
     *
     * @return <code>true</code> if download in progress; <code>false</code> otherwise
     */
    public static boolean isExecuting() {
        return isExecuting;
    }

    /**
     * Request update download.
     * Download performed in background. Events are dispatched to notify us about the result.
     *
     * @param request download request
     * @return <code>ChcpError.NONE</code> if download has started; otherwise - error details
     */
    public static ChcpError downloadUpdate(final UpdateDownloadRequest request) {
        // if download already in progress - exit
        if (isExecuting) {
            return ChcpError.DOWNLOAD_ALREADY_IN_PROGRESS;
        }

        // if installation is in progress - exit
        if (UpdatesInstaller.isInstalling()) {
            return ChcpError.CANT_DOWNLOAD_UPDATE_WHILE_INSTALLATION_IN_PROGRESS;
        }

        isExecuting = true;

        final UpdateLoaderWorker task = new UpdateLoaderWorker(request);
        executeTask(task);

        return ChcpError.NONE;
    }

    private static void executeTask(final WorkerTask task) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                task.run();
                isExecuting = false;

                EventBus.getDefault().post(task.result());
            }
        }).start();
    }
}