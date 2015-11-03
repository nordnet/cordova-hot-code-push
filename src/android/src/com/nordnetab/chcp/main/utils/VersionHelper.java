package com.nordnetab.chcp.main.utils;

import android.content.Context;
import android.content.pm.PackageManager;

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
    public static int applicationVersionCode(Context context) {
        int versionCode = 0;
        try {
            versionCode = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return versionCode;
    }
}