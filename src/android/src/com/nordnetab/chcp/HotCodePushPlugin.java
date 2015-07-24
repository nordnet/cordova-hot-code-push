package com.nordnetab.chcp;

import android.os.Environment;
import android.text.TextUtils;

import com.nordnetab.chcp.utils.Paths;

import org.apache.cordova.CordovaPlugin;

import java.io.File;

/**
 * Created by Nikolay Demyankov on 23.07.15.
 *
 * Plugin entry point.
 */
public class HotCodePushPlugin extends CordovaPlugin {

    public static final String FILE_PREFIX = "file://";
    public static final String WWW_FOLDER = "www";
    public static final String LOCAL_ASSETS_FOLDER = "file:///android_asset/www";
    public static final String APPLICATION_CONFIG_FILE_NAME = "chcp.json";
    public static final String CONTENT_MANIFEST_FILE_NAME = "chcp.manifest";

    public static final String BLANK_PAGE = "about:blank";
    public static final String CONTENT_FOLDER_DEFAULT = "cordova-hot-code-plugin";

    private static String configUrl;
    private static String contentFolderLocation;

    private static class PreferenceKeys {
        public static final String CONFIG_URL = "hot_code_push_config_url";
        public static final String CONTENT_FOLDER = "hot_code_push_local_dir";
    }

    public static String getContentFolderLocation() {
        return contentFolderLocation;
    }

    public static String getWwwFolder() {
        return Paths.get(contentFolderLocation, WWW_FOLDER);
    }

    public static String getDownloadFolderLocation() {
        return Paths.get(contentFolderLocation, "www_tmp");
    }

    public static String getApplicationConfigUrl() {
        return configUrl;
    }

    @Override
    protected void pluginInitialize() {
        processCordovaConfig();
    }

    private void processCordovaConfig() {
        // we already read preferences
        if (!TextUtils.isEmpty(configUrl)) {
            return;
        }

        // url, where config.json is located
        configUrl = preferences.getString(PreferenceKeys.CONFIG_URL, "");

        // get folder, where all data is stored
        String contentFolder = preferences.getString(PreferenceKeys.CONTENT_FOLDER, CONTENT_FOLDER_DEFAULT);
        contentFolderLocation = Paths.get(Environment.getExternalStorageDirectory().getAbsolutePath(), contentFolder);
    }

    @Override
    public void onStart() {
        super.onStart();

        //redirectToLocalStorage();
        //runUpdateProcedure();
    }

    private void redirectToLocalStorage() {
        String currentUrl = webView.getUrl();
        if (TextUtils.isEmpty(currentUrl) || currentUrl.equals(BLANK_PAGE)
                || !currentUrl.contains(LOCAL_ASSETS_FOLDER)) {
            return;
        }

        currentUrl = currentUrl.replace(LOCAL_ASSETS_FOLDER, "");
        String external = Paths.get(getWwwFolder(), currentUrl);
        if (!new File(external).exists()) {
            return;
        }

        webView.loadUrlIntoView(FILE_PREFIX + external, false);
        webView.clearHistory();
    }

    private void runUpdateProcedure() {
        UpdatesLoader.addUpdateTaskToQueue(cordova.getActivity(), getWwwFolder(),
                getDownloadFolderLocation(), getApplicationConfigUrl());
    }
}
