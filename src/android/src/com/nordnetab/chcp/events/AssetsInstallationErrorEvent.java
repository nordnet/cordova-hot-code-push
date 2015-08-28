package com.nordnetab.chcp.events;

import com.nordnetab.chcp.model.ChcpError;

/**
 * Created by Nikolay Demyankov on 28.08.15.
 *
 * Event is send when error occurred while copying assets from bundle onto external storage.
 */
public class AssetsInstallationErrorEvent extends PluginEvent {

    private static final String EVENT_NAME = "chcp_assetsInstallationError";

    /**
     * Class constructor
     */
    public AssetsInstallationErrorEvent() {
        super(EVENT_NAME, ChcpError.FAILED_TO_INSTALL_ASSETS_ON_EXTERNAL_STORAGE, null);
    }
}