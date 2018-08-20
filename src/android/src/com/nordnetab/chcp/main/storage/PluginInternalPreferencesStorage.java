package com.nordnetab.chcp.main.storage;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import com.nordnetab.chcp.main.config.PluginInternalPreferences;

/**
 * Created by Nikolay Demyankov on 28.07.15.
 * <p/>
 * 플러그인 내부 환경설정을 shared preferences에 저장하는 유틸리티 클래스
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
    // chcp_plugin_config 가져오기
    preferences = context.getSharedPreferences(PREF_FILE_NAME, 0);
  }

  @Override
  public boolean storeInPreference(PluginInternalPreferences config) {
    if (config == null) {
      return false;
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
      preferences.edit().putString(PREF_KEY, config.toString()).apply();
    }

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