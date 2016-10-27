//
//  NSString+HCPHash.h
//

#import <Foundation/Foundation.h>

/**
 *  Category for NSString class.
 *  Allow us to determine hash algorithm from NSString instance.
 */
@interface NSString (HCPHash)

/**
 *  Determines hash hash algorithm from the object instance.
 *
 *  @return hash algorithm string
 */
- (NSString *)getHashAlgorithm;

@end
