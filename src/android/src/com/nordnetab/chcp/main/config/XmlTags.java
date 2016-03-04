package com.nordnetab.chcp.main.config;

/**
 * Created by Nikolay Demyankov on 03.09.15.
 *
 * Plugin specific xml keys and attributes in config.xml
 */
final class XmlTags {

    private XmlTags() {
    }

    public static final String MAIN_TAG = "chcp";

    // keys for application config file url
    public static final String CONFIG_FILE_TAG = "config-file";
    public static final String CONFIG_FILE_URL_ATTRIBUTE = "url";

    // keys for updates auto download
    public static final String AUTO_DOWNLOAD_TAG = "auto-download";
    public static final String AUTO_DOWNLOAD_ENABLED_ATTRIBUTE = "enabled";

    // keys for updates auto installation
    public static final String AUTO_INSTALLATION_TAG = "auto-install";
    public static final String AUTO_INSTALLATION_ENABLED_ATTRIBUTE = "enabled";

    // keys for native interface version
    public static final String NATIVE_INTERFACE_TAG = "native-interface";
    public static final String NATIVE_INTERFACE_VERSION_ATTRIBUTE = "version";

}
