package com.nordnetab.chcp.main.events;

/**
 * Contributed by Torsten Freyhall on 09.02.16.
 * <p/>
 * Event is sent when an update is about to be installed.
 */
public class BeforeInstallEvent extends WorkerEvent {

    public static final String EVENT_NAME = "chcp_beforeInstall";

    /**
     * Class constructor.
     */
    public BeforeInstallEvent() {
        super(EVENT_NAME, null, null);
    }
}
