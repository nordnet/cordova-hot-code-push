//
//  HCPUpdateInstaller.m
//
//  Created by Nikolay Demyankov on 12.08.15.
//

#import "HCPUpdateInstaller.h"
#import "NSError+HCPExtension.h"
#import "HCPInstallationWorker.h"
#import "HCPUpdateLoader.h"
#import "HCPEvents.h"

@interface HCPUpdateInstaller() {
    id<HCPFilesStructure> _filesStructure;
}

@property (nonatomic, readwrite, getter=isInstallationInProgress) BOOL isInstallationInProgress;

@end

@implementation HCPUpdateInstaller

#pragma mark Public API

+ (HCPUpdateInstaller *)sharedInstance {
    static HCPUpdateInstaller *sharedInstance = nil;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        sharedInstance = [[self alloc] init];
    });
    
    return sharedInstance;
}

- (BOOL)installVersion:(id<HCPFilesStructure>)newVersionFS currentRelease:(id<HCPFilesStructure>)currentReleaseFS error:(NSError **)error {
    *error = nil;
    
    // if installing - exit
    if (_isInstallationInProgress) {
        *error = [NSError errorWithCode:0 description:@"Installation is already in progress"];
        return NO;
    }
    
    // check if there is anything to install
    if (![[NSFileManager defaultManager] fileExistsAtPath:newVersionFS.downloadFolder.path]) {
        *error = [NSError errorWithCode:kHCPNothingToInstallErrorCode description:@"Nothing to install"];
        return NO;
    }
    
    // launch installation
    [self execute:[[HCPInstallationWorker alloc] initWithNewReleaseFS:newVersionFS currentReleaseFS:currentReleaseFS]];
    
    return YES;
}

#pragma mark Private API

- (void)execute:(id<HCPWorker>)worker {
    _isInstallationInProgress = YES;
    dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
        [worker runWithComplitionBlock:nil];
        _isInstallationInProgress = NO;
    });
}

@end
