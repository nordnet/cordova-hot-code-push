//
//  HCPPluginConfig.m
//
//  Created by Nikolay Demyankov on 10.08.15.
//

#import "HCPPluginInternalPreferences.h"
#import "NSBundle+HCPExtension.h"

#pragma mark JSON keys for plugin options

static NSString *const APPLICATION_BUILD_VERSION = @"app_build_version";
static NSString *const WWW_FOLDER_INSTALLED_FLAG = @"www_folder_installed";

@implementation HCPPluginInternalPreferences

#pragma mark Public API

+ (HCPPluginInternalPreferences *)defaultConfig {
    HCPPluginInternalPreferences *pluginConfig = [[HCPPluginInternalPreferences alloc] init];
    pluginConfig.appBuildVersion = [NSBundle applicationBuildVersion];
    pluginConfig.wwwFolderInstalled = NO;
    
    return pluginConfig;
}

#pragma mark HCPJsonConvertable implementation

+ (instancetype)instanceFromJsonObject:(id)json {
    if (![json isKindOfClass:[NSDictionary class]]) {
        return nil;
    }
    NSDictionary *jsonObject = json;
    
    HCPPluginInternalPreferences *pluginConfig = [[HCPPluginInternalPreferences alloc] init];
    pluginConfig.appBuildVersion = [(NSNumber *)jsonObject[APPLICATION_BUILD_VERSION] integerValue];
    pluginConfig.wwwFolderInstalled = [(NSNumber *)jsonObject[WWW_FOLDER_INSTALLED_FLAG] boolValue];
    
    return pluginConfig;
}

- (id)toJson {
    NSMutableDictionary *jsonObject = [[NSMutableDictionary alloc] init];
    [jsonObject setObject:[NSNumber numberWithInteger:self.appBuildVersion] forKey:APPLICATION_BUILD_VERSION];
    [jsonObject setObject:[NSNumber numberWithBool:self.isWwwFolderInstalled] forKey:WWW_FOLDER_INSTALLED_FLAG];
    
    return jsonObject;
}

@end
