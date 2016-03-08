package com.nordnetab.chcp.main.config;

import android.content.Context;
import android.text.TextUtils;

import com.nordnetab.chcp.main.network.FileDownloader;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Nikolay Demyankov on 06.08.15.
 * <p/>
 * Model for hot-code-push specific preferences in config.xml.
 */
public class ChcpXmlConfig {

    private String configUrl;
    private boolean allowUpdatesAutoDownload;
    private boolean allowUpdatesAutoInstall;
    private boolean allowAutoRedirectionToLocalStorageIndexPage;
    private boolean allowUseOfInitialVersionFromAssets;
    private boolean requireFreshInstallAfterAppVersionUpdate;
    private String assetsRemoteZipUrl;

    private ChcpXmlConfig() {
        configUrl = "";
        allowUpdatesAutoDownload = true;
        allowUpdatesAutoInstall = true;
        allowAutoRedirectionToLocalStorageIndexPage = true;
        allowUseOfInitialVersionFromAssets = true;
        requireFreshInstallAfterAppVersionUpdate = true;
        assetsRemoteZipUrl = "";
    }

    /**
     * Getter for url to application config, that stored on server.
     * This is a path to chcp.json file.
     *
     * @return url to application config
     */
    public String getConfigUrl() {
        return configUrl;
    }

    /**
     * Setter for config url on server.
     *
     * @param configUrl url to application config
     */
    public void setConfigUrl(String configUrl) {
        this.configUrl = configUrl;
    }

    /**
     * Setter for the flag if updates auto download is allowed.
     *
     * @param isAllowed set to <code>true</code> to allow automatic update downloads.
     */
    public void allowUpdatesAutoDownload(boolean isAllowed) {
        allowUpdatesAutoDownload = isAllowed;
    }

    /**
     * Getter for the flag if updates auto download is allowed.
     * By default it is on, but you can disable it from JavaScript.
     *
     * @return <code>true</code> if automatic downloads are enabled, <code>false</code> - otherwise.
     */
    public boolean isAutoDownloadIsAllowed() {
        return allowUpdatesAutoDownload;
    }

    /**
     * Setter for the flag if updates auto installation is allowed.
     *
     * @param isAllowed set to <code>true</code> to allow automatic installation for the loaded updates.
     */
    public void allowUpdatesAutoInstall(boolean isAllowed) {
        allowUpdatesAutoInstall = isAllowed;
    }

    /**
     * Getter for the flag if updates auto installation is allowed.
     * By default it is on, but you can disable it from JavaScript.
     *
     * @return <code>true</code> if automatic installation is enabled, <code>false</code> - otherwise.
     */
    public boolean isAutoInstallIsAllowed() {
        return allowUpdatesAutoInstall;
    }

    /**
     * Setter for the flag if the initial version should be taken from the assets folder.
     *
     * @param isAllowed set to <code>true</code> to allow the usage of the initial version from the assets folder.
     */
    public void allowUseOfInitialVersionFromAssets(boolean isAllowed) {
        allowUseOfInitialVersionFromAssets = isAllowed;
    }

    /**
     * Getter for the flag if use of initial version from assets folder is allowed.
     * By default it is on, but you can disable it from JavaScript.
     *
     * @return <code>true</code> if the initial version should be taken from the assets folder, <code>false</code> - otherwise.
     */
    public boolean isUseOfInitialVersionFromAssetsAllowed() {
        return allowUseOfInitialVersionFromAssets;
    }

    /**
     * Setter for the flag if auto redirect to the local storage index page is allowed.
     *
     * @param isAllowed set to <code>true</code> to allow automatic redirection to the local storage index page.
     */
    public void allowAutoRedirectToLocalStorageIndexPage(boolean isAllowed) {
        allowAutoRedirectionToLocalStorageIndexPage = isAllowed;
    }

    /**
     * Getter for the flag if automatic redirection to the local storage index page is allowed.
     * By default it is on, but you can disable it from JavaScript.
     *
     * @return <code>true</code> if automatic redirection to the local storage index page, <code>false</code> - otherwise.
     */
    public boolean isAutoRedirectionToLocalStorageIndexPageAllowed() {
        return allowAutoRedirectionToLocalStorageIndexPage;
    }

    /**
     * Setter for the flag if a fresh install of the web-app code is required after the app is updated.
     * When set to <code>false</code> this is useful for SDKs.
     *
     * @param isRequired set to <code>true</code> to require a fresh install of the web-app code after the app is updated.
     */
    public void setRequireFreshInstallAfterAppUpdate(boolean isRequired) {
        requireFreshInstallAfterAppVersionUpdate = isRequired;
    }

    /**
     * Getter for the flag if a fresh install of the web-app code is required after the app is updated.
     * By default it is on, but you can disable it from JavaScript.
     *
     * @return <code>true</code> to require a fresh install of the web-app code after the app is updated, <code>false</code> - otherwise.
     */
    public boolean isFreshInstallAfterAppUpdateRequired() {
        return requireFreshInstallAfterAppVersionUpdate;
    }

    /**
     * Setter for the file download concurrency.
     *
     * @param value the number of concurrent file downloads.
     */
    public void setFileDownloadConcurrency(int value) {
        FileDownloader.setConcurrencyLevel(value);
    }

    /**
     * Getter for the file download concurrency.
     * By default it is 1, but you can change it from JavaScript.
     *
     * @return the number of files that will be downloaded concurrently during an update fetch.
     */
    public int getFileDownloadConcurrency() {
        return FileDownloader.getConcurrencyLevel();
    }

    /**
     * Setter for the assets remote ZIP file.
     *
     * @param value the remote URL of a ZIP that includes the assets.
     */
    public void setAssetsRemoteZipUrl(String value) {
        assetsRemoteZipUrl = value;
    }

    /**
     * Getter for the remote assets ZIP file.
     * By default it is an empty string, but you can change it from JavaScript.
     *
     * @return the remote URL of the ZIP file that includes that assets.
     */
    public String getAssetsRemoteZipUrl() {
        return assetsRemoteZipUrl;
    }

    /**
     * Load plugins specific preferences from Cordova's config.xml.
     *
     * @param context current context of the activity
     * @return hot-code-push plugin preferences
     */
    public static ChcpXmlConfig loadFromCordovaConfig(Context context) {
        ChcpXmlConfig chcpConfig = new ChcpXmlConfig();

        new ChcpXmlConfigParser().parse(context, chcpConfig);

        return chcpConfig;
    }

    /**
     * Apply and save options that has been send from web page.
     * Using this we can change plugin config from JavaScript.
     *
     * @param jsOptions options from web
     * @throws JSONException
     */
    public void mergeOptionsFromJs(JSONObject jsOptions) throws JSONException {
        if (jsOptions.has(XmlTags.CONFIG_FILE_TAG)) {
            String configUrl = jsOptions.getString(XmlTags.CONFIG_FILE_TAG);
            if (!TextUtils.isEmpty(configUrl)) {
                setConfigUrl(configUrl);
            }
        }

        if (jsOptions.has(XmlTags.AUTO_INSTALLATION_TAG)) {
            allowUpdatesAutoInstall(jsOptions.getBoolean(XmlTags.AUTO_INSTALLATION_TAG));
        }

        if (jsOptions.has(XmlTags.AUTO_DOWNLOAD_TAG)) {
            allowUpdatesAutoDownload(jsOptions.getBoolean(XmlTags.AUTO_DOWNLOAD_TAG));
        }

        if (jsOptions.has(XmlTags.USE_INITIAL_VERSION_FROM_ASSETS_TAG)) {
            allowUseOfInitialVersionFromAssets(jsOptions.getBoolean(XmlTags.USE_INITIAL_VERSION_FROM_ASSETS_TAG));
        }

        if (jsOptions.has(XmlTags.AUTO_REDIRECT_TO_LOCAL_STORAGE_INDEX_PAGE_TAG)) {
            allowAutoRedirectToLocalStorageIndexPage(jsOptions.getBoolean(XmlTags.AUTO_REDIRECT_TO_LOCAL_STORAGE_INDEX_PAGE_TAG));
        }

        if (jsOptions.has(XmlTags.REQUIRE_FRESH_INSTALL_AFTER_APP_UPDATE_TAG)) {
            setRequireFreshInstallAfterAppUpdate(jsOptions.getBoolean(XmlTags.REQUIRE_FRESH_INSTALL_AFTER_APP_UPDATE_TAG));
        }

        if (jsOptions.has(XmlTags.FILE_DOWNLOAD_CONCURRENCY_TAG)) {
            setFileDownloadConcurrency(jsOptions.getInt(XmlTags.FILE_DOWNLOAD_CONCURRENCY_TAG));
        }

        if (jsOptions.has(XmlTags.ASSETS_REMOTE_ZIP_URL_TAG)) {
            setAssetsRemoteZipUrl(jsOptions.getString(XmlTags.ASSETS_REMOTE_ZIP_URL_TAG));
        }
    }
}
