package com.nordnetab.chcp.events;

import com.nordnetab.chcp.config.ApplicationConfig;
import com.nordnetab.chcp.model.ChcpError;

import java.util.Map;

/**
 * Created by Nikolay Demyankov on 28.08.15.
 */
class WorkerEvent extends PluginEventImpl {

    private static final String CONFIG_KEY = "config";

    public final String taskId;

    /**
     * Class constructor
     *
     * @param eventName string identifier of the event
     * @param error     error information
     *
     * @see ChcpError
     */
    protected WorkerEvent(String eventName, ChcpError error, String taskId, ApplicationConfig appConfig) {
        super(eventName, error);

        this.taskId = taskId;
        if (appConfig != null) {
            data().put(CONFIG_KEY, appConfig);
        }
    }

    public ApplicationConfig applicationConfig() {
        final Map<String, Object> eventData = data();
        if (!eventData.containsKey(CONFIG_KEY)) {
            return null;
        }

        return (ApplicationConfig) eventData.get(CONFIG_KEY);
    }
}
