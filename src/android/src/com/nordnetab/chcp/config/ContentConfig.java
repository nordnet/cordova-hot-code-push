package com.nordnetab.chcp.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Created by Nikolay Demyankov on 22.07.15.
 */
public class ContentConfig {

    public enum UpdateTime {
        UNDEFINED(""),
        ON_START("start"),
        ON_RESUME("resume"),
        NOW("now");

        private String value;

        UpdateTime(String value) {
            this.value = value;
        }

        private static UpdateTime fromString(String value) {
            if ("start".equals(value)) {
                return ON_START;
            } else if ("resume".equals(value)) {
                return ON_RESUME;
            } else if ("now".equals(value)) {
                return NOW;
            }

            return UNDEFINED;
        }

        @Override
        public String toString() {
            return value;
        }
    }

    private static class JsonKeys {
        public static final String VERSION = "release";
        public static final String MINIMUM_NATIVE_VERSION = "min_native_interface";
        public static final String UPDATE = "update";
        public static final String CONTENT_URL = "content_url";
    }

    static ContentConfig fromJson(JsonNode node) {
        ContentConfig config = new ContentConfig();
        try {
            config.setReleaseVersion(node.get(JsonKeys.VERSION).asText());

            // minimum native version is now optional parameter
            if (node.has(JsonKeys.MINIMUM_NATIVE_VERSION)) {
                config.setMinimumNativeVersion(node.get(JsonKeys.MINIMUM_NATIVE_VERSION).asInt());
            } else {
                config.setMinimumNativeVersion(0);
            }

            config.setContentUrl(node.get(JsonKeys.CONTENT_URL).asText());
            config.setUpdateTime(
                    UpdateTime.fromString(node.get(JsonKeys.UPDATE).asText())
            );
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

    public String getReleaseVersion() {
        return releaseVersion;
    }

    private void setReleaseVersion(String releaseVersion) {
        this.releaseVersion = releaseVersion;
    }

    public int getMinimumNativeVersion() {
        return minimumNativeVersion;
    }

    private void setMinimumNativeVersion(int minimumNativeVersion) {
        this.minimumNativeVersion = minimumNativeVersion;
    }

    public String getContentUrl() {
        return contentUrl;
    }

    private void setContentUrl(String contentUrl) {
        this.contentUrl = contentUrl;
    }

    public UpdateTime getUpdateTime() {
        return updateTime;
    }

    private void setUpdateTime(UpdateTime updateTime) {
        this.updateTime = updateTime;
    }

    JsonNode toJson() {
        if (jsonNode == null) {
            jsonNode = generateJson();
        }

        return jsonNode;
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
}
