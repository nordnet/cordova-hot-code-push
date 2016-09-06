package com.nordnetab.chcp.main.js;

/**
 * Created by Nikolay Demyankov on 28.08.15.
 *
 * List of plugin methods, available from JS side.
 */
public final class JSAction {

    // Public API
    public static final String FETCH_UPDATE = "jsFetchUpdate";
    public static final String INSTALL_UPDATE = "jsInstallUpdate";
    public static final String CONFIGURE = "jsConfigure";
    public static final String REQUEST_APP_UPDATE = "jsRequestAppUpdate";
    public static final String IS_UPDATE_AVAILABLE_FOR_INSTALLATION = "jsIsUpdateAvailableForInstallation";
    public static final String GET_VERSION_INFO = "jsGetVersionInfo";

    // Private API
    public static final String INIT = "jsInitPlugin";

}
