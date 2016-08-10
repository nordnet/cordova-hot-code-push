//
//  NSData+HCPHash.h
//

#import <Foundation/Foundation.h>

/**
 *  Category for NSData class.
 *  Allow us to generate hash from NSData instance.
 */
@interface NSData (HCPHash)

/**
 *  Generate hash string from the object instance.
 *
 *  @return hash string
 */
- (NSString *)hashWithAlgorithm:(NSString *) algorithm;

@end
