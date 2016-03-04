package com.nordnetab.chcp.main.config;

import android.content.Context;
import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Nikolay Demyankov on 06.08.15.
 * <p/>
 * Model for hot-code-push specific preferences in config.xml.
 */
public class ChcpXmlConfig {

    private String configUrl;
    private boolean allowUpdatesAutoDownload;
    private boolean allowUpdatesAutoInstall;
    private int nativeInterfaceVersion;

    private ChcpXmlConfig() {
        configUrl = "";
        allowUpdatesAutoDownload = true;
        allowUpdatesAutoInstall = true;
        nativeInterfaceVersion = 1;
    }

    /**
     * Getter for url to application config, that stored on server.
     * This is a path to chcp.json file.
     *
     * @return url to application config
     */
    public String getConfigUrl() {
        return configUrl;
    }

    /**
     * Setter for config url on server.
     *
     * @param configUrl url to application config
     */
    public void setConfigUrl(String configUrl) {
        this.configUrl = configUrl;
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
     * Getter for current native interface version of the application.
     *
     * @return native version
     * */
    public int getNativeInterfaceVersion() {
        return nativeInterfaceVersion;
    }

    /**
     * Setter for current native interface version of the application.
     *
     * @param version version to set
     * */
    void setNativeInterfaceVersion(int version) {
        nativeInterfaceVersion = version > 0 ? version : 1;
    }

    /**
     * Load plugins specific preferences from Cordova's config.xml.
     *
     * @param context current context of the activity
     * @return hot-code-push plugin preferences
     */
    public static ChcpXmlConfig loadFromCordovaConfig(final Context context) {
        ChcpXmlConfig chcpConfig = new ChcpXmlConfig();

        new ChcpXmlConfigParser().parse(context, chcpConfig);

        return chcpConfig;
    }

    /**
     * Apply and save options that has been send from web page.
     * Using this we can change plugin config from JavaScript.
     *
     * @param jsOptions options from web
     * @throws JSONException
     */
    public void mergeOptionsFromJs(JSONObject jsOptions) throws JSONException {
        if (jsOptions.has(XmlTags.CONFIG_FILE_TAG)) {
            String configUrl = jsOptions.getString(XmlTags.CONFIG_FILE_TAG);
            if (!TextUtils.isEmpty(configUrl)) {
                setConfigUrl(configUrl);
            }
        }

        if (jsOptions.has(XmlTags.AUTO_INSTALLATION_TAG)) {
            allowUpdatesAutoInstall(jsOptions.getBoolean(XmlTags.AUTO_INSTALLATION_TAG));
        }

        if (jsOptions.has(XmlTags.AUTO_DOWNLOAD_TAG)) {
            allowUpdatesAutoDownload(jsOptions.getBoolean(XmlTags.AUTO_DOWNLOAD_TAG));
        }
    }
}
