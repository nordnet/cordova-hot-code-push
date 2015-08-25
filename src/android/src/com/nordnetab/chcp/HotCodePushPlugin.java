package com.nordnetab.chcp;

import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.nordnetab.chcp.config.ApplicationConfig;
import com.nordnetab.chcp.config.ChcpXmlConfig;
import com.nordnetab.chcp.config.ContentConfig;
import com.nordnetab.chcp.config.PluginConfig;
import com.nordnetab.chcp.events.NothingToInstallEvent;
import com.nordnetab.chcp.events.NothingToUpdateEvent;
import com.nordnetab.chcp.events.UpdateDownloadErrorEvent;
import com.nordnetab.chcp.events.UpdateInstallationErrorEvent;
import com.nordnetab.chcp.events.UpdateInstalledEvent;
import com.nordnetab.chcp.events.UpdateIsReadyToInstallEvent;
import com.nordnetab.chcp.js.PluginResultHelper;
import com.nordnetab.chcp.model.IPluginFilesStructure;
import com.nordnetab.chcp.model.PluginFilesStructureImpl;
import com.nordnetab.chcp.storage.ApplicationConfigStorage;
import com.nordnetab.chcp.storage.IConfigFileStorage;
import com.nordnetab.chcp.storage.IConfigPreferenceStorage;
import com.nordnetab.chcp.storage.PluginConfigStorage;
import com.nordnetab.chcp.updater.UpdatesInstaller;
import com.nordnetab.chcp.updater.UpdatesLoader;
import com.nordnetab.chcp.utils.AssetsHelper;
import com.nordnetab.chcp.utils.Paths;
import com.nordnetab.chcp.utils.VersionHelper;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.ConfigXmlParser;
import org.apache.cordova.CordovaArgs;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.net.URL;
import java.util.HashMap;

import de.greenrobot.event.EventBus;

/**
 * Created by Nikolay Demyankov on 23.07.15.
 * <p/>
 * Plugin entry point.
 */

// TODO: simplify events, like in iOS
// TODO: update queue: should store only 1 task, like in iOS
// TODO: simplify dependencies on update time, like in iOS
// TODO: set storage place to data directory, not sdcard

// DONE: change names of javascript actions, called from web

public class HotCodePushPlugin extends CordovaPlugin {

    private static final String FILE_PREFIX = "file://";
    public static final String WWW_FOLDER = "www";
    public static final String LOCAL_ASSETS_FOLDER = "file:///android_asset/www";

    public static final String BLANK_PAGE = "about:blank";

    private String startingPage;
    private IConfigFileStorage<ApplicationConfig> appConfigStorage;
    private PluginConfig pluginConfig;
    private IConfigPreferenceStorage<PluginConfig> pluginConfigStorage;
    private ChcpXmlConfig chcpXmlConfig;
    private IPluginFilesStructure fileStructure;

    //private ProgressDialog progressDialog;

    private HashMap<String, CallbackContext> fetchTasks;
    private CallbackContext installJsCallback;
    private CallbackContext jsDefaultCallback;

    private Handler handler;

    private boolean isPluginReadyForWork;

    private Socket devSocket;

    private static class JSActions {
        public static final String FETCH_UPDATE = "jsFetchUpdate";
        public static final String INSTALL_UPDATE = "jsInstallUpdate";
        public static final String CONFIGURE = "jsConfigure";
        public static final String INIT = "jsInitPlugin";
    }

    @Override
    public void initialize(final CordovaInterface cordova, final CordovaWebView webView) {
        super.initialize(cordova, webView);

        fetchTasks = new HashMap<String, CallbackContext>();
        handler = new Handler();

        fileStructure = new PluginFilesStructureImpl(cordova.getActivity());

        parseCordovaConfigXml();
        loadPluginConfig();

        appConfigStorage = new ApplicationConfigStorage(fileStructure);
    }

    private void connectToLocalDevSocket() {
        try {
            URL serverURL = new URL(pluginConfig.getConfigUrl());
            String socketUrl = serverURL.getProtocol() + serverURL.getAuthority();

            devSocket = IO.socket(socketUrl);
            devSocket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {

                @Override
                public void call(Object... args) {
                    Log.d("CHCP", "Socket connected");
                }

            }).on("release", new Emitter.Listener() {

                @Override
                public void call(Object... args) {
                    Log.d("CHCP", "New Release is available");
                    fetchUpdate(null);
                }

            }).on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {

                @Override
                public void call(Object... args) {
                    Log.d("CHCP", "Socket disonnected");
                }

            });
            devSocket.connect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean isWwwFolderExists() {
        return new File(fileStructure.wwwFolder()).exists();
    }

    private void parseCordovaConfigXml() {
        if (chcpXmlConfig != null) {
            return;
        }

        chcpXmlConfig = ChcpXmlConfig.parse(cordova.getActivity());
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
        pluginConfig.setConfigUrl(chcpXmlConfig.getConfigUrl());
    }

    @Override
    public void onStart() {
        super.onStart();

        EventBus.getDefault().register(this);

        if (chcpXmlConfig.getDevelopmentOptions().isEnabled()) {
            connectToLocalDevSocket();
        }

        // ensure that www folder installed on external storage
        isPluginReadyForWork = isWwwFolderExists() && !isApplicationHasBeenUpdated();
        if (!isPluginReadyForWork) {
            installWwwFolder();
            return;
        }

        redirectToLocalStorage();

        if (pluginConfig.isAutoInstallIsAllowed()) {
            ApplicationConfig appConfig = appConfigStorage.loadFromFolder(fileStructure.installationFolder());
            if (appConfig != null && appConfig.getContentConfig().getUpdateTime() == ContentConfig.UpdateTime.ON_START) {
                installUpdate(null);
            }
        }
    }

    private boolean isApplicationHasBeenUpdated() {
        return pluginConfig.getAppBuildVersion() < VersionHelper.applicationVersionCode(cordova.getActivity());
    }

    @Override
    public void onResume(boolean multitasking) {
        super.onResume(multitasking);

        if (!isPluginReadyForWork) {
            return;
        }

        if (pluginConfig.isAutoInstallIsAllowed()) {
            ApplicationConfig appConfig = appConfigStorage.loadFromFolder(fileStructure.installationFolder());
            if (appConfig != null && appConfig.getContentConfig().getUpdateTime() == ContentConfig.UpdateTime.ON_RESUME) {
                installUpdate(null);
            }
        }

        if (pluginConfig.isAutoDownloadIsAllowed()) {
            fetchUpdate(null);
        }
    }

    private void installWwwFolder() {
        //showProgressDialog();
        //webView.getView().setVisibility(View.INVISIBLE);

        AssetsHelper.copyAssetDirectoryToAppDirectory(cordova.getActivity().getAssets(),
                HotCodePushPlugin.WWW_FOLDER, fileStructure.wwwFolder());
    }

//    private void dismissProgressDialog() {
//        if (progressDialog == null) {
//            return;
//        }
//
//        progressDialog.dismiss();
//        progressDialog = null;
//    }
//
//    private void showProgressDialog() {
//        if (progressDialog != null) {
//            return;
//        }
//
//        Context context = cordova.getActivity();
//        Resources resources = context.getResources();
//        String packageName = context.getPackageName();
//        int msgIdentifier = resources.getIdentifier("chcp_installation_progress_message", "string", packageName);
//        String progressMessage = context.getString(msgIdentifier);
//
//        try {
//            progressDialog = ProgressDialog.show(context, "", progressMessage, true, false);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);

        devSocket.disconnect();

        super.onStop();
    }

    @Override
    public boolean execute(String action, CordovaArgs args, CallbackContext callbackContext) throws JSONException {
        Log.d("CHCP", "Action from JS: " + action);

        if (JSActions.INIT.equals(action)) {
            initJs(callbackContext);
            return true;
        }

        if (!isPluginReadyForWork) {
            return false;
        }

        boolean cmdProcessed = true;
        if (JSActions.FETCH_UPDATE.equals(action)) {
            fetchUpdate(callbackContext);
        } else if (JSActions.INSTALL_UPDATE.equals(action)) {
            installUpdate(callbackContext);
        } else if (JSActions.CONFIGURE.equals(action)) {
            jsSetPluginOptions(args, callbackContext);
        } else {
            cmdProcessed = false;
        }

        return cmdProcessed;
    }

    private void sendMessageToDefaultCallback(PluginResult message) {
        if (jsDefaultCallback == null) {
            return;
        }

        message.setKeepCallback(true);
        jsDefaultCallback.sendPluginResult(message);
    }

    private void initJs(CallbackContext callback) {
        jsDefaultCallback = callback;

        handler.post(new Runnable() {
            @Override
            public void run() {
                webView.clearHistory();
            }
        });
    }

    private void jsSetPluginOptions(CordovaArgs arguments, CallbackContext callback) {
        // TODO: send correct message back to JS
        try {
            JSONObject jsonObject = (JSONObject) arguments.get(0);
            pluginConfig.mergeOptionsFromJs(jsonObject);
            pluginConfigStorage.storeInPreference(pluginConfig);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        callback.success();
    }

    private void redirectToLocalStorage() {
        String currentUrl = webView.getUrl();
        if (TextUtils.isEmpty(currentUrl) || currentUrl.equals(BLANK_PAGE)
                || !currentUrl.contains(LOCAL_ASSETS_FOLDER)) {
            return;
        }

        currentUrl = currentUrl.replace(LOCAL_ASSETS_FOLDER, "");
        String external = Paths.get(fileStructure.wwwFolder(), currentUrl);
        if (!new File(external).exists()) {
            return;
        }

        webView.loadUrlIntoView(FILE_PREFIX + external, false);
    }

    private void fetchUpdate(CallbackContext jsCallback) {
        if (!isPluginReadyForWork) {
            return;
        }

        String taskId = UpdatesLoader.addUpdateTaskToQueue(cordova.getActivity(), pluginConfig.getConfigUrl(), fileStructure);
        if (jsCallback != null) {
            fetchTasks.put(taskId, jsCallback);
        }
    }

    private void installUpdate(CallbackContext jsCallback) {
        if (UpdatesInstaller.isInstalling()) {
            return;
        }

        boolean didLaunchInstall = UpdatesInstaller.install(fileStructure);
        if (!didLaunchInstall) {
            return;
        }

        if (jsCallback != null) {
            installJsCallback = jsCallback;
        }

//        handler.post(new Runnable() {
//            @Override
//            public void run() {
//                showProgressDialog();
//            }
//        });
    }

    private String getStartingPage() {
        if (TextUtils.isEmpty(startingPage)) {
            ConfigXmlParser parser = new ConfigXmlParser();
            parser.parse(cordova.getActivity());
            String url = parser.getLaunchUrl();

            startingPage = url.replace(LOCAL_ASSETS_FOLDER, "");
            startingPage = FILE_PREFIX + Paths.get(fileStructure.wwwFolder(), startingPage);
        }

        return startingPage;
    }

    // region Assets installation events

    public void onEvent(AssetsHelper.AssetsInstalledEvent event) {
        // update stored application version
        pluginConfig.setAppBuildVersion(VersionHelper.applicationVersionCode(cordova.getActivity()));
        pluginConfigStorage.storeInPreference(pluginConfig);

        isPluginReadyForWork = true;

        //resetApplicationToStartingPage();

        // we need small delay to let webview to reload the page
//        handler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                webView.getView().setVisibility(View.VISIBLE);
//                //dismissProgressDialog();
//                fetchUpdate(null);
//            }
//        }, 150);
        fetchUpdate(null);
    }

    public void onEvent(AssetsHelper.AssetsInstallationFailedEvent event) {
        Log.d("CHCP", "Can't install assets on device. Continue to work with default bundle");

//        handler.post(new Runnable() {
//            @Override
//            public void run() {
//                //webView.getView().setVisibility(View.VISIBLE);
////                dismissProgressDialog();
//            }
//        });
    }

    // endregion

    // region Update download events

    private CallbackContext pollFetchTaskJsCallback(String taskId) {
        if (!fetchTasks.containsKey(taskId)) {
            return null;
        }

        CallbackContext jsCallback = fetchTasks.get(taskId);
        fetchTasks.remove(taskId);

        return jsCallback;
    }

    public void onEvent(UpdateIsReadyToInstallEvent event) {
        Log.d("CHCP", "Update is ready for installation");

        PluginResult jsResult = PluginResultHelper.pluginResultFromEvent(event);

        // notify JS
        CallbackContext jsCallback = pollFetchTaskJsCallback(event.taskId);
        if (jsCallback != null) {
            jsCallback.sendPluginResult(jsResult);
        }

        sendMessageToDefaultCallback(jsResult);

        // perform installation if allowed or if we in local development mode
        if (pluginConfig.isAutoInstallIsAllowed()
                && (event.config.getContentConfig().getUpdateTime() == ContentConfig.UpdateTime.NOW)) {
            installUpdate(null);
        }
    }

    public void onEvent(NothingToUpdateEvent event) {
        Log.d("CHCP", "Nothing to update");

        PluginResult jsResult = PluginResultHelper.pluginResultFromEvent(event);

        //notify JS
        CallbackContext jsCallback = pollFetchTaskJsCallback(event.taskId);
        if (jsCallback != null) {
            jsCallback.sendPluginResult(jsResult);
        }

        sendMessageToDefaultCallback(jsResult);
    }

    public void onEvent(UpdateDownloadErrorEvent event) {
        Log.d("CHCP", "Failed to update");

        PluginResult jsResult = PluginResultHelper.pluginResultFromEvent(event);

        // notify JS
        CallbackContext jsCallback = pollFetchTaskJsCallback(event.taskId);
        if (jsCallback != null) {
            jsCallback.sendPluginResult(jsResult);
        }

        sendMessageToDefaultCallback(jsResult);
    }

    // endregion

    // region Update installation events

    public void onEvent(UpdateInstalledEvent event) {
        Log.d("CHCP", "Update is installed");

        final PluginResult jsResult = PluginResultHelper.pluginResultFromEvent(event);

        if (installJsCallback != null) {
            installJsCallback.sendPluginResult(jsResult);
            installJsCallback = null;
        }

        sendMessageToDefaultCallback(jsResult);
        resetApplicationToStartingPage();

//        handler.post(new Runnable() {
//            @Override
//            public void run() {
//                // hide dialog and show WebView
//                dismissProgressDialog();
//            }
//        });
    }

    private void resetApplicationToStartingPage() {
        cordova.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final String startingPage = getStartingPage();
                webView.loadUrlIntoView(startingPage, false);
            }
        });
    }

    public void onEvent(UpdateInstallationErrorEvent event) {
        Log.d("CHCP", "Failed to install");

        PluginResult jsResult = PluginResultHelper.pluginResultFromEvent(event);

        // notify js
        if (installJsCallback != null) {
            installJsCallback.sendPluginResult(jsResult);
            installJsCallback = null;
        }

        sendMessageToDefaultCallback(jsResult);

//        handler.post(new Runnable() {
//            @Override
//            public void run() {
//                // hide dialog and show WebView
//                dismissProgressDialog();
//            }
//        });
    }

    public void onEvent(NothingToInstallEvent event) {
        Log.d("CHCP", "Nothing to install");

        PluginResult jsResult = PluginResultHelper.pluginResultFromEvent(event);

        // notify JS
        if (installJsCallback != null) {
            installJsCallback.sendPluginResult(jsResult);
            installJsCallback = null;
        }

        sendMessageToDefaultCallback(jsResult);
    }

    // endregion
}
