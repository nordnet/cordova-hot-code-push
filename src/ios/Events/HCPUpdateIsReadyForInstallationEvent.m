//
//  HCPUpdateIsReadyForInstallationEvent.m
//  TestIosCHCP
//
//  Created by Nikolay Demyankov on 12.08.15.
//
//

#import "HCPUpdateIsReadyForInstallationEvent.h"

NSString *const kHCPUpdateIsReadyForInstallationEvent = @"HCPUpdateIsReadyForInstallationEvent";

@implementation HCPUpdateIsReadyForInstallationEvent

- (instancetype)initWithTaskId:(NSString *)taskId applicationConfig:(HCPApplicationConfig *)config {
    return [super initWithName:kHCPUpdateIsReadyForInstallationEvent taskId:taskId applicationConfig:config];
}

@end
