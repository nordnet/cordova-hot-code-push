//
//  HCPUpdateDownloadError.m
//  TestIosCHCP
//
//  Created by Nikolay Demyankov on 12.08.15.
//
//

#import "HCPUpdateDownloadErrorEvent.h"

NSString *const kHCPUpdateDownloadErrorEventName = @"HCPUpdateDownloadErrorEvent";
NSString *const kHCPUpdateDownloadErrorUserInfoKey = @"HCPUpdateDownloadErrorUserInfoKey";

@interface HCPUpdateDownloadErrorEvent()

@end

@implementation HCPUpdateDownloadErrorEvent

- (instancetype)initWithError:(NSError *)error taskId:(NSString *)taskId applicationConfig:(HCPApplicationConfig *)config {
    self = [super initWithName:kHCPUpdateDownloadErrorEventName taskId:taskId applicationConfig:config];
    if (self) {
        _error = error;
    }
    
    return self;
}

- (instancetype)initWithNotification:(NSNotification *)notification {
    self = [super initWithNotification:notification];
    if (self) {
        _error = notification.userInfo[kHCPUpdateDownloadErrorUserInfoKey];
    }
    
    return self;
}

- (NSDictionary *)userInfo {
    NSMutableDictionary *userInfo = [NSMutableDictionary dictionaryWithDictionary:[super userInfo]];
    userInfo[kHCPUpdateDownloadErrorUserInfoKey] = _error;
    
    return userInfo;
}

+ (id)fromNotification:(NSNotification *)notification {
    return [[HCPUpdateDownloadErrorEvent alloc] initWithNotification:notification];
}

@end
