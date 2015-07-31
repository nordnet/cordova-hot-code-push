package com.nordnetab.chcp.utils;

import android.content.Context;
import android.content.pm.PackageManager;

/**
 * Created by Nikolay Demyankov on 30.07.15.
 */
public class VersionHelper {

    private VersionHelper() {
    }

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
