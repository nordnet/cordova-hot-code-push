package com.nordnetab.chcp.events;

import com.nordnetab.chcp.config.ApplicationConfig;
import com.nordnetab.chcp.model.ChcpError;

/**
 * Created by Nikolay Demyankov on 25.08.15.
 */
public class UpdateInstallationErrorEvent extends PluginEvent {

    private static final String EVENT_NAME = "chcp_updateInstallFailed";

    public UpdateInstallationErrorEvent(ChcpError error, ApplicationConfig config) {
        super(EVENT_NAME, error, config);
    }
}
