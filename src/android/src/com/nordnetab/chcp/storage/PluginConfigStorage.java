package com.nordnetab.chcp.storage;

import android.content.Context;
import android.content.SharedPreferences;

import com.nordnetab.chcp.config.PluginConfig;

/**
 * Created by Nikolay Demyankov on 28.07.15.
 */
public class PluginConfigStorage implements IConfigPreferenceStorage<PluginConfig> {

    private static final String PREF_FILE_NAME = "chcp_plugin_config_pref";
    private static final String PREF_KEY = "config_json";

    private SharedPreferences preferences;

    public PluginConfigStorage(Context context) {
        preferences = context.getSharedPreferences(PREF_FILE_NAME, 0);
    }

    @Override
    public boolean storeInPreference(PluginConfig config) {
        if (config == null) {
            return false;
        }

        preferences.edit().putString(PREF_KEY, config.toString()).apply();

        return true;
    }

    @Override
    public PluginConfig loadFromPreference() {
        final String configJson = preferences.getString(PREF_KEY, null);
        if (configJson == null) {
            return null;
        }

        return PluginConfig.fromJson(configJson);
    }
}
