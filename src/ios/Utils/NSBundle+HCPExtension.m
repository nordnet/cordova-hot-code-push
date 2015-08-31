//
//  NSBundle+Extension.m
//
//  Created by Nikolay Demyankov on 10.08.15.
//

#import "NSBundle+HCPExtension.h"
#import "NSError+HCPExtension.h"
#import "HCPEvents.h"

static NSString *const WWW_FOLDER_IN_BUNDLE = @"www";

@implementation NSBundle (HCPExtension)

#pragma mark Public AIP

+ (NSInteger)applicationBuildVersion {
    NSBundle *mainBundle = [NSBundle mainBundle];
    id appBuildVersion = [mainBundle objectForInfoDictionaryKey: (NSString *)kCFBundleVersionKey];
    if (appBuildVersion == nil) {
        return 0;
    }
    
    return [appBuildVersion integerValue];
}

+ (NSString *)pathToWwwFolder {
    return [[[NSBundle mainBundle] resourcePath] stringByAppendingPathComponent:WWW_FOLDER_IN_BUNDLE];
}

+ (NSString *)pathToCordovaConfigXml {
    return [[NSBundle mainBundle] pathForResource:@"config" ofType:@"xml"];
}

+ (void)installWwwFolderToExternalStorageFolder:(NSURL *)externalFolderURL {
    dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
        [NSBundle __installWwwFolderToExternalStorageFolder:externalFolderURL];
    });
}

#pragma mark Private API

+ (void)__installWwwFolderToExternalStorageFolder:(NSURL *)externalFolderURL {
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
        return;
    }
    
    [self dispatchSuccessEvent];
}

/**
 *  Send event with error information.
 *
 *  @param error occured error
 */
+ (void)dispatchErrorEvent:(NSError *)error {
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
+ (void)dispatchSuccessEvent {
    NSNotification *notification = [HCPEvents notificationWithName:kHCPBundleAssetsInstalledOnExternalStorageEvent
                                                 applicationConfig:nil
                                                            taskId:nil];
    [[NSNotificationCenter defaultCenter] postNotification:notification];
}

@end
