//
//  HCPPluginConfig+UserDefaults.m
//
//  Created by Nikolay Demyankov on 13.08.15.
//

#import "HCPPluginInternalPreferences+UserDefaults.h"

static NSString *const PLUGIN_CONFIG_USER_DEFAULTS_KEY = @"plugin_config";

@implementation HCPPluginInternalPreferences (UserDefaults)

- (void)saveToUserDefaults {
    id json = [self toJson];
    
    NSUserDefaults *userDefaults = [NSUserDefaults standardUserDefaults];
    [userDefaults setObject:json forKey:PLUGIN_CONFIG_USER_DEFAULTS_KEY];
    [userDefaults synchronize];
}

+ (HCPPluginInternalPreferences *)loadFromUserDefaults {
    NSUserDefaults *userDefaults = [NSUserDefaults standardUserDefaults];
    id json = [userDefaults objectForKey:PLUGIN_CONFIG_USER_DEFAULTS_KEY];
    if (json) {
        return [HCPPluginInternalPreferences instanceFromJsonObject:json];
    }
    
    return nil;
}


@end
