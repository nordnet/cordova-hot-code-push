package com.nordnetab.chcp.main.storage;

import android.content.Context;
import android.content.SharedPreferences;

import com.nordnetab.chcp.main.config.PluginInternalPreferences;

/**
 * Created by Nikolay Demyankov on 28.07.15.
 * <p/>
 * Utility class to store plugin internal preferences in shared preferences
 *
 * @see PluginInternalPreferences
 * @see IObjectPreferenceStorage
 * @see SharedPreferences
 */
public class PluginInternalPreferencesStorage implements IObjectPreferenceStorage<PluginInternalPreferences> {

    private static final String PREF_FILE_NAME = "chcp_plugin_config_pref";
    private static final String PREF_KEY = "config_json";

    private SharedPreferences preferences;

    /**
     * Class constructor
     *
     * @param context application context
     */
    public PluginInternalPreferencesStorage(Context context) {
        preferences = context.getSharedPreferences(PREF_FILE_NAME, 0);
    }

    @Override
    public boolean storeInPreference(PluginInternalPreferences config) {
        if (config == null) {
            return false;
        }

        preferences.edit().putString(PREF_KEY, config.toString()).apply();

        return true;
    }

    @Override
    public PluginInternalPreferences loadFromPreference() {
        final String configJson = preferences.getString(PREF_KEY, null);
        if (configJson == null) {
            return null;
        }

        return PluginInternalPreferences.fromJson(configJson);
    }
}