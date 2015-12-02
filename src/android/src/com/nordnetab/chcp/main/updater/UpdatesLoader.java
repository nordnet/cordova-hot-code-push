package com.nordnetab.chcp.main.updater;

import android.content.Context;

import com.nordnetab.chcp.main.model.IPluginFilesStructure;

/**
 * Created by Nikolay Demyankov on 24.07.15.
 * <p/>
 * Utility class to perform update download.
 * It only schedules the download and executes it as soon as possible.
 * <p/>
 */
public class UpdatesLoader {

    private static boolean isExecuting;
    private static Runnable scheduledTask;

    /**
     * Add update download task to queue. It will be executed as fast as possible.
     *
     * @param context        application context
     * @param configURL      url from which we should download application config
     * @param filesStructure plugins files structure
     * @return string identifier of the task.
     */
    public static String addUpdateTaskToQueue(Context context, final String configURL, final IPluginFilesStructure filesStructure) {
        // for now - just exit if we are already doing some loading.
        // later - will return download queue
        if (isExecuting()) {
            return null;
        }

        UpdateLoaderWorker task = new UpdateLoaderWorker(context, configURL, filesStructure);
        scheduledTask = task;
        executeTaskFromQueue();

        return task.getWorkerId();
    }

    /**
     * Check if download currently in progress
     *
     * @return <code>true</code> if download in progress; <code>false</code> otherwise
     */
    public static boolean isExecuting() {
        return isExecuting;
    }

    private static void executeTaskFromQueue() {
        final Runnable task = scheduledTask;
        scheduledTask = null;
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
}