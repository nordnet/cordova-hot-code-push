//
//  HCPPlugin.m
//
//  Created by Nikolay Demyankov on 07.08.15.
//

#import <Cordova/CDVConfigParser.h>

#import "HCPPlugin.h"
#import "HCPApplicationConfig+Downloader.h"
#import "HCPContentManifest+Downloader.h"
#import "HCPFileDownloader.h"
#import "HCPFilesStructure.h"
#import "HCPFilesStructureImpl.h"
#import "HCPUpdateLoader.h"
#import "HCPEvents.h"
#import "HCPPluginInternalPreferences+UserDefaults.h"
#import "HCPUpdateInstaller.h"
#import "NSJSONSerialization+HCPExtension.h"
#import "CDVPluginResult+HCPEvents.h"
#import "HCPXmlConfig.h"
#import "NSBundle+HCPExtension.h"
#import "HCPApplicationConfigStorage.h"
#import "HCPAppUpdateRequestAlertDialog.h"

@interface HCPPlugin() {
    id<HCPFilesStructure> _filesStructure;
    HCPUpdateLoader *_updatesLoader;
    NSString *_defaultCallbackID;
    BOOL _isPluginReadyForWork;
    HCPPluginInternalPreferences *_pluginInternalPrefs;
    HCPUpdateInstaller *_updateInstaller;
    NSMutableArray *_fetchTasks;
    NSString *_installationCallback;
    HCPXmlConfig *_pluginXmllConfig;
    HCPApplicationConfig *_appConfig;
    HCPAppUpdateRequestAlertDialog *_appUpdateRequestDialog;
    SocketIOClient *_socketIOClient;
    BOOL _shouldFlushCache;
}

@end

#pragma mark Local constants declaration

static NSString *const DEFAULT_STARTING_PAGE = @"index.html";

@implementation HCPPlugin

#pragma mark Lifecycle

-(void)pluginInitialize {
    [self subscribeToEvents];
    [self doLocalInit];
    [self connectToDevServer];
    
    // install WWW folder if it is needed
    if ([self isWWwFolderNeedsToBeInstalled]) {
        [NSBundle installWwwFolderToExternalStorageFolder:_filesStructure.wwwFolder];
        return;
    }
    
    _isPluginReadyForWork = YES;
    [self resetIndexPageToExternalStorage];
    [self loadApplicationConfig];
    
    // install update if any exists
    if (_pluginXmllConfig.isUpdatesAutoInstallationAllowed) {
        [self _installUpdate:nil];
    }
}

- (void)onAppTerminate {
    [self unsubscribeFromEvents];
    [self disconnectFromDevServer];
}

- (void)onResume:(NSNotification *)notification {
    if (_pluginXmllConfig.isUpdatesAutoInstallationAllowed && _appConfig.contentConfig.updateTime == HCPUpdateOnResume) {
        [self _installUpdate:nil];
    }
}

#pragma mark Private API

/**
 *  Load application config from file system
 */
- (void)loadApplicationConfig {
    id<HCPConfigFileStorage> configStorage = [[HCPApplicationConfigStorage alloc] initWithFileStructure:_filesStructure];
    _appConfig = [configStorage loadFromFolder:_filesStructure.wwwFolder];
}

/**
 *  Check if www folder already exists on the external storage.
 *
 *  @return <code>YES</code> - www folder doesn't exist, we need to install it; <code>NO</code> - folder already installed
 */
- (BOOL)isWWwFolderNeedsToBeInstalled {
    NSFileManager *fileManager = [NSFileManager defaultManager];
    BOOL isApplicationUpdated = [NSBundle applicationBuildVersion] > _pluginInternalPrefs.appBuildVersion;
    BOOL isWWwFolderExists = [fileManager fileExistsAtPath:_filesStructure.wwwFolder.path];
    
    return isApplicationUpdated || !isWWwFolderExists;
}

/**
 *  Perform initialization of the plugin variables.
 */
- (void)doLocalInit {
    _fetchTasks = [[NSMutableArray alloc] init];
    _filesStructure = [[HCPFilesStructureImpl alloc] init];
    
    // init plugin config from xml
    _pluginXmllConfig = [HCPXmlConfig loadFromCordovaConfigXml];
    
    // load plugin internal preferences
    _pluginInternalPrefs = [HCPPluginInternalPreferences loadFromUserDefaults];
    if (_pluginInternalPrefs == nil) {
        _pluginInternalPrefs = [HCPPluginInternalPreferences defaultConfig];
        [_pluginInternalPrefs saveToUserDefaults];
    }
    
    // init updates loader
    _updatesLoader = [HCPUpdateLoader sharedInstance];
    [_updatesLoader setup:_filesStructure];
    
    // init updates installer
    _updateInstaller = [HCPUpdateInstaller sharedInstance];
    [_updateInstaller setup:_filesStructure];
}

/**
 *  Load update from the server.
 *
 *  @param callbackId id of the caller on JavaScript side; it will be used to send back the result of the download process
 *
 *  @return <code>YES</code> if download process started; <code>NO</code> otherwise
 */
- (BOOL)_fetchUpdate:(NSString *)callbackId {
    if (!_isPluginReadyForWork) {
        return NO;
    }
    
    NSString *taskId = [_updatesLoader addUpdateTaskToQueueWithConfigUrl:_pluginXmllConfig.configUrl];
    [self storeCallback:callbackId forFetchTask:taskId];
    
    return taskId != nil;
}

/**
 *  Store download callback for later use. 
 *  Callback is associated with the worker.
 *
 *  @param callbackId d of the caller on JavaScript side; it will be used to send back the result of the download process
 *  @param taskId     worker id, associated with this callback
 */
- (void)storeCallback:(NSString *)callbackId forFetchTask:(NSString *)taskId {
    if (callbackId == nil || taskId == nil) {
        return;
    }
    
    NSDictionary *dict = @{taskId:callbackId};
    if (_fetchTasks.count < 2) {
        [_fetchTasks addObject:dict];
    } else {
        [_fetchTasks replaceObjectAtIndex:1 withObject:dict];
    }
}

/**
 *  Get callback, associated with the given worker.
 *
 *  @param taskId worker id
 *
 *  @return callback id
 */
- (NSString *)pollCallbackForTask:(NSString *)taskId {
    NSString *callbackId = nil;
    NSInteger index = -1;
    
    for (NSInteger i=0, len=_fetchTasks.count; i<len; i++) {
        NSDictionary *dict = _fetchTasks[i];
        NSString *storedCallbackId = dict[taskId];
        if (storedCallbackId) {
            callbackId = storedCallbackId;
            index = i;
            break;
        }
    }
    
    if (callbackId) {
        [_fetchTasks removeObjectAtIndex:index];
    }
    
    return callbackId;
}

/**
 *  Install update.
 *
 *  @param callbackID callbackId id of the caller on JavaScript side; it will be used to send back the result of the installation process
 *
 *  @return <code>YES</code> if installation has started; <code>NO</code> otherwise
 */
- (BOOL)_installUpdate:(NSString *)callbackID {
    if (!_isPluginReadyForWork || [[HCPUpdateInstaller sharedInstance] isInstallationInProgress]) {
        return NO;
    }
    
    if (callbackID) {
        _installationCallback = callbackID;
    }

    // ignored for now
    NSError *error = nil;
    
    return [_updateInstaller launchUpdateInstallation:&error];
}

/**
 *  Load given url into the WebView
 *
 *  @param url url to load
 */
- (void)loadURL:(NSURL *)url {
    [[NSOperationQueue mainQueue] addOperationWithBlock:^{
        [self.webView loadRequest:[NSURLRequest requestWithURL:url cachePolicy:NSURLRequestReloadIgnoringLocalAndRemoteCacheData timeoutInterval:60.0]];
    }];
}

/**
 *  Redirect user to the index page that is located on the external storage.
 */
- (void)resetIndexPageToExternalStorage {
    NSString *currentUrl = self.webView.request.URL.path;
    if ([currentUrl containsString:_filesStructure.wwwFolder.absoluteString]) {
        return;
    }
    
    if (currentUrl.length == 0) {
        currentUrl = [self getStartingPagePath];
    }
    
    currentUrl = [currentUrl stringByReplacingOccurrencesOfString:[NSBundle pathToWwwFolder] withString:@""];
    NSURL *indexPageExternalURL = [self appendWwwFolderPathToPath:currentUrl];
    if (![[NSFileManager defaultManager] fileExistsAtPath:indexPageExternalURL.path]) {
        return;
    }
    
    // rewrite starting page: should load from external storage
    if ([self.viewController isKindOfClass:[CDVViewController class]]) {
        ((CDVViewController *)self.viewController).startPage = indexPageExternalURL.absoluteString;
    } else {
        NSLog(@"HotCodePushError: Can't make starting page to be from external storage. Main controller should be of type CDVViewController.");
    }
}

/**
 *  If needed - add path to www folder on the external storage to the provided path.
 *
 *  @param pagePath path to which we want add www folder
 *
 *  @return resulting path
 */
- (NSURL *)appendWwwFolderPathToPath:(NSString *)pagePath {
    if ([pagePath containsString:_filesStructure.wwwFolder.absoluteString]) {
        return [NSURL URLWithString:pagePath];
    }
    
    return [_filesStructure.wwwFolder URLByAppendingPathComponent:pagePath];
}

/**
 *  Getter for the starting page relative path.
 *  Retrieved either from hosting view controller or config.xml.
 *
 *  @return index page of the application.
 */
- (NSString *)getStartingPagePath {
    NSString *startPage = nil;
    if ([self.viewController isKindOfClass:[CDVViewController class]]) {
        startPage = ((CDVViewController *)self.viewController).startPage;
    } else {
        startPage = [self getStartingPageFromConfig];
    }
    
    return startPage;
}

/**
 *  Get index page from config.xml
 *
 *  @return index page of the application
 */
- (NSString *)getStartingPageFromConfig {
    CDVConfigParser* delegate = [[CDVConfigParser alloc] init];
    
    // read from config.xml in the app bundle
    NSURL* url = [NSURL fileURLWithPath:[NSBundle pathToCordovaConfigXml]];
    
    NSXMLParser *configParser = [[NSXMLParser alloc] initWithContentsOfURL:url];
    [configParser setDelegate:((id <NSXMLParserDelegate>)delegate)];
    [configParser parse];
    
    if (delegate.startPage) {
        return delegate.startPage;
    }
    
    return DEFAULT_STARTING_PAGE;
}

/**
 *  Notify JavaScript module about occured event. 
 *  For that we will use callback, received on plugin initialization stage.
 *
 *  @param result message to send to web side
 */
- (void)invokeDefaultCallbackWithMessage:(CDVPluginResult *)result {
    if (_defaultCallbackID == nil) {
        return;
    }
    [result setKeepCallbackAsBool:YES];
    
    [self.commandDelegate sendPluginResult:result callbackId:_defaultCallbackID];
}

#pragma mark Events

/**
 *  Subscribe to different events: lifecycle, plugin specific.
 */
- (void)subscribeToEvents {
    [self subscribeToLifecycleEvents];
    [self subscribeToCordovaEvents];
    [self subscribeToPluginInternalEvents];
}

/**
 *  Subscribe to Cordova events.
 */
- (void)subscribeToCordovaEvents {
    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector(webViewDidLoadPage:)
                                                 name:CDVPageDidLoadNotification
                                               object:nil];
}

/**
 *  Subscribe to lifecycle events.
 */
- (void)subscribeToLifecycleEvents {
    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector(onResume:)
                                                 name:UIApplicationWillEnterForegroundNotification
                                               object:nil];
}

/**
 *  Subscrive to plugin workflow events.
 */
- (void)subscribeToPluginInternalEvents {
    NSNotificationCenter *notificationCenter = [NSNotificationCenter defaultCenter];
    
    // bundle installation events
    [notificationCenter addObserver:self
                           selector:@selector(onAssetsInstalledOnExternalStorageEvent:)
                               name:kHCPBundleAssetsInstalledOnExternalStorageEvent
                             object:nil];
    [notificationCenter addObserver:self
                           selector:@selector(onAssetsInstallationErrorEvent:)
                               name:kHCPBundleAssetsInstallationErrorEvent
                             object:nil];
    
    // update download events
    [notificationCenter addObserver:self
                           selector:@selector(onUpdateDownloadErrorEvent:)
                               name:kHCPUpdateDownloadErrorEvent
                             object:nil];
    [notificationCenter addObserver:self
                           selector:@selector(onNothingToUpdateEvent:)
                               name:kHCPNothingToUpdateEvent
                             object:nil];
    [notificationCenter addObserver:self
                           selector:@selector(onUpdateIsReadyForInstallation:)
                               name:kHCPUpdateIsReadyForInstallationEvent
                             object:nil];
    
    // update installation events
    [notificationCenter addObserver:self
                           selector:@selector(onUpdateInstallationErrorEvent:)
                               name:kHCPUpdateInstallationErrorEvent
                             object:nil];
    [notificationCenter addObserver:self
                           selector:@selector(onUpdateInstalledEvent:)
                               name:kHCPUpdateIsInstalledEvent
                             object:nil];
    [notificationCenter addObserver:self
                           selector:@selector(onNothingToInstallEvent:)
                               name:kHCPNothingToInstallEvent
                             object:nil];
}

/**
 *  Remove subscription. 
 *  Should be called only when the application is terminated.
 */
- (void)unsubscribeFromEvents {
    [[NSNotificationCenter defaultCenter] removeObserver:self];
}

#pragma mark Cordova events

/**
 *  Method is called when WebView finished loading the page
 *
 *  @param notification captured notification
 */
- (void)webViewDidLoadPage:(NSNotification *)notification {
    if (!_shouldFlushCache) {
        return;
    }
    
    // In order to force js/css/images update we need to call "reload" method.
    // Don't know why, but only after that resource files get updated.
    // There is no such problem with html files.
    _shouldFlushCache = NO;
    [[NSOperationQueue mainQueue] addOperationWithBlock:^{
        [self.webView reload];
    }];
}


#pragma mark Bundle installation events

/**
 *  Method is called when we successfully installed www folder from bundle onto the external storage
 *
 *  @param notification captured notification with event details
 */
- (void)onAssetsInstalledOnExternalStorageEvent:(NSNotification *)notification {
    // update stored config with new application build version
    _pluginInternalPrefs.appBuildVersion = [NSBundle applicationBuildVersion];
    [_pluginInternalPrefs saveToUserDefaults];
    
    // allow work
    _isPluginReadyForWork = YES;
    
    // send notification to web
    [self invokeDefaultCallbackWithMessage:[CDVPluginResult pluginResultForNotification:notification]];
    
    // fetch update
    [self loadApplicationConfig];
    if (_pluginXmllConfig.isUpdatesAutoDowloadAllowed) {
        [self _fetchUpdate:nil];
    }
}

/**
 *  Method is called when error occured during the installation for the www folder from bundle on the external storage
 *
 *  @param notification captured notification with event details
 */
- (void)onAssetsInstallationErrorEvent:(NSNotification *)notification {
    _isPluginReadyForWork = NO;
    
    // send notification to web
    [self invokeDefaultCallbackWithMessage:[CDVPluginResult pluginResultForNotification:notification]];
}

#pragma mark Update download events

/**
 *  Method is called when error occured during the update download process.
 *
 *  @param notification captured notification with event details
 */
- (void)onUpdateDownloadErrorEvent:(NSNotification *)notification {
    NSError *error = notification.userInfo[kHCPEventUserInfoErrorKey];
    NSLog(@"Error during update: %@", error.userInfo[NSLocalizedDescriptionKey]);
    
    // send notification to the associated callback
    CDVPluginResult *pluginResult = [CDVPluginResult pluginResultForNotification:notification];
    NSString *callbackID = [self pollCallbackForTask:notification.userInfo[kHCPEventUserInfoTaskIdKey]];
    if (callbackID) {
        [self.commandDelegate sendPluginResult:pluginResult callbackId:callbackID];
    }
    
    // send notification to the default callback
    [self invokeDefaultCallbackWithMessage:pluginResult];
}

/**
 *  Method is called when there is nothing new to download from the server.
 *
 *  @param notification captured notification with event details.
 */
- (void)onNothingToUpdateEvent:(NSNotification *)notification {
    NSLog(@"Nothing to update");
    
    // send notification to the associated callback
    CDVPluginResult *pluginResult = [CDVPluginResult pluginResultForNotification:notification];
    NSString *callbackID = [self pollCallbackForTask:notification.userInfo[kHCPEventUserInfoTaskIdKey]];
    if (callbackID) {
        [self.commandDelegate sendPluginResult:pluginResult callbackId:callbackID];
    }
    
    // send notification to the default callback
    [self invokeDefaultCallbackWithMessage:pluginResult];
}

/**
 *  Method is called when update is loaded and ready for installation.
 *
 *  @param notification captured notification with event details
 */
- (void)onUpdateIsReadyForInstallation:(NSNotification *)notification {
    NSLog(@"Update is ready for installation");
    
    // send notification to the associated callback
    CDVPluginResult *pluginResult = [CDVPluginResult pluginResultForNotification:notification];
    NSString *callbackID = [self pollCallbackForTask:notification.userInfo[kHCPEventUserInfoTaskIdKey]];
    if (callbackID) {
        [self.commandDelegate sendPluginResult:pluginResult callbackId:callbackID];
    }
    
    // send notification to the default callback
    [self invokeDefaultCallbackWithMessage:pluginResult];
    
    // if it is allowed - launch the installation
    HCPApplicationConfig *newConfig = notification.userInfo[kHCPEventUserInfoApplicationConfigKey];
    if (_pluginXmllConfig.isUpdatesAutoInstallationAllowed && newConfig.contentConfig.updateTime == HCPUpdateNow) {
        [self _installUpdate:nil];
    }
}

#pragma mark Update installation events

/**
 *  Method is called when user requested to install the update, but there is nothing to install.
 *
 *  @param notification captured notification with the event details
 */
- (void)onNothingToInstallEvent:(NSNotification *)notification {
    CDVPluginResult *pluginResult = [CDVPluginResult pluginResultForNotification:notification];
    
    // send notification to the caller from the JavaScript side if there was any
    if (_installationCallback) {
        [self.commandDelegate sendPluginResult:pluginResult callbackId:_installationCallback];
        _installationCallback = nil;
    }
    
    // send notification to the default callback
    [self invokeDefaultCallbackWithMessage:pluginResult];
}

/**
 *  Method is called when error occured during the installation process.
 *
 *  @param notification captured notification with the event details
 */
- (void)onUpdateInstallationErrorEvent:(NSNotification *)notification {
    CDVPluginResult *pluginResult = [CDVPluginResult pluginResultForNotification:notification];
    
    // send notification to the caller from the JavaScript side if there was any
    if (_installationCallback) {
        [self.commandDelegate sendPluginResult:pluginResult callbackId:_installationCallback];
        _installationCallback = nil;
    }
    
    // send notification to the default callback
    [self invokeDefaultCallbackWithMessage:pluginResult];
}

/**
 *  Method is called when update has been installed.
 *
 *  @param notification captured notification with the event details
 */
- (void)onUpdateInstalledEvent:(NSNotification *)notification {
    CDVPluginResult *pluginResult = [CDVPluginResult pluginResultForNotification:notification];
    
    // send notification to the caller from the JavaScript side of there was any
    if (_installationCallback) {
        [self.commandDelegate sendPluginResult:pluginResult callbackId:_installationCallback];
        _installationCallback = nil;
    }
    
    // send notification to the default callback
    [self invokeDefaultCallbackWithMessage:pluginResult];
    
    // reload application to the index page
    NSURL *startingPageURL = [self appendWwwFolderPathToPath:[self getStartingPagePath]];
    _shouldFlushCache = YES;
    [self loadURL:startingPageURL];
}

#pragma mark Methods, invoked from Javascript

- (void)jsInitPlugin:(CDVInvokedUrlCommand *)command {
    _defaultCallbackID = command.callbackId;
    
    if (_pluginXmllConfig.isUpdatesAutoDowloadAllowed) {
        [self _fetchUpdate:nil];
    }
}

- (void)jsConfigure:(CDVInvokedUrlCommand *)command {
    if (!_isPluginReadyForWork) {
        return;
    }
    
    NSError *error = nil;
    id options = [NSJSONSerialization JSONObjectWithContentsFromString:command.arguments[0] error:&error];
    if (error) {
        [self.commandDelegate sendPluginResult:nil callbackId:command.callbackId];
        return;
    }
    
    [_pluginXmllConfig mergeOptionsFromJS:options];
    // TODO: store them somewhere?
    
    [self.commandDelegate sendPluginResult:nil callbackId:command.callbackId];
}

- (void)jsFetchUpdate:(CDVInvokedUrlCommand *)command {
    if (!_isPluginReadyForWork) {
        return;
    }
    
    [self _fetchUpdate:command.callbackId];
}

- (void)jsInstallUpdate:(CDVInvokedUrlCommand *)command {
    if (!_isPluginReadyForWork) {
        return;
    }
    
    [self _installUpdate:command.callbackId];
}

- (void)jsRequestAppUpdate:(CDVInvokedUrlCommand *)command {
    if (!_isPluginReadyForWork || command.arguments.count == 0) {
        return;
    }
    
    NSString* message = command.arguments[0];
    if (message.length == 0) {
        return;
    }
    
    _appUpdateRequestDialog = [[HCPAppUpdateRequestAlertDialog alloc] initWithMessage:message storeUrl:_appConfig.storeUrl onSuccessBlock:^{
        [self.commandDelegate sendPluginResult:[CDVPluginResult resultWithStatus:CDVCommandStatus_OK] callbackId:command.callbackId];
        _appUpdateRequestDialog = nil;
    } onFailureBlock:^{
        [self.commandDelegate sendPluginResult:[CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR] callbackId:command.callbackId];
        _appUpdateRequestDialog = nil;
    }];
    
    [_appUpdateRequestDialog show];
}

#pragma mark Socket IO

- (void)connectToDevServer {
    if (!_pluginXmllConfig.devOptions.isEnabled || (_socketIOClient.status == SocketIOClientStatusConnected) ) {
        return;
    }
    
    // reading url of the local server
    NSString *devServerURL = [_pluginXmllConfig.configUrl URLByDeletingLastPathComponent].absoluteString;
    devServerURL = [devServerURL substringToIndex:devServerURL.length-1];
    if (devServerURL.length == 0) {
        NSLog(@"Could not connect to local server: config-file preference is not set.");
        return;
    }
    
    @try {
        _socketIOClient = [[SocketIOClient alloc] initWithSocketURL:devServerURL opts:nil];
        [_socketIOClient on:@"connect" callback:^(NSArray * _Nonnull data, SocketAckEmitter * _Nullable emitter) {
            NSLog(@"socket connected");
        }];
        
        [_socketIOClient on:@"release" callback:^(NSArray * _Nonnull data, SocketAckEmitter * _Nullable emitter) {
            [self _fetchUpdate:nil];
        }];
        [_socketIOClient connect];
    } @catch (NSException *e) {
        NSLog(@"Exception: %@", e);
    }
}

- (void)disconnectFromDevServer {
    if (!_pluginXmllConfig.devOptions.isEnabled || (_socketIOClient.status != SocketIOClientStatusConnected)) {
        return;
    }
    
    [_socketIOClient close];
    [_socketIOClient removeAllHandlers];
}

@end
