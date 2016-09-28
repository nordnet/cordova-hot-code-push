//
//  HCPPlugin.m
//
//  Created by Nikolay Demyankov on 07.08.15.
//

#import <Cordova/CDVConfigParser.h>

#import "HCPPlugin.h"
#import "HCPFileDownloader.h"
#import "HCPFilesStructure.h"
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
#import "HCPAssetsFolderHelper.h"
#import "NSError+HCPExtension.h"
#import "HCPCleanupHelper.h"
#import "HCPUpdateRequest.h"

@interface HCPPlugin() {
    HCPFilesStructure *_filesStructure;
    NSString *_defaultCallbackID;
    BOOL _isPluginReadyForWork;
    HCPPluginInternalPreferences *_pluginInternalPrefs;
    NSString *_installationCallback;
    NSString *_downloadCallback;
    HCPXmlConfig *_pluginXmlConfig;
    HCPApplicationConfig *_appConfig;
    HCPAppUpdateRequestAlertDialog *_appUpdateRequestDialog;
    NSString *_indexPage;
    NSMutableArray<CDVPluginResult *> *_defaultCallbackStoredResults;
}

@end

#pragma mark Local constants declaration

static NSString *const DEFAULT_STARTING_PAGE = @"index.html";

@implementation HCPPlugin

#pragma mark Lifecycle

-(void)pluginInitialize {
    [self doLocalInit];
    [self subscribeToEvents];
    
    // install www folder if it is needed
    if ([self isWWwFolderNeedsToBeInstalled]) {
        [self installWwwFolder];
        return;
    }
    
    // cleanup file system: remove older releases, except current and the previous one
    [self cleanupFileSystemFromOldReleases];
    
    _isPluginReadyForWork = YES;
    [self resetIndexPageToExternalStorage];
    [self loadApplicationConfig];
    
    // install update if any exists
    if (_pluginXmlConfig.isUpdatesAutoInstallationAllowed &&
        _pluginInternalPrefs.readyForInstallationReleaseVersionName.length > 0) {
        [self _installUpdate:nil];
    }
}

- (void)onAppTerminate {
    [self unsubscribeFromEvents];
}

- (void)onResume:(NSNotification *)notification {
    if (!_pluginXmlConfig.isUpdatesAutoInstallationAllowed ||
        _pluginInternalPrefs.readyForInstallationReleaseVersionName.length == 0) {
        return;
    }
    
    // load app config from update folder and check, if we are allowed to install it
    HCPFilesStructure *fs = [[HCPFilesStructure alloc] initWithReleaseVersion:_pluginInternalPrefs.readyForInstallationReleaseVersionName];
    id<HCPConfigFileStorage> configStorage = [[HCPApplicationConfigStorage alloc] initWithFileStructure:fs];
    HCPApplicationConfig *configFromNewRelease = [configStorage loadFromFolder:fs.downloadFolder];
        
    if (configFromNewRelease.contentConfig.updateTime == HCPUpdateOnResume ||
        configFromNewRelease.contentConfig.updateTime == HCPUpdateNow) {
        [self _installUpdate:nil];
    }
}

#pragma mark Private API

- (void)installWwwFolder {
    _isPluginReadyForWork = NO;
    // reset www folder installed flag
    if (_pluginInternalPrefs.isWwwFolderInstalled) {
        _pluginInternalPrefs.wwwFolderInstalled = NO;
        _pluginInternalPrefs.readyForInstallationReleaseVersionName = @"";
        _pluginInternalPrefs.previousReleaseVersionName = @"";
        HCPApplicationConfig *config = [HCPApplicationConfig configFromBundle:[HCPFilesStructure defaultConfigFileName]];
        _pluginInternalPrefs.currentReleaseVersionName = config.contentConfig.releaseVersion;
        
        [_pluginInternalPrefs saveToUserDefaults];
    }
    
    [HCPAssetsFolderHelper installWwwFolderToExternalStorageFolder:_filesStructure.wwwFolder];
}

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
    BOOL isApplicationUpdated = ![[NSBundle applicationBuildVersion] isEqualToString:_pluginInternalPrefs.appBuildVersion];
    BOOL isWWwFolderExists = [fileManager fileExistsAtPath:_filesStructure.wwwFolder.path];
    BOOL isWWwFolderInstalled = _pluginInternalPrefs.isWwwFolderInstalled;
    
    return isApplicationUpdated || !isWWwFolderExists || !isWWwFolderInstalled;
}

/**
 *  Perform initialization of the plugin variables.
 */
- (void)doLocalInit {
    _defaultCallbackStoredResults = [[NSMutableArray alloc] init];
    
    // init plugin config from xml
    _pluginXmlConfig = [HCPXmlConfig loadFromCordovaConfigXml];
    
    // load plugin internal preferences
    _pluginInternalPrefs = [HCPPluginInternalPreferences loadFromUserDefaults];
    if (_pluginInternalPrefs == nil || _pluginInternalPrefs.currentReleaseVersionName.length == 0) {
        _pluginInternalPrefs = [HCPPluginInternalPreferences defaultConfig];
        [_pluginInternalPrefs saveToUserDefaults];
    }
    
    NSLog(@"Currently running release version %@", _pluginInternalPrefs.currentReleaseVersionName);
    
    // init file structure for www files
    _filesStructure = [[HCPFilesStructure alloc] initWithReleaseVersion:_pluginInternalPrefs.currentReleaseVersionName];
}

/**
 *  Load update from the server.
 *
 *  @param callbackId id of the caller on JavaScript side; it will be used to send back the result of the download process
 *
 *  @return <code>YES</code> if download process started; <code>NO</code> otherwise
 */
- (BOOL)_fetchUpdate:(NSString *)callbackId withOptions:(HCPFetchUpdateOptions *)options {
    if (!_isPluginReadyForWork) {
        return NO;
    }
    
    if (!options && self.defaultFetchUpdateOptions) {
        options = self.defaultFetchUpdateOptions;
    }
    
    HCPUpdateRequest *request = [[HCPUpdateRequest alloc] init];
    request.configURL = options.configFileURL ? options.configFileURL : _pluginXmlConfig.configUrl;
    request.requestHeaders = options.requestHeaders;
    request.currentWebVersion = _pluginInternalPrefs.currentReleaseVersionName;
    request.currentNativeVersion = _pluginXmlConfig.nativeInterfaceVersion;
    
    NSError *error = nil;
    [[HCPUpdateLoader sharedInstance] executeDownloadRequest:request error:&error];
    
    if (error) {
        if (callbackId) {
            CDVPluginResult *errorResult = [CDVPluginResult pluginResultWithActionName:kHCPUpdateDownloadErrorEvent
                                                                     applicationConfig:nil
                                                                                 error:error];
            [self.commandDelegate sendPluginResult:errorResult callbackId:callbackId];
        }
        
        return NO;
    }
    
    if (callbackId) {
        _downloadCallback = callbackId;
    }
    
    return YES;
}

/**
 *  Install update.
 *
 *  @param callbackID callbackId id of the caller on JavaScript side; it will be used to send back the result of the installation process
 *
 *  @return <code>YES</code> if installation has started; <code>NO</code> otherwise
 */
- (BOOL)_installUpdate:(NSString *)callbackID {
    if (!_isPluginReadyForWork) {
        return NO;
    }
    
    NSString *newVersion = _pluginInternalPrefs.readyForInstallationReleaseVersionName;
    NSString *currentVersion = _pluginInternalPrefs.currentReleaseVersionName;
    
    NSError *error = nil;
    [[HCPUpdateInstaller sharedInstance] installVersion:newVersion currentVersion:currentVersion error:&error];
    if (error) {
        if (error.code == kHCPNothingToInstallErrorCode) {
            NSNotification *notification = [HCPEvents notificationWithName:kHCPNothingToInstallEvent
                                                         applicationConfig:nil
                                                                    taskId:nil
                                                                     error:error];
            [self onNothingToInstallEvent:notification];
        } else {
            if (callbackID) {
                CDVPluginResult *errorResult = [CDVPluginResult pluginResultWithActionName:kHCPUpdateInstallationErrorEvent
                                                                         applicationConfig:nil
                                                                                     error:error];
                [self.commandDelegate sendPluginResult:errorResult callbackId:callbackID];
            }
        }
        
        return NO;
    }
    
    if (callbackID) {
        _installationCallback = callbackID;
    }

    return YES;
}

/**
 *  Load given url into the WebView
 *
 *  @param url url to load
 */
- (void)loadURL:(NSString *)url {
    [[NSOperationQueue mainQueue] addOperationWithBlock:^{
        NSURL *loadURL = [NSURL URLWithString:[NSString stringWithFormat:@"%@/%@", _filesStructure.wwwFolder.absoluteString, url]];
        NSURLRequest *request = [NSURLRequest requestWithURL:loadURL
                                                 cachePolicy:NSURLRequestReloadIgnoringCacheData
                                             timeoutInterval:10000];
#ifdef __CORDOVA_4_0_0
        [self.webViewEngine loadRequest:request];
#else
        [self.webView loadRequest:request];
#endif
    }];
}

/**
 *  Redirect user to the index page that is located on the external storage.
 */
- (void)resetIndexPageToExternalStorage {
    NSString *indexPageStripped = [self indexPageFromConfigXml];
    
    NSRange r = [indexPageStripped rangeOfCharacterFromSet:[NSCharacterSet characterSetWithCharactersInString:@"?#"] options:0];
    if (r.location != NSNotFound) {
        indexPageStripped = [indexPageStripped substringWithRange:NSMakeRange(0, r.location)];
    }
    
    NSURL *indexPageExternalURL = [self appendWwwFolderPathToPath:indexPageStripped];
    if (![[NSFileManager defaultManager] fileExistsAtPath:indexPageExternalURL.path]) {
        return;
    }
    
    // rewrite starting page www folder path: should load from external storage
    if ([self.viewController isKindOfClass:[CDVViewController class]]) {
        ((CDVViewController *)self.viewController).wwwFolderName = _filesStructure.wwwFolder.absoluteString;
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
    if ([pagePath hasPrefix:_filesStructure.wwwFolder.absoluteString]) {
        return [NSURL URLWithString:pagePath];
    }
    
    return [_filesStructure.wwwFolder URLByAppendingPathComponent:pagePath];
}

/**
 *  Get index page from config.xml
 *
 *  @return index page of the application
 */
- (NSString *)indexPageFromConfigXml {
    if (_indexPage) {
        return _indexPage;
    }
    
    CDVConfigParser* delegate = [[CDVConfigParser alloc] init];
    
    // read from config.xml in the app bundle
    NSURL* url = [NSURL fileURLWithPath:[NSBundle pathToCordovaConfigXml]];
    
    NSXMLParser *configParser = [[NSXMLParser alloc] initWithContentsOfURL:url];
    [configParser setDelegate:((id <NSXMLParserDelegate>)delegate)];
    [configParser parse];
    
    if (delegate.startPage) {
        _indexPage = delegate.startPage;
    } else {
        _indexPage = DEFAULT_STARTING_PAGE;
    }
    
    return _indexPage;
}

/**
 *  Notify JavaScript module about occured event. 
 *  For that we will use callback, received on plugin initialization stage.
 *
 *  @param result message to send to web side
 *  @return YES - result was sent to the web page; NO - otherwise
 */
- (BOOL)invokeDefaultCallbackWithMessage:(CDVPluginResult *)result {
    if (!_defaultCallbackID) {
        [_defaultCallbackStoredResults addObject:result];
        return NO;
    }
    
    [result setKeepCallbackAsBool:YES];
    [self.commandDelegate sendPluginResult:result callbackId:_defaultCallbackID];
    
    return YES;
}

- (void)dispatchDefaultCallbackStoredResults {
    if (!_defaultCallbackID || _defaultCallbackStoredResults.count == 0) {
        return;
    }
    
    for (CDVPluginResult *callResult in _defaultCallbackStoredResults) {
        [callResult setKeepCallbackAsBool:YES];
        [self.commandDelegate sendPluginResult:callResult callbackId:_defaultCallbackID];
    }
    [_defaultCallbackStoredResults removeAllObjects];
}

#pragma mark Events

/**
 *  Subscribe to different events: lifecycle, plugin specific.
 */
- (void)subscribeToEvents {
    [self subscribeToLifecycleEvents];
    [self subscribeToPluginInternalEvents];
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
                           selector:@selector(onBeforeAssetsInstalledOnExternalStorageEvent:)
                               name:kHCPBeforeBundleAssetsInstalledOnExternalStorageEvent
                             object:nil];
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
                           selector:@selector(onBeforeInstallEvent:)
                               name:kHCPBeforeInstallEvent
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

#pragma mark Bundle installation events

/**
 *  Method is called when we about to start installing www folder from bundle onto the external storage.
 *
 *  @param notification captured notification with event details
 */
- (void)onBeforeAssetsInstalledOnExternalStorageEvent:(NSNotification *)notification {
    CDVPluginResult *result = [CDVPluginResult pluginResultForNotification:notification];
    [self invokeDefaultCallbackWithMessage:result];
}

/**
 *  Method is called when we successfully installed www folder from bundle onto the external storage.
 *
 *  @param notification captured notification with event details
 */
- (void)onAssetsInstalledOnExternalStorageEvent:(NSNotification *)notification {
    // update stored config with new application build version
    _pluginInternalPrefs.appBuildVersion = [NSBundle applicationBuildVersion];
    _pluginInternalPrefs.wwwFolderInstalled = YES;
    [_pluginInternalPrefs saveToUserDefaults];
    
    // allow work
    _isPluginReadyForWork = YES;
    
    // send notification to web
    [self invokeDefaultCallbackWithMessage:[CDVPluginResult pluginResultForNotification:notification]];
    
    // fetch update
    [self loadApplicationConfig];
    
    if (_pluginXmlConfig.isUpdatesAutoDownloadAllowed &&
        ![HCPUpdateLoader sharedInstance].isDownloadInProgress &&
        ![HCPUpdateInstaller sharedInstance].isInstallationInProgress) {
        [self _fetchUpdate:nil withOptions:nil];
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
    NSLog(@"Error during update: %@", [error underlyingErrorLocalizedDesription]);
    
    // send notification to the associated callback
    CDVPluginResult *pluginResult = [CDVPluginResult pluginResultForNotification:notification];
    if (_downloadCallback) {
        [self.commandDelegate sendPluginResult:pluginResult callbackId:_downloadCallback];
        _downloadCallback = nil;
    }
    
    // send notification to the default callback
    [self invokeDefaultCallbackWithMessage:pluginResult];
    
    // probably never happens, but just for safety
    [self rollbackIfCorrupted:error];
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
    if (_downloadCallback) {
        [self.commandDelegate sendPluginResult:pluginResult callbackId:_downloadCallback];
        _downloadCallback = nil;
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
    // new application config from server
    HCPApplicationConfig *newConfig = notification.userInfo[kHCPEventUserInfoApplicationConfigKey];
    
    NSLog(@"Update is ready for installation: %@", newConfig.contentConfig.releaseVersion);
    
    // store, that we are ready for installation
    _pluginInternalPrefs.readyForInstallationReleaseVersionName = newConfig.contentConfig.releaseVersion;
    [_pluginInternalPrefs saveToUserDefaults];
    
    // send notification to the associated callback
    CDVPluginResult *pluginResult = [CDVPluginResult pluginResultForNotification:notification];
    if (_downloadCallback) {
        [self.commandDelegate sendPluginResult:pluginResult callbackId:_downloadCallback];
        _downloadCallback = nil;
    }
    
    // send notification to the default callback
    [self invokeDefaultCallbackWithMessage:pluginResult];
    
    // if it is allowed - launch the installation
    if (_pluginXmlConfig.isUpdatesAutoInstallationAllowed &&
        newConfig.contentConfig.updateTime == HCPUpdateNow &&
        ![HCPUpdateLoader sharedInstance].isDownloadInProgress &&
        ![HCPUpdateInstaller sharedInstance].isInstallationInProgress) {
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
 *  Method is called when installation is about to begin
 *
 *  @param notification captured notification with the event details
 */
- (void)onBeforeInstallEvent:(NSNotification *)notification {
    CDVPluginResult *pluginResult = [CDVPluginResult pluginResultForNotification:notification];
    
    // send notification to the default callback
    [self invokeDefaultCallbackWithMessage:pluginResult];
}

/**
 *  Method is called when error occured during the installation process.
 *
 *  @param notification captured notification with the event details
 */
- (void)onUpdateInstallationErrorEvent:(NSNotification *)notification {
    _pluginInternalPrefs.readyForInstallationReleaseVersionName = @"";
    [_pluginInternalPrefs saveToUserDefaults];
    
    CDVPluginResult *pluginResult = [CDVPluginResult pluginResultForNotification:notification];
    
    // send notification to the caller from the JavaScript side if there was any
    if (_installationCallback) {
        [self.commandDelegate sendPluginResult:pluginResult callbackId:_installationCallback];
        _installationCallback = nil;
    }
    
    // send notification to the default callback
    [self invokeDefaultCallbackWithMessage:pluginResult];
    
    // probably never happens, but just for safety
    NSError *error = notification.userInfo[kHCPEventUserInfoErrorKey];
    [self rollbackIfCorrupted:error];
}

/**
 *  Method is called when update has been installed.
 *
 *  @param notification captured notification with the event details
 */
- (void)onUpdateInstalledEvent:(NSNotification *)notification {
    _appConfig = notification.userInfo[kHCPEventUserInfoApplicationConfigKey];
    
    _pluginInternalPrefs.readyForInstallationReleaseVersionName = @"";
    _pluginInternalPrefs.previousReleaseVersionName = _pluginInternalPrefs.currentReleaseVersionName;
    _pluginInternalPrefs.currentReleaseVersionName = _appConfig.contentConfig.releaseVersion;
    [_pluginInternalPrefs saveToUserDefaults];
    
    _filesStructure = [[HCPFilesStructure alloc] initWithReleaseVersion:_pluginInternalPrefs.currentReleaseVersionName];
    
    CDVPluginResult *pluginResult = [CDVPluginResult pluginResultForNotification:notification];
    
    // send notification to the caller from the JavaScript side of there was any
    if (_installationCallback) {
        [self.commandDelegate sendPluginResult:pluginResult callbackId:_installationCallback];
        _installationCallback = nil;
    }
    
    // send notification to the default callback
    [self invokeDefaultCallbackWithMessage:pluginResult];
    
    // reload application to the index page
    [self loadURL:[self indexPageFromConfigXml]];
    
    [self cleanupFileSystemFromOldReleases];
}

#pragma mark Rollback process

/**
 *  Rollback to the previously installed version of the app.
 */
- (void)rollbackToPreviousRelease {
    _pluginInternalPrefs.readyForInstallationReleaseVersionName = @"";
    _pluginInternalPrefs.currentReleaseVersionName = _pluginInternalPrefs.previousReleaseVersionName;
    _pluginInternalPrefs.previousReleaseVersionName = @"";
    [_pluginInternalPrefs saveToUserDefaults];
    
    _filesStructure = [[HCPFilesStructure alloc] initWithReleaseVersion:_pluginInternalPrefs.currentReleaseVersionName];
    
    if (_appConfig) {
        [self loadApplicationConfig];
    }
    
    [self loadURL:[self indexPageFromConfigXml]];
}

/**
 *  Rollback to the previous/bundled version of the app, if error indicates that current release is corrupted.
 *
 *  @param error captured error
 */
- (void)rollbackIfCorrupted:(NSError *)error {
    if (error.code != kHCPLocalVersionOfApplicationConfigNotFoundErrorCode && error.code != kHCPLocalVersionOfManifestNotFoundErrorCode) {
        return;
    }
    
    if (_pluginInternalPrefs.previousReleaseVersionName.length > 0) {
        NSLog(@"WWW folder is corrupted, rolling back to previous version.");
        [self rollbackToPreviousRelease];
    } else {
        NSLog(@"WWW folder is corrupted, reinstalling it from bundle.");
        [self installWwwFolder];
    }
}

#pragma mark Cleanup process

- (void)cleanupFileSystemFromOldReleases {
    if (!_pluginInternalPrefs.currentReleaseVersionName.length) {
        return;
    }
    
    [HCPCleanupHelper removeUnusedReleasesExcept:@[_pluginInternalPrefs.currentReleaseVersionName,
                                                   _pluginInternalPrefs.previousReleaseVersionName,
                                                   _pluginInternalPrefs.readyForInstallationReleaseVersionName]];
}

#pragma mark Methods, invoked from Javascript

- (void)jsInitPlugin:(CDVInvokedUrlCommand *)command {
    _defaultCallbackID = command.callbackId;
    [self dispatchDefaultCallbackStoredResults];
    
    if (_pluginXmlConfig.isUpdatesAutoDownloadAllowed &&
        ![HCPUpdateLoader sharedInstance].isDownloadInProgress &&
        ![HCPUpdateInstaller sharedInstance].isInstallationInProgress) {
        [self _fetchUpdate:nil withOptions:nil];
    }
}

- (void)jsConfigure:(CDVInvokedUrlCommand *)command {
    if (!_isPluginReadyForWork) {
        [self sendPluginNotReadyToWorkMessageForEvent:nil callbackID:command.callbackId];
        return;
    }
    
    NSDictionary *options = command.arguments[0];
    [_pluginXmlConfig mergeOptionsFromJS:options];
    // TODO: store them somewhere?
    
    CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

- (void)jsFetchUpdate:(CDVInvokedUrlCommand *)command {
    if (!_isPluginReadyForWork) {
        [self sendPluginNotReadyToWorkMessageForEvent:kHCPUpdateDownloadErrorEvent callbackID:command.callbackId];
    }

    NSDictionary *optionsFromJS = command.arguments.count ? command.arguments[0] : nil;
    HCPFetchUpdateOptions *fetchOptions = [[HCPFetchUpdateOptions alloc] initWithDictionary:optionsFromJS];
    
    [self _fetchUpdate:command.callbackId withOptions:fetchOptions];
}

- (void)jsInstallUpdate:(CDVInvokedUrlCommand *)command {
    if (!_isPluginReadyForWork) {
        [self sendPluginNotReadyToWorkMessageForEvent:kHCPUpdateInstallationErrorEvent callbackID:command.callbackId];
        return;
    }
    
    [self _installUpdate:command.callbackId];
}

- (void)jsRequestAppUpdate:(CDVInvokedUrlCommand *)command {
    if (!_isPluginReadyForWork || command.arguments.count == 0) {
        [self sendPluginNotReadyToWorkMessageForEvent:nil callbackID:command.callbackId];
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

- (void)jsIsUpdateAvailableForInstallation:(CDVInvokedUrlCommand *)command {
    NSDictionary *data = nil;
    NSError *error = nil;
    if (_pluginInternalPrefs.readyForInstallationReleaseVersionName.length) {
        data = @{@"currentVersion": _pluginInternalPrefs.currentReleaseVersionName,
                 @"readyToInstallVersion": _pluginInternalPrefs.readyForInstallationReleaseVersionName};
    } else {
        error = [NSError errorWithCode:kHCPNothingToInstallErrorCode description:@"Nothing to install"];
    }
    
    CDVPluginResult *result = [CDVPluginResult pluginResultWithActionName:nil data:data error:error];
    [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
}

- (void)jsGetVersionInfo:(CDVInvokedUrlCommand *)command {
    NSDictionary *data = @{@"currentWebVersion": _pluginInternalPrefs.currentReleaseVersionName,
                           @"readyToInstallWebVersion": _pluginInternalPrefs.readyForInstallationReleaseVersionName,
                           @"previousWebVersion": _pluginInternalPrefs.previousReleaseVersionName,
                           @"appVersion": [NSBundle applicationVersionName],
                           @"buildVersion": [NSBundle applicationBuildVersion]};

    CDVPluginResult *result = [CDVPluginResult pluginResultWithActionName:nil data:data error:nil];
    [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
}

- (void)sendPluginNotReadyToWorkMessageForEvent:(NSString *)eventName callbackID:(NSString *)callbackID {
    NSError *error = [NSError errorWithCode:kHCPAssetsNotYetInstalledErrorCode
                                description:@"WWW folder from the bundle is not yet installed on the external device. Please, wait for this operation to finish."];
    CDVPluginResult *errorResult = [CDVPluginResult pluginResultWithActionName:eventName
                                                                          data:nil
                                                                         error:error];
    [self.commandDelegate sendPluginResult:errorResult callbackId:callbackID];
}

@end
