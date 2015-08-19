//
//  NSError+HCPExtension.m
//  TestIosCHCP
//
//  Created by Nikolay Demyankov on 12.08.15.
//
//

#import "NSError+HCPExtension.h"

NSString *const kHCPPluginErrorDomain = @"HCPPluginError";

@implementation NSError (HCPExtension)

+ (NSError *)errorWithCode:(NSInteger)errorCode description:(NSString *)description {
    NSDictionary *userInfo = @{NSLocalizedDescriptionKey: description};
    
    return [NSError errorWithDomain:kHCPPluginErrorDomain code:errorCode userInfo:userInfo];
}

@end
