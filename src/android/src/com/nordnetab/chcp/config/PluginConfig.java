package com.nordnetab.chcp.config;

import android.content.Context;
import android.text.TextUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nordnetab.chcp.utils.VersionHelper;

import org.apache.cordova.CordovaPreferences;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Created by Nikolay Demyankov on 28.07.15.
 * <p/>
 * Model for plugin preferences, that can be changed during runtime.
 * Using this you can disable/enable updates download and installation,
 * and even change application config file url (by default it is set in config.xml).
 * <p/>
 * Also, it stores current build version of the application,
 * so we could determine if it has been updated through the Google Play.
 */
public class PluginConfig {

    private static class JsonKeys {
        public static final String ALLOW_UPDATES_AUTO_DOWNLOAD = "auto-download";
        public static final String ALLOW_UPDATE_AUTO_INSTALL = "auto-install";
        public static final String CONFIG_URL = "config-file";
        public static final String APPLICATION_BUILD_VERSION = "app_build_version";
    }

    private boolean allowUpdatesAutoDownload;
    private boolean allowUpdatesAutoInstall;
    private String configUrl;
    private int appBuildVersion;

    /**
     * Create instance of the object from JSON string.
     *
     * @param json JSON string
     * @return object instance
     */
    public static PluginConfig fromJson(String json) {
        PluginConfig config = new PluginConfig();
        try {
            JsonNode jsonNode = new ObjectMapper().readTree(json);
            config.allowUpdatesAutoInstall(
                    jsonNode.get(JsonKeys.ALLOW_UPDATE_AUTO_INSTALL).asBoolean(false)
            );
            config.allowUpdatesAutoDownload(
                    jsonNode.get(JsonKeys.ALLOW_UPDATES_AUTO_DOWNLOAD).asBoolean(false)
            );
            config.setConfigUrl(
                    jsonNode.get(JsonKeys.CONFIG_URL).asText()
            );
            config.setAppBuildVersion(
                    jsonNode.get(JsonKeys.APPLICATION_BUILD_VERSION).asInt()
            );

        } catch (IOException e) {
            e.printStackTrace();

            config = null;
        }

        return config;
    }

    /**
     * Apply and save options that has been send from web page.
     * Using this we can change plugin config from JavaScript.
     *
     * @param jsOptions options from web
     * @throws JSONException
     */
    public void mergeOptionsFromJs(JSONObject jsOptions) throws JSONException {
        if (jsOptions.has(JsonKeys.CONFIG_URL)) {
            String configUrl = jsOptions.getString(JsonKeys.CONFIG_URL);
            if (!TextUtils.isEmpty(configUrl)) {
                setConfigUrl(configUrl);
            }
        }

        if (jsOptions.has(JsonKeys.ALLOW_UPDATE_AUTO_INSTALL)) {
            allowUpdatesAutoInstall(jsOptions.getBoolean(JsonKeys.ALLOW_UPDATE_AUTO_INSTALL));
        }

        if (jsOptions.has(JsonKeys.ALLOW_UPDATES_AUTO_DOWNLOAD)) {
            allowUpdatesAutoDownload(jsOptions.getBoolean(JsonKeys.ALLOW_UPDATES_AUTO_DOWNLOAD));
        }
    }

    /**
     * Create default plugin config.
     *
     * @param context            current application context
     * @param cordovaPreferences cordova preferences
     * @return default plugin config
     * @see CordovaPreferences
     */
    public static PluginConfig createDefaultConfig(Context context, CordovaPreferences cordovaPreferences) {
        PluginConfig config = new PluginConfig();
        config.allowUpdatesAutoInstall(true);
        config.allowUpdatesAutoDownload(true);

        // set version code of the app
        config.setAppBuildVersion(VersionHelper.applicationVersionCode(context));

        return config;
    }

    private PluginConfig() {
    }

    /**
     * Getter for build version of the app, which was detected on the last launch.
     * Using it we can determine if application has been updated through Google Play.
     *
     * @return build version of the app from last launch
     */
    public int getAppBuildVersion() {
        return appBuildVersion;
    }

    /**
     * Setter for build version.
     *
     * @param appBuildVersion new application build version
     */
    public void setAppBuildVersion(int appBuildVersion) {
        this.appBuildVersion = appBuildVersion;
    }

    /**
     * Setter for url, where application config is stored on the server.
     * Can be used to change config url on runtime.
     *
     * @param configUrl new config url
     */
    public void setConfigUrl(String configUrl) {
        this.configUrl = configUrl;
    }

    /**
     * Getter for config url
     *
     * @return config url
     */
    public String getConfigUrl() {
        return configUrl;
    }

    /**
     * Setter for the flag if updates auto download is allowed.
     *
     * @param isAllowed set to <code>true</code> to allow automatic update downloads.
     */
    public void allowUpdatesAutoDownload(boolean isAllowed) {
        allowUpdatesAutoDownload = isAllowed;
    }

    /**
     * Getter for the flag if updates auto download is allowed.
     * By default it is on, but you can disable it from JavaScript.
     *
     * @return <code>true</code> if automatic downloads are enabled, <code>false</code> - otherwise.
     */
    public boolean isAutoDownloadIsAllowed() {
        return allowUpdatesAutoDownload;
    }

    /**
     * Setter for the flag if updates auto installation is allowed.
     *
     * @param isAllowed set to <code>true</code> to allow automatic installation for the loaded updates.
     */
    public void allowUpdatesAutoInstall(boolean isAllowed) {
        allowUpdatesAutoInstall = isAllowed;
    }

    /**
     * Getter for the flag if updates auto installation is allowed.
     * By default it is on, but you can disable it from JavaScript.
     *
     * @return <code>true</code> if automatic installation is enabled, <code>false</code> - otherwise.
     */
    public boolean isAutoInstallIsAllowed() {
        return allowUpdatesAutoInstall;
    }

    /**
     * Convert object into JSON string
     *
     * @return JSON string
     */
    @Override
    public String toString() {
        JsonNodeFactory nodeFactory = JsonNodeFactory.instance;
        ObjectNode object = nodeFactory.objectNode();
        object.set(JsonKeys.ALLOW_UPDATE_AUTO_INSTALL, nodeFactory.booleanNode(allowUpdatesAutoInstall));
        object.set(JsonKeys.ALLOW_UPDATES_AUTO_DOWNLOAD, nodeFactory.booleanNode(allowUpdatesAutoDownload));
        object.set(JsonKeys.APPLICATION_BUILD_VERSION, nodeFactory.numberNode(appBuildVersion));
        object.set(JsonKeys.CONFIG_URL, nodeFactory.textNode(configUrl));

        return object.toString();
    }
}
