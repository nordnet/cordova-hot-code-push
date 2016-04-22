//
//  HCPAssetsFolderHelper.m
//
//  Created by Nikolay Demyankov on 03.11.15.
//

#import "HCPAssetsFolderHelper.h"
#import "HCPEvents.h"
#import "NSError+HCPExtension.h"
#import "NSBundle+HCPExtension.h"

@interface HCPAssetsFolderHelper()

/**
 *  Flag to check if installation is in progress, so if two pages will request the installation - they don't conflict
 */
@property (nonatomic) BOOL isWorking;

@end

@implementation HCPAssetsFolderHelper

#pragma mark Public API

+ (void)installWwwFolderToExternalStorageFolder:(NSURL *)externalFolderURL {
    HCPAssetsFolderHelper *helper = [HCPAssetsFolderHelper sharedInstance];
    if (helper.isWorking) {
        return;
    }
    helper.isWorking = YES;
    [helper dispatchBeforeInstallEvent];
    
    dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
        [helper __installWwwFolderToExternalStorageFolder:externalFolderURL];
    });
}

#pragma mark Private API

+ (HCPAssetsFolderHelper *)sharedInstance {
    static HCPAssetsFolderHelper *sharedInstance = nil;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        sharedInstance = [[HCPAssetsFolderHelper alloc] init];
    });
    
    return sharedInstance;
}

- (void)__installWwwFolderToExternalStorageFolder:(NSURL *)externalFolderURL {
    NSError *error = nil;
    NSFileManager *fileManager = [NSFileManager defaultManager];
    BOOL isWWwFolderExists = [fileManager fileExistsAtPath:externalFolderURL.path];
    
    // remove previous version of the www folder
    if (isWWwFolderExists) {
        [fileManager removeItemAtURL:[externalFolderURL URLByDeletingLastPathComponent] error:&error];
    }
    
    // create new www folder
    if (![fileManager createDirectoryAtURL:[externalFolderURL URLByDeletingLastPathComponent] withIntermediateDirectories:YES attributes:nil error:&error]) {
        [self dispatchErrorEvent:error];
        return;
    }
    
    // copy www folder from bundle to cache folder
    NSURL *localWww = [NSURL fileURLWithPath:[NSBundle pathToWwwFolder] isDirectory:YES];
    [fileManager copyItemAtURL:localWww toURL:externalFolderURL error:&error];
    if (error) {
        [self dispatchErrorEvent:error];
    } else {
        [self dispatchSuccessEvent];
    }
    
    self.isWorking = NO;
}

/**
 *  Send event with error information.
 *
 *  @param error occured error
 */
- (void)dispatchErrorEvent:(NSError *)error {
    NSString *errorMsg = [error.userInfo[NSUnderlyingErrorKey] localizedDescription];
    NSError *pluginError = [NSError errorWithCode:kHCPFailedToInstallAssetsOnExternalStorageErrorCode description:errorMsg];
    NSNotification *notification = [HCPEvents notificationWithName:kHCPBundleAssetsInstallationErrorEvent
                                                 applicationConfig:nil
                                                            taskId:nil
                                                             error:pluginError];
    
    [[NSNotificationCenter defaultCenter] postNotification:notification];
}

/**
 *  Send success event.
 */
- (void)dispatchSuccessEvent {
    NSNotification *notification = [HCPEvents notificationWithName:kHCPBundleAssetsInstalledOnExternalStorageEvent
                                                 applicationConfig:nil
                                                            taskId:nil];
    [[NSNotificationCenter defaultCenter] postNotification:notification];
}

/**
 *  Send before assets installed event.
 */
- (void)dispatchBeforeInstallEvent {
    NSNotification *notification = [HCPEvents notificationWithName:kHCPBeforeBundleAssetsInstalledOnExternalStorageEvent
                                                 applicationConfig:nil
                                                            taskId:nil];
    [[NSNotificationCenter defaultCenter] postNotification:notification];
}

@end
