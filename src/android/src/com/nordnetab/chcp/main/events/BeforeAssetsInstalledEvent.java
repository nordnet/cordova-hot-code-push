package com.nordnetab.chcp.main.events;

/**
 * Created by Nikolay Demyankov on 09.03.16.
 *
 * Event is dispatched right before plugin will start installing application assets on the external storage.
 */
public class BeforeAssetsInstalledEvent extends PluginEventImpl {

    public static final String EVENT_NAME = "chcp_beforeAssetsInstalledOnExternalStorage";

    /**
     * Class constructor
     */
    public BeforeAssetsInstalledEvent() {
        super(EVENT_NAME, null);
    }

}
