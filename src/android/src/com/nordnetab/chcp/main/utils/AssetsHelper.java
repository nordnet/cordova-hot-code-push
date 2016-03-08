package com.nordnetab.chcp.main.utils;

import android.content.res.AssetManager;
import android.util.Log;
import android.webkit.URLUtil;

import com.nordnetab.chcp.main.config.ContentConfig;
import com.nordnetab.chcp.main.events.AssetsInstallationErrorEvent;
import com.nordnetab.chcp.main.events.AssetsInstalledEvent;
import com.nordnetab.chcp.main.model.PluginFilesStructure;
import com.nordnetab.chcp.main.network.FileDownloader;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by Nikolay Demyankov on 21.07.15.
 * <p/>
 * Utility class to copy files from assets folder into the external storage.
 */
public class AssetsHelper {
    private static final int ZIP_BUFFER_SIZE = 1024 * 4;

    private static boolean isWorking;

    private AssetsHelper() {
    }

    public static void copyAssetsFromRemoteZipUrlToDirectory(final String assetsRemoteZipUrl, final String toDirectory) {
        if (isWorking) {
            return;
        }

        isWorking = true;

        new Thread(new Runnable() {
            @Override
            public void run() {
                long startTime;
                long elapsed;

                try {
                    recreateCacheFolder(toDirectory);

                    File assetsDestinationDirectory = new File(toDirectory);

                    String remoteFilePath = URLUtil.guessFileName(assetsRemoteZipUrl, null, null);

                    String assetsLocalZipFilePath = Paths.get(assetsDestinationDirectory.getParent(), remoteFilePath);

                    startTime = System.currentTimeMillis();
                    FileDownloader.download(assetsRemoteZipUrl, assetsLocalZipFilePath, null);
                    elapsed = System.currentTimeMillis() - startTime;
                    Log.d("CHCP", String.format("Downloading remote assets zip file took %d ms", elapsed));

                    startTime = System.currentTimeMillis();
                    extractZipFile(assetsLocalZipFilePath, toDirectory);
                    elapsed = System.currentTimeMillis() - startTime;
                    Log.d("CHCP", String.format("Extracting remote assets zip file took %d ms", elapsed));

                    FilesUtility.delete(assetsLocalZipFilePath);

                    isWorking = false;

                    EventBus.getDefault().post(new AssetsInstalledEvent());
                } catch(IOException exception) {
                    isWorking = false;

                    // fallback
                    createStubAppDirectory(toDirectory);
                }
            }
        }).start();
    }

    /**
     * Copy files from the assets folder into the specific folder on the external storage.
     * Method runs asynchronously. Results are dispatched through events.
     *
     * @param toDirectory   absolute path to the destination folder on the external storage
     *
     * @see AssetsInstallationErrorEvent
     * @see AssetsInstalledEvent
     * @see EventBus
     */
    public static void createStubAppDirectory(final String toDirectory) {
        if (isWorking) {
            return;
        }

        isWorking = true;

        new Thread(new Runnable() {
            @Override
            public void run() {
                recreateCacheFolder(toDirectory);

                if(createStubApplicationConfiguration(toDirectory) &&
                   createStubContentManifest(toDirectory)) {
                    EventBus.getDefault().post(new AssetsInstalledEvent());
                } else {
                    EventBus.getDefault().post(new AssetsInstallationErrorEvent());
                }

                isWorking = false;

            }
        }).start();
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

    private static void recreateCacheFolder(String directory) {
        FilesUtility.delete(directory);
        FilesUtility.ensureDirectoryExists(directory);
    }

    private static void copyAssetDirectory(AssetManager assetManager, String fromDirectory, String toDirectory) throws IOException {
        recreateCacheFolder(toDirectory);

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

    private static void extractZipFile(String assetsZipFilePath, String toDirectory) throws IOException {
        ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(assetsZipFilePath));

        try {
            ZipEntry zipEntry;

            while((zipEntry = zipInputStream.getNextEntry()) != null) {
                File extractedFile = new File(toDirectory, zipEntry.getName());
                if(zipEntry.isDirectory()) {
                    if(!extractedFile.isDirectory() && !extractedFile.mkdirs()) {
                        throw new IOException("Failed to create directory");
                    }
                } else {
                    byte[] buffer = new byte[ZIP_BUFFER_SIZE];
                    FileOutputStream fileOutputStream = new FileOutputStream(extractedFile, false);
                    BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream, buffer.length);

                    int bytesRead;
                    try {
                        while((bytesRead = zipInputStream.read(buffer, 0, buffer.length)) != -1) {
                            bufferedOutputStream.write(buffer, 0, bytesRead);
                        }
                    } finally {
                        bufferedOutputStream.flush();
                        bufferedOutputStream.close();

                        fileOutputStream.flush();
                        fileOutputStream.close();

                        zipInputStream.closeEntry();
                    }
                }
            }
        } finally {
            zipInputStream.close();
        }
    }

    private static boolean saveJsonObjectToFile(JSONObject jsonObject, File file) {
        try {
            FilesUtility.ensureDirectoryExists(file.getParent());

            FileOutputStream outputStream = new FileOutputStream(file, false);

            outputStream.write(jsonObject.toString().getBytes());

            outputStream.close();

            return true;
        } catch (IOException ex) {
            Log.d("CHCP", "Could not create an empty JSON file", ex);
            return false;
        }
    }

    private static boolean createStubApplicationConfiguration(String outputDirectory) {
        File configurationFile = new File(outputDirectory, PluginFilesStructure.CONFIG_FILE_NAME);
        JSONObject configuration = new JSONObject();

        try {
            configuration.put(ContentConfig.JsonKeys.VERSION, "");
            configuration.put(ContentConfig.JsonKeys.CONTENT_URL, "");
        } catch(JSONException e) {
            return false;
        }

        return saveJsonObjectToFile(configuration, configurationFile);
    }

    private static boolean createStubContentManifest(String outputDirectory) {
        File manifestFile = new File(outputDirectory, PluginFilesStructure.MANIFEST_FILE_NAME);
        JSONObject manifest = new JSONObject();

        return saveJsonObjectToFile(manifest, manifestFile);
    }
}