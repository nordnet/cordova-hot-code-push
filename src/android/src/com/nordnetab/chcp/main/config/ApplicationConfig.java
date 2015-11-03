package com.nordnetab.chcp.main.config;

import android.text.TextUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Created by Nikolay Demyankov on 22.07.15.
 * <p/>
 * Model for application config. Holds information from chcp.json file.
 */
public class ApplicationConfig {

    private static final String MARKET_URL_FORMAT = "market://details?id=%s";

    private static class JsonKeys {
        public static final String STORE_PACKAGE_IDENTIFIER = "android_identifier";
    }

    private String jsonString;
    private ContentConfig contentConfig;
    private String storeIdentifier;
    private String storeUrl;

    private ApplicationConfig() {
    }

    /**
     * Create instance of the config from json string.
     *
     * @param jsonString json to process
     * @return class instance
     */
    public static ApplicationConfig fromJson(String jsonString) {
        ApplicationConfig config = new ApplicationConfig();
        try {
            JsonNode json = new ObjectMapper().readTree(jsonString);

            config.setContentConfig(ContentConfig.fromJson(json));

            // store identifier is optional
            if (json.has(JsonKeys.STORE_PACKAGE_IDENTIFIER)) {
                config.setStoreIdentifier(json.get(JsonKeys.STORE_PACKAGE_IDENTIFIER).asText());
            } else {
                config.setStoreIdentifier("");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        config.jsonString = jsonString;

        return config;
    }

    /**
     * Generates JSON string from class instance.
     *
     * @return JSON formatted string
     */
    @Override
    public String toString() {
        if (TextUtils.isEmpty(jsonString)) {
            jsonString = generateJson();
        }

        return jsonString;
    }

    /**
     * Getter for content config.
     *
     * @return content config
     * @see ContentConfig
     */
    public ContentConfig getContentConfig() {
        return contentConfig;
    }

    /**
     * Getter for url, that leeds to the applications page on the Google Play Store.
     *
     * @return market url
     */
    public String getStoreUrl() {
        if (TextUtils.isEmpty(storeIdentifier)) {
            return "";
        }

        if (TextUtils.isEmpty(storeUrl)) {
            if (storeIdentifier.startsWith("http://")
                    || storeIdentifier.startsWith("https://")
                    || storeIdentifier.startsWith("market://")) {
                storeUrl = storeIdentifier;
            } else {
                storeUrl = String.format(MARKET_URL_FORMAT, storeIdentifier);
            }
        }

        return storeUrl;
    }

    // region Private API

    private void setContentConfig(ContentConfig config) {
        this.contentConfig = config;
    }

    private void setStoreIdentifier(String storeIdentifier) {
        this.storeIdentifier = storeIdentifier;
        storeUrl = "";
    }

    private String generateJson() {
        JsonNodeFactory nodeFactory = JsonNodeFactory.instance;

        ObjectNode json = (ObjectNode) contentConfig.toJson();
        json.set(JsonKeys.STORE_PACKAGE_IDENTIFIER, nodeFactory.textNode(storeIdentifier));

        return json.toString();
    }

    // endregion
}
