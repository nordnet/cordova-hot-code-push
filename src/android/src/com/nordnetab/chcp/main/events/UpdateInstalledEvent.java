package com.nordnetab.chcp.main.events;

import com.nordnetab.chcp.main.config.ApplicationConfig;

/**
 * Created by Nikolay Demyankov on 25.08.15.
 * <p/>
 * Event is send when update has been installed.
 */
public class UpdateInstalledEvent extends WorkerEvent {

    public static final String EVENT_NAME = "chcp_updateInstalled";

    /**
     * Class constructor.
     *
     * @param config application config that was used for installation
     */
    public UpdateInstalledEvent(ApplicationConfig config) {
        super(EVENT_NAME, null, config);
    }
}
