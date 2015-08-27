package com.nordnetab.chcp.config;

import android.content.Context;

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

    // plugin specific xml keys and attributes in config.xml
    private static class Keys {

        public static final String MAIN_TAG = "chcp";

        // keys for application config file url
        public static final String CONFIG_FILE_TAG = "config-file";
        public static final String CONFIG_FILE_URL_ATTRIBUTE = "url";

        // keys for local development
        public static final String LOCAL_DEVELOPMENT_TAG = "local-development";
        public static final String LOCAL_DEVELOPMENT_ENABLED_ATTRIBUTE = "enabled";
    }

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
        String name = xml.getName();
        if (name.equals(Keys.MAIN_TAG)) {
            isInsideChcpBlock = true;
            return;
        }

        if (!isInsideChcpBlock) {
            return;
        }

        if (name.equals(Keys.LOCAL_DEVELOPMENT_TAG)) {
            boolean isDevModeEnabled = xml.getAttributeValue(null, Keys.LOCAL_DEVELOPMENT_ENABLED_ATTRIBUTE).equals("true");
            chcpConfig.getDevelopmentOptions().setEnabled(isDevModeEnabled);

            return;
        }

        if (name.equals(Keys.CONFIG_FILE_TAG)) {
            processConfigFileBlock(xml);
        }
    }

    private void processConfigFileBlock(XmlPullParser xml) {
        String configUrl = xml.getAttributeValue(null, Keys.CONFIG_FILE_URL_ATTRIBUTE);

        chcpConfig.setConfigUrl(configUrl);
    }

    @Override
    public void handleEndTag(XmlPullParser xml) {
        if (didParseChcpBlock) {
            return;
        }

        String name = xml.getName();
        if (name.equals(Keys.MAIN_TAG)) {
            didParseChcpBlock = true;
            isInsideChcpBlock = false;
        }
    }
}