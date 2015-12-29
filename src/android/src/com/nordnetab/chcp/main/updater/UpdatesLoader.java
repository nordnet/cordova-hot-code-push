package com.nordnetab.chcp.main.updater;

import android.content.Context;

import de.greenrobot.event.EventBus;

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
     * Add update download task to queue. It will be executed as fast as possible.
     *
     * @param context        application context
     * @param configURL      url from which we should download application config
     * @param currentVersion current version of the content
     * @return string identifier of the task.
     */
    public static boolean downloadUpdate(final Context context, final String configURL, final String currentVersion) {
        // if we download already in progress - exit
        if (isExecuting) {
            return false;
        }

        // if we are installing - exit
        if (UpdatesInstaller.isInstalling()) {
            return false;
        }

        isExecuting = true;

        UpdateLoaderWorker task = new UpdateLoaderWorker(context, configURL, currentVersion);
        executeTask(task);

        return true;
    }

    /**
     * Check if download currently in progress
     *
     * @return <code>true</code> if download in progress; <code>false</code> otherwise
     */
    public static boolean isExecuting() {
        return isExecuting;
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