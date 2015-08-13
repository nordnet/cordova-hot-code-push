//
//  CDVPluginResult+HCPEvent.m
//  TestIosCHCP
//
//  Created by Nikolay Demyankov on 13.08.15.
//
//

#import "CDVPluginResult+HCPEvent.h"
#import "HCPApplicationConfig.h"
#import "HCPEvents.h"

static NSString *const ACTION_KEY = @"action";

static NSString *const DATA_KEY = @"data";
static NSString *const DATA_USER_INFO_CONFIG = @"config";

static NSString *const ERROR_KEY = @"error";
static NSString *const ERROR_USER_INFO_CODE = @"code";
static NSString *const ERROR_USER_INFO_DESCRIPTION = @"description";

@implementation CDVPluginResult (HCPEvent)

+ (NSDictionary *)constructErrorBlock:(NSError *)error {
    return @{ERROR_USER_INFO_CODE: @(error.code),
             ERROR_USER_INFO_DESCRIPTION: error.userInfo[NSLocalizedDescriptionKey]};
}

+ (NSDictionary *)constructDataBlock:(HCPApplicationConfig *)appConfig {
    return @{DATA_USER_INFO_CONFIG: [appConfig toJson]};
}

+ (CDVPluginResult *)pluginResultForNotification:(NSNotification *)notification {
    HCPApplicationConfig *appConfig = notification.userInfo[kHCPEventUserInfoApplicationConfigKey];
    NSError *error = notification.userInfo[kHCPEventUserInfoErrorKey];
    
    NSMutableDictionary *jsonObject = [[NSMutableDictionary alloc] init];
    jsonObject[ACTION_KEY] = notification.name;
    
    if (error) {
        jsonObject[ERROR_KEY] = [self constructErrorBlock:error];
    }
    
    if (appConfig) {
        jsonObject[DATA_KEY] = [self constructDataBlock:appConfig];
    }
    
    NSData *jsonData = [NSJSONSerialization dataWithJSONObject:jsonObject options:kNilOptions error:nil];
    NSString *jsonString = [[NSString alloc] initWithData:jsonData encoding:NSUTF8StringEncoding];
    
    return [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:jsonString];
}

@end
