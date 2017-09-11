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

// 사용자 기본 설정 로드
+ (HCPPluginInternalPreferences *)loadFromUserDefaults {
    // NSUserDefaults는 [value, key]로 저장되는 local storage
    NSUserDefaults *userDefaults = [NSUserDefaults standardUserDefaults];
    id json = [userDefaults objectForKey:PLUGIN_CONFIG_USER_DEFAULTS_KEY];
    if (json) {
        // 값이 있으면 json Object로 반환한다
        return [HCPPluginInternalPreferences instanceFromJsonObject:json];
    }
    
    return nil;
}


@end
