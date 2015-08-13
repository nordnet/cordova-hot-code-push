//
//  HCPPlugin.m
//  TestIosCHCP
//
//  Created by Nikolay Demyankov on 07.08.15.
//
//

#import "HCPPlugin.h"
#import "HCPApplicationConfig+Downloader.h"
#import "HCPContentManifest+Downloader.h"
#import "HCPFileDownloader.h"
#import "HCPFilesStructure.h"
#import "HCPFilesStructureImpl.h"
#import "HCPUpdateLoader.h"
#import "HCPEvents.h"

// Socket IO support:
// 1) Add hook to copy files from: https://github.com/socketio/socket.io-client-swift/tree/master/SocketIOClientSwift
// 2) Add hook to enable support for swift: https://github.com/cowbell/cordova-plugin-geofence/blob/20de72b918c779511919f7e38d07721112d4f5c8/hooks/add_swift_support.js
// Additional info: http://stackoverflow.com/questions/25448976/how-to-write-cordova-plugin-in-swift

@interface HCPPlugin() {
    id<HCPFilesStructure> _filesStructure;
    HCPUpdateLoader *_updatesLoader;
}

@end

@implementation HCPPlugin

// TODO: test when update is running and we press Home button

#pragma mark Lifecycle

-(void)pluginInitialize {
    _filesStructure = [[HCPFilesStructureImpl alloc] init];
    
    _updatesLoader = [HCPUpdateLoader sharedInstance];
    [_updatesLoader setup:_filesStructure];
    
    //NSString *path = [[NSBundle mainBundle] pathForResource:@"index" ofType:@"html" inDirectory:@"www"];
    //NSLog(@"path is: %@", path);
    
    //NSLog(@"WWW directory: %@", [[[NSBundle mainBundle] resourcePath] stringByAppendingPathComponent:@"www"]);
    
    [self subscribeToEvents];
    
    [_updatesLoader addUpdateTaskToQueueWithConfigUrl:nil];
}

- (void)onAppTerminate {
    [self unsubscribeFromEvents];
}

- (void)onResume:(NSNotification *)notification {
    NSLog(@"onResume is called");
    
    
}

- (void)onPause:(NSNotification *)notification {
    NSLog(@"onPause is called");
}

#pragma mark Events

- (void)subscribeToEvents {
    [self subscriveToLifecycleEvents];
    [self subscriveToPluginInternalEvents];
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

#pragma mark Update download events

- (void)onUpdateDownloadErrorEvent:(NSNotification *)notification {
    NSError *error = notification.userInfo[kHCPEventUserInfoErrorKey];
    
    NSLog(@"Error during update: %@", error.userInfo[NSLocalizedDescriptionKey]);
}

- (void)onNothingToUpdateEvent:(NSNotification *)notification {
    
    NSLog(@"Nothing to update");
}

- (void)onUpdateIsReadyForInstallation:(NSNotification *)notification {
    
    NSLog(@"Update is ready for installation");
}

#pragma mark Update installation events

- (void)onUpdateInstallationErrorEvent:(NSNotification *)notification {
    
}

- (void)onUpdateInstalledEvent:(NSNotification *)notification {
    
}

@end
