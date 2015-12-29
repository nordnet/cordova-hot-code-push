package com.nordnetab.chcp.main.model;

import android.content.Context;
import android.os.Environment;

import com.nordnetab.chcp.main.utils.Paths;

/**
 * Created by Nikolay Demyankov on 21.08.15.
 * <p/>
 */
public class PluginFilesStructure {

    private static final String CONFIG_FILE_NAME = "chcp.json";
    private static final String MANIFEST_FILE_NAME = "chcp.manifest";

    private static final String PLUGIN_FOLDER = "cordova-hot-code-push-plugin";

    private static final String MAIN_CONTENT_FOLDER = "www";
    private static final String DOWNLOAD_FOLDER = "update";

    private String contentFolder;
    private String wwwFolder;
    private String downloadFolder;

    public static String getPluginRootFolder(final Context context) {
        return Paths.get(context.getFilesDir().getAbsolutePath(), PLUGIN_FOLDER);
        //return Paths.get(Environment.getExternalStorageDirectory().getAbsolutePath(), PLUGIN_FOLDER);
    }

    /**
     * Class constructor
     *
     * @param context application context
     */
    public PluginFilesStructure(final Context context, final String releaseVersion) {
        // uncomment this line, if you want store files on sdcard instead of application file directory
        //contentFolder = Paths.get(Environment.getExternalStorageDirectory().getAbsolutePath(), PLUGIN_FOLDER, releaseVersion);
        contentFolder = Paths.get(context.getFilesDir().getAbsolutePath(), PLUGIN_FOLDER, releaseVersion);
    }

    public void switchToRelease(final String releaseVersion) {
        int idx = contentFolder.lastIndexOf("/");
        contentFolder = Paths.get(contentFolder.substring(0, idx), releaseVersion);

        // reset values
        wwwFolder = null;
        downloadFolder = null;
    }

    public String getContentFolder() {
        return contentFolder;
    }

    public String getWwwFolder() {
        if (wwwFolder == null) {
            wwwFolder = Paths.get(getContentFolder(), MAIN_CONTENT_FOLDER);
        }

        return wwwFolder;
    }

    public String getDownloadFolder() {
        if (downloadFolder == null) {
            downloadFolder = Paths.get(getContentFolder(), DOWNLOAD_FOLDER);
        }

        return downloadFolder;
    }

    public String configFileName() {
        return CONFIG_FILE_NAME;
    }

    public String manifestFileName() {
        return MANIFEST_FILE_NAME;
    }
}
