package com.nordnetab.chcp.main.events;

import com.nordnetab.chcp.main.model.ChcpError;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Nikolay Demyankov on 25.08.15.
 * <p/>
 * Implementation of the IPluginEvent interface.
 * Also, base class for all plugin specific events.
 * All events are dispatched and captured through EventBus.
 *
 * @see de.greenrobot.event.EventBus
 */
class PluginEventImpl implements IPluginEvent {

    private final ChcpError error;
    private final String eventName;
    private final Map<String, Object>data;

    /**
     * Class constructor
     *
     * @param eventName string identifier of the event
     * @param error     error information
     *
     * @see ChcpError
     */
    protected PluginEventImpl(String eventName, ChcpError error) {
        this.eventName = eventName;
        this.error = error;
        this.data = new HashMap<String, Object>();
    }

    @Override
    public String name() {
        return eventName;
    }

    @Override
    public ChcpError error() {
        return error;
    }

    @Override
    public Map<String, Object> data() {
        return data;
    }
}