package com.nordnetab.chcp.config;

import android.content.Context;

/**
 * Created by Nikolay Demyankov on 06.08.15.
 *
 * Model for hot-code-push specific preferences in config.xml.
 */
public class ChcpXmlConfig {

    private String configUrl;
    private DevelopmentOptions developmentOptions;

    private ChcpXmlConfig(){
        configUrl = "";
        developmentOptions = new DevelopmentOptions();
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
    void setConfigUrl(String configUrl) {
        this.configUrl = configUrl;
    }

    /**
     * Getter for local development options.
     *
     * @return local development options
     *
     * @see DevelopmentOptions
     */
    public DevelopmentOptions getDevelopmentOptions() {
        return developmentOptions;
    }

    /**
     * Load plugins specific preferences from Cordova's config.xml.
     *
     * @param context current context of the activity
     *
     * @return hot-code-push plugin preferences
     */
    public static ChcpXmlConfig loadFromCordovaConfig(Context context) {
        ChcpXmlConfig chcpConfig = new ChcpXmlConfig();

        new ChcpXmlConfigParser().parse(context, chcpConfig);

        return chcpConfig;
    }
}
