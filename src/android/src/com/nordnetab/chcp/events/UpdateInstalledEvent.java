package com.nordnetab.chcp.events;

import com.nordnetab.chcp.config.ApplicationConfig;

/**
 * Created by Nikolay Demyankov on 25.08.15.
 */
public class UpdateInstalledEvent extends PluginEvent {

    private static final String EVENT_NAME = "chcp_updateInstalled";

    public UpdateInstalledEvent(ApplicationConfig config) {
        super(EVENT_NAME, null, config);
    }
}
