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

@interface HCPPlugin() {
    id<HCPFilesStructure> _filesStructure;
    HCPUpdateLoader *_updatesLoader;
    NSString *_defaultCallbackID;
    NSString *_wwwFolderPathInBundle;
    BOOL _isPluginReadyForWork;
    HCPPluginConfig *_pluginConfig;
    HCPUpdateInstaller *_updateInstaller;
    NSMutableArray *_fetchTasks;
    NSString *_installationCallback;
    HCPXmlConfig *_pluginXmllConfig;
    HCPApplicationConfig *_appConfig;
    
    SocketIOClient *_socketIOClient;
}

@end

static NSString *const BLANK_PAGE = @"about:blank";
static NSString *const WWW_FOLDER_IN_BUNDLE = @"www";

@implementation HCPPlugin

#pragma mark Lifecycle

-(void)pluginInitialize {
    [self subscribeToEvents];
    [self doLocalInit];
    [self connectToDevServer];
    
    // install WWW folder if it is needed
    if ([self isWWwFolderNeedsToBeInstalled]) {
        dispatch_async(dispatch_queue_create(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
            [self installWwwFolder];
            [self loadApplicationConfig];
            if (_pluginConfig.isUpdatesAutoDowloadAllowed) {
                [self _fetchUpdate:nil];
            }
        });
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
    if (_pluginConfig.isUpdatesAutoInstallationAllowed && [self _installUpdate:nil]) {
        return;
    }
    
    if (_pluginConfig.isUpdatesAutoDowloadAllowed) {
        [self _fetchUpdate:nil];
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

- (void)installWwwFolder {
    NSError *error = nil;
    NSFileManager *fileManager = [NSFileManager defaultManager];
    BOOL isWWwFolderExists = [fileManager fileExistsAtPath:_filesStructure.wwwFolder.path];
    // remove previous version of the www folder
    if (isWWwFolderExists) {
        [fileManager removeItemAtURL:[_filesStructure.wwwFolder URLByDeletingLastPathComponent] error:&error];
    }
    
    // create new www folder
    if (![fileManager createDirectoryAtURL:[_filesStructure.wwwFolder URLByDeletingLastPathComponent] withIntermediateDirectories:YES attributes:nil error:&error]) {
        NSLog(@"%@", [error.userInfo[NSUnderlyingErrorKey] localizedDescription]);
        return;
    }
    
    // copy www folder from bundle to cache folder
    NSURL *localWww = [NSURL fileURLWithPath:[self pathToWwwFolderInBundle] isDirectory:YES];
    _isPluginReadyForWork = [fileManager copyItemAtURL:localWww toURL:_filesStructure.wwwFolder error:&error];
    if (error) {
        NSLog(@"%@", [error.userInfo[NSUnderlyingErrorKey] localizedDescription]);
        return;
    }
    
    // update stored config with new application build version
    _pluginConfig.appBuildVersion = [NSBundle applicationBuildVersion];
    [_pluginConfig saveToUserDefaults];
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
    
    //TODO: show progress dialog
    
    return YES;
}

- (void)loadURL:(NSURL *)url {
    [self.webView loadRequest:[NSURLRequest requestWithURL:url]];
}

- (void)resetIndexPageToExternalStorage {
    NSString *currentUrl = self.webView.request.URL.path;
    if ([currentUrl containsString:_filesStructure.wwwFolder.path]) {
        return;
    }
    
    if (currentUrl.length == 0 || [currentUrl isEqualToString:BLANK_PAGE]) {
        currentUrl = [self getStartingPagePath];
    }
    
    currentUrl = [currentUrl stringByReplacingOccurrencesOfString:[self pathToWwwFolderInBundle] withString:@""];
    NSString *indexPageExternalPath = [_filesStructure.wwwFolder URLByAppendingPathComponent:currentUrl].path;
    if (![[NSFileManager defaultManager] fileExistsAtPath:indexPageExternalPath]) {
        return;
    }
    
    // rewrite starting page: should load from external storage
    if ([self.viewController isKindOfClass:[CDVViewController class]]) {
        ((CDVViewController *)self.viewController).startPage = [NSURL fileURLWithPath:indexPageExternalPath].absoluteString;
    } else {
        NSLog(@"HotCodePushError: Can't make starting page to be from external storage. Main controller should be of type CDVViewController.");
    }
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
    NSString* path = [[NSBundle mainBundle] pathForResource:@"config" ofType:@"xml"];
    NSURL* url = [NSURL fileURLWithPath:path];
    
    NSXMLParser *configParser = [[NSXMLParser alloc] initWithContentsOfURL:url];
    [configParser setDelegate:((id <NSXMLParserDelegate>)delegate)];
    [configParser parse];
    
    if (delegate.startPage) {
        return delegate.startPage;
    }
    
    return @"index.html";
}

- (NSString *)pathToWwwFolderInBundle {
    if (_wwwFolderPathInBundle == nil) {
        _wwwFolderPathInBundle = [[[NSBundle mainBundle] resourcePath] stringByAppendingPathComponent:WWW_FOLDER_IN_BUNDLE];
    }
    
    return _wwwFolderPathInBundle;
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
    
    // update download events
    [notificationCenter addObserver:self selector:@selector(onUpdateDownloadErrorEvent:) name:kHCPUpdateDownloadErrorEvent object:nil];
    [notificationCenter addObserver:self selector:@selector(onNothingToUpdateEvent:) name:kHCPNothingToUpdateEvent object:nil];
    [notificationCenter addObserver:self selector:@selector(onUpdateIsReadyForInstallation:) name:kHCPUpdateIsReadyForInstallationEvent object:nil];
    
    // update installation events
    [notificationCenter addObserver:self selector:@selector(onUpdateInstallationErrorEvent:) name:kHCPUpdateInstallationErrorEvent object:nil];
    [notificationCenter addObserver:self selector:@selector(onUpdateInstalledEvent:) name:kHCPUpdateIsInstalledEvent object:nil];
    
}

- (void)unsubscribeFromEvents {
    [[NSNotificationCenter defaultCenter] removeObserver:self];
}

#pragma mark Cordova events

- (void)didLoadWebPage:(NSNotification *)notification {
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
    
    // TODO: hide installation progress dialog
    
}

- (void)onUpdateInstalledEvent:(NSNotification *)notification {
    CDVPluginResult *pluginResult = [CDVPluginResult pluginResultForNotification:notification];
    
    if (_installationCallback) {
        [self.commandDelegate sendPluginResult:pluginResult callbackId:_installationCallback];
        _installationCallback = nil;
    }
    
    [self invokeDefaultCallbackWithMessage:pluginResult];
    
    // TODO: remove installation progress dialog
    
    NSURL *startingPageURL = [_filesStructure.wwwFolder URLByAppendingPathComponent:[self getStartingPagePath]];
    [self loadURL:startingPageURL];
}

#pragma mark Methods, invoked from Javascript

- (void)jsInitPlugin:(CDVInvokedUrlCommand *)command {
    _defaultCallbackID = command.callbackId;
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
