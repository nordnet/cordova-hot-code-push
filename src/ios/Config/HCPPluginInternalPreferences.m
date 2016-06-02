//
//  HCPPluginConfig.m
//
//  Created by Nikolay Demyankov on 10.08.15.
//

#import "HCPPluginInternalPreferences.h"
#import "NSBundle+HCPExtension.h"
#import "HCPApplicationConfig.h"
#import "HCPFilesStructure.h"

#pragma mark JSON keys for plugin options

static NSString *const APPLICATION_BUILD_VERSION = @"app_build_version";
static NSString *const WWW_FOLDER_INSTALLED_FLAG = @"www_folder_installed";
static NSString *const PREVIOUS_RELEASE_VERSION_NAME = @"previous_release_version_name";
static NSString *const CURRENT_RELEASE_VERSION_NAME = @"current_release_version_name";
static NSString *const READY_FOR_INSTALLATION_RELEASE_VERSION_NAME = @"ready_for_installation_release_version_name";

@implementation HCPPluginInternalPreferences

#pragma mark Public API

+ (HCPPluginInternalPreferences *)defaultConfig {
    HCPPluginInternalPreferences *pluginConfig = [[HCPPluginInternalPreferences alloc] init];
    pluginConfig.appBuildVersion = [NSBundle applicationBuildVersion];
    pluginConfig.wwwFolderInstalled = NO;
    pluginConfig.previousReleaseVersionName = @"";
    pluginConfig.readyForInstallationReleaseVersionName = @"";
    
    HCPApplicationConfig *config = [HCPApplicationConfig configFromBundle:[HCPFilesStructure defaultConfigFileName]];
    pluginConfig.currentReleaseVersionName = config.contentConfig.releaseVersion;
    
    return pluginConfig;
}

#pragma mark HCPJsonConvertable implementation

+ (instancetype)instanceFromJsonObject:(id)json {
    if (![json isKindOfClass:[NSDictionary class]]) {
        return nil;
    }
    NSDictionary *jsonObject = json;
    
    HCPPluginInternalPreferences *pluginConfig = [[HCPPluginInternalPreferences alloc] init];
    pluginConfig.appBuildVersion = (NSString *)jsonObject[APPLICATION_BUILD_VERSION];
    pluginConfig.wwwFolderInstalled = [(NSNumber *)jsonObject[WWW_FOLDER_INSTALLED_FLAG] boolValue];
    pluginConfig.currentReleaseVersionName = (NSString *)jsonObject[CURRENT_RELEASE_VERSION_NAME];
    pluginConfig.previousReleaseVersionName = (NSString *)jsonObject[PREVIOUS_RELEASE_VERSION_NAME];
    pluginConfig.readyForInstallationReleaseVersionName = (NSString *)jsonObject[READY_FOR_INSTALLATION_RELEASE_VERSION_NAME];
    
    return pluginConfig;
}

- (id)toJson {
    NSMutableDictionary *jsonObject = [[NSMutableDictionary alloc] init];
    jsonObject[APPLICATION_BUILD_VERSION] = self.appBuildVersion;
    jsonObject[WWW_FOLDER_INSTALLED_FLAG] = [NSNumber numberWithBool:self.isWwwFolderInstalled];
    jsonObject[PREVIOUS_RELEASE_VERSION_NAME] = self.previousReleaseVersionName;
    jsonObject[CURRENT_RELEASE_VERSION_NAME] = self.currentReleaseVersionName;
    jsonObject[READY_FOR_INSTALLATION_RELEASE_VERSION_NAME] = self.readyForInstallationReleaseVersionName;
    
    return jsonObject;
}

@end
