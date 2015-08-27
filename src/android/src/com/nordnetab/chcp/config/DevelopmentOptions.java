package com.nordnetab.chcp.config;

/**
 * Created by Nikolay Demyankov on 27.08.15.
 */
public class DevelopmentOptions {

    private boolean enabled;

    public DevelopmentOptions() {
        enabled = false;
    }

    public boolean isEnabled() {
        return enabled;
    }

    void setEnabled(boolean isEnabled) {
        enabled = isEnabled;
    }

}
