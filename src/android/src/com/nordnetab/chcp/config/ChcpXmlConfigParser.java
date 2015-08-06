package com.nordnetab.chcp.config;

import android.content.Context;

import org.apache.cordova.ConfigXmlParser;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

/**
 * Created by Nikolay Demyankov on 06.08.15.
 */

class ChcpXmlConfigParser extends ConfigXmlParser {

    private static class Keys {
        public static final String MAIN_TAG = "chcp";

        public static final String CONFIG_FILE_TAG = "config-file";
        public static final String CONFIG_FILE_URL_ATTRIBUTE = "url";

        public static final String LOCAL_DEVELOPMENT_TAG = "local-development";
        public static final String LOCAL_DEVELOPMENT_ENABLED_ATTRIBUTE = "enabled";

        public static final String INJECT_JS_CODE_TAG = "inject-js-code";
        public static final String INJECT_JS_SCRIPT_TAG = "inject-js-script";
        public static final String INJECT_JS_SCRIPT_PATH_ATTRIBUTE = "path";
    }

    private ChcpXmlConfig chcpConfig;

    private boolean isInsideChcpBlock;
    private boolean didParseChcpBlock;
    private boolean isInsideDevelopmentBlock;
    private boolean isInsideInjectJsCodeBlock;

    public void parse(Context action, ChcpXmlConfig chcpConfig) {
        this.chcpConfig = chcpConfig;

        isInsideChcpBlock = false;
        didParseChcpBlock = false;
        isInsideDevelopmentBlock = false;

        super.parse(action);
    }

    @Override
    public void parse(XmlPullParser xml) {
        int eventType = -1;

        while (eventType != XmlPullParser.END_DOCUMENT && !didParseChcpBlock) {
            if (eventType == XmlPullParser.START_TAG) {
                handleStartTag(xml);
            } else if (eventType == XmlPullParser.END_TAG) {
                handleEndTag(xml);
            } else if (eventType == XmlPullParser.TEXT) {
                handleText(xml);
            }

            try {
                eventType = xml.next();
            } catch (XmlPullParserException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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
            isInsideDevelopmentBlock = true;
            boolean isDevModeEnabled = xml.getAttributeValue(null, Keys.LOCAL_DEVELOPMENT_ENABLED_ATTRIBUTE).equals("true");
            chcpConfig.getDevelopmentOptions().setEnabled(isDevModeEnabled);

            return;
        }

        if (isInsideDevelopmentBlock) {
            try {
                processDevelopmentBlock(xml);
            } catch (XmlPullParserException e) {
                e.printStackTrace();
            }

            return;
        }

        if (name.equals(Keys.CONFIG_FILE_TAG)) {
            processConfigFileBlock(xml);
        }
    }

    public void handleText(XmlPullParser xml) {
        if (isInsideInjectJsCodeBlock) {
            String jsCode = xml.getText();
            chcpConfig.getDevelopmentOptions().getJsCodeForInjection().add(jsCode);
        }
    }

    private void processDevelopmentBlock(XmlPullParser xml) throws XmlPullParserException {
        String name = xml.getName();
        if (name.equals(Keys.INJECT_JS_CODE_TAG)) {
            isInsideInjectJsCodeBlock = true;
            return;
        }

        if (name.equals(Keys.INJECT_JS_SCRIPT_TAG)) {
            String jsScript = xml.getAttributeValue(null, Keys.INJECT_JS_SCRIPT_PATH_ATTRIBUTE);
            chcpConfig.getDevelopmentOptions().getJsScriptsForInjection().add(jsScript);
        }
    }

    private void processConfigFileBlock(XmlPullParser xml) {
        String configUrl = xml.getAttributeValue(null, Keys.CONFIG_FILE_URL_ATTRIBUTE);

        chcpConfig.setConfigUrl(configUrl);
    }

    @Override
    public void handleEndTag(XmlPullParser xml) {
        String name = xml.getName();
        if (name.equals(Keys.MAIN_TAG)) {
            didParseChcpBlock = true;
            isInsideChcpBlock = false;
            return;
        }

        if (name.equals(Keys.LOCAL_DEVELOPMENT_TAG)) {
            isInsideDevelopmentBlock = false;
            return;
        }

        if (name.equals(Keys.INJECT_JS_CODE_TAG)) {
            isInsideInjectJsCodeBlock = false;
        }
    }
}