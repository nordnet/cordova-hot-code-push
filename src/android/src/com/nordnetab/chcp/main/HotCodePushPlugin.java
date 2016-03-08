package com.nordnetab.chcp.main;

import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

import com.nordnetab.chcp.main.core.HotCodePushController;
import com.nordnetab.chcp.main.core.IHotCodePushEventListener;
import com.nordnetab.chcp.main.events.AssetsInstallationErrorEvent;
import com.nordnetab.chcp.main.events.AssetsInstalledEvent;
import com.nordnetab.chcp.main.events.AutoDownloadNotAllowedErrorEvent;
import com.nordnetab.chcp.main.events.AutoInstallNotAllowedErrorEvent;
import com.nordnetab.chcp.main.events.NothingToInstallEvent;
import com.nordnetab.chcp.main.events.NothingToUpdateEvent;
import com.nordnetab.chcp.main.events.RollbackPerformedEvent;
import com.nordnetab.chcp.main.events.UpdateDownloadErrorEvent;
import com.nordnetab.chcp.main.events.UpdateInstallationErrorEvent;
import com.nordnetab.chcp.main.events.UpdateInstalledEvent;
import com.nordnetab.chcp.main.events.UpdateIsReadyToInstallEvent;
import com.nordnetab.chcp.main.js.JSAction;
import com.nordnetab.chcp.main.js.PluginResultHelper;
import com.nordnetab.chcp.main.model.ChcpError;
import com.nordnetab.chcp.main.model.PluginFilesStructure;
import com.nordnetab.chcp.main.storage.ApplicationConfigStorage;
import com.nordnetab.chcp.main.utils.Paths;
import com.nordnetab.chcp.main.view.AppUpdateRequestDialog;

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

/**
 * Created by Nikolay Demyankov on 23.07.15.
 * <p/>
 * Plugin main class.
 */
public class HotCodePushPlugin extends CordovaPlugin implements IHotCodePushEventListener {

    private static final String FILE_PREFIX = "file://";
    private static final String LOCAL_ASSETS_FOLDER = "file:///android_asset/www";

    private ApplicationConfigStorage appConfigStorage;
    private Handler handler;

    private CallbackContext installJsCallback;
    private CallbackContext jsDefaultCallback;
    private CallbackContext downloadJsCallback;

    private String startingPage;
    private boolean dontReloadOnStart;

    // region Plugin lifecycle

    @Override
    public void initialize(final CordovaInterface cordova, final CordovaWebView webView) {
        super.initialize(cordova, webView);

        appConfigStorage = new ApplicationConfigStorage();

        handler = new Handler();
    }

    @Override
    public void onStart() {
        super.onStart();

        HotCodePushController.getInstance(cordova.getActivity()).registerListener(this);

        if (!HotCodePushController.getInstance(cordova.getActivity()).isReady()) {
            dontReloadOnStart = true;
            return;
        }

        // reload only if we on local storage
        if (!dontReloadOnStart) {
            dontReloadOnStart = true;

            if(HotCodePushController.getInstance(cordova.getActivity()).getCordovaConfigXml().isAutoRedirectionToLocalStorageIndexPageAllowed()) {
                redirectToLocalStorageIndexPage();
            }
        }

        installUpdate(null);
    }

    @Override
    public void onResume(boolean multitasking) {
        super.onResume(multitasking);

        if (!HotCodePushController.getInstance(cordova.getActivity()).isReady()) {
            return;
        }

        installUpdate(null);
    }

    @Override
    public void onStop() {
        HotCodePushController.getInstance(cordova.getActivity()).unregisterListener(this);

        super.onStop();
    }

    // endregion

    // region JavaScript processing

    @Override
    public boolean execute(String action, CordovaArgs args, CallbackContext callbackContext) throws JSONException {
        boolean cmdProcessed = true;
        if (JSAction.INIT.equals(action)) {
            jsInit(callbackContext);
        } else if (JSAction.FETCH_UPDATE.equals(action)) {
            jsFetchUpdate(callbackContext);
        } else if (JSAction.INSTALL_UPDATE.equals(action)) {
            jsInstallUpdate(callbackContext);
        } else if (JSAction.CONFIGURE.equals(action)) {
            jsSetPluginOptions(args, callbackContext);
        } else if (JSAction.REQUEST_APP_UPDATE.equals(action)) {
            jsRequestAppUpdate(args, callbackContext);
        } else {
            cmdProcessed = false;
        }

        return cmdProcessed;
    }

    /**
     * Send message to default plugin callback.
     * Default callback - is a callback that we receive on initialization (device ready).
     * Through it we are broadcasting different events.
     *
     * @param message message to send to web side
     */
    private void sendMessageToDefaultCallback(PluginResult message) {
        if (jsDefaultCallback == null) {
            return;
        }

        message.setKeepCallback(true);
        jsDefaultCallback.sendPluginResult(message);
    }

    /**
     * Initialize default callback, received from the web side.
     *
     * @param callback callback to use for events broadcasting
     */
    private void jsInit(CallbackContext callback) {
        jsDefaultCallback = callback;

        // Clear web history.
        // In some cases this is necessary, because on the launch we redirect user to the
        // external storage. And if he presses back button - browser will lead him back to
        // assets folder, which we don't want.
        handler.post(new Runnable() {
            @Override
            public void run() {
                webView.clearHistory();

            }
        });

        // fetch update when we are initialized
        fetchUpdate(null);
    }

    /**
     * Check for update.
     * Method is called from JS side.
     *
     * @param callback js callback
     */
    private void jsFetchUpdate(CallbackContext callback) {
        if (!HotCodePushController.getInstance(cordova.getActivity()).isReady()) {
            sendPluginNotReadyToWork(UpdateDownloadErrorEvent.EVENT_NAME, callback);
            return;
        }

        fetchUpdate(callback);
    }

    /**
     * Install the update.
     * Method is called from JS side.
     *
     * @param callback js callback
     */
    private void jsInstallUpdate(CallbackContext callback) {
        if (!HotCodePushController.getInstance(cordova.getActivity()).isReady()) {
            sendPluginNotReadyToWork(UpdateInstallationErrorEvent.EVENT_NAME, callback);
            return;
        }

        installUpdate(callback);
    }

    /**
     * Send to JS side event with message, that plugin is installing assets on the external storage and not yet ready for work.
     * That happens only on the first launch.
     *
     * @param eventName event name, that is send to JS side
     * @param callback  JS callback
     */
    private void sendPluginNotReadyToWork(String eventName, CallbackContext callback) {
        PluginResult pluginResult = PluginResultHelper.createPluginResult(eventName, null, ChcpError.ASSETS_FOLDER_IN_NOT_YET_INSTALLED);
        callback.sendPluginResult(pluginResult);
    }

    /**
     * Set plugin options. Method is called from JavaScript.
     *
     * @param arguments arguments from JavaScript
     * @param callback  callback where to send result
     */
    private void jsSetPluginOptions(CordovaArgs arguments, CallbackContext callback) {
        if (!HotCodePushController.getInstance(cordova.getActivity()).isReady()) {
            sendPluginNotReadyToWork("", callback);
            return;
        }

        try {
            JSONObject jsonObject = (JSONObject) arguments.get(0);
            HotCodePushController.getInstance(cordova.getActivity()).getCordovaConfigXml().mergeOptionsFromJs(jsonObject);
            // TODO: store them somewhere?
        } catch (JSONException e) {
            Log.d("CHCP", "Failed to process plugin options, received from JS.", e);
        }

        callback.success();
    }

    /**
     * Show dialog with request to update the application through the Google Play.
     *
     * @param arguments arguments from JavaScript
     * @param callback  callback where to send result
     */
    private void jsRequestAppUpdate(final CordovaArgs arguments, final CallbackContext callback) {
        if (!HotCodePushController.getInstance(cordova.getActivity()).isReady()) {
            sendPluginNotReadyToWork("", callback);
            return;
        }

        String msg = null;
        try {
            msg = (String) arguments.get(0);
        } catch (JSONException e) {
            Log.d("CHCP", "Dialog message is not set", e);
        }

        if (TextUtils.isEmpty(msg)) {
            return;
        }

        PluginFilesStructure fileStructure = HotCodePushController.getInstance(cordova.getActivity()).getPluginFilesStructure();
        final String storeURL = appConfigStorage.loadFromFolder(fileStructure.getWwwFolder()).getStoreUrl();

        new AppUpdateRequestDialog(cordova.getActivity(), msg, storeURL, callback).show();
    }

    /**
     * Perform update availability check.
     *
     * @param jsCallback callback where to send the result;
     *                   used, when update is requested manually from JavaScript
     */
    private void fetchUpdate(CallbackContext jsCallback) {
        if (!HotCodePushController.getInstance(cordova.getActivity()).isReady()) {
            return;
        }

        if (jsCallback != null) {
            downloadJsCallback = jsCallback;
        }

        boolean isAutoDownload = (jsCallback == null);
        HotCodePushController.getInstance(cordova.getActivity()).fetchUpdate(isAutoDownload);
    }

    /**
     * Install update if any available.
     *
     * @param jsCallback callback where to send the result;
     *                   used, when installation os requested manually from JavaScript
     */
    private void installUpdate(CallbackContext jsCallback) {
        if (!HotCodePushController.getInstance(cordova.getActivity()).isReady()) {
            return;
        }

        if (jsCallback != null) {
            installJsCallback = jsCallback;
        }

        boolean isAutoInstall = (jsCallback == null);
        HotCodePushController.getInstance(cordova.getActivity()).installUpdate(isAutoInstall);
    }

    // endregion

    // region Private API

    /**
     * Redirect user onto the page, that resides on the external storage instead of the assets folder.
     */
    private void redirectToLocalStorageIndexPage() {
        final String indexPage = getStartingPage();

        // remove query and fragment parameters from the index page path
        // TODO: cleanup this fragment
        String strippedIndexPage = indexPage;
        if (strippedIndexPage.contains("#") || strippedIndexPage.contains("?")) {
            int idx = strippedIndexPage.lastIndexOf("?");
            if (idx >= 0) {
                strippedIndexPage = strippedIndexPage.substring(0, idx);
            } else {
                idx = strippedIndexPage.lastIndexOf("#");
                strippedIndexPage = strippedIndexPage.substring(0, idx);
            }
        }

        String wwwFolder = HotCodePushController.getInstance(cordova.getActivity()).getPluginFilesStructure().getWwwFolder();

        // make sure, that index page exists
        String external = Paths.get(wwwFolder, strippedIndexPage);
        if (!new File(external).exists()) {
            Log.d("CHCP", "External starting page not found. Aborting page change.");
            return;
        }

        // load index page from the external source
        external = Paths.get(wwwFolder, indexPage);
        webView.loadUrlIntoView(FILE_PREFIX + external, false);

        Log.d("CHCP", "Loading external page: " + external);
    }

    /**
     * Getter for the startup page.
     *
     * @return startup page relative path
     */
    private String getStartingPage() {
        if (!TextUtils.isEmpty(startingPage)) {
            return startingPage;
        }

        ConfigXmlParser parser = new ConfigXmlParser();
        parser.parse(cordova.getActivity());
        String url = parser.getLaunchUrl();

        startingPage = url.replace(LOCAL_ASSETS_FOLDER, "");

        return startingPage;
    }

    private void redirectToLocalStorageIndexPageInBackground() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                redirectToLocalStorageIndexPage();
            }
        });
    }

    // endregion

    // region Assets installation events

    public void onEvent(AssetsInstalledEvent event) {
        PluginResult result = PluginResultHelper.pluginResultFromEvent(event);
        sendMessageToDefaultCallback(result);
    }

    public void onEvent(AssetsInstallationErrorEvent event) {
        PluginResult result = PluginResultHelper.pluginResultFromEvent(event);
        sendMessageToDefaultCallback(result);
    }

    // endregion

    // region Update download events

    public void onEvent(AutoDownloadNotAllowedErrorEvent event) {
    }

    public void onEvent(UpdateIsReadyToInstallEvent event) {
        PluginResult jsResult = PluginResultHelper.pluginResultFromEvent(event);

        // notify JS
        if (downloadJsCallback != null) {
            downloadJsCallback.sendPluginResult(jsResult);
            downloadJsCallback = null;
        }

        sendMessageToDefaultCallback(jsResult);
    }

    public void onEvent(NothingToUpdateEvent event) {
        PluginResult jsResult = PluginResultHelper.pluginResultFromEvent(event);

        //notify JS
        if (downloadJsCallback != null) {
            downloadJsCallback.sendPluginResult(jsResult);
            downloadJsCallback = null;
        }

        sendMessageToDefaultCallback(jsResult);
    }

    public void onEvent(UpdateDownloadErrorEvent event) {
        PluginResult jsResult = PluginResultHelper.pluginResultFromEvent(event);

        // notify JS
        if (downloadJsCallback != null) {
            downloadJsCallback.sendPluginResult(jsResult);
            downloadJsCallback = null;
        }

        sendMessageToDefaultCallback(jsResult);
    }

    // endregion

    // region Update installation events

    public void onEvent(AutoInstallNotAllowedErrorEvent event) {
    }

    public void onEvent(UpdateInstalledEvent event) {
        final PluginResult jsResult = PluginResultHelper.pluginResultFromEvent(event);

        if (installJsCallback != null) {
            installJsCallback.sendPluginResult(jsResult);
            installJsCallback = null;
        }

        sendMessageToDefaultCallback(jsResult);

        if(HotCodePushController.getInstance(cordova.getActivity()).getCordovaConfigXml().isAutoRedirectionToLocalStorageIndexPageAllowed()) {
            redirectToLocalStorageIndexPageInBackground();
        }
    }

    public void onEvent(UpdateInstallationErrorEvent event) {
        PluginResult jsResult = PluginResultHelper.pluginResultFromEvent(event);

        // notify js
        if (installJsCallback != null) {
            installJsCallback.sendPluginResult(jsResult);
            installJsCallback = null;
        }

        sendMessageToDefaultCallback(jsResult);
    }

    public void onEvent(NothingToInstallEvent event) {
        PluginResult jsResult = PluginResultHelper.pluginResultFromEvent(event);

        // notify JS
        if (installJsCallback != null) {
            installJsCallback.sendPluginResult(jsResult);
            installJsCallback = null;
        }

        sendMessageToDefaultCallback(jsResult);
    }

    // endregion

    // region Rollback events

    public void onEvent(RollbackPerformedEvent event) {
        if(HotCodePushController.getInstance(cordova.getActivity()).getCordovaConfigXml().isAutoRedirectionToLocalStorageIndexPageAllowed()) {
            redirectToLocalStorageIndexPageInBackground();
        }
    }

    // endregion
}
