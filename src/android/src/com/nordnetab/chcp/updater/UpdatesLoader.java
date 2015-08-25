package com.nordnetab.chcp.updater;

import android.content.Context;

import com.nordnetab.chcp.model.IPluginFilesStructure;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by Nikolay Demyankov on 24.07.15.
 *
 *
 */
public class UpdatesLoader {

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
