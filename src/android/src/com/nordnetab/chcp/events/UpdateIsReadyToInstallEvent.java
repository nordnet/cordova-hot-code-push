package com.nordnetab.chcp.events;

import com.nordnetab.chcp.config.ApplicationConfig;

/**
 * Created by Nikolay Demyankov on 25.08.15.
 * <p/>
 * Event is send when update is successfully loaded and ready for installation.
 */
public class UpdateIsReadyToInstallEvent extends DownloadEvent {

    private static final String EVENT_NAME = "chcp_updateIsReadyToInstall";

    /**
     * Class constructor
     *
     * @param taskId download task identifier
     * @param config application config that was used for update download
     */
    public UpdateIsReadyToInstallEvent(String taskId, ApplicationConfig config) {
        super(EVENT_NAME, taskId, null, config);
    }
}
