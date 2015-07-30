package com.nordnetab.chcp.config;

import android.content.Context;
import android.content.pm.PackageManager;
import android.text.TextUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.apache.cordova.CordovaPreferences;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Created by Nikolay Demyankov on 28.07.15.
 *
 *
 */
public class PluginConfig {

    private static class JsonKeys {
        public static final String ALLOW_UPDATES_AUTO_DOWNLOAD = "allow_auto_download";
        public static final String ALLOW_UPDATE_AUTO_INSTALL = "allow_auto_install";
        public static final String CONFIG_URL = "config_url";
        public static final String APPLICATION_BUILD_VERSION = "app_build_version";
    }

    private static class CordovaPreferenceKeys {
        public static final String CONFIG_URL = "hot_code_push_config_url";
    }

    private boolean allowUpdatesAutoDownload;
    private boolean allowUpdatesAutoInstall;
    private String configUrl;
    private int appBuildVersion;

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

    public static PluginConfig createDefaultConfig(Context context, CordovaPreferences cordovaPreferences) {
        PluginConfig config = new PluginConfig();
        config.allowUpdatesAutoInstall(true);
        config.allowUpdatesAutoDownload(true);

        config.setConfigUrl(cordovaPreferences.getString(CordovaPreferenceKeys.CONFIG_URL, ""));

        // get version code of the app
        int versionCode = 0;
        try {
            versionCode = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        config.setAppBuildVersion(versionCode);

        return config;
    }

    private PluginConfig() {
    }

    public int getAppBuildVersion() {
        return appBuildVersion;
    }

    public void setAppBuildVersion(int appBuildVersion) {
        this.appBuildVersion = appBuildVersion;
    }

    public void setConfigUrl(String configUrl) {
        this.configUrl = configUrl;
    }

    public String getConfigUrl() {
        return configUrl;
    }

    public void allowUpdatesAutoDownload(boolean isAllowed) {
        allowUpdatesAutoDownload = isAllowed;
    }

    public boolean isAutoDownloadIsAllowed() {
        return allowUpdatesAutoDownload;
    }

    public void allowUpdatesAutoInstall(boolean isAllowed) {
        allowUpdatesAutoInstall = isAllowed;
    }

    public boolean isAutoInstallIsAllowed() {
        return allowUpdatesAutoInstall;
    }

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
