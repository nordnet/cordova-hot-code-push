package com.nordnetab.chcp.main.config;

import android.content.Context;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nordnetab.chcp.main.model.PluginFilesStructure;
import com.nordnetab.chcp.main.utils.VersionHelper;

import java.io.IOException;

/**
 * Created by Nikolay Demyankov on 28.07.15.
 * <p/>
 * Model for plugin internal preferences.
 */
public class PluginInternalPreferences {

    // json keys of the preference
    private static final String APPLICATION_BUILD_VERSION = "app_build_version";
    private static final String WWW_FOLDER_INSTALLED_FLAG = "www_folder_installed";
    private static final String PREVIOUS_RELEASE_VERSION_NAME = "previous_release_version_name";
    private static final String CURRENT_RELEASE_VERSION_NAME = "current_release_version_name";
    private static final String READY_FOR_INSTALLATION_RELEASE_VERSION_NAME = "ready_for_installation_release_version_name";

    private int appBuildVersion;
    private boolean wwwFolderInstalled;
    private String currentReleaseVersionName;
    private String previousReleaseVersionName;
    private String readyForInstallationReleaseVersionName;

    /**
     * Create instance of the object from JSON string.
     *
     * @param json JSON string
     * @return object instance
     */
    public static PluginInternalPreferences fromJson(final String json) {
        PluginInternalPreferences config = new PluginInternalPreferences();
        try {
            JsonNode jsonNode = new ObjectMapper().readTree(json);
            config.setAppBuildVersion(
                    jsonNode.get(APPLICATION_BUILD_VERSION).asInt()
            );
            config.setWwwFolderInstalled(
                    jsonNode.get(WWW_FOLDER_INSTALLED_FLAG).asBoolean()
            );

            if (jsonNode.has(CURRENT_RELEASE_VERSION_NAME)) {
                config.setCurrentReleaseVersionName(
                        jsonNode.get(CURRENT_RELEASE_VERSION_NAME).asText()
                );
            }

            if (jsonNode.has(PREVIOUS_RELEASE_VERSION_NAME)) {
                config.setPreviousReleaseVersionName(
                        jsonNode.get(PREVIOUS_RELEASE_VERSION_NAME).asText()
                );
            }

            if (jsonNode.has(READY_FOR_INSTALLATION_RELEASE_VERSION_NAME)) {
                config.setReadyForInstallationReleaseVersionName(
                        jsonNode.get(READY_FOR_INSTALLATION_RELEASE_VERSION_NAME).asText()
                );
            }
        } catch (IOException e) {
            e.printStackTrace();

            config = null;
        }

        return config;
    }

    private PluginInternalPreferences() {
        currentReleaseVersionName = "";
        previousReleaseVersionName = "";
        readyForInstallationReleaseVersionName = "";
    }

    /**
     * Create default preferences.
     *
     * @param context application context
     * @return default plugin internal preferences
     */
    public static PluginInternalPreferences createDefault(final Context context) {
        final PluginInternalPreferences pluginPrefs = new PluginInternalPreferences();
        pluginPrefs.setAppBuildVersion(VersionHelper.applicationVersionCode(context));
        pluginPrefs.setWwwFolderInstalled(false);
        pluginPrefs.setPreviousReleaseVersionName("");
        pluginPrefs.setReadyForInstallationReleaseVersionName("");
        pluginPrefs.setCurrentReleaseVersionName("");

        // read app config from assets to get current release version
        final ApplicationConfig appConfig = ApplicationConfig.configFromAssets(context, PluginFilesStructure.CONFIG_FILE_NAME);
        if (appConfig != null) {
            pluginPrefs.setCurrentReleaseVersionName(appConfig.getContentConfig().getReleaseVersion());
        }

        return pluginPrefs;
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
     * Getter for current release version name.
     *
     * @return current release version name
     */
    public String getCurrentReleaseVersionName() {
        return currentReleaseVersionName;
    }

    /**
     * Setter for current release version name.
     *
     * @param currentReleaseVersionName current release version
     */
    public void setCurrentReleaseVersionName(final String currentReleaseVersionName) {
        this.currentReleaseVersionName = currentReleaseVersionName;
    }

    /**
     * Getter for previous release version name.
     *
     * @return previous release version name
     */
    public String getPreviousReleaseVersionName() {
        return previousReleaseVersionName;
    }

    /**
     * Setter for previous release version name.
     *
     * @param previousReleaseVersionName previous release version name
     */
    public void setPreviousReleaseVersionName(String previousReleaseVersionName) {
        this.previousReleaseVersionName = previousReleaseVersionName;
    }

    /**
     * Getter for version, that is ready for installation.
     *
     * @return version to install
     */
    public String getReadyForInstallationReleaseVersionName() {
        return readyForInstallationReleaseVersionName;
    }

    /**
     * Setter for version, that is ready for installation.
     *
     * @param readyForInstallationReleaseVersionName version to install
     */
    public void setReadyForInstallationReleaseVersionName(String readyForInstallationReleaseVersionName) {
        this.readyForInstallationReleaseVersionName = readyForInstallationReleaseVersionName;
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
        object.set(APPLICATION_BUILD_VERSION, nodeFactory.numberNode(appBuildVersion));
        object.set(WWW_FOLDER_INSTALLED_FLAG, nodeFactory.booleanNode(wwwFolderInstalled));
        object.set(CURRENT_RELEASE_VERSION_NAME, nodeFactory.textNode(currentReleaseVersionName));
        object.set(PREVIOUS_RELEASE_VERSION_NAME, nodeFactory.textNode(previousReleaseVersionName));
        object.set(READY_FOR_INSTALLATION_RELEASE_VERSION_NAME, nodeFactory.textNode(readyForInstallationReleaseVersionName));

        return object.toString();
    }
}
