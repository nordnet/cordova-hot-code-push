package com.nordnetab.chcp.main;

import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import com.nordnetab.chcp.main.config.*;
import com.nordnetab.chcp.main.events.*;
import com.nordnetab.chcp.main.js.JSAction;
import com.nordnetab.chcp.main.js.PluginResultHelper;
import com.nordnetab.chcp.main.model.ChcpError;
import com.nordnetab.chcp.main.model.PluginFilesStructure;
import com.nordnetab.chcp.main.model.UpdateTime;
import com.nordnetab.chcp.main.storage.ApplicationConfigStorage;
import com.nordnetab.chcp.main.storage.IObjectFileStorage;
import com.nordnetab.chcp.main.storage.IObjectPreferenceStorage;
import com.nordnetab.chcp.main.storage.PluginInternalPreferencesStorage;
import com.nordnetab.chcp.main.updater.UpdateDownloadRequest;
import com.nordnetab.chcp.main.updater.UpdatesInstaller;
import com.nordnetab.chcp.main.updater.UpdatesLoader;
import com.nordnetab.chcp.main.utils.AssetsHelper;
import com.nordnetab.chcp.main.utils.CleanUpHelper;
import com.nordnetab.chcp.main.utils.Paths;
import com.nordnetab.chcp.main.utils.VersionHelper;
import com.nordnetab.chcp.main.view.AppUpdateRequestDialog;
import org.apache.cordova.*;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Nikolay Demyankov on 23.07.15.
 * Plugin main class.
 */
public class HotCodePushPlugin extends CordovaPlugin {

  private static final String FILE_PREFIX = "file://";
  private static final String WWW_FOLDER = "www";
  private static final String LOCAL_ASSETS_FOLDER = "file:///android_asset/www";

  private String startingPage;
  private IObjectFileStorage<ApplicationConfig> appConfigStorage;
  private PluginInternalPreferences pluginInternalPrefs;
  private IObjectPreferenceStorage<PluginInternalPreferences> pluginInternalPrefsStorage;
  private ChcpXmlConfig chcpXmlConfig;
  private PluginFilesStructure fileStructure;

  private CallbackContext installJsCallback;
  private CallbackContext jsDefaultCallback;
  private CallbackContext downloadJsCallback;

  private Handler handler;
  private boolean isPluginReadyForWork;
  private boolean dontReloadOnStart;

  private List<PluginResult> defaultCallbackStoredResults;
  private FetchUpdateOptions defaultFetchUpdateOptions;
  private boolean isSuccessUpdated = false;

  // region Plugin lifecycle

  /**
   * 플러그인 생성 후 호출되고 초기화한다
   */
  @Override
  public void initialize(final CordovaInterface cordova, final CordovaWebView webView) {
    super.initialize(cordova, webView);
    Log.d("CHCP", "onLoad로 인한 init");

    // Cordova config.xml을 파싱함
    parseCordovaConfigXml();

    // 내부 환경설정 로드. PluginInternalPrefs에 저장
    loadPluginInternalPreferences();

    Log.d("CHCP", "현재 실행되는 릴리즈 버전 " + pluginInternalPrefs.getCurrentReleaseVersionName());

    // 릴리즈 폴더가 저장되는 디렉토리에서 불필요한 릴리즈 폴더를 삭제함
    cleanupFileSystemFromOldReleases(chcpXmlConfig);

    handler = new Handler();
    fileStructure = new PluginFilesStructure(cordova.getActivity(), pluginInternalPrefs.getCurrentReleaseVersionName());

    //chcp.json
    appConfigStorage = new ApplicationConfigStorage();

    defaultCallbackStoredResults = new ArrayList<PluginResult>();
  }

  /**
   * 액티비티가 사용자에게 보여줬을 경우 호출 됨.
   */
  @Override
  public void onStart() {
    super.onStart();

    Log.d("CHCP", "onStart 액티비티 호출 시점");

    final EventBus eventBus = EventBus.getDefault();
    if (!eventBus.isRegistered(this)) {
      eventBus.register(this);
    }

    // 패키징된 www폴더를 외부 저장소에 설치하는 과정
    // 만약 설치가 안되어 있다면
    isPluginReadyForWork = isPluginReadyForWork();
    if (!isPluginReadyForWork) {
      dontReloadOnStart = true;

      Log.d("CHCP", "wwwFolder 설치");
      // wwwfolder 설치
      installWwwFolder();
      return;
    }
    Log.d("CHCP", "wwwFolder 설치 되었음");


    // 로컬 저장소에있는 경우에만 리로드 한다
    if (!dontReloadOnStart) {
      dontReloadOnStart = true;
      redirectToLocalStorageIndexPage();
    }

    // udpate를 설치한다
    Log.d("CHCP", "업데이트 설치 확인");
    Log.d("CHCP", "자동설치 허용 : " + chcpXmlConfig.isAutoInstallIsAllowed());
    Log.d("CHCP", "설치진행여부 : " + UpdatesInstaller.isInstalling());
    Log.d("CHCP", "업데이트 다운로드여부 : " + UpdatesLoader.isExecuting());
    Log.d("CHCP", "내부설정 : " + pluginInternalPrefs.toString());

    if (chcpXmlConfig.isAutoInstallIsAllowed() &&
      !UpdatesInstaller.isInstalling() &&
      !UpdatesLoader.isExecuting() &&
      !TextUtils.isEmpty(pluginInternalPrefs.getReadyForInstallationReleaseVersionName())) {
      Log.d("CHCP", "업데이트 설치 수행");

      installUpdate(null);
    }
  }

  /**
   * 어플리케이션이 Foreground로 넘어올 경우 수행됨
   */
  @Override
  public void onResume(boolean multitasking) {
    super.onResume(multitasking);

    if (!isPluginReadyForWork) {
      return;
    }

    if (!chcpXmlConfig.isAutoInstallIsAllowed() ||
      UpdatesInstaller.isInstalling() ||
      UpdatesLoader.isExecuting() ||
      TextUtils.isEmpty(pluginInternalPrefs.getReadyForInstallationReleaseVersionName())) {
      return;
    }

    final PluginFilesStructure fs = new PluginFilesStructure(cordova.getActivity(), pluginInternalPrefs.getReadyForInstallationReleaseVersionName());
    final ApplicationConfig appConfig = appConfigStorage.loadFromFolder(fs.getDownloadFolder());
    if (appConfig == null) {
      return;
    }

    final UpdateTime updateTime = appConfig.getContentConfig().getUpdateTime();
    if (updateTime == UpdateTime.ON_RESUME || updateTime == UpdateTime.NOW) {
      installUpdate(null);
    }
  }

  @Override
  public void onStop() {
    EventBus.getDefault().unregister(this);

    super.onStop();
  }

  // endregion

  // region Plugin 외부 프로퍼티 external properties

  /**
   * Setter for default fetch update options.
   * If this one is defined and no options has come form JS side - we use them.
   * If preferences came from JS side - we ignore the default ones.
   *
   * @param options options
   */
  public void setDefaultFetchUpdateOptions(final FetchUpdateOptions options) {
    this.defaultFetchUpdateOptions = options;
  }

  /**
   * Getter for currently used default fetch update options.
   *
   * @return default fetch options
   */
  public FetchUpdateOptions getDefaultFetchUpdateOptions() {
    return defaultFetchUpdateOptions;
  }

  // endregion

  // region Config loaders and initialization

  /**
   * cordova config.xml에서 chcp 환경설정을 읽는다
   *
   * @see ChcpXmlConfig
   */
  private void parseCordovaConfigXml() {
    // 초기 init시에는 null이므로 pass
    if (chcpXmlConfig != null) {
      return;
    }
    // 읽어온 ConfigXml을 읽어와 저장한다
    chcpXmlConfig = ChcpXmlConfig.loadFromCordovaConfig(cordova.getActivity());
  }

  /**
   * 플러그인 내부 환경설정을 로딩
   *
   * @see PluginInternalPreferences
   * @see PluginInternalPreferencesStorage
   */
  private void loadPluginInternalPreferences() {
    if (pluginInternalPrefs != null) {
      return;
    }

    pluginInternalPrefsStorage = new PluginInternalPreferencesStorage(cordova.getActivity());
    // 환경설정 가져오기, 없으면 null
    PluginInternalPreferences config = pluginInternalPrefsStorage.loadFromPreference();

    // 환경설정이 없거나 현재 릴리즈 버전명이 없으면
    if (config == null || TextUtils.isEmpty(config.getCurrentReleaseVersionName())) {
      // 디폴트 환경설정 생성
      Log.d("CHCP", "디폴트 환경설정 생성");
      config = PluginInternalPreferences.createDefault(cordova.getActivity());
      // sharedPreference에 저장
      pluginInternalPrefsStorage.storeInPreference(config);
    }
    pluginInternalPrefs = config;
  }

  // endregion

  // region JavaScript processing

  /**
   * js bridge를 통해 넘어오는 Action에 따라 Native 처리를 분기해준다
   *
   * @param action          js에서 넘어오는 event
   * @param args            js에서 넘어오는 매개변수
   * @param callbackContext js로 넘어갈 Callback 함수
   * @return boolean
   * @throws JSONException
   */
  @Override
  public boolean execute(String action, CordovaArgs args, CallbackContext callbackContext) throws JSONException {
    boolean cmdProcessed = true;
    Log.d("CHCP", "execute 호출 : " + action);
    // Cordova Ready가 되면 exec로 Init 호출
    if (JSAction.INIT.equals(action)) {
      jsInit(callbackContext);
    } else if (JSAction.FETCH_UPDATE.equals(action)) {
      jsFetchUpdate(callbackContext, args);
    } else if (JSAction.INSTALL_UPDATE.equals(action)) {
      jsInstallUpdate(callbackContext);
    } else if (JSAction.CONFIGURE.equals(action)) {
      jsSetPluginOptions(args, callbackContext);
    } else if (JSAction.REQUEST_APP_UPDATE.equals(action)) {
      jsRequestAppUpdate(args, callbackContext);
    } else if (JSAction.IS_UPDATE_AVAILABLE_FOR_INSTALLATION.equals(action)) {
      jsIsUpdateAvailableForInstallation(callbackContext);
    } else if (JSAction.GET_VERSION_INFO.equals(action)) {
      jsGetVersionInfo(callbackContext);
    } else {
      cmdProcessed = false;
    }

    return cmdProcessed;
  }

  /**
   * Send message to default plugin callback.
   * Default callback - is a callback that we receive on initialization (device ready).
   * Through it we are broadcasting different events.
   * <p/>
   * If callback is not set yet - message will be stored until it is initialized.
   *
   * @param message message to send to web side
   * @return true if message was sent; false - otherwise
   */
  private boolean sendMessageToDefaultCallback(final PluginResult message) {
    if (jsDefaultCallback == null) {
      defaultCallbackStoredResults.add(message);
      return false;
    }

    message.setKeepCallback(true);
    jsDefaultCallback.sendPluginResult(message);

    return true;
  }

  /**
   * 기본 콜백에 대해 저장된 이벤트를 디스패치 한다.
   */
  private void dispatchDefaultCallbackStoredResults() {
    if (defaultCallbackStoredResults.size() == 0 || jsDefaultCallback == null) {
      return;
    }

    for (PluginResult result : defaultCallbackStoredResults) {
      sendMessageToDefaultCallback(result);
    }

    defaultCallbackStoredResults.clear();
  }

  /**
   * Initialize default callback, received from the web side.
   *
   * @param callback callback to use for events broadcasting
   */
  private void jsInit(CallbackContext callback) {
    // callback을 저장해뒀다가, 필요한 경우 return함
    jsDefaultCallback = callback;
    dispatchDefaultCallbackStoredResults();

    /*
      web history 삭제
      어플리케이션 시작시, 사용자를 외부 저장소에 있는 index.html로 redirection시키므로 필요한 경우가 있다.
      사용자가 뒤로가기 버튼 등, 임의의 행동을 할 경우 우리가 원하지 않는 경로도 돌아가게 될 수 있다
     */
    handler.post(new Runnable() {
      @Override
      public void run() {
        webView.clearHistory();
      }
    });

    // 초기화 될 때 update 가져오기.
    if (chcpXmlConfig.isAutoDownloadIsAllowed() &&
      !UpdatesInstaller.isInstalling() && !UpdatesLoader.isExecuting()) {
      fetchUpdate();
    }
  }

  /**
   * Check for update.
   * Method is called from JS side.
   *
   * @param callback js callback
   */
  private void jsFetchUpdate(CallbackContext callback, CordovaArgs args) {
    if (!isPluginReadyForWork) {
      sendPluginNotReadyToWork(UpdateDownloadErrorEvent.EVENT_NAME, callback);
      return;
    }

    FetchUpdateOptions fetchOptions = null;
    try {
      fetchOptions = new FetchUpdateOptions(args.optJSONObject(0));
    } catch (JSONException ignored) {
    }

    Log.d("CHCP", "업데이트 시작");

    fetchUpdate(callback, fetchOptions);
  }

  /**
   * Install the update.
   * Method is called from JS side.
   *
   * @param callback js callback
   */
  private void jsInstallUpdate(CallbackContext callback) {
    if (!isPluginReadyForWork) {
      sendPluginNotReadyToWork(UpdateInstallationErrorEvent.EVENT_NAME, callback);
      return;
    }

    installUpdate(callback);
  }

  /**
   * Send to JS side event with message, that plugin is installing assets on the external storage and not yet ready for work.
   * That happens only on the first launch.
   *
   * @param eventName event name, that is send to JS side
   * @param callback  JS callback
   */
  private void sendPluginNotReadyToWork(String eventName, CallbackContext callback) {
    PluginResult pluginResult = PluginResultHelper.createPluginResult(eventName, null, ChcpError.ASSETS_FOLDER_IN_NOT_YET_INSTALLED);
    callback.sendPluginResult(pluginResult);
  }

  /**
   * Set plugin options. Method is called from JavaScript.
   *
   * @param arguments arguments from JavaScript
   * @param callback  callback where to send result
   */
  @Deprecated
  private void jsSetPluginOptions(CordovaArgs arguments, CallbackContext callback) {
    if (!isPluginReadyForWork) {
      sendPluginNotReadyToWork("", callback);
      return;
    }

    try {
      JSONObject jsonObject = (JSONObject) arguments.get(0);
      chcpXmlConfig.mergeOptionsFromJs(jsonObject);
      // TODO: store them somewhere?
    } catch (JSONException e) {
      Log.d("CHCP", "Failed to process plugin options, received from JS.", e);
    }

    callback.success();
  }

  /**
   * Show dialog with request to update the application through the Google Play.
   *
   * @param arguments arguments from JavaScript
   * @param callback  callback where to send result
   */
  private void jsRequestAppUpdate(final CordovaArgs arguments, final CallbackContext callback) {
    if (!isPluginReadyForWork) {
      sendPluginNotReadyToWork("", callback);
      return;
    }

    String msg = null;
    try {
      msg = (String) arguments.get(0);
    } catch (JSONException e) {
      Log.d("CHCP", "Dialog message is not set", e);

    }

    if (TextUtils.isEmpty(msg)) {
      return;
    }

    final String storeURL = appConfigStorage.loadFromFolder(fileStructure.getWwwFolder()).getStoreUrl();

    new AppUpdateRequestDialog(cordova.getActivity(), msg, storeURL, callback).show();
  }

  /**
   * Check if new version was loaded and can be installed.
   *
   * @param callback callback where to send the result
   */
  private void jsIsUpdateAvailableForInstallation(final CallbackContext callback) {
    Map<String, Object> data = null;
    ChcpError error = null;
    final String readyForInstallationVersionName = pluginInternalPrefs.getReadyForInstallationReleaseVersionName();
    if (!TextUtils.isEmpty(readyForInstallationVersionName)) {
      data = new HashMap<String, Object>();
      data.put("readyToInstallVersion", readyForInstallationVersionName);
      data.put("currentVersion", pluginInternalPrefs.getCurrentReleaseVersionName());
    } else {
      error = ChcpError.NOTHING_TO_INSTALL;
    }

    PluginResult pluginResult = PluginResultHelper.createPluginResult(null, data, error);
    callback.sendPluginResult(pluginResult);
  }

  /**
   * Get information about app and web versions.
   *
   * @param callback callback where to send the result
   */
  private void jsGetVersionInfo(final CallbackContext callback) {
    final Context context = cordova.getActivity();
    final Map<String, Object> data = new HashMap<String, Object>();
    data.put("currentWebVersion", pluginInternalPrefs.getCurrentReleaseVersionName());
    data.put("readyToInstallWebVersion", pluginInternalPrefs.getReadyForInstallationReleaseVersionName());
    data.put("previousWebVersion", pluginInternalPrefs.getPreviousReleaseVersionName());
    data.put("appVersion", VersionHelper.applicationVersionName(context));
    data.put("buildVersion", VersionHelper.applicationVersionCode(context));

    final PluginResult pluginResult = PluginResultHelper.createPluginResult(null, data, null);
    callback.sendPluginResult(pluginResult);
  }

  // convenience method
  private void fetchUpdate() {
    fetchUpdate(null, new FetchUpdateOptions());
  }

  /**
   * 업데이트 가능 여부 확인
   * cordova ready시 자동수행 할 수도 있고
   * javascript에서 임의로 수행 할 수도있다.
   *
   * @param jsCallback   callback where to send the result;
   *                     used, when update is requested manually from JavaScript
   * @param fetchOptions "https://github.com/nordnet/cordova-hot-code-push/wiki/Fetch-update"
   */
  private void fetchUpdate(CallbackContext jsCallback, FetchUpdateOptions fetchOptions) {
    Log.d("CHCP", "업데이트 수행 부분 들어옴");

    if (!isPluginReadyForWork) {
      return;
    }

    Map<String, String> requestHeaders = null;
    // chcp config에 저장되어있는 server URL 가져오기
    String configURL = chcpXmlConfig.getConfigUrl();
    Log.d("CHCP", "서버 URL 가져옴 : " + configURL);


    // fetchOption이 없으면 default 설정으로
    if (fetchOptions == null) {
      fetchOptions = defaultFetchUpdateOptions;
    }

    // fetchOption이 있으면 변경
    if (fetchOptions != null) {
      requestHeaders = fetchOptions.getRequestHeaders();
      final String optionalConfigURL = fetchOptions.getConfigURL();
      if (!TextUtils.isEmpty(optionalConfigURL)) {
        configURL = optionalConfigURL;
      }
    }

    Log.d("CHCP", "페치 옵션 가져옴 : " + (fetchOptions != null ? fetchOptions.getConfigURL() : null));

    // 업데이트 다운로드 리퀘스트 설정
    final UpdateDownloadRequest request = UpdateDownloadRequest.builder(cordova.getActivity())
      .setConfigURL(configURL)
      .setCurrentNativeVersion(chcpXmlConfig.getNativeInterfaceVersion())
      .setCurrentReleaseVersion(pluginInternalPrefs.getCurrentReleaseVersionName())
      .setRequestHeaders(requestHeaders)
      .build();

    final ChcpError error = UpdatesLoader.downloadUpdate(request);
    if (error != ChcpError.NONE) {
      if (jsCallback != null) {
        PluginResult errorResult = PluginResultHelper.createPluginResult(UpdateDownloadErrorEvent.EVENT_NAME, null, error);
        jsCallback.sendPluginResult(errorResult);
      }
      return;
    }

    if (jsCallback != null) {
      downloadJsCallback = jsCallback;
    }
  }

  /**
   * 업데이트가 가능하다면 업데이트를 설치한다
   *
   * @param jsCallback callback where to send the result;
   *                   used, when installation os requested manually from JavaScript
   */
  private void installUpdate(CallbackContext jsCallback) {
    if (!isPluginReadyForWork) {
      return;
    }

    Log.d("CHCP", "updatesInstaller 실행");

    ChcpError error = UpdatesInstaller.install(cordova.getActivity(),
      pluginInternalPrefs.getReadyForInstallationReleaseVersionName(),    // 인스톨 할 버전
      pluginInternalPrefs.getCurrentReleaseVersionName());                // 현재 버전

    if (error != ChcpError.NONE) {
      if (jsCallback != null) {
        PluginResult errorResult = PluginResultHelper.createPluginResult(UpdateInstallationErrorEvent.EVENT_NAME, null, error);
        jsCallback.sendPluginResult(errorResult);
      }

      return;
    }

    if (jsCallback != null) {
      installJsCallback = jsCallback;
    }
  }

  // endregion

  // region Private API

  /**
   * Check if plugin can perform it's duties.
   *
   * @return <code>true</code> - plugin is ready; otherwise - <code>false</code>
   */
  private boolean isPluginReadyForWork() {
    /// wwwfolder 존재여부 확인
    boolean isWwwFolderExists = isWwwFolderExists();
    boolean isWwwFolderInstalled = pluginInternalPrefs.isWwwFolderInstalled();
    boolean isApplicationHasBeenUpdated = isApplicationHasBeenUpdated();

    return isWwwFolderExists && isWwwFolderInstalled && !isApplicationHasBeenUpdated;
  }

  /**
   * Check if external version of www folder exists.
   *
   * @return <code>true</code> if it is in place; <code>false</code> - otherwise
   */
  private boolean isWwwFolderExists() {
    return new File(fileStructure.getWwwFolder()).exists();
  }

  /**
   * Check if application has been updated through the Google Play since the last launch.
   *
   * @return <code>true</code> if application was update; <code>false</code> - otherwise
   */
  private boolean isApplicationHasBeenUpdated() {
    return pluginInternalPrefs.getAppBuildVersion() != VersionHelper.applicationVersionCode(cordova.getActivity());
  }

  /**
   * Install assets folder onto the external storage
   */
  private void installWwwFolder() {
    isPluginReadyForWork = false;

    // reset www folder installed flag
    if (pluginInternalPrefs.isWwwFolderInstalled()) {
      pluginInternalPrefs.setWwwFolderInstalled(false);
      pluginInternalPrefs.setReadyForInstallationReleaseVersionName("");
      pluginInternalPrefs.setPreviousReleaseVersionName("");

      final ApplicationConfig appConfig = ApplicationConfig.configFromAssets(cordova.getActivity(), PluginFilesStructure.CONFIG_FILE_NAME);
      pluginInternalPrefs.setCurrentReleaseVersionName(appConfig.getContentConfig().getReleaseVersion());

      pluginInternalPrefsStorage.storeInPreference(pluginInternalPrefs);
    }

    AssetsHelper.copyAssetDirectoryToAppDirectory(cordova.getActivity().getApplicationContext(), WWW_FOLDER, fileStructure.getWwwFolder());
  }

  /**
   * Redirect user onto the page, that resides on the external storage instead of the assets folder.
   */
  private void redirectToLocalStorageIndexPage() {
    final String indexPage = getStartingPage();

    // remove query and fragment parameters from the index page path
    // TODO: cleanup this fragment
    String strippedIndexPage = indexPage;
    if (strippedIndexPage.contains("#") || strippedIndexPage.contains("?")) {
      int idx = strippedIndexPage.lastIndexOf("?");
      if (idx >= 0) {
        strippedIndexPage = strippedIndexPage.substring(0, idx);
      } else {
        idx = strippedIndexPage.lastIndexOf("#");
        strippedIndexPage = strippedIndexPage.substring(0, idx);
      }
    }

    // make sure, that index page exists
    String external = Paths.get(fileStructure.getWwwFolder(), strippedIndexPage);
    if (!new File(external).exists()) {
      Log.d("CHCP", "External starting page not found. Aborting page change.");
      return;
    }

    // load index page from the external source
    external = Paths.get(fileStructure.getWwwFolder(), indexPage);
    Log.d("CHCP", "업데이트 성공여부 : " + isSuccessUpdated);
    webView.loadUrlIntoView(FILE_PREFIX + external, false);
  }

  /**
   * Getter for the startup page.
   *
   * @return startup page relative path
   */
  private String getStartingPage() {
    if (!TextUtils.isEmpty(startingPage)) {
      return startingPage;
    }

    ConfigXmlParser parser = new ConfigXmlParser();
    parser.parse(cordova.getActivity());
    String url = parser.getLaunchUrl();

    startingPage = url.replace(LOCAL_ASSETS_FOLDER, "");

    return startingPage;
  }

  // endregion

  // region Assets installation events

  @SuppressWarnings("unused")
  @Subscribe
  public void onEvent(final BeforeAssetsInstalledEvent event) {
    Log.d("CHCP", "Dispatching before assets installed event");
    final PluginResult result = PluginResultHelper.pluginResultFromEvent(event);

    sendMessageToDefaultCallback(result);
  }

  /**
   * Listener for event that assets folder are now installed on the external storage.
   * From that moment all content will be displayed from it.
   *
   * @param event event details
   * @see AssetsInstalledEvent
   * @see AssetsHelper
   * @see EventBus
   */
  @SuppressWarnings("unused")
  @Subscribe
  public void onEvent(final AssetsInstalledEvent event) {
    // update stored application version
    pluginInternalPrefs.setAppBuildVersion(VersionHelper.applicationVersionCode(cordova.getActivity()));
    pluginInternalPrefs.setWwwFolderInstalled(true);
    pluginInternalPrefsStorage.storeInPreference(pluginInternalPrefs);

    isPluginReadyForWork = true;

    PluginResult result = PluginResultHelper.pluginResultFromEvent(event);
    sendMessageToDefaultCallback(result);

    if (chcpXmlConfig.isAutoDownloadIsAllowed() &&
      !UpdatesInstaller.isInstalling() && !UpdatesLoader.isExecuting()) {
      fetchUpdate();
    }
  }

  /**
   * Listener for event that we failed to install assets folder on the external storage.
   * If so - nothing we can do, plugin is not gonna work.
   *
   * @param event event details
   * @see AssetsInstallationErrorEvent
   * @see AssetsHelper
   * @see EventBus
   */
  @SuppressWarnings("unused")
  @Subscribe
  public void onEvent(AssetsInstallationErrorEvent event) {
    Log.d("CHCP", "Can't install assets on device. Continue to work with default bundle");

    PluginResult result = PluginResultHelper.pluginResultFromEvent(event);
    sendMessageToDefaultCallback(result);
  }

  // endregion

  // region Update download events

  /**
   * Listener for the event that update is loaded and ready for the installation.
   *
   * @param event event information
   * @see EventBus
   * @see UpdateIsReadyToInstallEvent
   * @see UpdatesLoader
   */
  @SuppressWarnings("unused")
  @Subscribe
  public void onEvent(UpdateIsReadyToInstallEvent event) {
    final ContentConfig newContentConfig = event.applicationConfig().getContentConfig();
    Log.d("CHCP", "Update is ready for installation: " + newContentConfig.getReleaseVersion());

    pluginInternalPrefs.setReadyForInstallationReleaseVersionName(newContentConfig.getReleaseVersion());
    pluginInternalPrefsStorage.storeInPreference(pluginInternalPrefs);

    PluginResult jsResult = PluginResultHelper.pluginResultFromEvent(event);

    // notify JS
    if (downloadJsCallback != null) {
      downloadJsCallback.sendPluginResult(jsResult);
      downloadJsCallback = null;
    }

    sendMessageToDefaultCallback(jsResult);

    // perform installation if allowed
    if (chcpXmlConfig.isAutoInstallIsAllowed() && newContentConfig.getUpdateTime() == UpdateTime.NOW) {
      installUpdate(null);
    }
  }

  /**
   * Listener for event that there is no update available at the moment.
   * We are as fresh as possible.
   *
   * @param event event information
   * @see EventBus
   * @see NothingToUpdateEvent
   * @see UpdatesLoader
   */
  @SuppressWarnings("unused")
  @Subscribe
  public void onEvent(NothingToUpdateEvent event) {
    Log.d("CHCP", "Nothing to update");

    PluginResult jsResult = PluginResultHelper.pluginResultFromEvent(event);

    //notify JS
    if (downloadJsCallback != null) {
      downloadJsCallback.sendPluginResult(jsResult);
      downloadJsCallback = null;
    }

    sendMessageToDefaultCallback(jsResult);
  }

  /**
   * Listener for event that an update is about to begin
   *
   * @param event event information
   * @see EventBus
   * @see BeforeInstallEvent
   * @see UpdatesLoader
   */
  @SuppressWarnings("unused")
  @Subscribe
  public void onEvent(BeforeInstallEvent event) {
    Log.d("CHCP", "Dispatching Before install event");

    PluginResult jsResult = PluginResultHelper.pluginResultFromEvent(event);

    sendMessageToDefaultCallback(jsResult);
  }

  /**
   * Listener for event that some error has happened during the update download process.
   *
   * @param event event information
   * @see EventBus
   * @see UpdateDownloadErrorEvent
   * @see UpdatesLoader
   */
  @SuppressWarnings("unused")
  @Subscribe
  public void onEvent(UpdateDownloadErrorEvent event) {
    Log.d("CHCP", "Failed to update");

    final ChcpError error = event.error();
    if (error == ChcpError.LOCAL_VERSION_OF_APPLICATION_CONFIG_NOT_FOUND || error == ChcpError.LOCAL_VERSION_OF_MANIFEST_NOT_FOUND) {
      Log.d("CHCP", "Can't load application config from installation folder. Reinstalling external folder");
      installWwwFolder();
    }

    PluginResult jsResult = PluginResultHelper.pluginResultFromEvent(event);

    // notify JS
    if (downloadJsCallback != null) {
      downloadJsCallback.sendPluginResult(jsResult);
      downloadJsCallback = null;
    }

    sendMessageToDefaultCallback(jsResult);

    rollbackIfCorrupted(event.error());
  }

  // endregion

  // region Update installation events

  /**
   * 업데이트 설치가 된 뒤 호출되는 이벤트를 처리하는 곳
   *
   * @param event event information
   * @see EventBus
   * @see UpdateInstalledEvent
   * @see UpdatesInstaller
   */
  @SuppressWarnings("unused")
  @Subscribe
  public void onEvent(UpdateInstalledEvent event) {
    isSuccessUpdated = true;
    Log.d("CHCP", "Update is installed");
    Log.d("CHCP", "업데이트 설치 된 뒤 이벤트를 보내면 onEvent에서 처리함");

    final ContentConfig newContentConfig = event.applicationConfig().getContentConfig();

    // update preferences
    pluginInternalPrefs.setPreviousReleaseVersionName(pluginInternalPrefs.getCurrentReleaseVersionName());
    pluginInternalPrefs.setCurrentReleaseVersionName(newContentConfig.getReleaseVersion());
    pluginInternalPrefs.setReadyForInstallationReleaseVersionName("");
    pluginInternalPrefsStorage.storeInPreference(pluginInternalPrefs);

    fileStructure = new PluginFilesStructure(cordova.getActivity(), newContentConfig.getReleaseVersion());

    final PluginResult jsResult = PluginResultHelper.pluginResultFromEvent(event);

    if (installJsCallback != null) {
      installJsCallback.sendPluginResult(jsResult);
      installJsCallback = null;
    }

    sendMessageToDefaultCallback(jsResult);

    // reset content to index page
    handler.post(new Runnable() {
      @Override
      public void run() {
        HotCodePushPlugin.this.redirectToLocalStorageIndexPage();
      }
    });

    Log.d("CHCP", "여기가 실행된다");

    cleanupFileSystemFromOldReleases(chcpXmlConfig);
  }

  /**
   * Listener for event that some error happened during the update installation.
   *
   * @param event event information
   * @see UpdateInstallationErrorEvent
   * @see EventBus
   * @see UpdatesInstaller
   */
  @SuppressWarnings("unused")
  @Subscribe
  public void onEvent(UpdateInstallationErrorEvent event) {
    Log.d("CHCP", "Failed to install");

    PluginResult jsResult = PluginResultHelper.pluginResultFromEvent(event);

    // notify js
    if (installJsCallback != null) {
      installJsCallback.sendPluginResult(jsResult);
      installJsCallback = null;
    }

    sendMessageToDefaultCallback(jsResult);

    rollbackIfCorrupted(event.error());
  }

  /**
   * Listener for event that there is nothing to install.
   *
   * @param event event information
   * @see NothingToInstallEvent
   * @see UpdatesInstaller
   * @see EventBus
   */
  @SuppressWarnings("unused")
  @Subscribe
  public void onEvent(NothingToInstallEvent event) {
    Log.d("CHCP", "Nothing to install");

    PluginResult jsResult = PluginResultHelper.pluginResultFromEvent(event);

    // notify JS
    if (installJsCallback != null) {
      installJsCallback.sendPluginResult(jsResult);
      installJsCallback = null;
    }

    sendMessageToDefaultCallback(jsResult);
  }

  // endregion

  // region Cleanup process

  /**
   * 불필요한 릴리즈 폴더 제거
   *
   * @param chcpXmlConfig
   */
  private void cleanupFileSystemFromOldReleases(ChcpXmlConfig chcpXmlConfig) {
    boolean isAllowRemoveBackup = chcpXmlConfig.isAllowRemoveBackup();

    Log.d("CHCP", "isAllowRemoveBackup의 값은 : " + isAllowRemoveBackup);

    if (TextUtils.isEmpty(pluginInternalPrefs.getCurrentReleaseVersionName())) {
      return;
    }

    // 릴리즈 폴더 확인 후 삭제
    // 1. CurrentReleaseVersionName
    // 2. PreviousReleaseVersionName
    // 3. ReadyForInstallationReleaseVersionName

    // '인스톨 릴리즈'가 들어가는 이유는 어플 초기 실행시 자동 다운로드가 되어있을 경우
    // 삭제를 방지하도록 하기 위함
    if (isAllowRemoveBackup) {
      // 백업 지우도록 처리
      CleanUpHelper.removeReleaseFolders(cordova.getActivity(),
        new String[]{
          pluginInternalPrefs.getCurrentReleaseVersionName(),               // 현재 릴리즈 버전
          pluginInternalPrefs.getReadyForInstallationReleaseVersionName()   // 인스톨 될 릴리즈 버전 명
        }
      );
      pluginInternalPrefs.setPreviousReleaseVersionName("");
    } else {
      // 백업 안지우게 처리
      CleanUpHelper.removeReleaseFolders(cordova.getActivity(),
        new String[]{
          pluginInternalPrefs.getCurrentReleaseVersionName(),               // 현재 릴리즈 버전
          pluginInternalPrefs.getPreviousReleaseVersionName(),              // 과거 릴리즈 버전
          pluginInternalPrefs.getReadyForInstallationReleaseVersionName()   // 인스톨 될 릴리즈 버전 명
        }
      );
    }
  }

  //endregion

  // region Rollback process

  /**
   * Rollback to the previous/bundle version, if this is needed.
   *
   * @param error error, based on which we will decide
   */
  private void rollbackIfCorrupted(ChcpError error) {
    if (error != ChcpError.LOCAL_VERSION_OF_APPLICATION_CONFIG_NOT_FOUND &&
      error != ChcpError.LOCAL_VERSION_OF_MANIFEST_NOT_FOUND) {
      return;
    }

    if (pluginInternalPrefs.getPreviousReleaseVersionName().length() > 0) {
      Log.d("CHCP", "Current release is corrupted, trying to rollback to the previous one");
      rollbackToPreviousRelease();
    } else {
      Log.d("CHCP", "Current release is corrupted, reinstalling www folder from assets");
      installWwwFolder();
    }
  }

  /**
   * Rollback to the previously installed version of the web content.
   */
  private void rollbackToPreviousRelease() {
    pluginInternalPrefs.setCurrentReleaseVersionName(pluginInternalPrefs.getPreviousReleaseVersionName());
    pluginInternalPrefs.setPreviousReleaseVersionName("");
    pluginInternalPrefs.setReadyForInstallationReleaseVersionName("");
    pluginInternalPrefsStorage.storeInPreference(pluginInternalPrefs);

    fileStructure.switchToRelease(pluginInternalPrefs.getCurrentReleaseVersionName());

    handler.post(new Runnable() {
      @Override
      public void run() {
        redirectToLocalStorageIndexPage();
      }
    });
  }

  // endregion
}
