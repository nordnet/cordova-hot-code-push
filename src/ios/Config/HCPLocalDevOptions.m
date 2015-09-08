//
//  HCPLocalDevOptions.m
//
//  Created by Nikolay Demyankov on 07.08.15.
//

#import "HCPLocalDevOptions.h"

@interface HCPLocalDevOptions()

@end

@implementation HCPLocalDevOptions

- (instancetype)init {
    self = [super init];
    if (self) {
        self.enabled = NO;
    }
    
    return self;
}

@end