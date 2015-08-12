//
//  HCPNothingToUpdateEvent.h
//  TestIosCHCP
//
//  Created by Nikolay Demyankov on 12.08.15.
//
//

#import "HCPUpdateProgressEvent.h"

extern NSString *const kHCPNothingToUpdateEventName;

@interface HCPNothingToUpdateEvent : HCPUpdateProgressEvent

- (instancetype)initWithTaskId:(NSString *)taskId applicationConfig:(HCPApplicationConfig *)config;

@end
