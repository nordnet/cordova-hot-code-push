//
//  NSData+HCPHash.m
//

#import <CommonCrypto/CommonDigest.h>
#import "NSData+HCPHash.h"

@implementation NSData (HCPHash)

- (NSString *)hashWithAlgorithm:(NSString *) algorithm  {
    // Create byte array of unsigned chars
    unsigned int digestLength = 0;
    NSMutableString *output;
    
    if([algorithm isEqualToString:@"md5"]) {
        digestLength = CC_MD5_DIGEST_LENGTH;
        output = [NSMutableString stringWithCapacity:(CC_MD5_DIGEST_LENGTH * 2)];
    } else if([algorithm isEqualToString:@"sha1"]) {
        digestLength = CC_SHA1_DIGEST_LENGTH;
        output = [NSMutableString stringWithCapacity:(CC_SHA1_DIGEST_LENGTH * 2 + 6)];
        [output appendString:@":sha1:"];
    } else if([algorithm isEqualToString:@"sha256"]) {
        digestLength = CC_SHA256_DIGEST_LENGTH;
        output = [NSMutableString stringWithCapacity:(CC_SHA256_DIGEST_LENGTH * 2 + 8)];
        [output appendString:@":sha256:"];
    }

    unsigned char hashBuffer[digestLength];
    
    // Create hash value, store in buffer
    if([algorithm isEqualToString:@"md5"]) {
        CC_MD5(self.bytes, (unsigned int)self.length, hashBuffer);
    } else if([algorithm isEqualToString:@"sha1"]) {
        CC_SHA1(self.bytes, (unsigned int)self.length, hashBuffer);
    } else if([algorithm isEqualToString:@"sha256"]) {
        CC_SHA256(self.bytes, (unsigned int)self.length, hashBuffer);
    }

    // Convert unsigned char buffer to NSString of hex values
    for(int i = 0; i < digestLength; i++) {
        [output appendFormat:@"%02x",hashBuffer[i]];
    }
    
    return output;
}



@end
