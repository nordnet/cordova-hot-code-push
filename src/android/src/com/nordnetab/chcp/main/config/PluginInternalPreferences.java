package com.nordnetab.chcp.main.config;

import android.content.Context;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nordnetab.chcp.main.utils.VersionHelper;

import java.io.IOException;

/**
 * Created by Nikolay Demyankov on 28.07.15.
 * <p/>
 * Model for plugin internal preferences.
 */
public class PluginInternalPreferences {

    private static class JsonKeys {
        public static final String APPLICATION_BUILD_VERSION = "app_build_version";
        public static final String WWW_FOLDER_INSTALLED_FLAG = "www_folder_installed";
    }

    private int appBuildVersion;
    private boolean wwwFolderInstalled;

    /**
     * Create instance of the object from JSON string.
     *
     * @param json JSON string
     * @return object instance
     */
    public static PluginInternalPreferences fromJson(String json) {
        PluginInternalPreferences config = new PluginInternalPreferences();
        try {
            JsonNode jsonNode = new ObjectMapper().readTree(json);
            config.setAppBuildVersion(
                    jsonNode.get(JsonKeys.APPLICATION_BUILD_VERSION).asInt()
            );
            config.setWwwFolderInstalled(
                    jsonNode.get(JsonKeys.WWW_FOLDER_INSTALLED_FLAG).asBoolean()
            );
        } catch (IOException e) {
            e.printStackTrace();

            config = null;
        }

        return config;
    }

    private PluginInternalPreferences() {
    }

    public static PluginInternalPreferences createDefault(Context context) {
        PluginInternalPreferences config = new PluginInternalPreferences();

        config.setAppBuildVersion(VersionHelper.applicationVersionCode(context));
        config.setWwwFolderInstalled(false);

        return config;
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
     * Getter for flag if www folder was installed on the external storage.
     *
     * @return <code>true</code> - www folder was installed; otherwise - <code>false</code>
     */
    public boolean isWwwFolderInstalled() {
        return wwwFolderInstalled;
    }

    /**
     * Setter for the flag that www folder was installed on the external storage.
     *
     * @param isWwwFolderInstalled is www folder is installed
     */
    public void setWwwFolderInstalled(boolean isWwwFolderInstalled) {
        wwwFolderInstalled = isWwwFolderInstalled;
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
        object.set(JsonKeys.APPLICATION_BUILD_VERSION, nodeFactory.numberNode(appBuildVersion));
        object.set(JsonKeys.WWW_FOLDER_INSTALLED_FLAG, nodeFactory.booleanNode(wwwFolderInstalled));

        return object.toString();
    }
}
