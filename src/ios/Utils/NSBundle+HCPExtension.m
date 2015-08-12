//
//  NSBundle+Extension.m
//  TestIosCHCP
//
//  Created by Nikolay Demyankov on 10.08.15.
//
//

#import "NSBundle+HCPExtension.h"

@implementation NSBundle (HCPExtension)

+ (NSInteger)applicationBuildVersion {
    NSBundle *mainBundle = [NSBundle mainBundle];
    id appBuildVersion = [mainBundle objectForInfoDictionaryKey: (NSString *)kCFBundleVersionKey];
    if (appBuildVersion == nil) {
        return 0;
    }
    
    return [appBuildVersion integerValue];
}

@end
