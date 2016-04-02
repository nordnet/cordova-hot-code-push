package com.nordnetab.chcp.main.config;

import android.content.Context;
import android.text.TextUtils;

import org.apache.cordova.ConfigXmlParser;
import org.xmlpull.v1.XmlPullParser;

/**
 * Created by Nikolay Demyankov on 06.08.15.
 * <p/>
 * XML parser for Cordova's config.xml.
 * Used to read plugin specific preferences.
 *
 * @see ChcpXmlConfig
 */
class ChcpXmlConfigParser extends ConfigXmlParser {

    private ChcpXmlConfig chcpConfig;

    private boolean isInsideChcpBlock;
    private boolean didParseChcpBlock;

    /**
     * Parse config.xml.
     * Result is set into passed ChcpXmlConfig instance.
     *
     * @param context    current context
     * @param chcpConfig config instance to which we will set preferences from config.xml
     * @see ChcpXmlConfig
     */
    public void parse(Context context, ChcpXmlConfig chcpConfig) {
        this.chcpConfig = chcpConfig;

        isInsideChcpBlock = false;
        didParseChcpBlock = false;

        super.parse(context);
    }

    @Override
    public void handleStartTag(XmlPullParser xml) {
        if (didParseChcpBlock) {
          return;
        }

        final String name = xml.getName();
        if (name.equals(XmlTags.MAIN_TAG)) {
            isInsideChcpBlock = true;
            return;
        }

        // proceed only if we are parsing our plugin preferences
        if (!isInsideChcpBlock) {
            return;
        }

        // parse configuration file preference
        if (name.equals(XmlTags.CONFIG_FILE_TAG)) {
            processConfigFileBlock(xml);
            return;
        }

        // parse auto download preference
        if (name.equals(XmlTags.AUTO_DOWNLOAD_TAG)) {
            processAutoDownloadBlock(xml);
            return;
        }

        // parse auto installation preference
        if (name.equals(XmlTags.AUTO_INSTALLATION_TAG)) {
            processAutoInstallationBlock(xml);
            return;
        }

        // parse use initial version from assets preference
        if (name.equals(XmlTags.USE_INITIAL_VERSION_FROM_ASSETS_TAG)) {
            processUseInitialVersionFromAssetsBlock(xml);
            return;
        }

        // parse auto redirect to start page preference
        if (name.equals(XmlTags.AUTO_REDIRECT_TO_LOCAL_STORAGE_INDEX_PAGE_TAG)) {
            processAutoRedirectToLocalStorageIndexPageBlock(xml);
            return;
        }

        // parse require fresh install after app update
        if (name.equals(XmlTags.REQUIRE_FRESH_INSTALL_AFTER_APP_UPDATE_TAG)) {
            processRequireFreshInstallAfterAppUpdate(xml);
            return;
        }

        // parse require file download concurrency
        if (name.equals(XmlTags.FILE_DOWNLOAD_CONCURRENCY_TAG)) {
            processFileDownloadConcurrency(xml);
            return;
        }

        // parse the assets remote zip url
        if (name.equals(XmlTags.ASSETS_REMOTE_ZIP_URL_TAG)) {
            processAssetsRemoteZipUrl(xml);
            return;
        }
    }

    private void processConfigFileBlock(XmlPullParser xml) {
        String configUrl = xml.getAttributeValue(null, XmlTags.CONFIG_FILE_URL_ATTRIBUTE);

        chcpConfig.setConfigUrl(configUrl);
    }

    private void processAutoDownloadBlock(XmlPullParser xml) {
        boolean isEnabled = xml.getAttributeValue(null, XmlTags.AUTO_DOWNLOAD_ENABLED_ATTRIBUTE).equals("true");
        chcpConfig.allowUpdatesAutoDownload(isEnabled);
    }

    private void processAutoInstallationBlock(XmlPullParser xml) {
        boolean isEnabled = xml.getAttributeValue(null, XmlTags.AUTO_INSTALLATION_ENABLED_ATTRIBUTE).equals("true");
        chcpConfig.allowUpdatesAutoInstall(isEnabled);
    }

    private void processUseInitialVersionFromAssetsBlock(XmlPullParser xml) {
        boolean isEnabled = xml.getAttributeValue(null, XmlTags.USE_INITIAL_VERSION_FROM_ASSETS_ENABLED_ATTRIBUTE).equals("true");
        chcpConfig.allowUseOfInitialVersionFromAssets(isEnabled);
    }

    private void processAutoRedirectToLocalStorageIndexPageBlock(XmlPullParser xml) {
        boolean isEnabled = xml.getAttributeValue(null, XmlTags.AUTO_REDIRECT_TO_LOCAL_STORAGE_INDEX_PAGE_ENABLED_ATTRIBUTE).equals("true");
        chcpConfig.allowAutoRedirectToLocalStorageIndexPage(isEnabled);
    }

    private void processRequireFreshInstallAfterAppUpdate(XmlPullParser xml) {
        boolean isEnabled = xml.getAttributeValue(null, XmlTags.REQUIRE_FRESH_INSTALL_AFTER_APP_UPDATE_ENABLED_ATTRIBUTE).equals("true");
        chcpConfig.setRequireFreshInstallAfterAppUpdate(isEnabled);
    }

    private void processFileDownloadConcurrency(XmlPullParser xml) {
        String attributeValue = xml.getAttributeValue(null, XmlTags.FILE_DOWNLOAD_CONCURRENCY_VALUE_ATTRIBUTE);
        if(!TextUtils.isEmpty(attributeValue)) {
            int integerValue = Integer.parseInt(attributeValue);
            chcpConfig.setFileDownloadConcurrency(integerValue);
        }
    }

    private void processAssetsRemoteZipUrl(XmlPullParser xml) {
        String attributeValue = xml.getAttributeValue(null, XmlTags.ASSETSE_REMOTE_ZIP_URL_VALUE_ATTRIBUTE);
        chcpConfig.setAssetsRemoteZipUrl(attributeValue);
    }

    @Override
    public void handleEndTag(XmlPullParser xml) {
        if (didParseChcpBlock) {
            return;
        }

        String name = xml.getName();
        if (name.equals(XmlTags.MAIN_TAG)) {
            didParseChcpBlock = true;
            isInsideChcpBlock = false;
        }
    }
}
