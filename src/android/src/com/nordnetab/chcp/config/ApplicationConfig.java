package com.nordnetab.chcp.config;

import android.text.TextUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Created by Nikolay Demyankov on 22.07.15.
 */
public class ApplicationConfig {

    private static class JsonKeys {
        public static final String STORE_PACKAGE_IDENTIFIER = "android_identifier";
    }

    public static ApplicationConfig fromJson(String jsonString) {
        ApplicationConfig config = new ApplicationConfig();
        try {
            JsonNode json = new ObjectMapper().readTree(jsonString);

            config.setContentConfig(ContentConfig.fromJson(json));
            config.setStoreIdentifier(json.get(JsonKeys.STORE_PACKAGE_IDENTIFIER).asText());
        } catch (Exception e) {
            e.printStackTrace();
        }

        config.jsonString = jsonString;

        return config;
    }

    @Override
    public String toString() {
        if (TextUtils.isEmpty(jsonString)) {
            jsonString = generateJson();
        }

        return jsonString;
    }

    private String generateJson() {
        JsonNodeFactory nodeFactory = JsonNodeFactory.instance;

        ObjectNode json = (ObjectNode) contentConfig.toJson();
        json.set(JsonKeys.STORE_PACKAGE_IDENTIFIER, nodeFactory.textNode(storeIdentifier));

        return json.toString();
    }

    private String jsonString;
    private ContentConfig contentConfig;
    private String storeIdentifier;
    private String storeUrl;

    private ApplicationConfig() {
    }

    public ContentConfig getContentConfig() {
        return contentConfig;
    }

    public String getStoreUrl() {
        if (TextUtils.isEmpty(storeUrl)) {
            if (storeIdentifier.startsWith("http://")
                    || storeIdentifier.startsWith("https://")
                    || storeIdentifier.startsWith("market://")) {
                storeUrl = storeIdentifier;
            } else {
                storeUrl = "market://details?id=" + storeIdentifier;
            }
        }

        return storeUrl;
    }

    private void setContentConfig(ContentConfig config) {
        this.contentConfig = config;
    }

    private void setStoreIdentifier(String storeIdentifier) {
        this.storeIdentifier = storeIdentifier;
        storeUrl = "";
    }
}
