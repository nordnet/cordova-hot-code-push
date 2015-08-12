//
//  HCPUpdateProgressEvent.h
//  TestIosCHCP
//
//  Created by Nikolay Demyankov on 12.08.15.
//
//

#import <Foundation/Foundation.h>
#import "HCPApplicationConfig.h"
#import "HCPNotificationCenterEvent.h"

extern NSString *const kHCPUpdateDownloadTaskIdUserInfoKey;
extern NSString *const kHCPUpdateDownloadApplicationConfigUserInfoKey;

@interface HCPUpdateProgressEvent : NSObject<HCPNotificationCenterEvent>

@property (nonatomic, strong, readonly) NSString *taskId;
@property (nonatomic, strong, readonly) HCPApplicationConfig *appConfig;

- (instancetype)initWithName:(NSString *)eventName taskId:(NSString *)taskId applicationConfig:(HCPApplicationConfig *)config;

@property (nonatomic, strong) NSDictionary *userInfo;

@end
