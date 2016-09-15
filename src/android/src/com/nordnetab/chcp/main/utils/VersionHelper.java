package com.nordnetab.chcp.main.utils;

import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

/**
 * Created by Nikolay Demyankov on 30.07.15.
 * <p/>
 * Small helper to extract applications build version.
 */
public class VersionHelper {

    private VersionHelper() {
    }

    /**
     * Getter for application build version.
     *
     * @param context application context
     * @return build version
     */
    public static int applicationVersionCode(final Context context) {
        int versionCode = 0;
        try {
            versionCode = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            Log.d("CHCP", "Can't get version code", e);
        }

        return versionCode;
    }

    /**
     * Getter for application version name.
     *
     * @param context application context
     * @return version name
     */
    public static String applicationVersionName(final Context context) {
        String versionName = "";
        try {
            versionName = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            Log.d("CHCP", "Can't get version name", e);
        }

        return versionName;
    }
}