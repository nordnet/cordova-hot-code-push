package com.nordnetab.chcp.main.events;

import com.nordnetab.chcp.main.model.ChcpError;

/**
 * Created by Or Arnon on 07.03.16.
 * <p/>
 * Event is send when an auto install is initiated but according to the config it is not allowed.
 */
public class AutoInstallNotAllowedErrorEvent extends PluginEventImpl {

    public static final String EVENT_NAME = "chcp_autoUpdateInstallNotAllowed";

    /**
     * Class constructor.
     *
     */
    public AutoInstallNotAllowedErrorEvent() {
        super(EVENT_NAME, ChcpError.AUTO_INSTALL_IS_NOT_ALLOWED);
    }
}
