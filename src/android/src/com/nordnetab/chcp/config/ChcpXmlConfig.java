package com.nordnetab.chcp.config;

import android.content.Context;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Nikolay Demyankov on 06.08.15.
 */
public class ChcpXmlConfig {

    public static class DevelopmentOptions {
        private boolean enabled;

        public DevelopmentOptions() {
            enabled = false;
        }

        public boolean isEnabled() {
            return enabled;
        }

        void setEnabled(boolean isEnabled) {
            enabled = isEnabled;
        }
    }

    private String configUrl;
    private DevelopmentOptions developmentOptions;

    private ChcpXmlConfig(){
        configUrl = "";
        developmentOptions = new DevelopmentOptions();
    }

    public String getConfigUrl() {
        return configUrl;
    }

    void setConfigUrl(String configUrl) {
        this.configUrl = configUrl;
    }

    public DevelopmentOptions getDevelopmentOptions() {
        return developmentOptions;
    }

    void setDevelopmentOptions(DevelopmentOptions devOptions) {
        developmentOptions = devOptions;
    }

    public static ChcpXmlConfig parse(Context context) {
        ChcpXmlConfig chcpConfig = new ChcpXmlConfig();

        new ChcpXmlConfigParser().parse(context, chcpConfig);

        return chcpConfig;
    }
}
