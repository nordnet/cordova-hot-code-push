package com.nordnetab.chcp.events;

import com.nordnetab.chcp.config.ApplicationConfig;
import com.nordnetab.chcp.model.ChcpError;

/**
 * Created by Nikolay Demyankov on 25.08.15.
 */
class DownloadEvent extends PluginEvent {

    public final String taskId;

    protected DownloadEvent(String eventName, String taskId, ChcpError error, ApplicationConfig config) {
        super(eventName, error, config);

        this.taskId = taskId;
    }

}
