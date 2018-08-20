package com.nordnetab.chcp.main.config;

import android.content.Context;

import org.apache.cordova.ConfigXmlParser;
import org.xmlpull.v1.XmlPullParser;

/**
 * Cordova config.xml 파서
 * Plugin 환경설정을 읽기위해 사용된다
 *
 * @see ChcpXmlConfig
 */
class ChcpXmlConfigParser extends ConfigXmlParser {

  private ChcpXmlConfig chcpConfig;

  private boolean isInsideChcpBlock;
  private boolean didParseChcpBlock;

  /**
   * config.xml을 파싱한다
   * 결과는 ChcpXmlConfig 인스턴스에 전달된다
   *
   * @param context    current context
   * @param chcpConfig config instance to which we will set preferences from config.xml
   * @see ChcpXmlConfig
   */
  public void parse(final Context context, final ChcpXmlConfig chcpConfig) {
    this.chcpConfig = chcpConfig;

    isInsideChcpBlock = false;
    didParseChcpBlock = false;

    super.parse(context);
  }

  @Override
  public void handleStartTag(final XmlPullParser xml) {
    /* 확인할것 -> "https://github.com/nordnet/cordova-hot-code-push/wiki/Build-options" */

    // 이미 파싱한 chcp 블록이면 pass
    if (didParseChcpBlock) {
      return;
    }

    // XML tag name 가져오기
    final String name = xml.getName();

    // <chcp> 태그인 경우
    if (XmlTags.MAIN_TAG.equals(name)) {
      // chcp 내부 Block 선언
      isInsideChcpBlock = true;
      return;
    }

    // 플러그인 환경설정을 파싱하는 경우에만 진행할 것
    if (!isInsideChcpBlock) {
      return;
    }

    // 구성 파일 환경 설정 파싱
    if (XmlTags.CONFIG_FILE_TAG.equals(name)) {
      processConfigFileBlock(xml);
      return;
    }

    // 자동 다운로드 환경 설정 파싱
    if (XmlTags.AUTO_DOWNLOAD_TAG.equals(name)) {
      processAutoDownloadBlock(xml);
      return;
    }

    // 자동 설치 환경 설정 파싱
    if (XmlTags.AUTO_INSTALLATION_TAG.equals(name)) {
      processAutoInstallationBlock(xml);
      return;
    }

    // Native Navigation 인터페이스 버전 파싱
    if (XmlTags.NATIVE_INTERFACE_TAG.equals(name)) {
      processNativeInterfaceBlock(xml);
    }

    // Backup remove or not option 설정
    if (XmlTags.REMOVE_BACKUP_TAG.equals(name)) {
      // do Remove Backup process
      processRemoveBackupBlock(xml);
    }
  }

  /**
   * 백업 관련 config 세팅
   */
  private void processRemoveBackupBlock(XmlPullParser xml) {
    boolean isEnabled = xml.getAttributeValue(null, XmlTags.REMOVE_BACKUP_ENABLED_ATTRIBUTE).equals("true");

    // 백업 여부 저장
    chcpConfig.allowRemoveBackup(isEnabled);
  }

  @Override
  public void handleEndTag(final XmlPullParser xml) {
    // 이미 파싱한 chcp 블록이면 pass
    if (didParseChcpBlock) {
      return;
    }

    final String name = xml.getName();

    // <chcp> 태그인 경우
    if (XmlTags.MAIN_TAG.equals(name)) {
      // 파싱 완료 처리
      didParseChcpBlock = true;
      isInsideChcpBlock = false;
    }
  }

  private void processConfigFileBlock(final XmlPullParser xml) {
    // "url" = "serverURL/chcp.json"
    String configUrl = xml.getAttributeValue(null, XmlTags.CONFIG_FILE_URL_ATTRIBUTE);

    // chcp setting에 chcp.json이 위치한 url을 넣는다
    chcpConfig.setConfigUrl(configUrl);
  }

  private void processAutoDownloadBlock(final XmlPullParser xml) {
    // "enable": "true" or "false"
    boolean isEnabled = xml.getAttributeValue(null, XmlTags.AUTO_DOWNLOAD_ENABLED_ATTRIBUTE).equals("true");

    // 자동 다운로드 여부(true or false)
    chcpConfig.allowUpdatesAutoDownload(isEnabled);
  }

  private void processAutoInstallationBlock(final XmlPullParser xml) {
    // "enable": "true" or "false"
    boolean isEnabled = xml.getAttributeValue(null, XmlTags.AUTO_INSTALLATION_ENABLED_ATTRIBUTE).equals("true");

    // 자동 설치 여부(true or false)
    chcpConfig.allowUpdatesAutoInstall(isEnabled);
  }

  private void processNativeInterfaceBlock(final XmlPullParser xml) {
    final String nativeVersionStr = xml.getAttributeValue(null, XmlTags.NATIVE_INTERFACE_VERSION_ATTRIBUTE);
    final int nativeVersion = Integer.parseInt(nativeVersionStr);

    // 1이 기본값
    chcpConfig.setNativeInterfaceVersion(nativeVersion);
  }
}
