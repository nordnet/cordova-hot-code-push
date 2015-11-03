package com.nordnetab.chcp.main.events;

import com.nordnetab.chcp.main.config.ApplicationConfig;
import com.nordnetab.chcp.main.model.ChcpError;

/**
 * Created by Nikolay Demyankov on 25.08.15.
 * <p/>
 * Event is send when some error happened during the update download.
 */
public class UpdateDownloadErrorEvent extends WorkerEvent {

    private static final String EVENT_NAME = "chcp_updateLoadFailed";

    /**
     * Class constructor.
     *
     * @param taskId identifier of the current download task
     * @param error  error information
     * @param config application config that was used for update download
     */
    public UpdateDownloadErrorEvent(String taskId, ChcpError error, ApplicationConfig config) {
        super(EVENT_NAME, error, taskId, config);
    }
}
