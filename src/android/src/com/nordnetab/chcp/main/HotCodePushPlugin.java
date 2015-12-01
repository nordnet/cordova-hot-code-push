package com.nordnetab.chcp.main;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

import com.nordnetab.chcp.main.config.ApplicationConfig;
import com.nordnetab.chcp.main.config.ChcpXmlConfig;
import com.nordnetab.chcp.main.config.PluginInternalPreferences;
import com.nordnetab.chcp.main.events.AssetsInstallationErrorEvent;
import com.nordnetab.chcp.main.events.AssetsInstalledEvent;
import com.nordnetab.chcp.main.events.NothingToInstallEvent;
import com.nordnetab.chcp.main.events.NothingToUpdateEvent;
import com.nordnetab.chcp.main.events.UpdateDownloadErrorEvent;
import com.nordnetab.chcp.main.events.UpdateInstallationErrorEvent;
import com.nordnetab.chcp.main.events.UpdateInstalledEvent;
import com.nordnetab.chcp.main.events.UpdateIsReadyToInstallEvent;
import com.nordnetab.chcp.main.js.JSAction;
import com.nordnetab.chcp.main.js.PluginResultHelper;
import com.nordnetab.chcp.main.model.IPluginFilesStructure;
import com.nordnetab.chcp.main.model.PluginFilesStructureImpl;
import com.nordnetab.chcp.main.model.UpdateTime;
import com.nordnetab.chcp.main.storage.ApplicationConfigStorage;
import com.nordnetab.chcp.main.storage.IObjectFileStorage;
import com.nordnetab.chcp.main.storage.IObjectPreferenceStorage;
import com.nordnetab.chcp.main.storage.PluginInternalPreferencesStorage;
import com.nordnetab.chcp.main.updater.UpdatesInstaller;
import com.nordnetab.chcp.main.updater.UpdatesLoader;
import com.nordnetab.chcp.main.utils.AssetsHelper;
import com.nordnetab.chcp.main.utils.Paths;
import com.nordnetab.chcp.main.utils.VersionHelper;

import com.nordnetab.chcp.main.model.ChcpError;

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
import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by Nikolay Demyankov on 23.07.15.
 * <p/>
 * Plugin main class.
 */
public class HotCodePushPlugin extends CordovaPlugin {

    private static final String FILE_PREFIX = "file://";
    private static final String WWW_FOLDER = "www";
    private static final String LOCAL_ASSETS_FOLDER = "file:///android_asset/www";

    private String startingPage;
    private IObjectFileStorage<ApplicationConfig> appConfigStorage;
    private PluginInternalPreferences pluginInternalPrefs;
    private IObjectPreferenceStorage<PluginInternalPreferences> pluginInternalPrefsStorage;
    private ChcpXmlConfig chcpXmlConfig;
    private IPluginFilesStructure fileStructure;

    private List<DownloadTaskJsCallback> fetchTasks;
    private CallbackContext installJsCallback;
    private CallbackContext jsDefaultCallback;

    private Handler handler;
    private boolean isPluginReadyForWork;

    /**
     * Helper class to store JavaScript callbacks
     */
    private static class DownloadTaskJsCallback {
        /**
         * task identifier
         */
        public final String taskId;
        /**
         * javascript callback
         */
        public final CallbackContext callback;

        /**
         * Class constructor
         *
         * @param taskId   task identifier
         * @param callback javascript callback
         */
        public DownloadTaskJsCallback(String taskId, CallbackContext callback) {
            this.taskId = taskId;
            this.callback = callback;
        }
    }

    // region Plugin lifecycle

    @Override
    public void initialize(final CordovaInterface cordova, final CordovaWebView webView) {
        super.initialize(cordova, webView);

        fetchTasks = new ArrayList<DownloadTaskJsCallback>();
        handler = new Handler();

        fileStructure = new PluginFilesStructureImpl(cordova.getActivity());

        parseCordovaConfigXml();
        loadPluginInternalPreferences();

        appConfigStorage = new ApplicationConfigStorage(fileStructure);
    }

    @Override
    public void onStart() {
        super.onStart();

        EventBus.getDefault().register(this);

        // ensure that www folder installed on external storage;
        // if not - install it
        // TODO: need to check if we can restore www folder from the backup.
        isPluginReadyForWork = isPluginReadyForWork();
        if (!isPluginReadyForWork) {
            installWwwFolder();
            return;
        }

        redirectToLocalStorage();

        // install update if there is anything to install
        if (chcpXmlConfig.isAutoInstallIsAllowed()) {
            installUpdate(null);
        }
    }

    @Override
    public void onResume(boolean multitasking) {
        super.onResume(multitasking);

        if (!isPluginReadyForWork) {
            return;
        }

        if (chcpXmlConfig.isAutoInstallIsAllowed()) {
            ApplicationConfig appConfig = appConfigStorage.loadFromFolder(fileStructure.installationFolder());
            if (appConfig != null && appConfig.getContentConfig().getUpdateTime() == UpdateTime.ON_RESUME) {
                installUpdate(null);
            }
        }
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);

        super.onStop();
    }

    // endregion

    // region Config loaders and initialization

    /**
     * Read hot-code-push plugin preferences from cordova config.xml
     *
     * @see ChcpXmlConfig
     */
    private void parseCordovaConfigXml() {
        if (chcpXmlConfig != null) {
            return;
        }

        chcpXmlConfig = ChcpXmlConfig.loadFromCordovaConfig(cordova.getActivity());
    }

    /**
     * Load plugin internal preferences.
     *
     * @see PluginInternalPreferences
     * @see PluginInternalPreferencesStorage
     */
    private void loadPluginInternalPreferences() {
        if (pluginInternalPrefs != null) {
            return;
        }

        pluginInternalPrefsStorage = new PluginInternalPreferencesStorage(cordova.getActivity());
        PluginInternalPreferences config = pluginInternalPrefsStorage.loadFromPreference();
        if (config == null) {
            config = PluginInternalPreferences.createDefault(cordova.getActivity());
            pluginInternalPrefsStorage.storeInPreference(config);
        }
        pluginInternalPrefs = config;
    }

    // endregion

    // region JavaScript processing

    @Override
    public boolean execute(String action, CordovaArgs args, CallbackContext callbackContext) throws JSONException {
        // initialize even if we are not ready to do all other work
        if (JSAction.INIT.equals(action)) {
            initJs(callbackContext);
            return true;
        }

        // if www folder is not yet created on external storage - ignore requests from JavaScript
        if (!isPluginReadyForWork) {
            return false;
        }

        boolean cmdProcessed = true;
        if (JSAction.FETCH_UPDATE.equals(action)) {
            fetchUpdate(callbackContext);
        } else if (JSAction.INSTALL_UPDATE.equals(action)) {
            installUpdate(callbackContext);
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
    private void initJs(CallbackContext callback) {
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
        if (chcpXmlConfig.isAutoDownloadIsAllowed()) {
            fetchUpdate(null);
        }
    }

    /**
     * Set plugin options. Method is called from JavaScript.
     *
     * @param arguments arguments from JavaScript
     * @param callback  callback where to send result
     */
    private void jsSetPluginOptions(CordovaArgs arguments, CallbackContext callback) {
        try {
            JSONObject jsonObject = (JSONObject) arguments.get(0);
            chcpXmlConfig.mergeOptionsFromJs(jsonObject);
            // TODO: store them somewhere?
        } catch (JSONException e) {
            e.printStackTrace();
        }

        callback.success();
    }

    /**
     * Show dialog with request to update the application through the Google Play.
     *
     * @param arguments arguments from JavaScript
     * @param callback  callback where to send result
     */
    private void jsRequestAppUpdate(CordovaArgs arguments, final CallbackContext callback) {
        String msg = null;
        try {
            msg = (String) arguments.get(0);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (TextUtils.isEmpty(msg)) {
            return;
        }

        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(cordova.getActivity());
        dialogBuilder.setCancelable(false);
        dialogBuilder.setMessage(msg);
        dialogBuilder.setPositiveButton(cordova.getActivity().getString(android.R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                callback.success();
                dialog.dismiss();

                String storeURL = appConfigStorage.loadFromFolder(fileStructure.wwwFolder()).getStoreUrl();
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(storeURL));
                cordova.getActivity().startActivity(intent);
            }
        });
        dialogBuilder.setNegativeButton(cordova.getActivity().getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                callback.error("");
            }
        });

        dialogBuilder.show();
    }

    /**
     * Perform update availability check.
     * Basically, queue update task.
     *
     * @param jsCallback callback where to send the result;
     *                   used, when update is requested manually from JavaScript
     */
    private void fetchUpdate(CallbackContext jsCallback) {
        if (!isPluginReadyForWork) {
            return;
        }

        String taskId = UpdatesLoader.addUpdateTaskToQueue(cordova.getActivity(), chcpXmlConfig.getConfigUrl(), fileStructure);
        if (taskId == null) {
            return;
        }

        if (jsCallback != null) {
            putFetchTaskJsCallback(taskId, jsCallback);
        }
    }

    /**
     * Install update if any available.
     *
     * @param jsCallback callback where to send the result;
     *                   used, when installation os requested manually from JavaScript
     */
    private void installUpdate(CallbackContext jsCallback) {
        if (UpdatesInstaller.isInstalling()) {
            return;
        }

        if (jsCallback != null) {
            installJsCallback = jsCallback;
        }

        if (UpdatesInstaller.install(fileStructure)) {
            // TODO: Temporary fix. Need some better way to restore after installation failure.
            // ensure that we set the www folder as invalid, temporarily,
            // so that if the app crashes or is killed we don't run from a corrupted www folder
//            pluginInternalPrefs.setWwwFolderInstalled(false);
//            pluginInternalPrefsStorage.storeInPreference(pluginInternalPrefs);
        }
    }

    // endregion

    // region Private API

    /**
     * Check if plugin can perform it's duties.
     * Basically, we will check 2 main things:
     * 1. if www folder installed
     * 2. if application has been updated through the Goolge Play from the last launch.
     *
     * @return <code>true</code> - plugin is ready; otherwise - <code>false</code>
     */
    private boolean isPluginReadyForWork() {
        boolean isWwwFolderExists = isWwwFolderExists();
        boolean isWwwFolderInstalled = pluginInternalPrefs.isWwwFolderInstalled();
        boolean isApplicationHasBeenUpdated = isApplicationHasBeenUpdated();

        return isWwwFolderExists && isWwwFolderInstalled && !isApplicationHasBeenUpdated;
    }

    /**
     * Check if external version of www folder exists.
     *
     * @return <code>true</code> if it is in place; <code>false</code> - otherwise
     */
    private boolean isWwwFolderExists() {
        return new File(fileStructure.wwwFolder()).exists();
    }

    /**
     * Check if application has been updated through the Google Play since the last launch.
     *
     * @return <code>true</code> if application was update; <code>false</code> - otherwise
     */
    private boolean isApplicationHasBeenUpdated() {
        return pluginInternalPrefs.getAppBuildVersion() < VersionHelper.applicationVersionCode(cordova.getActivity());
    }

    /**
     * Install assets folder onto the external storage
     */
    private void installWwwFolder() {
        // reset www folder installed flag
        if (pluginInternalPrefs.isWwwFolderInstalled()) {
            pluginInternalPrefs.setWwwFolderInstalled(false);
            pluginInternalPrefsStorage.storeInPreference(pluginInternalPrefs);
        }

        AssetsHelper.copyAssetDirectoryToAppDirectory(cordova.getActivity().getAssets(), WWW_FOLDER, fileStructure.wwwFolder());
    }

    /**
     * Redirect user onto the page, that resides on the external storage instead of the assets folder.
     */
    private void redirectToLocalStorage() {
        String currentUrl = webView.getUrl();
        if (TextUtils.isEmpty(currentUrl)) {
            currentUrl = getStartingPage();
        } else if (!currentUrl.contains(LOCAL_ASSETS_FOLDER)) {
            return;
        }

        currentUrl = currentUrl.replace(LOCAL_ASSETS_FOLDER, "");
        String external = Paths.get(fileStructure.wwwFolder(), currentUrl);
        if (!new File(external).exists()) {
            Log.d("CHCP", "External starting page not found. Aborting page change.");
            return;
        }

        webView.loadUrlIntoView(FILE_PREFIX + external, false);
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

    // endregion

    // region Assets installation events

    /**
     * Listener for event that assets folder are now installed on the external storage.
     * From that moment all content will be displayed from it.
     *
     * @param event event details
     * @see AssetsInstalledEvent
     * @see AssetsHelper
     * @see EventBus
     */
    @SuppressWarnings("unused")
    public void onEvent(AssetsInstalledEvent event) {
        // update stored application version
        pluginInternalPrefs.setAppBuildVersion(VersionHelper.applicationVersionCode(cordova.getActivity()));
        pluginInternalPrefs.setWwwFolderInstalled(true);
        pluginInternalPrefsStorage.storeInPreference(pluginInternalPrefs);

        isPluginReadyForWork = true;

        PluginResult result = PluginResultHelper.pluginResultFromEvent(event);
        sendMessageToDefaultCallback(result);

        fetchUpdate(null);
    }

    /**
     * Listener for event that we failed to install assets folder on the external storage.
     * If so - nothing we can do, plugin is not gonna work.
     *
     * @param event event details
     * @see AssetsInstallationErrorEvent
     * @see AssetsHelper
     * @see EventBus
     */
    @SuppressWarnings("unused")
    public void onEvent(AssetsInstallationErrorEvent event) {
        Log.d("CHCP", "Can't install assets on device. Continue to work with default bundle");

        PluginResult result = PluginResultHelper.pluginResultFromEvent(event);
        sendMessageToDefaultCallback(result);
    }

    // endregion

    // region Update download events

    /**
     * Get JavaScript callback that is associated with the given task identifier.
     *
     * @param taskId task whose callback we need
     * @return JavaScript callback where we should send the result
     * <p/>
     * TODO: need cleaner approach
     */
    private CallbackContext pollFetchTaskJsCallback(String taskId) {
        CallbackContext callback = null;
        int foundIndex = -1;
        for (int i = 0, len = fetchTasks.size(); i < len; i++) {
            DownloadTaskJsCallback jsTask = fetchTasks.get(i);
            if (jsTask.taskId.equals(taskId)) {
                callback = jsTask.callback;
                foundIndex = i;
                break;
            }
        }

        if (foundIndex >= 0) {
            fetchTasks.remove(foundIndex);
        }

        return callback;
    }

    /**
     * Store JavaScript callback until download has finished his job.
     *
     * @param taskId          download task identifier
     * @param callbackContext JavaScript callback where we should send result in the future
     */
    private void putFetchTaskJsCallback(String taskId, CallbackContext callbackContext) {
        // for now we store only 2 tasks
        DownloadTaskJsCallback taskJsCallback = new DownloadTaskJsCallback(taskId, callbackContext);
        if (fetchTasks.size() < 2) {
            fetchTasks.add(taskJsCallback);
        } else {
            fetchTasks.set(1, taskJsCallback);
        }
    }

    /**
     * Listener for the event that update is loaded and ready for the installation.
     *
     * @param event event information
     * @see EventBus
     * @see UpdateIsReadyToInstallEvent
     * @see UpdatesLoader
     */
    @SuppressWarnings("unused")
    public void onEvent(UpdateIsReadyToInstallEvent event) {
        Log.d("CHCP", "Update is ready for installation");

        PluginResult jsResult = PluginResultHelper.pluginResultFromEvent(event);

        // notify JS
        CallbackContext jsCallback = pollFetchTaskJsCallback(event.taskId);
        if (jsCallback != null) {
            jsCallback.sendPluginResult(jsResult);
        }

        sendMessageToDefaultCallback(jsResult);

        // perform installation if allowed
        if (chcpXmlConfig.isAutoInstallIsAllowed()
                && (event.applicationConfig().getContentConfig().getUpdateTime() == UpdateTime.NOW)) {
            installUpdate(null);
        }
    }

    /**
     * Listener for event that there is no update available at the moment.
     * We are as fresh as possible.
     *
     * @param event event information
     * @see EventBus
     * @see NothingToUpdateEvent
     * @see UpdatesLoader
     */
    @SuppressWarnings("unused")
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

    /**
     * Listener for event that some error has happened during the update download process.
     *
     * @param event event information
     * @see EventBus
     * @see UpdateDownloadErrorEvent
     * @see UpdatesLoader
     */
    @SuppressWarnings("unused")
    public void onEvent(UpdateDownloadErrorEvent event) {
        Log.d("CHCP", "Failed to update");

        // TODO: Temporary fix. Need some better way to restore after download failure.
        final ChcpError error = event.error();
        if (error == ChcpError.LOCAL_VERSION_OF_APPLICATION_CONFIG_NOT_FOUND || error == ChcpError.LOCAL_VERSION_OF_MANIFEST_NOT_FOUND) {
            Log.d("CHCP", "Can't load application config from installation folder. Reinstalling external folder");
            installWwwFolder();
        }

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

    /**
     * Listener for event that we successfully installed new update.
     *
     * @param event event information
     * @see EventBus
     * @see UpdateInstalledEvent
     * @see UpdatesInstaller
     */
    @SuppressWarnings("unused")
    public void onEvent(UpdateInstalledEvent event) {
        Log.d("CHCP", "Update is installed");

        // reconfirm that our www folder is now valid
        // TODO: Temporary fix. Need some better way to restore after installation failure.
//        pluginInternalPrefs.setWwwFolderInstalled(true);
//        pluginInternalPrefsStorage.storeInPreference(pluginInternalPrefs);

        final PluginResult jsResult = PluginResultHelper.pluginResultFromEvent(event);

        if (installJsCallback != null) {
            installJsCallback.sendPluginResult(jsResult);
            installJsCallback = null;
        }

        sendMessageToDefaultCallback(jsResult);
        resetApplicationToStartingPage();
    }

    /**
     * Reset web content to starting page.
     * Called after the update.
     */
    private void resetApplicationToStartingPage() {
        cordova.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                webView.clearHistory();
                webView.clearCache();
                final String external = Paths.get(fileStructure.wwwFolder(), getStartingPage());
                if (!new File(external).exists()) {
                    Log.d("CHCP", "External starting page not found. Aborting page change.");
                    return;
                }
                final String externalStartingPage = FILE_PREFIX + external + "?" + System.currentTimeMillis();
                webView.loadUrlIntoView(externalStartingPage, false);
            }
        });
    }

    /**
     * Listener for event that some error happened during the update installation.
     *
     * @param event event information
     * @see UpdateInstallationErrorEvent
     * @see EventBus
     * @see UpdatesInstaller
     */
    @SuppressWarnings("unused")
    public void onEvent(UpdateInstallationErrorEvent event) {
        Log.d("CHCP", "Failed to install");

        PluginResult jsResult = PluginResultHelper.pluginResultFromEvent(event);

        // notify js
        if (installJsCallback != null) {
            installJsCallback.sendPluginResult(jsResult);
            installJsCallback = null;
        }

        sendMessageToDefaultCallback(jsResult);
    }

    /**
     * Listener for event that there is nothing to install.
     *
     * @param event event information
     * @see NothingToInstallEvent
     * @see UpdatesInstaller
     * @see EventBus
     */
    @SuppressWarnings("unused")
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
