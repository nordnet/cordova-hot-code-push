//
//  HCPUpdateLoader.m
//  TestIosCHCP
//
//  Created by Nikolay Demyankov on 11.08.15.
//
//

#import "HCPUpdateLoader.h"
#import "HCPUpdateLoaderWorker.h"

@interface HCPUpdateLoader() {
    BOOL _isExecuting;
    id<HCPWorker> _scheduledTask;
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
    id<HCPWorker> task = [[HCPUpdateLoaderWorker alloc] initWithConfigUrl:configUrl filesStructure:_filesStructure];
    if (_isExecuting) {
        _scheduledTask = task;
    } else {
        [self executeTask:task];
    }
    
    return task.workerId;
}

#pragma mark Private API

- (void)executeTask:(id<HCPWorker>)task {
    dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
        _isExecuting = YES;
        [task run];
        if (_scheduledTask) {
            [self executeTask:_scheduledTask];
            _scheduledTask = nil;
        } else {
            _isExecuting = NO;
        }
    });
}

@end
