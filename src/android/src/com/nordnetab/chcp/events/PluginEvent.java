package com.nordnetab.chcp.events;

import com.nordnetab.chcp.config.ApplicationConfig;
import com.nordnetab.chcp.model.ChcpError;

/**
 * Created by Nikolay Demyankov on 25.08.15.
 * <p/>
 * Base class for plugin specific events.
 * All events are dispatched and captured through EventBus.
 *
 * @see de.greenrobot.event.EventBus
 */
public class PluginEvent {

    /**
     * Error information, that is attached to the event
     */
    public final ChcpError error;

    /**
     * Application config, that is attached to the event.
     * If this is a download event - application config is a config, that was used to download new content.
     * If this is an installation event - application config is a config, that was used for installation.
     **/
    public final ApplicationConfig config;

    /**
     * String identifier of the event.
     * Used for sending events in JavaScript.
     */
    public final String eventName;

    /**
     * Class constructor
     *
     * @param eventName string identifier of the event
     * @param error     error information
     * @param config    application config
     */
    protected PluginEvent(String eventName, ChcpError error, ApplicationConfig config) {
        this.eventName = eventName;
        this.error = error;
        this.config = config;
    }
}
