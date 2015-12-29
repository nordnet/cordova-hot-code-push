package com.nordnetab.chcp.main.model;

import android.content.Context;

import com.nordnetab.chcp.main.utils.Paths;

/**
 * Created by Nikolay Demyankov on 21.08.15.
 * <p/>
 * <p/>
 * Model for plugins working files and folders.
 */
public class PluginFilesStructure {

    /**
     * Application config file name.
     */
    public static final String CONFIG_FILE_NAME = "chcp.json";

    /**
     * Manifest file name.
     */
    public static final String MANIFEST_FILE_NAME = "chcp.manifest";

    private static final String PLUGIN_FOLDER = "cordova-hot-code-push-plugin";

    private static final String MAIN_CONTENT_FOLDER = "www";
    private static final String DOWNLOAD_FOLDER = "update";

    private String contentFolder;
    private String wwwFolder;
    private String downloadFolder;

    /**
     * Get root folder, where all plugin files are located.
     *
     * @param context application context
     * @return absolute path to root folder
     */
    public static String getPluginRootFolder(final Context context) {
        return Paths.get(context.getFilesDir().getAbsolutePath(), PLUGIN_FOLDER);
        //return Paths.get(Environment.getExternalStorageDirectory().getAbsolutePath(), PLUGIN_FOLDER);
    }

    /**
     * Constructor.
     *
     * @param context        application context
     * @param releaseVersion version name, for which we need file structure.
     */
    public PluginFilesStructure(final Context context, final String releaseVersion) {
        // uncomment this line, if you want store files on sdcard instead of application file directory
        //contentFolder = Paths.get(Environment.getExternalStorageDirectory().getAbsolutePath(), PLUGIN_FOLDER, releaseVersion);
        contentFolder = Paths.get(context.getFilesDir().getAbsolutePath(), PLUGIN_FOLDER, releaseVersion);
    }

    /**
     * Switch model to another release.
     *
     * @param releaseVersion to what release we are switching
     */
    public void switchToRelease(final String releaseVersion) {
        int idx = contentFolder.lastIndexOf("/");
        contentFolder = Paths.get(contentFolder.substring(0, idx), releaseVersion);

        // reset values
        wwwFolder = null;
        downloadFolder = null;
    }

    /**
     * Getter for version content folder.
     *
     * @return content folder absolute path
     */
    public String getContentFolder() {
        return contentFolder;
    }

    /**
     * Getter for version's www folder: where all web project files are located.
     *
     * @return www folder absolute path
     */
    public String getWwwFolder() {
        if (wwwFolder == null) {
            wwwFolder = Paths.get(getContentFolder(), MAIN_CONTENT_FOLDER);
        }

        return wwwFolder;
    }

    /**
     * Getter for the folder, where downloaded content is placed.
     *
     * @return download folder absolute path
     */
    public String getDownloadFolder() {
        if (downloadFolder == null) {
            downloadFolder = Paths.get(getContentFolder(), DOWNLOAD_FOLDER);
        }

        return downloadFolder;
    }

}
