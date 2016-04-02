package com.nordnetab.chcp.main.events;

import com.nordnetab.chcp.main.model.ChcpError;

/**
 * Created by Or Arnon on 07.03.16.
 * <p/>
 * Event is send when an auto download is initiated but according to the config it is not allowed.
 */
public class AutoDownloadNotAllowedErrorEvent extends PluginEventImpl {

    public static final String EVENT_NAME = "chcp_autoUpdateLoadNotAllowed";

    /**
     * Class constructor.
     *
     */
    public AutoDownloadNotAllowedErrorEvent() {
        super(EVENT_NAME, ChcpError.AUTO_UPDATE_IS_NOT_ALLOWED);
    }
}
