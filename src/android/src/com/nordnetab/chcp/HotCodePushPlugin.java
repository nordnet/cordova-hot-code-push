package com.nordnetab.chcp;

import android.app.ProgressDialog;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.nordnetab.chcp.config.ApplicationConfig;
import com.nordnetab.chcp.config.ContentConfig;
import com.nordnetab.chcp.storage.ApplicationConfigStorage;
import com.nordnetab.chcp.updater.UpdatesInstaller;
import com.nordnetab.chcp.updater.UpdatesLoader;
import com.nordnetab.chcp.utils.Paths;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.ConfigXmlParser;
import org.apache.cordova.CordovaArgs;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.json.JSONException;

import java.io.File;
import java.util.HashMap;

import de.greenrobot.event.EventBus;

/**
 * Created by Nikolay Demyankov on 23.07.15.
 *
 * Plugin entry point.
 */
public class HotCodePushPlugin extends CordovaPlugin {

    // TODO: remove www folder after native update

    private static final String FILE_PREFIX = "file://";
    public static final String WWW_FOLDER = "www";
    private static final String WWW_DOWNLOAD_FOLDER = "www_tmp";
    private static final String WWW_BACKUP_FOLDER = "www_backup";
    public static final String LOCAL_ASSETS_FOLDER = "file:///android_asset/www";
    public static final String APPLICATION_CONFIG_FILE_NAME = "chcp.json";
    public static final String CONTENT_MANIFEST_FILE_NAME = "chcp.manifest";

    public static final String BLANK_PAGE = "about:blank";
    public static final String CONTENT_FOLDER_DEFAULT = "cordova-hot-code-plugin";

    private static String configUrl;
    private static String contentFolderLocation;
    private static String startingPage;
    private static ApplicationConfigStorage appConfigStorage;
    private static String wwwFolder;
    private static String backupFolder;
    private static String downloadFolder;

    private ProgressDialog installProgressDialog;

    private HashMap<String, CallbackContext> fetchTasks;
    private CallbackContext installJsCallback;

    private static class PreferenceKeys {
        public static final String CONFIG_URL = "hot_code_push_config_url";
        public static final String CONTENT_FOLDER = "hot_code_push_local_dir";
    }

    private static class JSActions {
        public static final String FETCH_UPDATE = "fetchUpdate";
        public static final String INSTALL_UPDATE = "installUpdate";
    }

    public static String getContentFolderLocation() {
        return contentFolderLocation;
    }

    public static String getWwwFolder() {
        if (wwwFolder == null) {
            wwwFolder = Paths.get(getContentFolderLocation(), WWW_FOLDER);
        }

        return wwwFolder;
    }

    public static String getDownloadFolder() {
        if (downloadFolder == null) {
            downloadFolder = Paths.get(getContentFolderLocation(), WWW_DOWNLOAD_FOLDER);
        }

        return downloadFolder;
    }

    public static String getBackupFolder() {
        if (backupFolder == null) {
            backupFolder = Paths.get(getContentFolderLocation(), WWW_BACKUP_FOLDER);
        }

        return backupFolder;
    }

    public static String getApplicationConfigUrl() {
        return configUrl;
    }

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);

        processCordovaConfig();

        fetchTasks = new HashMap<String, CallbackContext>();

        if (appConfigStorage == null) {
            appConfigStorage = new ApplicationConfigStorage(cordova.getActivity(), getWwwFolder());
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        EventBus.getDefault().register(this);

        redirectToLocalStorage();

        ApplicationConfig appConfig = appConfigStorage.loadFromPreference();
        if (appConfig != null && appConfig.getContentConfig().getUpdateTime() == ContentConfig.UpdateTime.ON_START) {
            installUpdate(null);
        }
    }

    @Override
    public void onResume(boolean multitasking) {
        super.onResume(multitasking);

        ApplicationConfig appConfig = appConfigStorage.loadFromPreference();
        if (appConfig != null && appConfig.getContentConfig().getUpdateTime() == ContentConfig.UpdateTime.ON_RESUME) {
            installUpdate(null);
        }

        fetchUpdate(null);
    }

    @Override
    public void onStop() {
        super.onStop();

        EventBus.getDefault().unregister(this);
    }

    @Override
    public boolean execute(String action, CordovaArgs args, CallbackContext callbackContext) throws JSONException {
        Log.d("CHCP", "Action from JS: " + action);

        boolean cmdProcessed = true;
        if (JSActions.FETCH_UPDATE.equals(action)) {
            fetchUpdate(callbackContext);
        } else if (JSActions.INSTALL_UPDATE.equals(action)) {
            installUpdate(callbackContext);
        } else {
            cmdProcessed = false;
        }

        return cmdProcessed;
    }

    private void processCordovaConfig() {
        // we already read preferences
        if (!TextUtils.isEmpty(configUrl)) {
            return;
        }

        // url, where config.json is located
        configUrl = preferences.getString(PreferenceKeys.CONFIG_URL, "");

        // get folder, where all data is stored
        String contentFolderName = preferences.getString(PreferenceKeys.CONTENT_FOLDER, CONTENT_FOLDER_DEFAULT);
        //contentFolderLocation = Paths.get(Environment.getExternalStorageDirectory().getAbsolutePath(), contentFolderName);
        contentFolderLocation = Paths.get(cordova.getActivity().getFilesDir().getAbsolutePath(), contentFolderName);
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

    private void fetchUpdate(CallbackContext jsCallback) {
        String taskId = UpdatesLoader.addUpdateTaskToQueue(cordova.getActivity(), getWwwFolder(),
                getDownloadFolder(), getApplicationConfigUrl());

        if (jsCallback != null) {
            fetchTasks.put(taskId, jsCallback);
        }
    }

    private void installUpdate(CallbackContext jsCallback) {
        if (jsCallback != null) {
            installJsCallback = jsCallback;
        }

        if (installProgressDialog != null && installProgressDialog.isShowing()) {
            // already in progress
            return;
        }

        if (UpdatesInstaller.install(cordova.getActivity(), getDownloadFolder(), getWwwFolder(), getBackupFolder())) {
            cordova.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    installProgressDialog = ProgressDialog.show(cordova.getActivity(), "", "Loading, please wait...", true, false);
                }
            });
        }
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

    // region Update download events

    private CallbackContext pollFetchTaskJsCallback(String taskId) {
        if (!fetchTasks.containsKey(taskId)) {
            return null;
        }

        CallbackContext jsCallback = fetchTasks.get(taskId);
        fetchTasks.remove(taskId);

        return jsCallback;
    }

    public void onEvent(UpdatesLoader.UpdateIsReadyToInstallEvent event) {
        Log.d("CHCP", "Update is ready for installation");

        // notify JS
        CallbackContext jsCallback = pollFetchTaskJsCallback(event.taskId);
        if (jsCallback != null) {
            jsCallback.success();
        }

        //perform installation if allowed
        if (event.config.getContentConfig().getUpdateTime() == ContentConfig.UpdateTime.NOW) {
            installUpdate(null);
        }
    }

    public void onEvent(UpdatesLoader.NothingToUpdateEvent event) {
        Log.d("CHCP", "Nothing to update");

        //notify JS
        CallbackContext jsCallback = pollFetchTaskJsCallback(event.taskId);
        if (jsCallback != null) {
            jsCallback.success();
        }
    }

    public void onEvent(UpdatesLoader.UpdateErrorEvent event) {
        Log.d("CHCP", "Failed to update");

        // notify JS
        CallbackContext jsCallback = pollFetchTaskJsCallback(event.taskId);
        if (jsCallback != null) {
            // TODO: add message
            jsCallback.error("Some error message");
        }
    }

    // endregion

    // region Update installation events

    public void onEvent(UpdatesInstaller.UpdateInstalledEvent event) {
        Log.d("CHCP", "Update is installed");

        if (installJsCallback != null) {
            installJsCallback.success();
            installJsCallback = null;
        }

        cordova.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {

//                String startingPage = Paths.get(getWwwFolder(), getStartingPage());
//                webView.loadUrlIntoView(FILE_PREFIX + startingPage, false);
//
//                webView.clearHistory();
//                webView.clearCache();

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
        if (installJsCallback != null) {
            installJsCallback.error("Some error of installation");
            installJsCallback = null;
        }

        cordova.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // hide dialog and show WebView
                if (installProgressDialog != null && installProgressDialog.isShowing()) {
                    installProgressDialog.dismiss();
                    installProgressDialog = null;
                }
            }
        });
    }

    public void onEvent(UpdatesInstaller.NothingToInstallEvent event) {
        Log.d("CHCP", "Nothing to install");
        if (installJsCallback != null) {
            installJsCallback.success();
            installJsCallback = null;
        }
    }

    // endregion
}
