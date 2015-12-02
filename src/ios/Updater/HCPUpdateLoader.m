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

- (NSString *)addUpdateTaskToQueueWithConfigUrl:(NSURL *)configUrl {
    // TODO: add better communication between installer and loader.
    if (_isExecuting) {
        return nil;
    }
    
    id<HCPWorker> task = [[HCPUpdateLoaderWorker alloc] initWithConfigUrl:configUrl filesStructure:_filesStructure];
    [self executeTask:task];
    
    return task.workerId;
}

- (void)executeTask:(id<HCPWorker>)task {
    _isExecuting = YES;
    // execute in background, so the callbacks don't block main thread
    dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
        [task runWithComplitionBlock:^{
            _isExecuting = NO;
        }];
    });
}

@end
