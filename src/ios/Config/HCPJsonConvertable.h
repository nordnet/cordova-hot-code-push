//
//  HCPJsonConvertable.h
//  TestIosCHCP
//
//  Created by Nikolay Demyankov on 07.08.15.
//
//

#import <Foundation/Foundation.h>

@protocol HCPJsonConvertable <NSObject>

- (NSString *)toJsonString;

+ (instancetype)fromJsonString:(NSString *)json;

@end
