package com.nordnetab.chcp.updater;

import android.content.Context;

import com.nordnetab.chcp.model.IPluginFilesStructure;

/**
 * Created by Nikolay Demyankov on 24.07.15.
 *
 * Utility class to perform update download.
 * It only schedules the download and executes it as soon as possible.
 *
 * Queue consists from 1 task, because we don't need to store 100 tasks for download request,
 * we need only the last one. But if for some reason you need a large queue of download tasks -
 * use ConcurrentLinkedQueue, that is commented out at the moment.
 */
public class UpdatesLoader {

//    private static ConcurrentLinkedQueue<Runnable> queue;
    private static boolean isExecuting;
    private static Runnable scheduledTask;

    /**
     * Add update download task to queue. It will be executed as fast as possible.
     *
     * @param context application context
     * @param configURL url from which we should download application config
     * @param filesStructure plugins files structure
     *
     * @return string identifier of the task.
     */
    public static String addUpdateTaskToQueue(Context context, final String configURL, final IPluginFilesStructure filesStructure) {
        UpdateLoaderWorker task = new UpdateLoaderWorker(context, configURL, filesStructure);
//        addTaskToQueue(task);
        scheduledTask = task;
        if (!isExecuting()) {
            executeTaskFromQueue();
        }

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
        //final Runnable task = getQueue().poll();
        final Runnable task = scheduledTask;
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

//    private static ConcurrentLinkedQueue<Runnable> getQueue() {
//        if (queue == null) {
//            queue = new ConcurrentLinkedQueue<Runnable>();
//        }
//
//        return queue;
//    }
//
//    private static void addTaskToQueue(Runnable task) {
//        ConcurrentLinkedQueue<Runnable> queue = getQueue();
//        queue.add(task);
//    }
}