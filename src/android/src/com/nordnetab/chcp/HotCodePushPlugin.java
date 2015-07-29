package com.nordnetab.chcp;

import android.app.ProgressDialog;
import android.net.Uri;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.nordnetab.chcp.config.ApplicationConfig;
import com.nordnetab.chcp.config.ContentConfig;
import com.nordnetab.chcp.config.PluginConfig;
import com.nordnetab.chcp.js.PluginResultHelper;
import com.nordnetab.chcp.storage.ApplicationConfigStorage;
import com.nordnetab.chcp.storage.PluginConfigStorage;
import com.nordnetab.chcp.updater.UpdatesInstaller;
import com.nordnetab.chcp.updater.UpdatesLoader;
import com.nordnetab.chcp.utils.AssetsHelper;
import com.nordnetab.chcp.utils.Paths;
import com.nordnetab.chcp.utils.URLUtility;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.ConfigXmlParser;
import org.apache.cordova.CordovaArgs;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaResourceApi;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.json.JSONException;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import de.greenrobot.event.EventBus;

/**
 * Created by Nikolay Demyankov on 23.07.15.
 * <p/>
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
    public static final String CONTENT_FOLDER_DEFAULT = "cordova-hot-code-push-plugin";

    private static String contentFolderLocation;
    private static String startingPage;
    private static ApplicationConfigStorage appConfigStorage;
    private static String wwwFolder;
    private static String backupFolder;
    private static String downloadFolder;
    private static PluginConfig pluginConfig;
    private static PluginConfigStorage pluginConfigStorage;

    private ProgressDialog installProgressDialog;

    private HashMap<String, CallbackContext> fetchTasks;
    private CallbackContext installJsCallback;
    private CallbackContext jsDefaultCallback;

    private static class JSActions {
        public static final String FETCH_UPDATE = "fetchUpdate";
        public static final String INSTALL_UPDATE = "installUpdate";
        public static final String CONFIGURE = "configure";
        public static final String INIT = "init";
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
        return pluginConfig.getConfigUrl();
    }

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);

        fetchTasks = new HashMap<String, CallbackContext>();

        loadPluginConfig();

        // location of the cache folder
        if (contentFolderLocation == null) {
            contentFolderLocation = Paths.get(Environment.getExternalStorageDirectory().getAbsolutePath(), CONTENT_FOLDER_DEFAULT);
            //contentFolderLocation = Paths.get(cordova.getActivity().getFilesDir().getAbsolutePath(), CONTENT_FOLDER_DEFAULT);
        }

        if (appConfigStorage == null) {
            appConfigStorage = new ApplicationConfigStorage(cordova.getActivity(), getWwwFolder());
        }

        ensureWwwFolderExists();
    }

    // TODO: move to background thread
    private void ensureWwwFolderExists() {
        String externalWwwFolder = getWwwFolder();
        if (new File(externalWwwFolder).exists()) {
            return;
        }

        AssetsHelper.copyAssetDirectoryToAppDirectory(cordova.getActivity().getAssets(), HotCodePushPlugin.WWW_FOLDER, externalWwwFolder);
    }

    private void loadPluginConfig() {
        if (pluginConfig != null) {
            return;
        }

        pluginConfigStorage = new PluginConfigStorage(cordova.getActivity());
        PluginConfig config = pluginConfigStorage.loadFromPreference();
        if (config == null) {
            config = PluginConfig.createDefaultConfig(cordova.getActivity(), preferences);
            pluginConfigStorage.storeInPreference(config);
        }
        pluginConfig = config;
    }

    @Override
    public void onStart() {
        super.onStart();

        EventBus.getDefault().register(this);

        redirectToLocalStorage();

        if (pluginConfig.isAutoInstallIsAllowed()) {
            ApplicationConfig appConfig = appConfigStorage.loadFromPreference();
            if (appConfig != null && appConfig.getContentConfig().getUpdateTime() == ContentConfig.UpdateTime.ON_START) {
                installUpdate(null);
            }
        }
    }

    @Override
    public void onResume(boolean multitasking) {
        super.onResume(multitasking);

        if (pluginConfig.isAutoInstallIsAllowed()) {
            ApplicationConfig appConfig = appConfigStorage.loadFromPreference();
            if (appConfig != null && appConfig.getContentConfig().getUpdateTime() == ContentConfig.UpdateTime.ON_RESUME) {
                installUpdate(null);
            }
        }

        if (pluginConfig.isAutoDownloadIsAllowed()) {
            fetchUpdate(null);
        }
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
        } else if (JSActions.CONFIGURE.equals(action)) {
            // TODO: add configure
        } else if (JSActions.INIT.equals(action)) {
            initJs(callbackContext);
        } else {
            cmdProcessed = false;
        }

        return cmdProcessed;
    }

    private void initJs(CallbackContext callback) {
        jsDefaultCallback = callback;

        if (shouldReloadOnInit) {
            shouldReloadOnInit = false;
            PluginResult jsResult = PluginResultHelper.getReloadPageAction(getStartingPage());
            jsResult.setKeepCallback(true);
            jsDefaultCallback.sendPluginResult(jsResult);
        }
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
            webView.loadUrlIntoView(BLANK_PAGE, false);
            return;
        }

        webView.loadUrlIntoView(FILE_PREFIX + external, false);
        webView.clearHistory();
        webView.clearCache();
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

        boolean didLaunchInstall = UpdatesInstaller.install(cordova.getActivity(), getDownloadFolder(), getWwwFolder(), getBackupFolder());
        if (didLaunchInstall && installProgressDialog == null) {
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
            startingPage = FILE_PREFIX + Paths.get(getWwwFolder(), startingPage);
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

        PluginResult jsResult = PluginResultHelper.getResultForUpdateLoadSuccess();

        // notify JS
        CallbackContext jsCallback = pollFetchTaskJsCallback(event.taskId);
        if (jsCallback != null) {
            jsCallback.sendPluginResult(jsResult);
        }

        if (jsDefaultCallback != null) {
            jsResult.setKeepCallback(true);
            jsDefaultCallback.sendPluginResult(jsResult);
        }

        //perform installation if allowed
        if (pluginConfig.isAutoInstallIsAllowed()
                && event.config.getContentConfig().getUpdateTime() == ContentConfig.UpdateTime.NOW) {
            installUpdate(null);
        }
    }

    public void onEvent(UpdatesLoader.NothingToUpdateEvent event) {
        Log.d("CHCP", "Nothing to update");

        PluginResult jsResult = PluginResultHelper.getResultForNothingToUpdate();

        //notify JS
        CallbackContext jsCallback = pollFetchTaskJsCallback(event.taskId);
        if (jsCallback != null) {
            jsCallback.sendPluginResult(jsResult);
        }

        if (jsDefaultCallback != null) {
            jsResult.setKeepCallback(true);
            jsDefaultCallback.sendPluginResult(jsResult);
        }
    }

    public void onEvent(UpdatesLoader.UpdateErrorEvent event) {
        Log.d("CHCP", "Failed to update");

        PluginResult jsResult = PluginResultHelper.getResultForUpdateLoadError(event.error);

        // notify JS
        CallbackContext jsCallback = pollFetchTaskJsCallback(event.taskId);
        if (jsCallback != null) {
            jsCallback.sendPluginResult(jsResult);
        }

        if (jsDefaultCallback != null) {
            jsResult.setKeepCallback(true);
            jsDefaultCallback.sendPluginResult(jsResult);
        }
    }

    // endregion

    // region Update installation events

    private boolean shouldReloadOnInit;

    public void onEvent(UpdatesInstaller.UpdateInstalledEvent event) {
        Log.d("CHCP", "Update is installed");

        final PluginResult jsResult = PluginResultHelper.getResultForInstallationSuccess();

        if (installJsCallback != null) {
            installJsCallback.sendPluginResult(jsResult);
            installJsCallback = null;
        }

        if (jsDefaultCallback != null) {
            jsResult.setKeepCallback(true);
            jsDefaultCallback.sendPluginResult(jsResult);

            PluginResult reloadAction = PluginResultHelper.getReloadPageAction(getStartingPage());
            reloadAction.setKeepCallback(true);
            jsDefaultCallback.sendPluginResult(reloadAction);
        } else {
            shouldReloadOnInit = true;
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

    public void onEvent(UpdatesInstaller.InstallationErrorEvent event) {
        Log.d("CHCP", "Failed to install");

        PluginResult jsResult = PluginResultHelper.getResultForInstallationError(event.error);

        // notify js
        if (installJsCallback != null) {
            installJsCallback.sendPluginResult(jsResult);
            installJsCallback = null;
        }

        if (jsDefaultCallback != null) {
            jsResult.setKeepCallback(true);
            jsDefaultCallback.sendPluginResult(jsResult);
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

        PluginResult jsResult = PluginResultHelper.getResultForNothingToInstall();

        // notify JS
        if (installJsCallback != null) {
            installJsCallback.sendPluginResult(jsResult);
            installJsCallback = null;
        }

        if (jsDefaultCallback != null) {
            jsResult.setKeepCallback(true);
            jsDefaultCallback.sendPluginResult(jsResult);
        }

    }

    // endregion
}
