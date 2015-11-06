//
//  HCPUpdateLoader.m
//
//  Created by Nikolay Demyankov on 11.08.15.
//

#import "HCPUpdateLoader.h"
#import "HCPUpdateLoaderWorker.h"
#import "HCPUpdateInstaller.h"

@interface HCPUpdateLoader() {
    __block BOOL _isExecuting;
    id<HCPFilesStructure> _filesStructure;
}

@end

@implementation HCPUpdateLoader

#pragma mark Public API

+ (HCPUpdateLoader *)sharedInstance {
    static HCPUpdateLoader *sharedInstance = nil;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        sharedInstance = [[self alloc] init];
    });
    
    return sharedInstance;
}

- (BOOL)isDownloadInProgress {
    return _isExecuting;
}

- (void)setup:(id<HCPFilesStructure>)filesStructure {
    _filesStructure = filesStructure;
}

- (NSString *)addUpdateTaskToQueueWithConfigUrl:(NSURL *)configUrl headers: (NSDictionary*) headers {
    // TODO: add better communication between installer and loader.
    // For now - skip update load request if installation or download is in progress.
    if ([HCPUpdateInstaller sharedInstance].isInstallationInProgress || _isExecuting) {
        return nil;
    }
    HCPUpdateLoaderWorker* task = [[HCPUpdateLoaderWorker alloc] initWithConfigUrl:configUrl filesStructure:_filesStructure];
        
    task.headers = headers;
    [self executeTask:task];
    
    return task.workerId;
}

- (void)executeTask:(id<HCPWorker>)task {
    _isExecuting = YES;
    [task runWithComplitionBlock:^{
        _isExecuting = NO;
    }];
}

@end
