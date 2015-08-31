//
//  HCPPluginConfig+UserDefaults.m
//
//  Created by Nikolay Demyankov on 13.08.15.
//

#import "HCPPluginConfig+UserDefaults.h"

static NSString *const PLUGIN_CONFIG_USER_DEFAULTS_KEY = @"plugin_config";

@implementation HCPPluginConfig (UserDefaults)

- (void)saveToUserDefaults {
    id json = [self toJson];
    
    NSUserDefaults *userDefaults = [NSUserDefaults standardUserDefaults];
    [userDefaults setObject:json forKey:PLUGIN_CONFIG_USER_DEFAULTS_KEY];
    [userDefaults synchronize];
}

+ (HCPPluginConfig *)loadFromUserDefaults {
    NSUserDefaults *userDefaults = [NSUserDefaults standardUserDefaults];
    id json = [userDefaults objectForKey:PLUGIN_CONFIG_USER_DEFAULTS_KEY];
    if (json) {
        return [HCPPluginConfig instanceFromJsonObject:json];
    }
    
    return nil;
}


@end
