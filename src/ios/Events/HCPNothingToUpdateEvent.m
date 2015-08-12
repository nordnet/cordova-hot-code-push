//
//  HCPNothingToUpdateEvent.m
//  TestIosCHCP
//
//  Created by Nikolay Demyankov on 12.08.15.
//
//

#import "HCPNothingToUpdateEvent.h"

NSString *const kHCPNothingToUpdateEventName = @"HCPNothingToUpdateEventName";

@implementation HCPNothingToUpdateEvent

- (instancetype)initWithTaskId:(NSString *)taskId applicationConfig:(HCPApplicationConfig *)config {
    return [super initWithName:kHCPNothingToUpdateEventName taskId:taskId applicationConfig:config];
}

@end
