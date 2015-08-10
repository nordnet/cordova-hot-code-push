//
//  NSBundle+Extension.m
//  TestIosCHCP
//
//  Created by Nikolay Demyankov on 10.08.15.
//
//

#import "NSBundle+Extension.h"

@implementation NSBundle (Extension)

+ (NSInteger)applicationBuildVersion {
    NSBundle *mainBundle = [NSBundle mainBundle];
    id appBuildVersion = [mainBundle objectForInfoDictionaryKey: (NSString *)kCFBundleVersionKey];
    if (appBuildVersion == nil) {
        return 0;
    }
    
    return [appBuildVersion integerValue];
}

@end
