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

    // keys for auto redirect to local storage index page
    public static final String AUTO_REDIRECT_TO_LOCAL_STORAGE_INDEX_PAGE_TAG = "auto-redirect-to-local-storage-index-page";
    public static final String AUTO_REDIRECT_TO_LOCAL_STORAGE_INDEX_PAGE_ENABLED_ATTRIBUTE = "enabled";

    // keys for use of initial version from assets
    public static final String USE_INITIAL_VERSION_FROM_ASSETS_TAG = "use-initial-version-from-assets";
    public static final String USE_INITIAL_VERSION_FROM_ASSETS_ENABLED_ATTRIBUTE = "enabled";

    // keys for require fresh install after app update
    public static final String REQUIRE_FRESH_INSTALL_AFTER_APP_UPDATE_TAG = "require-fresh-install-after-app-update";
    public static final String REQUIRE_FRESH_INSTALL_AFTER_APP_UPDATE_ENABLED_ATTRIBUTE = "enabled";

    // keys for file download concurrency
    public static final String FILE_DOWNLOAD_CONCURRENCY_TAG = "file-download-concurrency";
    public static final String FILE_DOWNLOAD_CONCURRENCY_VALUE_ATTRIBUTE = "value";

    // keys for assets remote zip url
    public static final String ASSETS_REMOTE_ZIP_URL_TAG = "assets-remote-zip-url";
    public static final String ASSETSE_REMOTE_ZIP_URL_VALUE_ATTRIBUTE = "value";
}
