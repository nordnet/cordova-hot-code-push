//
//  HCPUpdateProgressEvent.m
//  TestIosCHCP
//
//  Created by Nikolay Demyankov on 12.08.15.
//
//

#import "HCPUpdateProgressEvent.h"

NSString *const kHCPUpdateDownloadTaskIdUserInfoKey = @"HCPUpdateDownloadTaskIdUserInfoKey";
NSString *const kHCPUpdateDownloadApplicationConfigUserInfoKey = @"HCPUpdateDownloadApplicationConfigUserInfoKey";

@interface HCPUpdateProgressEvent() {
    NSString *_eventName;
}

@end

@implementation HCPUpdateProgressEvent

- (instancetype)initWithName:(NSString *)eventName taskId:(NSString *)taskId applicationConfig:(HCPApplicationConfig *)config {
    self = [super init];
    if (self) {
        _appConfig = config;
        _taskId = taskId;
        _eventName = eventName;
    }
        
    return self;
}

- (instancetype)initWithNotification:(NSNotification *)notification {
    self = [super init];
    if (self) {
        _taskId = notification.userInfo[kHCPUpdateDownloadTaskIdUserInfoKey];
        _appConfig = notification.userInfo[kHCPUpdateDownloadApplicationConfigUserInfoKey];
        _eventName = notification.name;
    }
    
    return self;
}

- (NSNotification *)notification {
    return [NSNotification notificationWithName:_eventName object:nil userInfo:self.userInfo];
}

// should be overriden by the child class
+ (id)fromNotification:(NSNotification *)notification {
    return nil;
}

- (NSDictionary *)userInfo {
    NSMutableDictionary *dictionary = [[NSMutableDictionary alloc] init];
    
    if (self.appConfig) {
        dictionary[kHCPUpdateDownloadApplicationConfigUserInfoKey] = self.appConfig;
    }
    
    dictionary[kHCPUpdateDownloadTaskIdUserInfoKey] = self.taskId;
    
    return dictionary;
}

@end
