package com.nordnetab.chcp.main.events;

/**
 * Created by kopihao on 28.11.17.
 * <p/>
 * Event is send when progress require to be updated during the update download.
 */
public class UpdateDownloadProgressEvent extends WorkerEvent {

    public static final String EVENT_NAME = "chcp_updateLoadProgress";

    /**
     * Class constructor.
     *
     * @param percentage progress of this event
     */
    public UpdateDownloadProgressEvent(double percentage) {
        super(EVENT_NAME, null, percentage);
    }

    /**
     * Class constructor
     *
     * @param percentage  progress of an event
     * @param completed   completed progress count
     * @param outstanding outstanding progress count
     */
    public UpdateDownloadProgressEvent(double percentage, double completed, double outstanding) {
        super(EVENT_NAME, null, percentage, completed, outstanding);
    }
}
