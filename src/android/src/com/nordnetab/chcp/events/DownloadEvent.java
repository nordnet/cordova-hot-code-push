package com.nordnetab.chcp.events;

import com.nordnetab.chcp.config.ApplicationConfig;
import com.nordnetab.chcp.model.ChcpError;

/**
 * Created by Nikolay Demyankov on 25.08.15.
 * <p/>
 * Base class for update download events.
 * Used internally.
 */
class DownloadEvent extends PluginEvent {

    /**
     * ID of the current download task
     */
    public final String taskId;

    /**
     * Class constructor
     *
     * @param eventName name of the event
     * @param taskId    download task identifier
     * @param error     error information
     * @param config    application config that was used for download
     */
    protected DownloadEvent(String eventName, String taskId, ChcpError error, ApplicationConfig config) {
        super(eventName, error, config);

        this.taskId = taskId;
    }

}
