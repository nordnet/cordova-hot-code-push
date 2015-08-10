//
//  HCPApplicationConfig.h
//  TestIosCHCP
//
//  Created by Nikolay Demyankov on 10.08.15.
//
//

#import <Foundation/Foundation.h>

#import "HCPJsonConvertable.h"
#import "HCPContentConfig.h"

@interface HCPApplicationConfig : NSObject<HCPJsonConvertable>

@property (nonatomic, strong, readonly) NSString *storeUrl;
@property (nonatomic, strong, readonly) HCPContentConfig *contentConfig;

@end
