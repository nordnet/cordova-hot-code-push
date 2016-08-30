//
//  NSBundle+Extension.m
//
//  Created by Nikolay Demyankov on 10.08.15.
//

#import "NSBundle+HCPExtension.h"
#import "NSError+HCPExtension.h"
#import "HCPEvents.h"

static NSString *const WWW_FOLDER_IN_BUNDLE = @"www";

@implementation NSBundle (HCPExtension)

#pragma mark Public API

+ (NSString *)applicationBuildVersion {
    NSBundle *mainBundle = [NSBundle mainBundle];
    
    return [mainBundle objectForInfoDictionaryKey:@"CFBundleVersion"];
}

+ (NSString *)applicationVersionName {
    NSBundle *mainBundle = [NSBundle mainBundle];
    
    return [mainBundle objectForInfoDictionaryKey:@"CFBundleShortVersionString"];
}

+ (NSString *)pathToWwwFolder {
    return [[[NSBundle mainBundle] resourcePath] stringByAppendingPathComponent:WWW_FOLDER_IN_BUNDLE];
}

+ (NSString *)pathToCordovaConfigXml {
    return [[NSBundle mainBundle] pathForResource:@"config" ofType:@"xml"];
}

@end
