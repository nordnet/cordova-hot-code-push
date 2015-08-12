//
//  HCPUpdateIsReadyForInstallationEvent.h
//  TestIosCHCP
//
//  Created by Nikolay Demyankov on 12.08.15.
//
//

#import "HCPUpdateProgressEvent.h"

extern NSString *const kHCPUpdateIsReadyForInstallationEvent;

@interface HCPUpdateIsReadyForInstallationEvent : HCPUpdateProgressEvent

- (instancetype)initWithTaskId:(NSString *)taskId applicationConfig:(HCPApplicationConfig *)config;

@end
