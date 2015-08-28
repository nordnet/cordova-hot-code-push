//
//  HCPPlugin.m
//  TestIosCHCP
//
//  Created by Nikolay Demyankov on 07.08.15.
//
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
#import "HCPPluginConfig+UserDefaults.h"
#import "HCPUpdateInstaller.h"
#import "NSJSONSerialization+HCPExtension.h"
#import "CDVPluginResult+HCPEvent.h"
#import "HCPXmlConfig.h"
#import "NSBundle+HCPExtension.h"
#import "HCPApplicationConfigStorage.h"
#import "HCPAppUpdateRequestAlertDialog.h"

@interface HCPPlugin() {
    id<HCPFilesStructure> _filesStructure;
    HCPUpdateLoader *_updatesLoader;
    NSString *_defaultCallbackID;
    BOOL _isPluginReadyForWork;
    HCPPluginConfig *_pluginConfig;
    HCPUpdateInstaller *_updateInstaller;
    NSMutableArray *_fetchTasks;
    NSString *_installationCallback;
    HCPXmlConfig *_pluginXmllConfig;
    HCPApplicationConfig *_appConfig;
    HCPAppUpdateRequestAlertDialog *_appUpdateRequestDialog;
    SocketIOClient *_socketIOClient;
}

@end

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
    
    [self performUpdateProcedureOnStart];
}

- (void)onAppTerminate {
    [self unsubscribeFromEvents];
    [self disconnectFromDevServer];
}

- (void)onResume:(NSNotification *)notification {
    NSLog(@"onResume is called");
    if (_pluginConfig.isUpdatesAutoInstallationAllowed && _appConfig.contentConfig.updateTime == HCPUpdateOnResume) {
        [self _installUpdate:nil];
    }
}

- (void)onPause:(NSNotification *)notification {
    NSLog(@"onPause is called");
}

#pragma mark Private API

- (void)performUpdateProcedureOnStart {
    if (_pluginConfig.isUpdatesAutoInstallationAllowed) {
        [self _installUpdate:nil];
    }
}

- (void)loadApplicationConfig {
    id<HCPConfigFileStorage> configStorage = [[HCPApplicationConfigStorage alloc] initWithFileStructure:_filesStructure];
    _appConfig = [configStorage loadFromFolder:_filesStructure.wwwFolder];
}

- (BOOL)isWWwFolderNeedsToBeInstalled {
    NSFileManager *fileManager = [NSFileManager defaultManager];
    BOOL isApplicationUpdated = [NSBundle applicationBuildVersion] > _pluginConfig.appBuildVersion;
    BOOL isWWwFolderExists = [fileManager fileExistsAtPath:_filesStructure.wwwFolder.path];
    
    return isApplicationUpdated || !isWWwFolderExists;
}

- (void)doLocalInit {
    _fetchTasks = [[NSMutableArray alloc] init];
    _filesStructure = [[HCPFilesStructureImpl alloc] init];
    
    _pluginXmllConfig = [HCPXmlConfig loadFromCordovaConfigXml];
    _pluginConfig = [HCPPluginConfig loadFromUserDefaults];
    if (_pluginConfig == nil) {
        _pluginConfig = [HCPPluginConfig defaultConfig];
        [_pluginConfig saveToUserDefaults];
    }
    
    _pluginConfig.configUrl = _pluginXmllConfig.configUrl;
    
    _updatesLoader = [HCPUpdateLoader sharedInstance];
    [_updatesLoader setup:_filesStructure];
    
    _updateInstaller = [HCPUpdateInstaller sharedInstance];
    [_updateInstaller setup:_filesStructure];
}

- (BOOL)_fetchUpdate:(NSString *)callbackId {
    if (!_isPluginReadyForWork) {
        return NO;
    }
    
    NSString *taskId = [_updatesLoader addUpdateTaskToQueueWithConfigUrl:_pluginConfig.configUrl];
    [self storeCallback:callbackId forFetchTask:taskId];
    
    return taskId != nil;
}

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

- (BOOL)_installUpdate:(NSString *)callbackID {
    if (!_isPluginReadyForWork) {
        return NO;
    }

    NSError *error = nil;
    if (![_updateInstaller launchUpdateInstallation:&error]) {
        //TODO: send nothing to update message
        return NO;
    }

    if (callbackID) {
        _installationCallback = callbackID;
    }
    
    return YES;
}

- (void)loadURL:(NSURL *)url {
    [self.webView loadRequest:[NSURLRequest requestWithURL:url]];
}

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

- (NSURL *)appendWwwFolderPathToPath:(NSString *)pagePath {
    if ([pagePath containsString:_filesStructure.wwwFolder.absoluteString]) {
        return [NSURL URLWithString:pagePath];
    }
    
    return [_filesStructure.wwwFolder URLByAppendingPathComponent:pagePath];
}

- (NSString *)getStartingPagePath {
    NSString *startPage = nil;
    if ([self.viewController isKindOfClass:[CDVViewController class]]) {
        startPage = ((CDVViewController *)self.viewController).startPage;
    } else {
        startPage = [self getStartingPageFromConfig];
    }
    
    return startPage;
}

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

- (void)invokeDefaultCallbackWithMessage:(CDVPluginResult *)result {
    if (_defaultCallbackID == nil) {
        return;
    }
    [result setKeepCallbackAsBool:YES];
    
    [self.commandDelegate sendPluginResult:result callbackId:_defaultCallbackID];
}

#pragma mark Events

- (void)subscribeToEvents {
    [self subscriveToLifecycleEvents];
    [self subscribeToCordovaEvents];
    [self subscriveToPluginInternalEvents];
}

- (void)subscribeToCordovaEvents {
    NSNotificationCenter *notificationCenter = [NSNotificationCenter defaultCenter];
    [notificationCenter addObserver:self selector:@selector(didLoadWebPage:) name:CDVPageDidLoadNotification object:nil];
}

- (void)subscriveToLifecycleEvents {
    NSNotificationCenter *notificationCenter = [NSNotificationCenter defaultCenter];
    [notificationCenter addObserver:self selector:@selector(onPause:) name:UIApplicationDidEnterBackgroundNotification object:nil];
    [notificationCenter addObserver:self selector:@selector(onResume:) name:UIApplicationWillEnterForegroundNotification object:nil];
}

- (void)subscriveToPluginInternalEvents {
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
}

- (void)unsubscribeFromEvents {
    [[NSNotificationCenter defaultCenter] removeObserver:self];
}

#pragma mark Cordova events

- (void)didLoadWebPage:(NSNotification *)notification {
}

#pragma mark Bundle installation events

- (void)onAssetsInstalledOnExternalStorageEvent:(NSNotification *)notification {
    // update stored config with new application build version
    _pluginConfig.appBuildVersion = [NSBundle applicationBuildVersion];
    [_pluginConfig saveToUserDefaults];
    
    // allow work
    _isPluginReadyForWork = YES;
    
    // send notification to web
    [self invokeDefaultCallbackWithMessage:[CDVPluginResult pluginResultForNotification:notification]];
    
    // fetch update
    [self loadApplicationConfig];
    if (_pluginConfig.isUpdatesAutoDowloadAllowed) {
        [self _fetchUpdate:nil];
    }
}

- (void)onAssetsInstallationErrorEvent:(NSNotification *)notification {
    _isPluginReadyForWork = NO;
    
    // send notification to web
    [self invokeDefaultCallbackWithMessage:[CDVPluginResult pluginResultForNotification:notification]];
}

#pragma mark Update download events

- (void)onUpdateDownloadErrorEvent:(NSNotification *)notification {
    NSError *error = notification.userInfo[kHCPEventUserInfoErrorKey];
    NSLog(@"Error during update: %@", error.userInfo[NSLocalizedDescriptionKey]);
    
    CDVPluginResult *pluginResult = [CDVPluginResult pluginResultForNotification:notification];
    NSString *callbackID = [self pollCallbackForTask:notification.userInfo[kHCPEventUserInfoTaskIdKey]];
    if (callbackID) {
        [self.commandDelegate sendPluginResult:pluginResult callbackId:callbackID];
    }
    
    [self invokeDefaultCallbackWithMessage:pluginResult];
}

- (void)onNothingToUpdateEvent:(NSNotification *)notification {
    NSLog(@"Nothing to update");
    
    CDVPluginResult *pluginResult = [CDVPluginResult pluginResultForNotification:notification];
    NSString *callbackID = [self pollCallbackForTask:notification.userInfo[kHCPEventUserInfoTaskIdKey]];
    if (callbackID) {
        [self.commandDelegate sendPluginResult:pluginResult callbackId:callbackID];
    }
    
    [self invokeDefaultCallbackWithMessage:pluginResult];
}

- (void)onUpdateIsReadyForInstallation:(NSNotification *)notification {
    NSLog(@"Update is ready for installation");
    
    CDVPluginResult *pluginResult = [CDVPluginResult pluginResultForNotification:notification];
    NSString *callbackID = [self pollCallbackForTask:notification.userInfo[kHCPEventUserInfoTaskIdKey]];
    if (callbackID) {
        [self.commandDelegate sendPluginResult:pluginResult callbackId:callbackID];
    }
    [self invokeDefaultCallbackWithMessage:pluginResult];
    
    HCPApplicationConfig *newConfig = notification.userInfo[kHCPEventUserInfoApplicationConfigKey];
    if (_pluginConfig.isUpdatesAutoInstallationAllowed && newConfig.contentConfig.updateTime == HCPUpdateNow) {
        [self _installUpdate:nil];
    }
}

#pragma mark Update installation events

- (void)onUpdateInstallationErrorEvent:(NSNotification *)notification {
    CDVPluginResult *pluginResult = [CDVPluginResult pluginResultForNotification:notification];
    
    if (_installationCallback) {
        [self.commandDelegate sendPluginResult:pluginResult callbackId:_installationCallback];
        _installationCallback = nil;
    }
    
    [self invokeDefaultCallbackWithMessage:pluginResult];
}

- (void)onUpdateInstalledEvent:(NSNotification *)notification {
    CDVPluginResult *pluginResult = [CDVPluginResult pluginResultForNotification:notification];
    
    if (_installationCallback) {
        [self.commandDelegate sendPluginResult:pluginResult callbackId:_installationCallback];
        _installationCallback = nil;
    }
    
    [self invokeDefaultCallbackWithMessage:pluginResult];
    
    NSURL *startingPageURL = [self appendWwwFolderPathToPath:[self getStartingPagePath]];
    [self loadURL:startingPageURL];
}

#pragma mark Methods, invoked from Javascript

- (void)jsInitPlugin:(CDVInvokedUrlCommand *)command {
    _defaultCallbackID = command.callbackId;
    
    if (_pluginConfig.isUpdatesAutoDowloadAllowed) {
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
    
    [_pluginConfig mergeOptionsFromJS:options];
    [_pluginConfig saveToUserDefaults];
    
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
    if (!_pluginXmllConfig.devOptions.isEnabled || [_socketIOClient connected]) {
        return;
    }
    
    NSString *devServerURL = [_pluginConfig.configUrl URLByDeletingLastPathComponent].absoluteString;
    devServerURL = [devServerURL substringToIndex:devServerURL.length-1];
    
    _socketIOClient = [[SocketIOClient alloc] initWithSocketURL:devServerURL options:nil];
    [_socketIOClient on:@"connect" callback:^(NSArray* data, void (^ack)(NSArray*)) {
        NSLog(@"socket connected");
    }];
    [_socketIOClient on:@"release" callback:^(NSArray* data, void (^ack)(NSArray*)) {
        [self _fetchUpdate:nil];
    }];
    [_socketIOClient connect];

}

- (void)disconnectFromDevServer {
    if (!_pluginXmllConfig.devOptions.isEnabled || ![_socketIOClient connected]) {
        return;
    }
    
    [_socketIOClient closeWithFast:NO];
}

@end
