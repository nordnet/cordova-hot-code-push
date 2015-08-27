package com.nordnetab.chcp.config;

/**
 * Created by Nikolay Demyankov on 27.08.15.
 * <p/>
 * Model for local development options.
 * By "local development" we mean, that you use
 * {@link https://github.com/nordnet/cordova-hot-code-push-cli} to start local server.
 * If so - plugin will try to connect to it via socket and listen for updates in web content.
 * On every change plugin will trigger update download.
 * <p/>
 * This can help you to speed up development process, so you would not have to re-build application after each change.
 */
public class DevelopmentOptions {

    private boolean enabled;

    /**
     * Constructor
     */
    public DevelopmentOptions() {
        enabled = false;
    }

    /**
     * Is local development enabled.
     *
     * @return <code>true</code> if local development is enabled, <code>false</code> otherwise.
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Used to enable/disable local development.
     * Used internally. If you want to enable/disable it - use preference in config.xml
     *
     * @param isEnabled if <code>true</code> - local development will be enabled.
     */
    void setEnabled(boolean isEnabled) {
        enabled = isEnabled;
    }

}
