package com.nordnetab.chcp;

import android.app.ProgressDialog;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.nordnetab.chcp.config.ContentConfig;
import com.nordnetab.chcp.utils.Paths;

import org.apache.cordova.ConfigXmlParser;
import org.apache.cordova.CordovaPlugin;

import java.io.File;

import de.greenrobot.event.EventBus;

/**
 * Created by Nikolay Demyankov on 23.07.15.
 *
 * Plugin entry point.
 */
public class HotCodePushPlugin extends CordovaPlugin {

    // TODO: remove www folder after native update

    public static final String FILE_PREFIX = "file://";
    public static final String WWW_FOLDER = "www";
    public static final String LOCAL_ASSETS_FOLDER = "file:///android_asset/www";
    public static final String APPLICATION_CONFIG_FILE_NAME = "chcp.json";
    public static final String CONTENT_MANIFEST_FILE_NAME = "chcp.manifest";

    public static final String BLANK_PAGE = "about:blank";
    public static final String CONTENT_FOLDER_DEFAULT = "cordova-hot-code-plugin";

    private static String configUrl;
    private static String contentFolderLocation;
    private static String startingPage;


    private ProgressDialog installProgressDialog;

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

        EventBus.getDefault().register(this);

        redirectToLocalStorage();
        fetchUpdate();
    }

    @Override
    public void onStop() {
        super.onStop();

        EventBus.getDefault().unregister(this);
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

    private void fetchUpdate() {
        UpdatesLoader.addUpdateTaskToQueue(cordova.getActivity(), getWwwFolder(),
                getDownloadFolderLocation(), getApplicationConfigUrl());
    }

    private void installUpdate() {
        if (installProgressDialog != null && installProgressDialog.isShowing()) {
            // already in progress
            return;
        }

        cordova.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                installProgressDialog = ProgressDialog.show(cordova.getActivity(), "", "Loading, please wait...", true, false);
            }
        });

        UpdatesInstaller.install(cordova.getActivity(), getDownloadFolderLocation(), getWwwFolder());
    }

    private String getStartingPage() {
        if (TextUtils.isEmpty(startingPage)) {
            ConfigXmlParser parser = new ConfigXmlParser();
            parser.parse(cordova.getActivity());
            String url = parser.getLaunchUrl();

            startingPage = url.replace(LOCAL_ASSETS_FOLDER, "");
        }

        return startingPage;
    }

    // region Events

    public void onEvent(UpdatesLoader.UpdateIsReadyToInstallEvent event) {
        Log.d("CHCP", "Update is ready for installation");

        if (event.config.getContentConfig().getUpdateTime() == ContentConfig.UpdateTime.NOW) {
            installUpdate();
        }
    }

    public void onEvent(UpdatesLoader.NothingToUpdateEvent event) {
        Log.d("CHCP", "Nothing to update");
    }

    public void onEvent(UpdatesLoader.UpdateErrorEvent event) {
        Log.d("CHCP", "Failed to update");
    }

    public void onEvent(UpdatesInstaller.UpdateInstalledEvent event) {
        Log.d("CHCP", "Update is installed");

        cordova.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {

                //TODO: call page reload from javascript

                String startingPage = Paths.get(getWwwFolder(), getStartingPage());
                webView.loadUrlIntoView(FILE_PREFIX + startingPage, false);

                webView.clearHistory();
                webView.clearCache();

                Log.d("CHCP", "Can go back: " + webView.canGoBack());

                // hide dialog and show WebView
                if (installProgressDialog != null && installProgressDialog.isShowing()) {
                    installProgressDialog.dismiss();
                    installProgressDialog = null;
                }
            }
        });
    }

    public void onEvent(UpdatesInstaller.InstallationErrorEvent event) {
        Log.d("CHCP", "Failed to install");


    }

    @Override
    public Boolean shouldAllowNavigation(String url) {
        Log.d("CHCP", "shouldAllowNavigation");
        return super.shouldAllowNavigation(url);
    }

    @Override
    public boolean onOverrideUrlLoading(String url) {
        Log.d("CHCP", "onOverrideUrlLoading");
        return super.onOverrideUrlLoading(url);
    }

    // endregion
}
