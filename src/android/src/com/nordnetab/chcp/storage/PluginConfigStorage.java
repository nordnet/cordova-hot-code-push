package com.nordnetab.chcp.storage;

import android.content.Context;

import com.nordnetab.chcp.config.PluginConfig;

/**
 * Created by Nikolay Demyankov on 28.07.15.
 */
public class PluginConfigStorage extends Storage<PluginConfig> {

    public PluginConfigStorage(Context applicationContext) {
        super(PluginConfig.class, applicationContext, null);
    }

    @Override
    protected PluginConfig createInstance(String json) {
        return PluginConfig.fromJson(json);
    }
}
