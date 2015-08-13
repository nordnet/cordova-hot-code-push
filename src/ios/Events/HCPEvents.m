//
//  HCPEvents.m
//  TestIosCHCP
//
//  Created by Nikolay Demyankov on 13.08.15.
//
//

#import "HCPEvents.h"

NSString *const kHCPUpdateDownloadErrorEvent = @"HCPUpdateDownloadErrorEvent";
NSString *const kHCPNothingToUpdateEvent = @"HCPNothingToUpdateEvent";
NSString *const kHCPUpdateIsReadyForInstallationEvent = @"HCPUpdateIsReadyForInstallationEvent";
NSString *const kHCPUpdateInstallationErrorEvent = @"HCPUpdateInstallationErrorEvent";
NSString *const kHCPUpdateIsInstalledEvent = @"HCPUpdateIsInstalledEvent";

NSString *const kHCPEventUserInfoErrorKey = @"error";
NSString *const kHCPEventUserInfoTaskIdKey = @"taskId";
NSString *const kHCPEventUserInfoApplicationConfigKey = @"appConfig";

@implementation HCPEvents

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


@end
