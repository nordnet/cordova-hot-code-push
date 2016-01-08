package com.nordnetab.chcp.main.utils;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.nordnetab.chcp.main.model.PluginFilesStructure;

import java.io.File;

/**
 * Created by Nikolay Demyankov on 29.12.15.
 * <p/>
 * Helper class to clean up file system and remove old releases folders.
 */
public class CleanUpHelper {

    private static boolean isExecuting;

    private final File rootFolder;

    /**
     * Constructor.
     *
     * @param rootFolder root folder, where releases are stored
     */
    private CleanUpHelper(final String rootFolder) {
        this.rootFolder = new File(rootFolder);
    }

    /**
     * Remove release folders.
     *
     * @param context          application context
     * @param excludedReleases which releases are leave alive.
     */
    public static void removeReleaseFolders(final Context context, final String[] excludedReleases) {
        if (isExecuting) {
            return;
        }
        isExecuting = true;

        final String rootFolder = PluginFilesStructure.getPluginRootFolder(context);

        new Thread(new Runnable() {
            @Override
            public void run() {
                new CleanUpHelper(rootFolder).removeFolders(excludedReleases);
                isExecuting = false;
            }
        }).start();
    }

    private void removeFolders(final String[] excludedReleases) {
        if (!rootFolder.exists()) {
            return;
        }

        File[] files = rootFolder.listFiles();
        for (File file : files) {
            boolean isIgnored = false;
            for (String excludedReleaseName : excludedReleases) {
                if (TextUtils.isEmpty(excludedReleaseName)) {
                    continue;
                }

                if (file.getName().equals(excludedReleaseName)) {
                    isIgnored = true;
                    break;
                }
            }

            if (!isIgnored) {
                Log.d("CHCP", "Deleting old release folder: " + file.getName());
                FilesUtility.delete(file);
            }
        }
    }

}
