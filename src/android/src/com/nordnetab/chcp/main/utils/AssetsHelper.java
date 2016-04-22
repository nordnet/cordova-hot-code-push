package com.nordnetab.chcp.main.utils;

import android.content.res.AssetManager;

import com.nordnetab.chcp.main.events.AssetsInstallationErrorEvent;
import com.nordnetab.chcp.main.events.AssetsInstalledEvent;
import com.nordnetab.chcp.main.events.BeforeAssetsInstalledEvent;

import org.greenrobot.eventbus.EventBus;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by Nikolay Demyankov on 21.07.15.
 * <p/>
 * Utility class to copy files from assets folder into the external storage.
 */
public class AssetsHelper {

    private static boolean isWorking;

    private AssetsHelper() {
    }

    /**
     * Copy files from the assets folder into the specific folder on the external storage.
     * Method runs asynchronously. Results are dispatched through events.
     *
     * @param assetManager  assets manager
     * @param fromDirectory which directory in assets we want to copy
     * @param toDirectory   absolute path to the destination folder on the external storage
     *
     * @see AssetsInstallationErrorEvent
     * @see AssetsInstalledEvent
     * @see EventBus
     */
    public static void copyAssetDirectoryToAppDirectory(final AssetManager assetManager, final String fromDirectory, final String toDirectory) {
        if (isWorking) {
            return;
        }
        isWorking = true;

        // notify, that we are starting assets installation
        EventBus.getDefault().post(new BeforeAssetsInstalledEvent());

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    copyAssetDirectory(assetManager, fromDirectory, toDirectory);
                    EventBus.getDefault().post(new AssetsInstalledEvent());
                } catch (IOException e) {
                    e.printStackTrace();
                    EventBus.getDefault().post(new AssetsInstallationErrorEvent());
                } finally {
                    isWorking = false;
                }
            }
        }).start();
    }

    private static void copyAssetDirectory(AssetManager assetManager, String fromDirectory, String toDirectory) throws IOException {
        // recreate cache folder
        FilesUtility.delete(toDirectory);
        FilesUtility.ensureDirectoryExists(toDirectory);

        // copy files
        String[] files = assetManager.list(fromDirectory);
        for (String file : files) {
            final String destinationFileAbsolutePath = com.nordnetab.chcp.main.utils.Paths.get(toDirectory, file);
            final String assetFileAbsolutePath = Paths.get(fromDirectory, file).substring(1);

            String subFiles[] = assetManager.list(assetFileAbsolutePath);
            if (subFiles.length == 0) {
                copyAssetFile(assetManager, assetFileAbsolutePath, destinationFileAbsolutePath);
            } else {
                copyAssetDirectory(assetManager, assetFileAbsolutePath, destinationFileAbsolutePath);
            }
        }
    }

    /**
     * Copies asset file to destination path
     */
    private static void copyAssetFile(AssetManager assetManager, String assetFilePath, String destinationFilePath) throws IOException {
        InputStream in = assetManager.open(assetFilePath);
        OutputStream out = new FileOutputStream(destinationFilePath);

        // Transfer bytes from in to out
        byte[] buf = new byte[8192];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }

        in.close();
        out.close();
    }
}