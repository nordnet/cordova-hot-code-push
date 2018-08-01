package com.nordnetab.chcp.main.utils;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import com.nordnetab.chcp.main.model.PluginFilesStructure;

import java.io.File;

/**
 * Created by Nikolay Demyankov on 29.12.15.
 * <p/>
 * Helper class to clean up file system and remove old releases folders.
 */
public class CleanUpHelper {

  private static boolean isExecuting;

  private final File rootFolder;

  /**
   * Constructor.
   *
   * @param rootFolder root folder, where releases are stored
   */
  private CleanUpHelper(final String rootFolder) {
    this.rootFolder = new File(rootFolder);
  }

  /**
   * 릴리즈 폴더 삭제
   *
   * @param context          application context
   * @param excludedReleases which releases are leave alive.
   */
  public static void removeReleaseFolders(final Context context, final String[] excludedReleases) {
    // 현재 작업을 실행 했다면
    if (isExecuting) {
      return;
    }
    isExecuting = true;

    final String rootFolder = PluginFilesStructure.getPluginRootFolder(context);

    new Thread(new Runnable() {
      @Override
      public void run() {
        new CleanUpHelper(rootFolder).removeFolders(excludedReleases);
        isExecuting = false;
      }
    }).start();
  }

  private void removeFolders(final String[] excludedReleases) {
    if (!rootFolder.exists()) {
      return;
    }

    File[] files = rootFolder.listFiles();
    for (File file : files) {
      boolean isIgnored = false;
      Log.d("CHCP", "Root Folder 내에 있는 File list : " + file.getName());

      for (String excludedReleaseName : excludedReleases) {
        Log.d("CHCP", "ExcludedRelease 파일 : " + excludedReleaseName);

        if (TextUtils.isEmpty(excludedReleaseName)) {
          continue;
        }

        if (file.getName().equals(excludedReleaseName)) {
          Log.d("CHCP", "삭제에서 제외될 파일 : " + file.getName());
          isIgnored = true;
          break;
        }
      }

      if (!isIgnored) {
        Log.d("CHCP", "이전 릴리즈 폴더 삭제 : " + file.getName());
        FilesUtility.delete(file);
      }
    }
  }

}
