//
//  NSError+HCPExtension.m
//  TestIosCHCP
//
//  Created by Nikolay Demyankov on 12.08.15.
//
//

#import "NSError+HCPExtension.h"

static NSString *const ERROR_DOMAIN = @"HCPError";

@implementation NSError (HCPExtension)

+ (NSError *)errorWithCode:(NSInteger)errorCode description:(NSString *)description {
    NSDictionary *userInfo = @{NSLocalizedDescriptionKey: description};
    
    return [[NSError alloc] initWithDomain:ERROR_DOMAIN code:errorCode userInfo:userInfo];
}

@end
