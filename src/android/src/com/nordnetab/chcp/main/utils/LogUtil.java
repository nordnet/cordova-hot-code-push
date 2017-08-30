package com.nordnetab.chcp.main.utils;

import android.os.Build;
import android.util.Log;

/**
 * Created by CI on 2017-08-30.
 */

public class LogUtil {

  public static void Debug(String tag, String msg) {
    // 4.4 이하의 경우
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
      System.out.println(tag + " - " + msg);
    } else {
      Log.d(tag, msg);
    }
  }

  public static void Debug(String tag, String msg, Throwable tr) {
    Log.d(tag, msg, tr);
  }
}
