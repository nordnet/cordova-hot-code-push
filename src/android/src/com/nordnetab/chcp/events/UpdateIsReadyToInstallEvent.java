package com.nordnetab.chcp.events;

import com.nordnetab.chcp.config.ApplicationConfig;

/**
 * Created by Nikolay Demyankov on 25.08.15.
 */
public class UpdateIsReadyToInstallEvent extends DownloadEvent {

    private static final String EVENT_NAME = "chcp_updateIsReadyToInstall";

    public UpdateIsReadyToInstallEvent(String taskId, ApplicationConfig config) {
        super(taskId, EVENT_NAME, null, config);
    }
}
