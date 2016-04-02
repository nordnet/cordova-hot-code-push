package com.nordnetab.chcp.main.core;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.nordnetab.chcp.main.config.ApplicationConfig;
import com.nordnetab.chcp.main.config.ChcpXmlConfig;
import com.nordnetab.chcp.main.config.ContentConfig;
import com.nordnetab.chcp.main.config.PluginInternalPreferences;
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
import com.nordnetab.chcp.main.model.ChcpError;
import com.nordnetab.chcp.main.model.PluginFilesStructure;
import com.nordnetab.chcp.main.model.UpdateTime;
import com.nordnetab.chcp.main.network.FileDownloader;
import com.nordnetab.chcp.main.storage.ApplicationConfigStorage;
import com.nordnetab.chcp.main.storage.IObjectFileStorage;
import com.nordnetab.chcp.main.storage.IObjectPreferenceStorage;
import com.nordnetab.chcp.main.storage.PluginInternalPreferencesStorage;
import com.nordnetab.chcp.main.updater.UpdatesInstaller;
import com.nordnetab.chcp.main.updater.UpdatesLoader;
import com.nordnetab.chcp.main.utils.AssetsHelper;
import com.nordnetab.chcp.main.utils.CleanUpHelper;
import com.nordnetab.chcp.main.utils.VersionHelper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

/**
 * Created by orarnon on 29/02/2016.
 */
public class HotCodePushController {
    private static final String WWW_FOLDER = "www";

    private static HotCodePushController sInstance;

    private Context applicationContext;

    private IObjectFileStorage<ApplicationConfig> appConfigStorage;
    private PluginInternalPreferences pluginInternalPrefs;
    private IObjectPreferenceStorage<PluginInternalPreferences> pluginInternalPrefsStorage;
    private ChcpXmlConfig chcpXmlConfig;
    private PluginFilesStructure fileStructure;

    private boolean isReady;

    private final Object EVENT_LISTENERS_LOCK = new Object();
    private List<IHotCodePushEventListener> eventListeners;

    // region singleton design pattern

    public static synchronized HotCodePushController getInstance(Context context) {
        if(sInstance == null) {
            sInstance = new HotCodePushController(context);
        }

        return sInstance;
    }

    private HotCodePushController(Context context) {
        applicationContext = context.getApplicationContext();

        eventListeners = new ArrayList<>();

        parseCordovaConfigXml();
        loadPluginInternalPreferences();

        Log.d("CHCP", "Currently running release version " + pluginInternalPrefs.getCurrentReleaseVersionName());

        appConfigStorage = new ApplicationConfigStorage();
        fileStructure = new PluginFilesStructure(applicationContext, pluginInternalPrefs.getCurrentReleaseVersionName());

        // clean up file system
        if (!TextUtils.isEmpty(pluginInternalPrefs.getCurrentReleaseVersionName())) {
            CleanUpHelper.removeReleaseFolders(applicationContext,
                    new String[]{
                            pluginInternalPrefs.getCurrentReleaseVersionName(),
                            pluginInternalPrefs.getPreviousReleaseVersionName(),
                            pluginInternalPrefs.getReadyForInstallationReleaseVersionName()
                    }
            );
        }
    }

    // endregion

    // region public API

    public void registerListener(IHotCodePushEventListener listener) {
        synchronized (EVENT_LISTENERS_LOCK) {
            eventListeners.add(listener);

            final EventBus eventBus = EventBus.getDefault();
            if (!eventBus.isRegistered(this)) {
                eventBus.register(this);
            }

            // ensure that www folder installed on external storage;
            // if not - install it
            isReady = isReady();
            if (!isReady) {
                installWwwFolder();
            }
        }
    }

    public void unregisterListener(IHotCodePushEventListener listener) {
        synchronized (EVENT_LISTENERS_LOCK) {
            eventListeners.remove(listener);

            if(eventListeners.size() < 1) {
                EventBus.getDefault().unregister(this);
            }
        }
    }

    /**
     * Perform update availability check.
     *
     */
    public void fetchUpdate(boolean isAutoDownload) {
        if (!isReady) {
            EventBus.getDefault().post(new UpdateDownloadErrorEvent(ChcpError.ASSETS_FOLDER_IN_NOT_YET_INSTALLED, null));
            return;
        }

        if(isAutoDownload && !shouldAutoDownload()) {
            EventBus.getDefault().post(new AutoDownloadNotAllowedErrorEvent());
            return;
        }

        ChcpError error =
                UpdatesLoader.downloadUpdate(applicationContext, chcpXmlConfig.getConfigUrl(), pluginInternalPrefs.getCurrentReleaseVersionName());
        if(error != ChcpError.NONE) {
            EventBus.getDefault().post(new UpdateDownloadErrorEvent(error, null));
        }
    }

    /**
     * Install update if any available.
     *
     */
    public void installUpdate(boolean isAutoInstall) {
        if (!isReady) {
            EventBus.getDefault().post(new UpdateInstallationErrorEvent(ChcpError.ASSETS_FOLDER_IN_NOT_YET_INSTALLED, null));
            return;
        }

        if(isAutoInstall && !shouldAutoInstall()) {
            EventBus.getDefault().post(new AutoInstallNotAllowedErrorEvent());
            return;
        }

        ChcpError error =
                UpdatesInstaller.install(applicationContext, pluginInternalPrefs.getReadyForInstallationReleaseVersionName(), pluginInternalPrefs.getCurrentReleaseVersionName());
        if(error != ChcpError.NONE) {
            EventBus.getDefault().post(new UpdateInstallationErrorEvent(error, null));
        }
    }

    public ChcpXmlConfig getCordovaConfigXml() {
        return chcpXmlConfig;
    }

    public PluginInternalPreferences getPluginInternalPreferences() {
        return pluginInternalPrefs;
    }

    public PluginFilesStructure getPluginFilesStructure() {
        return fileStructure;
    }


    /**
     * Check if plugin can perform it's duties.
     *
     * @return <code>true</code> - plugin is ready; otherwise - <code>false</code>
     */
    public boolean isReady() {
        boolean isWwwFolderExists = isWwwFolderExists();
        boolean isWwwFolderInstalled = pluginInternalPrefs.isWwwFolderInstalled();
        boolean isApplicationHasBeenUpdated = chcpXmlConfig.isFreshInstallAfterAppUpdateRequired() && isApplicationHasBeenUpdated();

        return isWwwFolderExists && isWwwFolderInstalled && !isApplicationHasBeenUpdated;
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

        chcpXmlConfig = ChcpXmlConfig.loadFromCordovaConfig(applicationContext);
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

        pluginInternalPrefsStorage = new PluginInternalPreferencesStorage(applicationContext);
        PluginInternalPreferences config = pluginInternalPrefsStorage.loadFromPreference();
        if ((config == null) || TextUtils.isEmpty(config.getCurrentReleaseVersionName())) {
            config = PluginInternalPreferences.createDefault(applicationContext, chcpXmlConfig.isUseOfInitialVersionFromAssetsAllowed());
            pluginInternalPrefsStorage.storeInPreference(config);
        }
        pluginInternalPrefs = config;
    }

    // endregion

    // region Private API

    /**
     * Check if external version of www folder exists.
     *
     * @return <code>true</code> if it is in place; <code>false</code> - otherwise
     */
    private boolean isWwwFolderExists() {
        return new File(fileStructure.getWwwFolder()).exists();
    }

    /**
     * Check if application has been updated through the Google Play since the last launch.
     *
     * @return <code>true</code> if application was update; <code>false</code> - otherwise
     */
    private boolean isApplicationHasBeenUpdated() {
        return pluginInternalPrefs.getAppBuildVersion() != VersionHelper.applicationVersionCode(applicationContext);
    }

    /**
     * Install assets folder onto the external storage
     */
    private void installWwwFolder() {
        isReady = false;

        // reset www folder installed flag
        if (pluginInternalPrefs.isWwwFolderInstalled()) {
            pluginInternalPrefs.setWwwFolderInstalled(false);
            pluginInternalPrefsStorage.storeInPreference(pluginInternalPrefs);
        }

        if(chcpXmlConfig.isUseOfInitialVersionFromAssetsAllowed()) {
            AssetsHelper.copyAssetDirectoryToAppDirectory(applicationContext.getAssets(), WWW_FOLDER, fileStructure.getWwwFolder());
        } else {
            String assetsRemoteZipUrl = chcpXmlConfig.getAssetsRemoteZipUrl();
            if (TextUtils.isEmpty(assetsRemoteZipUrl)) {
                AssetsHelper.createStubAppDirectory(fileStructure.getWwwFolder());
            } else {
                AssetsHelper.copyAssetsFromRemoteZipUrlToDirectory(chcpXmlConfig.getAssetsRemoteZipUrl(), fileStructure.getWwwFolder());
            }
        }
    }

    private boolean shouldAutoDownload() {
        return chcpXmlConfig.isAutoDownloadIsAllowed();
    }

    private boolean shouldAutoInstall() {
        if(!chcpXmlConfig.isAutoInstallIsAllowed()) {
            return false;
        }

        final PluginFilesStructure readyForInstallationReleaseVersionFileStructure =
                new PluginFilesStructure(applicationContext, pluginInternalPrefs.getReadyForInstallationReleaseVersionName());
        final ApplicationConfig appConfig = appConfigStorage.loadFromFolder(readyForInstallationReleaseVersionFileStructure.getDownloadFolder());
        if (appConfig == null) {
            return false;
        }

        final UpdateTime updateTime = appConfig.getContentConfig().getUpdateTime();
        if ((updateTime != UpdateTime.ON_RESUME) && (updateTime != UpdateTime.NOW)) {
            return false;
        }

        return true;
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
    @Subscribe
    public void onEvent(AssetsInstalledEvent event) {
        Log.d("CHCP", "Assets installed");

        // update stored application version
        pluginInternalPrefs.setAppBuildVersion(VersionHelper.applicationVersionCode(applicationContext));
        pluginInternalPrefs.setWwwFolderInstalled(true);
        pluginInternalPrefsStorage.storeInPreference(pluginInternalPrefs);

        isReady = true;

        synchronized (EVENT_LISTENERS_LOCK) {
            for (IHotCodePushEventListener listener : eventListeners) {
                listener.onEvent(event);
            }
        }

        if (chcpXmlConfig.isAutoDownloadIsAllowed() &&
            !UpdatesInstaller.isInstalling() &&
            !UpdatesLoader.isExecuting()) {
            fetchUpdate(false);
        }
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
    @Subscribe
    public void onEvent(AssetsInstallationErrorEvent event) {
        Log.d("CHCP", "Can't install assets on device. Continue to work with default bundle");

        synchronized (EVENT_LISTENERS_LOCK) {
            for (IHotCodePushEventListener listener : eventListeners) {
                listener.onEvent(event);
            }
        }
    }

    // endregion

    // region Update download events

    /**
     * Listener for event that we failed to automatically download an update because it's not allowed by the configuration.
     *
     * @param event event details
     * @see AutoDownloadNotAllowedErrorEvent
     * @see EventBus
     */
    @SuppressWarnings("unused")
    @Subscribe
    public void onEvent(AutoDownloadNotAllowedErrorEvent event) {
        Log.d("CHCP", "Auto download is not allowed");

        synchronized (EVENT_LISTENERS_LOCK) {
            for (IHotCodePushEventListener listener : eventListeners) {
                listener.onEvent(event);
            }
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
    @Subscribe
    public void onEvent(UpdateIsReadyToInstallEvent event) {
        final ContentConfig newContentConfig = event.applicationConfig().getContentConfig();
        Log.d("CHCP", "Update is ready for installation: " + newContentConfig.getReleaseVersion());

        pluginInternalPrefs.setReadyForInstallationReleaseVersionName(newContentConfig.getReleaseVersion());
        pluginInternalPrefsStorage.storeInPreference(pluginInternalPrefs);

        synchronized (EVENT_LISTENERS_LOCK) {
            for (IHotCodePushEventListener listener : eventListeners) {
                listener.onEvent(event);
            }
        }

        // perform installation if allowed
        if (chcpXmlConfig.isAutoInstallIsAllowed() &&
            (newContentConfig.getUpdateTime() == UpdateTime.NOW)) {
            installUpdate(false);
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
    @Subscribe
    public void onEvent(NothingToUpdateEvent event) {
        Log.d("CHCP", "Nothing to update");

        synchronized (EVENT_LISTENERS_LOCK) {
            for (IHotCodePushEventListener listener : eventListeners) {
                listener.onEvent(event);
            }
        }
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
    @Subscribe
    public void onEvent(UpdateDownloadErrorEvent event) {
        Log.d("CHCP", "Failed to update");

        final ChcpError error = event.error();
        if ((error == ChcpError.LOCAL_VERSION_OF_APPLICATION_CONFIG_NOT_FOUND) ||
            (error == ChcpError.LOCAL_VERSION_OF_MANIFEST_NOT_FOUND)) {
            Log.d("CHCP", "Can't load application config from installation folder. Reinstalling external folder");
            installWwwFolder();
        }

        synchronized (EVENT_LISTENERS_LOCK) {
            for (IHotCodePushEventListener listener : eventListeners) {
                listener.onEvent(event);
            }
        }

        rollbackIfCorrupted(event.error());
    }

    // endregion

    // region Update installation events

    /**
     * Listener for event that we failed to automatically install an update because it's not allowed by the configuration.
     *
     * @param event event details
     * @see AutoInstallNotAllowedErrorEvent
     * @see EventBus
     */
    @SuppressWarnings("unused")
    @Subscribe
    public void onEvent(AutoInstallNotAllowedErrorEvent event) {
        Log.d("CHCP", "Auto install is not allowed");

        synchronized (EVENT_LISTENERS_LOCK) {
            for (IHotCodePushEventListener listener : eventListeners) {
                listener.onEvent(event);
            }
        }
    }

    /**
     * Listener for event that we successfully installed new update.
     *
     * @param event event information
     * @see EventBus
     * @see UpdateInstalledEvent
     * @see UpdatesInstaller
     */
    @SuppressWarnings("unused")
    @Subscribe
    public void onEvent(UpdateInstalledEvent event) {
        Log.d("CHCP", "Update is installed");

        final ContentConfig newContentConfig = event.applicationConfig().getContentConfig();

        // update preferences
        pluginInternalPrefs.setPreviousReleaseVersionName(pluginInternalPrefs.getCurrentReleaseVersionName());
        pluginInternalPrefs.setCurrentReleaseVersionName(newContentConfig.getReleaseVersion());
        pluginInternalPrefs.setReadyForInstallationReleaseVersionName("");
        pluginInternalPrefsStorage.storeInPreference(pluginInternalPrefs);

        fileStructure = new PluginFilesStructure(applicationContext, newContentConfig.getReleaseVersion());

        synchronized (EVENT_LISTENERS_LOCK) {
            for (IHotCodePushEventListener listener : eventListeners) {
                listener.onEvent(event);
            }
        }
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
    @Subscribe
    public void onEvent(UpdateInstallationErrorEvent event) {
        Log.d("CHCP", "Failed to install. Error: " + event.error().getErrorDescription());

        synchronized (EVENT_LISTENERS_LOCK) {
            for (IHotCodePushEventListener listener : eventListeners) {
                listener.onEvent(event);
            }
        }

        rollbackIfCorrupted(event.error());
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
    @Subscribe
    public void onEvent(NothingToInstallEvent event) {
        Log.d("CHCP", "Nothing to install");

        synchronized (EVENT_LISTENERS_LOCK) {
            for (IHotCodePushEventListener listener : eventListeners) {
                listener.onEvent(event);
            }
        }
    }

    /**
     * Listener for event that a rollback has been performed.
     *
     * @param event event information
     * @see RollbackPerformedEvent
     * @see EventBus
     */
    @SuppressWarnings("unused")
    @Subscribe
    public void onEvent(RollbackPerformedEvent event) {
        Log.d("CHCP", "Rollback performed");

        synchronized (EVENT_LISTENERS_LOCK) {
            for (IHotCodePushEventListener listener : eventListeners) {
                listener.onEvent(event);
            }
        }
    }

    // endregion

    // region Rollback process

    /**
     * Rollback to the previous/bundle version, if this is needed.
     *
     * @param error error, based on which we will decide
     */
    private void rollbackIfCorrupted(ChcpError error) {
        if ((error != ChcpError.LOCAL_VERSION_OF_APPLICATION_CONFIG_NOT_FOUND) &&
            (error != ChcpError.LOCAL_VERSION_OF_MANIFEST_NOT_FOUND)) {
            return;
        }

        if (pluginInternalPrefs.getPreviousReleaseVersionName().length() > 0) {
            Log.d("CHCP", "Current release is corrupted, trying to rollback to the previous one");
            rollbackToPreviousRelease();
        } else {
            Log.d("CHCP", "Current release is corrupted, reinstalling www folder from assets");
            installWwwFolder();
        }
    }

    /**
     * Rollback to the previously installed version of the web content.
     */
    private void rollbackToPreviousRelease() {
        pluginInternalPrefs.setCurrentReleaseVersionName(pluginInternalPrefs.getPreviousReleaseVersionName());
        pluginInternalPrefs.setPreviousReleaseVersionName("");
        pluginInternalPrefs.setReadyForInstallationReleaseVersionName("");
        pluginInternalPrefsStorage.storeInPreference(pluginInternalPrefs);

        fileStructure.switchToRelease(pluginInternalPrefs.getCurrentReleaseVersionName());

        EventBus.getDefault().post(new RollbackPerformedEvent());
    }

    // endregion
}
