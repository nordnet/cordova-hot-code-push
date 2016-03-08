package com.nordnetab.chcp.main.events;

/**
 * Created by Or Arnon on 01.03.16.
 * <p/>
 * Event is sent when a rollback has been performed.
 */
public class RollbackPerformedEvent extends PluginEventImpl {

    public static final String EVENT_NAME = "chcp_rollbackPerformed";

    /**
     * Class constructor
     */
    public RollbackPerformedEvent() {
        super(EVENT_NAME, null);
    }
}
