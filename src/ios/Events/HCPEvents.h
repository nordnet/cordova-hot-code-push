//
//  HCPEvents.h
//  TestIosCHCP
//
//  Created by Nikolay Demyankov on 13.08.15.
//
//

#import <Foundation/Foundation.h>
#import "HCPApplicationConfig.h"

extern NSString *const kHCPUpdateDownloadErrorEvent;
extern NSString *const kHCPNothingToUpdateEvent;
extern NSString *const kHCPUpdateIsReadyForInstallationEvent;
extern NSString *const kHCPUpdateInstallationErrorEvent;
extern NSString *const kHCPUpdateIsInstalledEvent;

extern NSString *const kHCPEventUserInfoErrorKey;
extern NSString *const kHCPEventUserInfoTaskIdKey;
extern NSString *const kHCPEventUserInfoApplicationConfigKey;

@interface HCPEvents : NSObject

+ (NSNotification *)notificationWithName:(NSString *)name applicationConfig:(HCPApplicationConfig *)appConfig taskId:(NSString *)taskId error:(NSError *)error;
+ (NSNotification *)notificationWithName:(NSString *)name applicationConfig:(HCPApplicationConfig *)appConfig taskId:(NSString *)taskId;

@end
