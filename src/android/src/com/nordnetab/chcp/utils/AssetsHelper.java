package com.nordnetab.chcp.utils;

import android.content.res.AssetManager;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by Nikolay Demyankov on 21.07.15.
 *
 *
 */
public class AssetsHelper {

    private AssetsHelper() {
    }

    public static boolean copyAssetDirectoryToAppDirectory(final AssetManager assetManager, final String fromDirectory, final String toDirectory) {
        try {
            copyAssetDirectory(assetManager, fromDirectory, toDirectory);

            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    private static void copyAssetDirectory(AssetManager assetManager, String fromDirectory, String toDirectory) throws IOException {
        FilesUtility.ensureDirectoryExists(toDirectory);

        String[] files = assetManager.list(fromDirectory);
        for (String file : files) {
            final String destinationFileAbsolutePath = com.nordnetab.chcp.utils.Paths.get(toDirectory, file);
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
