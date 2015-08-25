package com.nordnetab.chcp.model;

import android.content.Context;
import android.os.Environment;

import com.nordnetab.chcp.utils.Paths;

/**
 * Created by Nikolay Demyankov on 21.08.15.
 */
public class PluginFilesStructureImpl implements PluginFilesStructure {

    private static final String CONFIG_FILE_NAME = "chcp.json";
    private static final String MANIFEST_FILE_NAME = "chcp.manifest";

    private static final String PLUGIN_FOLDER = "cordova-hot-code-push-plugin";

    private static final String MAIN_CONTENT_FOLDER = "www";
    private static final String DOWNLOAD_FOLDER = "www_tmp";
    private static final String BACKUP_FOLDER = "www_backup";
    private static final String INSTALLATION_FOLDER = "www_install";

    private String contentFolder;
    private String wwwFolder;
    private String downloadFolder;
    private String backupFolder;
    private String installationFolder;

    public PluginFilesStructureImpl(Context context) {
        contentFolder = Paths.get(Environment.getExternalStorageDirectory().getAbsolutePath(), PLUGIN_FOLDER);
        //contentFolder = Paths.get(context.getFilesDir().getAbsolutePath(), PLUGIN_FOLDER);
    }

    @Override
    public String contentFolder() {
        return contentFolder;
    }

    @Override
    public String wwwFolder() {
        if (wwwFolder == null) {
            wwwFolder = Paths.get(contentFolder(), MAIN_CONTENT_FOLDER);
        }

        return wwwFolder;
    }

    @Override
    public String downloadFolder() {
        if (downloadFolder == null) {
            downloadFolder = Paths.get(contentFolder(), DOWNLOAD_FOLDER);
        }

        return downloadFolder;
    }

    @Override
    public String backupFolder() {
        if (backupFolder == null) {
            backupFolder = Paths.get(contentFolder(), BACKUP_FOLDER);
        }

        return backupFolder;
    }

    @Override
    public String installationFolder() {
        if (installationFolder == null) {
            installationFolder = Paths.get(contentFolder(), INSTALLATION_FOLDER);
        }

        return installationFolder;
    }

    @Override
    public String configFileName() {
        return CONFIG_FILE_NAME;
    }

    @Override
    public String manifestFileName() {
        return MANIFEST_FILE_NAME;
    }
}
