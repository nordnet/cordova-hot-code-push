//
//  HCPContentConfig.h
//  TestIosCHCP
//
//  Created by Nikolay Demyankov on 10.08.15.
//
//

#import <Foundation/Foundation.h>

#import "HCPJsonConvertable.h"

typedef NS_ENUM(NSUInteger, HCPUpdateTime) {
    HCPUpdateTimeUndefined = 0,
    HCPUpdateNow = 1,
    HCPUpdateOnStart = 2,
    HCPUpdateOnResume = 3
};

@interface HCPContentConfig : NSObject<HCPJsonConvertable>

@property (nonatomic, strong, readonly) NSString *releaseVersion;
@property (nonatomic, readonly) NSInteger minimumNativeVersion;
@property (nonatomic, strong, readonly) NSString *contentUrl;
@property (nonatomic, readonly) HCPUpdateTime updateTime;

@end
