package com.nordnetab.chcp.main.updater;

import android.content.Context;
import android.util.Log;
import com.nordnetab.chcp.main.events.BeforeInstallEvent;
import com.nordnetab.chcp.main.events.NothingToInstallEvent;
import com.nordnetab.chcp.main.model.ChcpError;
import com.nordnetab.chcp.main.model.PluginFilesStructure;
import org.greenrobot.eventbus.EventBus;

import java.io.File;

/**
 * Created by Nikolay Demyankov on 22.07.15.
 * <p/>
 * Utility class to perform update installation.
 */
public class UpdatesInstaller {

  private static boolean isInstalling;

  /**
   * 현재 설치중인지 확인
   *
   * @return <code>true</code> - installation is in progress; <code>false</code> - otherwise
   */
  public static boolean isInstalling() {
    return isInstalling;
  }

  /**
   * 업데이트 실행 요청 부분
   * 인스톨은 background에서 수행됨. Events are dispatched to notify us about the result.
   * <p>
   * BACKUP을 지우기 위해선 이부분을 손 봐야 함
   *
   * @param context        application context
   * @param newVersion     version to install
   * @param currentVersion current content version
   * @return <code>ChcpError.NONE</code> if installation started; otherwise - error details
   * @see NothingToInstallEvent
   * @see com.nordnetab.chcp.main.events.UpdateInstallationErrorEvent
   * @see com.nordnetab.chcp.main.events.UpdateInstalledEvent
   */
  public static ChcpError install(final Context context, final String newVersion, final String currentVersion) {
    // 현재 인스톨중이면 나가기
    if (isInstalling) {
      return ChcpError.INSTALLATION_ALREADY_IN_PROGRESS;
    }

    // 업데이트 로드 중이면 나가기
    if (UpdatesLoader.isExecuting()) {
      return ChcpError.CANT_INSTALL_WHILE_DOWNLOAD_IN_PROGRESS;
    }

    // 다운로드 폴더가 존재하지만 설치할 내용이 없다면
    final PluginFilesStructure newReleaseFS = new PluginFilesStructure(context, newVersion);
    if (!new File(newReleaseFS.getDownloadFolder()).exists() || newVersion.equals(currentVersion)) {
      return ChcpError.NOTHING_TO_INSTALL;
    }

    // 설치작업 전 이벤트 등록
    dispatchBeforeInstallEvent();

    Log.d("CHCP", "업데이트 Task 작성");
    // 설치 워커 등록
    final WorkerTask task = new InstallationWorker(context, newVersion, currentVersion);

    Log.d("CHCP", "업데이트 Task 실행");

    // 설치 실행
    execute(task);

    return ChcpError.NONE;
  }

  private static void execute(final WorkerTask task) {
    isInstalling = true;
    new Thread(new Runnable() {
      @Override
      public void run() {
        //InstallationWorker의 run이 수행
        task.run();
        isInstalling = false;

        // dispatch resulting event
        EventBus.getDefault().post(task.result());
      }
    }).start();
  }

  private static void dispatchBeforeInstallEvent() {
    EventBus.getDefault().post(new BeforeInstallEvent());
  }
}
