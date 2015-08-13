//
//  HCPUpdateInstaller.m
//  TestIosCHCP
//
//  Created by Nikolay Demyankov on 12.08.15.
//
//

#import "HCPUpdateInstaller.h"
#import "NSError+HCPExtension.h"
#import "HCPInstallationWorker.h"

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

- (void)setup:(id<HCPFilesStructure>)filesStructure {
    _filesStructure = filesStructure;
}

- (BOOL)launchUpdateInstallation:(NSError **)error {
    *error = nil;
    
    // if installing - exit
    if (_isInstallationInProgress) {
        *error = [NSError errorWithCode:0 description:@"Installation is already in progress"];
        return NO;
    }
    
    // check if there is anything to install
    if (![[NSFileManager defaultManager] fileExistsAtPath:_filesStructure.downloadFolder.absoluteString]) {
        *error = [NSError errorWithCode:0 description:@"Nothing to install"];
        return NO;
    }
    
    // launch installation
    [self execute:[[HCPInstallationWorker alloc] initWithFileStructure:_filesStructure]];
    
    return YES;
}

#pragma mark Private API

- (void)execute:(id<HCPWorker>)worker {
    dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
        _isInstallationInProgress = YES;
        [worker run];
        _isInstallationInProgress = NO;
    });
}

@end
