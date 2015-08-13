//
//  HCPPluginConfig.m
//  TestIosCHCP
//
//  Created by Nikolay Demyankov on 10.08.15.
//
//

#import "HCPPluginConfig.h"
#import "NSBundle+HCPExtension.h"

static NSString *const ALLOW_UPDATES_AUTO_DOWNLOAD = @"allow_auto_download";
static NSString *const ALLOW_UPDATE_AUTO_INSTALL = @"allow_auto_install";
static NSString *const CONFIG_URL = @"config_url";
static NSString *const APPLICATION_BUILD_VERSION = @"app_build_version";

@implementation HCPPluginConfig

#pragma mark Public API

+ (instancetype)defaultConfig {
    HCPPluginConfig *pluginConfig = [[HCPPluginConfig alloc] init];
    pluginConfig.allowUpdatesAutoDownload = YES;
    pluginConfig.allowUpdatesAutoInstallation = YES;
    pluginConfig.appBuildVersion = [NSBundle applicationBuildVersion];
    
    return pluginConfig;
}

- (void)mergeOptionsFromJS:(NSDictionary *)jsOptions {
    if (jsOptions[CONFIG_URL]) {
        self.configUrl = [NSURL URLWithString:jsOptions[CONFIG_URL]];
    }
    
    if (jsOptions[ALLOW_UPDATE_AUTO_INSTALL]) {
        self.allowUpdatesAutoInstallation = [(NSNumber *)jsOptions[ALLOW_UPDATE_AUTO_INSTALL] boolValue];
    }
    
    if (jsOptions[ALLOW_UPDATES_AUTO_DOWNLOAD]) {
        self.allowUpdatesAutoDownload = [(NSNumber *)jsOptions[ALLOW_UPDATES_AUTO_DOWNLOAD] boolValue];
    }
}

#pragma mark HCPJsonConvertable implementation

+ (instancetype)instanceFromJsonObject:(id)json {
    if ([json isKindOfClass:[NSDictionary class]]) {
        return nil;
    }
    NSDictionary *jsonObject = json;
    
    HCPPluginConfig *pluginConfig = [[HCPPluginConfig alloc] init];
    pluginConfig.allowUpdatesAutoDownload = [(NSNumber *)jsonObject[ALLOW_UPDATES_AUTO_DOWNLOAD] boolValue];
    pluginConfig.allowUpdatesAutoInstallation = [(NSNumber *)jsonObject[ALLOW_UPDATE_AUTO_INSTALL] boolValue];
    pluginConfig.configUrl = [NSURL URLWithString:jsonObject[CONFIG_URL]];
    pluginConfig.appBuildVersion = [(NSNumber *)jsonObject[APPLICATION_BUILD_VERSION] integerValue];
    
    return pluginConfig;
}

- (id)toJson {
    NSMutableDictionary *jsonObject = [[NSMutableDictionary alloc] init];
    [jsonObject setObject:[NSNumber numberWithBool:self.allowUpdatesAutoDownload] forKey:ALLOW_UPDATES_AUTO_DOWNLOAD];
    [jsonObject setObject:[NSNumber numberWithBool:self.allowUpdatesAutoInstallation] forKey:ALLOW_UPDATE_AUTO_INSTALL];
    [jsonObject setObject:self.configUrl.absoluteString forKey:CONFIG_URL];
    [jsonObject setObject:[NSNumber numberWithInteger:self.appBuildVersion] forKey:APPLICATION_BUILD_VERSION];
    
    return jsonObject;
}

@end
