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
    HCPFilesStructure *_filesStructure;
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

- (BOOL)installVersion:(NSString *)newVersion currentVersion:(NSString *)currentVersion error:(NSError **)error {
    *error = nil;
    
    // if installing - exit
    if (_isInstallationInProgress) {
        *error = [NSError errorWithCode:kHCPInstallationAlreadyInProgressErorrCode
                            description:@"Installation is already in progress"];
        return NO;
    }
    
    // if download in progress - exit
    if ([HCPUpdateLoader sharedInstance].isDownloadInProgress) {
        *error = [NSError errorWithCode:kHCPCantInstallWhileDownloadInProgressErrorCode
                            description:@"Can't perform the installation, while update download in progress"];
        return NO;
    }
    
    HCPFilesStructure *newVersionFS = [[HCPFilesStructure alloc] initWithReleaseVersion:newVersion];
    
    // check if there is anything to install
    if (![[NSFileManager defaultManager] fileExistsAtPath:newVersionFS.downloadFolder.path] ||
            [newVersion isEqualToString:currentVersion]) {
        *error = [NSError errorWithCode:kHCPNothingToInstallErrorCode description:@"Nothing to install"];
        return NO;
    }
    
    // launch installation
    id<HCPWorker> installationTask = [[HCPInstallationWorker alloc] initWithNewVersion:newVersion currentVersion:currentVersion];
    [self execute:installationTask];
    
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
