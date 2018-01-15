//
//  HCPEvents.m
//
//  Created by Nikolay Demyankov on 13.08.15.
//

#import "HCPEvents.h"

#pragma mark Event names declaration

NSString *const kHCPUpdateDownloadErrorEvent = @"chcp_updateLoadFailed";
NSString *const kHCPUpdateDownloadProgressEvent = @"chcp_updateLoadProgress";
NSString *const kHCPNothingToUpdateEvent = @"chcp_nothingToUpdate";
NSString *const kHCPUpdateIsReadyForInstallationEvent = @"chcp_updateIsReadyToInstall";
NSString *const kHCPBeforeInstallEvent = @"chcp_beforeInstall";
NSString *const kHCPUpdateInstallationErrorEvent = @"chcp_updateInstallFailed";
NSString *const kHCPUpdateIsInstalledEvent = @"chcp_updateInstalled";
NSString *const kHCPNothingToInstallEvent = @"chcp_nothingToInstall";
NSString *const kHCPBeforeBundleAssetsInstalledOnExternalStorageEvent = @"chcp_beforeAssetsInstalledOnExternalStorage";
NSString *const kHCPBundleAssetsInstalledOnExternalStorageEvent = @"chcp_assetsInstalledOnExternalStorage";
NSString *const kHCPBundleAssetsInstallationErrorEvent = @"chcp_assetsInstallationError";

NSString *const kHCPEventUserInfoErrorKey = @"error";
NSString *const kHCPEventUserInfoTaskIdKey = @"taskId";
NSString *const kHCPEventUserInfoApplicationConfigKey = @"appConfig";
NSString *const kHCPEventUserInfoProgressKey = @"progress";
NSString *const kHCPEventUserInfoProgressCompletedKey = @"progressCompleted";
NSString *const kHCPEventUserInfoProgressOutstandingKey = @"progressOutstanding";

@implementation HCPEvents

#pragma mark Public API

+ (NSNotification *)notificationWithName:(NSString *)name applicationConfig:(HCPApplicationConfig *)appConfig taskId:(NSString *)taskId {
    return [HCPEvents notificationWithName:name applicationConfig:appConfig taskId:taskId error:nil];
}

+ (NSNotification *)notificationWithName:(NSString *)name applicationConfig:(HCPApplicationConfig *)appConfig taskId:(NSString *)taskId error:(NSError *)error {
    NSMutableDictionary *userInfo = [[NSMutableDictionary alloc] init];
    if (appConfig) {
        userInfo[kHCPEventUserInfoApplicationConfigKey] = appConfig;
    }
    
    if (taskId) {
        userInfo[kHCPEventUserInfoTaskIdKey] = taskId;
    }
    
    if (error) {
        userInfo[kHCPEventUserInfoErrorKey] = error;
    }
    
    return [NSNotification notificationWithName:name object:nil userInfo:userInfo];
}

+ (NSNotification *)notificationWithName:(NSString *)name progress:(double)progress progressCompleted:(double)progressCompleted progressOutstanding:(double)progressOutstanding{
    NSMutableDictionary *userInfo = [[NSMutableDictionary alloc] init];
    if (progress) {
        userInfo[kHCPEventUserInfoProgressKey] = progress;
    }
    
    if (progressCompleted) {
        userInfo[kHCPEventUserInfoProgressCompletedKey] = progressCompleted;
    }
    
    if (progressOutstanding) {
        userInfo[kHCPEventUserInfoProgressOutstandingKey] = progressOutstanding;
    }
    
    userInfo[kHCPEventUserInfoErrorKey] = nil;

    return [NSNotification notificationWithName:name object:nil userInfo:userInfo];
}

@end
