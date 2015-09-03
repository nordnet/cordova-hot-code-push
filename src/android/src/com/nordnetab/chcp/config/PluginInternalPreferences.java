package com.nordnetab.chcp.config;

import android.content.Context;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nordnetab.chcp.utils.VersionHelper;

import java.io.IOException;

/**
 * Created by Nikolay Demyankov on 28.07.15.
 * <p/>
 * Model for plugin internal preferences.
 * <p/>
 * For now it stores only current build version of the application,
 * so we could determine if it has been updated through the Google Play.
 * <p/>
 * Later this can be extended with additional internal stuff.
 */
public class PluginInternalPreferences {

    private static class JsonKeys {
        public static final String APPLICATION_BUILD_VERSION = "app_build_version";
    }

    private int appBuildVersion;

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

        // set version code of the app
        config.setAppBuildVersion(VersionHelper.applicationVersionCode(context));

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
     * Convert object into JSON string
     *
     * @return JSON string
     */
    @Override
    public String toString() {
        JsonNodeFactory nodeFactory = JsonNodeFactory.instance;
        ObjectNode object = nodeFactory.objectNode();
        object.set(JsonKeys.APPLICATION_BUILD_VERSION, nodeFactory.numberNode(appBuildVersion));

        return object.toString();
    }
}
