package com.nordnetab.chcp.main.updater;

import android.content.Context;
import android.util.Log;
import com.nordnetab.chcp.main.config.ApplicationConfig;
import com.nordnetab.chcp.main.config.ContentManifest;
import com.nordnetab.chcp.main.events.UpdateInstallationErrorEvent;
import com.nordnetab.chcp.main.events.UpdateInstalledEvent;
import com.nordnetab.chcp.main.events.WorkerEvent;
import com.nordnetab.chcp.main.model.ChcpError;
import com.nordnetab.chcp.main.model.ManifestDiff;
import com.nordnetab.chcp.main.model.ManifestFile;
import com.nordnetab.chcp.main.model.PluginFilesStructure;
import com.nordnetab.chcp.main.storage.ApplicationConfigStorage;
import com.nordnetab.chcp.main.storage.ContentManifestStorage;
import com.nordnetab.chcp.main.storage.IObjectFileStorage;
import com.nordnetab.chcp.main.utils.FilesUtility;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Created by Nikolay Demyankov on 28.07.15.
 * <p/>
 * Worker, that implements installation logic.
 * During the installation process events are dispatched to notify the subscribers about the progress.
 * <p/>
 * Used internally.
 */
class InstallationWorker implements WorkerTask {

  private ManifestDiff manifestDiff;
  private ApplicationConfig newAppConfig;

  private PluginFilesStructure newReleaseFS;
  private PluginFilesStructure currentReleaseFS;

  private WorkerEvent resultEvent;

  /**
   * Constructor.
   *
   * @param context        application context
   * @param newVersion     version to install
   * @param currentVersion current content version
   */
  InstallationWorker(final Context context, final String newVersion, final String currentVersion) {
    // NOTE 이부분에서 백업 파일이 만들어짐
    newReleaseFS = new PluginFilesStructure(context, newVersion);
    currentReleaseFS = new PluginFilesStructure(context, currentVersion);
  }

  //이후 이부분이 수행
  @Override
  public void run() {
    // run 수행 전 init을 통해 Manifest File 비교
    Log.d("CHCP", "업데이트 Init 실행");

    if (!init()) {
      return;
    }

    // 업데이트 검증
    Log.d("CHCP", "업데이트 검증");

    if (!isUpdateValid(newReleaseFS.getDownloadFolder(), manifestDiff)) {
      // 검증 실패시 ERROR
      setResultForError(ChcpError.UPDATE_IS_INVALID);
      return;
    }

    // IMPORTANT
    // 현재 릴리즈 폴더의 내용을 new 릴리즈 폴더로 복사
    Log.d("CHCP", "현재 릴리즈 폴더의 내용을 New Release폴더로 복사");

    if (!copyFilesFromCurrentReleaseToNewRelease()) {
      setResultForError(ChcpError.FAILED_TO_COPY_FILES_FROM_PREVIOUS_RELEASE);
      return;
    }

    // Manifest 비교를 통해 새 릴리즈에서 사용하지 않는 files 삭제
    Log.d("CHCP", "Manifest 비교를 통해 사용하지 않는 File 삭제");

    deleteUnusedFiles();

    // 업데이트 설치
    boolean isInstalled = moveFilesFromInstallationFolderToWwwFolder();

    // 설치 실패시
    if (!isInstalled) {
      Log.d("CHCP", "설치 실패, 새로받은 파일들 삭제");

      cleanUpOnFailure();
      setResultForError(ChcpError.FAILED_TO_COPY_NEW_CONTENT_FILES);
      return;
    }

    // 다운로드 폴더를 지운다 (update/)
    Log.d("CHCP", "다운로드 폴더 삭제 update/");

    cleanUpOnSuccess();

    // 업데이트 성공 알람을 보낸다
    Log.d("CHCP", "업데이트 성공 이벤트 보냄");

    setSuccessResult();
  }

  /**
   * run 수행 직후 실행
   *
   * @return <code>true</code> if all initialized and ready; <code>false</code> - otherwise
   */
  private boolean init() {
    // chcp.json  Config 가져오기
    Log.d("CHCP", "Chcp.json 가져오기");

    IObjectFileStorage<ApplicationConfig> appConfigStorage = new ApplicationConfigStorage();

    // update 폴더 임시 생성
    Log.d("CHCP", "Update 임시 폴더 생성");

    newAppConfig = appConfigStorage.loadFromFolder(newReleaseFS.getDownloadFolder());
    if (newAppConfig == null) {
      setResultForError(ChcpError.LOADED_VERSION_OF_APPLICATION_CONFIG_NOT_FOUND);
      return false;
    }

    // 로컬에 있는 manifest File Path 가져오기
    Log.d("CHCP", "로컬에 있는 manifest 파일 패스 가져오기");

    IObjectFileStorage<ContentManifest> manifestStorage = new ContentManifestStorage();
    ContentManifest oldManifest = manifestStorage.loadFromFolder(currentReleaseFS.getWwwFolder());
    if (oldManifest == null) {
      setResultForError(ChcpError.LOCAL_VERSION_OF_MANIFEST_NOT_FOUND);
      return false;
    }

    // 다운로드 폴더에서 manifest File Path 가져오기
    Log.d("CHCP", "다운로드 폴더에서 manifest 파일 패스 가져오기");

    ContentManifest newManifest = manifestStorage.loadFromFolder(newReleaseFS.getDownloadFolder());
    if (newManifest == null) {
      setResultForError(ChcpError.LOADED_VERSION_OF_MANIFEST_NOT_FOUND);
      return false;
    }

    // Manifest 차이점 가져오기
    Log.d("CHCP", "manifest 차이점 비교");

    manifestDiff = oldManifest.calculateDifference(newManifest);

    return true;
  }

  /**
   * 현재 릴리즈 폴더의 모든 내용을 새로운 릴리즈 폴더로 복사
   *
   * @return <code>true</code> if files are copied; <code>false</code> - otherwise.
   */
  private boolean copyFilesFromCurrentReleaseToNewRelease() {
    boolean result = true;
    final File currentWwwFolder = new File(currentReleaseFS.getWwwFolder());
    final File newWwwFolder = new File(newReleaseFS.getWwwFolder());

    try {
      // 만약 새버전의 www folder가 존재한다면 지움
      Log.d("CHCP", "만약 새 버전의 폴더가 존재한다면 지움");

      if (newWwwFolder.exists()) {
        Log.d("CHCP", "새 버전의 폴더가 존재해서 지움");

        FilesUtility.delete(newWwwFolder);
      }

      Log.d("CHCP", "현재 버전에서 새 버전으로 파일 복사");

      FilesUtility.copy(currentWwwFolder, newWwwFolder);
    } catch (Exception e) {
      e.printStackTrace();
      result = false;
    }

    return result;
  }

  /**
   * Perform cleaning when we failed to install the update.
   */
  private void cleanUpOnFailure() {
    FilesUtility.delete(newReleaseFS.getContentFolder());
  }

  /**
   * 업데이트가 인스톨 된 뒤, 다운로드폴더를 지운다
   */
  private void cleanUpOnSuccess() {
    FilesUtility.delete(newReleaseFS.getDownloadFolder());
  }

  /**
   * Manifest 비교를 통해프로젝트에서 사용되지 않는 파일을 삭제한다
   */
  private void deleteUnusedFiles() {
    final List<ManifestFile> files = manifestDiff.deletedFiles();
    for (ManifestFile file : files) {
      File fileToDelete = new File(newReleaseFS.getWwwFolder(), file.name);
      FilesUtility.delete(fileToDelete);
    }
  }

  /**
   * 다운 받은 파일(update/~)을 새로운 www 폴더로 복사한다
   *
   * @return <code>true</code> if files are copied; <code>false</code> - otherwise
   */
  private boolean moveFilesFromInstallationFolderToWwwFolder() {
    try {
      Log.d("CHCP", "다운로드 받은 파일을 새로운 www폴더로 이동");

      FilesUtility.copy(newReleaseFS.getDownloadFolder(), newReleaseFS.getWwwFolder());

      return true;
    } catch (IOException e) {
      e.printStackTrace();

      return false;
    }
  }

  /**
   * 업데이트를 설치할 준비가 되었는지 확인
   * We will check, if all files are loaded and their hashes are correct.
   *
   * @param downloadFolderPath folder, where our files are situated
   *                           다운로드 된 파일이 있는 폴더
   * @param manifestDiff       difference between old and the new manifest. Holds information about updated files.
   *                           ManifestFile의 차이점을 담은 매개변수
   * @return <code>true</code> update is valid and we are good to go; <code>false</code> - otherwise
   */
  private boolean isUpdateValid(String downloadFolderPath, ManifestDiff manifestDiff) {
    // 다운로더 폴더 패스 ~~/update
    Log.d("CHCP", "다운로드 폴더 패스 가져오기");

    File downloadFolder = new File(downloadFolderPath);

    // 존재한다면 나가기
    if (!downloadFolder.exists()) {
      Log.d("CHCP", "다운로드 폴더 존재하므로 out");

      return false;
    }

    boolean isValid = true;
    // Manifest변경점에서 업데이트 할 파일들만 가져오기
    Log.d("CHCP", "manifest에서 변경된 파일들만 가져옴");

    List<ManifestFile> updateFileList = manifestDiff.getUpdateFiles();

    for (ManifestFile updatedFile : updateFileList) {
      // 파일 생성
      File file = new File(downloadFolder, updatedFile.name);

      try {
        if (!file.exists() ||
          !FilesUtility.calculateFileHash(file).equals(updatedFile.hash)) {
          isValid = false;
          break;
        }
      } catch (Exception e) {
        e.printStackTrace();
        isValid = false;
        break;
      }
    }

    return isValid;
  }

  // region Events

  private void setResultForError(final ChcpError error) {
    resultEvent = new UpdateInstallationErrorEvent(error, newAppConfig);
  }

  private void setSuccessResult() {
    resultEvent = new UpdateInstalledEvent(newAppConfig);
  }

  @Override
  public WorkerEvent result() {
    return resultEvent;
  }

  // endregion
}
