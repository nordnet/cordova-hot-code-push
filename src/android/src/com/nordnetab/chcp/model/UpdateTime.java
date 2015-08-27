package com.nordnetab.chcp.model;

/**
 * Created by Nikolay Demyankov on 27.08.15.
 */
public enum UpdateTime {
    UNDEFINED(""),
    ON_START("start"),
    ON_RESUME("resume"),
    NOW("now");

    private String value;

    UpdateTime(String value) {
        this.value = value;
    }

    public static UpdateTime fromString(String value) {
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
