//
//  HCPPluginConfig+UserDefaults.m
//  TestIosCHCP
//
//  Created by Nikolay Demyankov on 13.08.15.
//
//

#import "HCPPluginConfig+UserDefaults.h"

@implementation HCPPluginConfig (UserDefaults)

- (void)saveToUserDefaults {
    id json = [self toJson];
    
    NSUserDefaults *userDefaults = [NSUserDefaults standardUserDefaults];
    [userDefaults setObject:json forKey:@"plugin_config"];
    [userDefaults synchronize];
}

+ (HCPPluginConfig *)loadFromUserDefaults {
    NSUserDefaults *userDefaults = [NSUserDefaults standardUserDefaults];
    
    id json = [userDefaults objectForKey:@"plugin_config"];
    if (json) {
        return [HCPPluginConfig defaultConfig];
    }
    
    return [HCPPluginConfig instanceFromJsonObject:json];
}


@end
