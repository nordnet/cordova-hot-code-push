package com.nordnetab.chcp.main.model;

/**
 * Created by Nikolay Demyankov on 27.08.15.
 * <p/>
 * Enum holds list of options, when we should perform the update.
 */
public enum UpdateTime {
    /**
     * Value is undefined
     */
    UNDEFINED(""),

    /**
     * Update should be performed on application start
     */
    ON_START("start"),

    /**
     * Update should be performed when application is resumed
     */
    ON_RESUME("resume"),

    /**
     * Update should be performed as soon as possible. For example, when download is finished.
     */
    NOW("now");

    private String value;

    UpdateTime(String value) {
        this.value = value;
    }

    /**
     * Convert string value to enum instance.
     *
     * @param value string value
     * @return enum value
     */
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
