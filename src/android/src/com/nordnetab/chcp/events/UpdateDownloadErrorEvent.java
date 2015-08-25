package com.nordnetab.chcp.events;

import com.nordnetab.chcp.config.ApplicationConfig;
import com.nordnetab.chcp.model.ChcpError;

/**
 * Created by Nikolay Demyankov on 25.08.15.
 */
public class UpdateDownloadErrorEvent extends DownloadEvent {

    private static final String EVENT_NAME = "chcp_updateLoadFailed";

    public UpdateDownloadErrorEvent(String taskId, ChcpError error, ApplicationConfig config) {
        super(EVENT_NAME, taskId, error, config);
    }
}
