package com.nordnetab.chcp.main.utils;

import android.content.Context;
import android.content.res.AssetManager;

import com.nordnetab.chcp.main.events.AssetsInstallationErrorEvent;
import com.nordnetab.chcp.main.events.AssetsInstalledEvent;
import com.nordnetab.chcp.main.events.BeforeAssetsInstalledEvent;

import org.apache.cordova.LOG;
import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

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
     * @param applicationContext
     * @param assetManager       assets manager
     * @param fromDirectory      which directory in assets we want to copy
     * @param toDirectory        absolute path to the destination folder on the external storage
     * @see AssetsInstallationErrorEvent
     * @see AssetsInstalledEvent
     * @see EventBus
     */
    public static void copyAssetDirectoryToAppDirectory(final Context applicationContext, final AssetManager assetManager, final String fromDirectory, final String toDirectory) {
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
                    long start = System.currentTimeMillis();
                    copyAssetDirectory(applicationContext, fromDirectory, toDirectory);
                    EventBus.getDefault().post(new AssetsInstalledEvent());
                    long time = System.currentTimeMillis() - start;
                    LOG.d("CHCP", "Copied assets in : %d ms", time);
                } catch (IOException e) {
                    e.printStackTrace();
                    EventBus.getDefault().post(new AssetsInstallationErrorEvent());
                } finally {
                    isWorking = false;
                }
            }
        }).start();
    }

    private static void copyAssetDirectory(Context applicationContext, String fromDirectory, String toDirectory) throws IOException {
        // recreate cache folder
        FilesUtility.delete(toDirectory);
        FilesUtility.ensureDirectoryExists(toDirectory);
        JarFile jarFile = new JarFile(applicationContext.getApplicationInfo().sourceDir);

        String prefix = "assets/" + fromDirectory;
        int prefixLength = prefix.length();
        Enumeration<JarEntry> enu = jarFile.entries();
        while (enu.hasMoreElements()) {
            JarEntry fileJarEntry = enu.nextElement();
            String name = fileJarEntry.getName();
            if (!fileJarEntry.isDirectory() && name.startsWith(prefix)) {
                final String destinationFileAbsolutePath = Paths.get(toDirectory, name.substring(prefixLength));

                copyAssetFile(jarFile.getInputStream(fileJarEntry), destinationFileAbsolutePath);
            }
        }

    }

    /**
     * Copies asset file to destination path
     */
    private static void copyAssetFile(InputStream in, String destinationFilePath) throws IOException {
        FilesUtility.ensureDirectoryExists(new File(destinationFilePath).getParent());
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