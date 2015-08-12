//
//  NSError+HCPExtension.h
//  TestIosCHCP
//
//  Created by Nikolay Demyankov on 12.08.15.
//
//

#import <Foundation/Foundation.h>

@interface NSError (HCPExtension)

+ (NSError *)errorWithCode:(NSInteger)errorCode description:(NSString *)description;

@end
