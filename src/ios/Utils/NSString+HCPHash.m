//
//  NSString+HCPHash.m
//

#import <CommonCrypto/CommonDigest.h>
#import "NSData+HCPHash.h"

@implementation NSString (HCPHash)

- (NSString *)getHashAlgorithm  {
    if([self hasPrefix:@":sha1:"]) {
        return @"sha1";
    } else if([self hasPrefix:@":sha256:"]) {
        return @"sha256";
    } else {
        return @"md5";
    }
}



@end
