//
//  HCPUpdateLoader.m
//
//  Created by Nikolay Demyankov on 11.08.15.
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

- (void)setup:(id<HCPFilesStructure>)filesStructure {
    _filesStructure = filesStructure;
}

- (NSString *)addUpdateTaskToQueueWithConfigUrl:(NSURL *)configUrl {
    id<HCPWorker> task = [[HCPUpdateLoaderWorker alloc] initWithConfigUrl:configUrl filesStructure:_filesStructure];
    [task run];
    
    return task.workerId;
}


@end
