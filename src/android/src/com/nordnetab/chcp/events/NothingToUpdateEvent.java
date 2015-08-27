package com.nordnetab.chcp.events;

import com.nordnetab.chcp.config.ApplicationConfig;

/**
 * Created by Nikolay Demyankov on 25.08.15.
 */
public class NothingToUpdateEvent extends DownloadEvent {

    private static final String EVENT_NAME = "chcp_nothingToUpdate";

    public NothingToUpdateEvent(String taskId, ApplicationConfig config) {
        super(EVENT_NAME, taskId, null, config);
    }

}
