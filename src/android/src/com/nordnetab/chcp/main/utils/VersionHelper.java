package com.nordnetab.chcp.main.utils;

import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

/**
 * 어플리케이션의 빌드 버전을 가져오는 Helper 클래스
 */
public class VersionHelper {

  private VersionHelper() {
  }

  /**
   * 어플리케이션 빌드 버전을 가져온다
   *
   * @param context application context
   * @return build version
   */
  public static int applicationVersionCode(final Context context) {
    int versionCode = 0;
    try {
      // Android Manifest에 있는 VersionCode attribute를 가져온다
      versionCode = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
    } catch (PackageManager.NameNotFoundException e) {
      Log.d("CHCP", "Can't get version code", e);
    }

    return versionCode;
  }

  /**
   * 어플리케이션 버전 이름을 가져온다
   *
   * @param context application context
   * @return version name
   */
  public static String applicationVersionName(final Context context) {
    String versionName = "";
    try {
      // Android Manifest에 있는 VersionName attribute를 가져온다
      versionName = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
    } catch (PackageManager.NameNotFoundException e) {
      Log.d("CHCP", "Can't get version name", e);
    }

    return versionName;
  }
}