package com.nordnetab.chcp.main.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nordnetab.chcp.main.model.UpdateTime;

/**
 * Created by Nikolay Demyankov on 22.07.15.
 * <p/>
 * Model for content configuration.
 * Holds information about current/new release, when to perform the update installation and so on.
 * Basically, it is a part of the chcp.json file, just moved to separate class for convenience.
 */
public class ContentConfig {

    // JSON keys to parse chcp.json
    private static class JsonKeys {
        public static final String VERSION = "release";
        public static final String MINIMUM_NATIVE_VERSION = "min_native_interface";
        public static final String UPDATE = "update";
        public static final String CONTENT_URL = "content_url";
    }

    /**
     * Create instance of the class from JSON node.
     *
     * @param node JSON node with data from chcp.json file
     * @return content configuration object
     * @see JsonNode
     */
    static ContentConfig fromJson(JsonNode node) {
        ContentConfig config = new ContentConfig();
        try {
            if (node.has(JsonKeys.VERSION)) {
                config.setReleaseVersion(node.get(JsonKeys.VERSION).asText());
            }

            if (node.has(JsonKeys.CONTENT_URL)) {
                config.setContentUrl(node.get(JsonKeys.CONTENT_URL).asText());
            }

            // minimum native version
            if (node.has(JsonKeys.MINIMUM_NATIVE_VERSION)) {
                config.setMinimumNativeVersion(node.get(JsonKeys.MINIMUM_NATIVE_VERSION).asInt());
            } else {
                config.setMinimumNativeVersion(0);
            }

            // when to perform update
            if (node.has(JsonKeys.UPDATE)) {
                config.setUpdateTime(UpdateTime.fromString(node.get(JsonKeys.UPDATE).asText()));
            } else {
                config.setUpdateTime(UpdateTime.ON_START);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return config;
    }

    private String releaseVersion;
    private int minimumNativeVersion;
    private String contentUrl;
    private UpdateTime updateTime;
    private JsonNode jsonNode;

    private ContentConfig() {
    }

    /**
     * Getter for the content's version.
     * Used to determine if the new release is available on the server.
     *
     * @return content version
     */
    public String getReleaseVersion() {
        return releaseVersion;
    }

    /**
     * Getter for minimum required version of the native part.
     * By this value we will determine if it is possible to install new version of web content
     * into current version of the app.
     *
     * @return minimum required native version for installing current web content
     */
    public int getMinimumNativeVersion() {
        return minimumNativeVersion;
    }

    /**
     * Getter for url on the server where all content is stored.
     * All updated/new files are loaded relative to this url.
     *
     * @return content url
     */
    public String getContentUrl() {
        return contentUrl;
    }

    /**
     * Getter for the preference, when we should install the update.
     *
     * @return update time preference
     * @see UpdateTime
     */
    public UpdateTime getUpdateTime() {
        return updateTime;
    }

    /**
     * Convert object into JSON node instance.
     *
     * @return JSON node
     * @see JsonNode
     */
    JsonNode toJson() {
        if (jsonNode == null) {
            jsonNode = generateJson();
        }

        return jsonNode;
    }

    // region Private API

    private void setReleaseVersion(String releaseVersion) {
        this.releaseVersion = releaseVersion;
    }

    private void setMinimumNativeVersion(int minimumNativeVersion) {
        this.minimumNativeVersion = minimumNativeVersion;
    }

    private void setContentUrl(String contentUrl) {
        this.contentUrl = contentUrl;
    }

    private void setUpdateTime(UpdateTime updateTime) {
        this.updateTime = updateTime;
    }

    private JsonNode generateJson() {
        JsonNodeFactory nodeFactory = JsonNodeFactory.instance;

        ObjectNode node = nodeFactory.objectNode();
        node.set(JsonKeys.CONTENT_URL, nodeFactory.textNode(contentUrl));
        node.set(JsonKeys.MINIMUM_NATIVE_VERSION, nodeFactory.numberNode(minimumNativeVersion));
        node.set(JsonKeys.VERSION, nodeFactory.textNode(releaseVersion));
        node.set(JsonKeys.UPDATE, nodeFactory.textNode(updateTime.toString()));

        return node;
    }

    // endregion
}
