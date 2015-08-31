//
//  NSData+MD5.h
//
//  Created by Nikolay Demyankov on 10.08.15.
//

#import <Foundation/Foundation.h>

/**
 *  Category for NSData class.
 *  Allow us to generate MD5 hash from NSData instance.
 */
@interface NSData (HCPMD5)

/**
 *  Generate md5 hash string from the object instance.
 *
 *  @return hash string
 */
- (NSString *)md5;

@end
