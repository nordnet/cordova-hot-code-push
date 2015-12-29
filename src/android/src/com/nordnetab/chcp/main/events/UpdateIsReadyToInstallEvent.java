package com.nordnetab.chcp.main.events;

import com.nordnetab.chcp.main.config.ApplicationConfig;

/**
 * Created by Nikolay Demyankov on 25.08.15.
 * <p/>
 * Event is send when update is successfully loaded and ready for installation.
 */
public class UpdateIsReadyToInstallEvent extends WorkerEvent {

    public static final String EVENT_NAME = "chcp_updateIsReadyToInstall";

    /**
     * Class constructor
     *
     * @param config application config that was used for update download
     */
    public UpdateIsReadyToInstallEvent(ApplicationConfig config) {
        super(EVENT_NAME, null, config);
    }
}
