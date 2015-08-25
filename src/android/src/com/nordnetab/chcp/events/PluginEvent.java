package com.nordnetab.chcp.events;

import com.nordnetab.chcp.config.ApplicationConfig;
import com.nordnetab.chcp.model.ChcpError;

/**
 * Created by Nikolay Demyankov on 25.08.15.
 *
 */
public class PluginEvent {

    public final ChcpError error;
    public final ApplicationConfig config;
    public final String eventName;

    protected PluginEvent(String eventName, ChcpError error, ApplicationConfig config) {
        this.eventName = eventName;
        this.error = error;
        this.config = config;
    }
}
