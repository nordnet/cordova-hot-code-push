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
#import "HCPUpdateDownloadErrorEvent.h"

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
    [notificationCenter addObserver:self selector:@selector(onUpdateDownloadErrorEvent:) name:kHCPUpdateDownloadErrorEventName object:nil];
}

- (void)unsubscribeFromEvents {
    [[NSNotificationCenter defaultCenter] removeObserver:self];
}

- (void)onUpdateDownloadErrorEvent:(NSNotification *)notification {
    HCPUpdateDownloadErrorEvent *event = [HCPUpdateDownloadErrorEvent fromNotification:notification];
    
    NSLog(@"Error during update: %@", event.error.userInfo[NSLocalizedDescriptionKey]);
}

@end
