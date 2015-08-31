//
//  CDVPluginResult+HCPEvents.m
//
//  Created by Nikolay Demyankov on 13.08.15.
//

#import "CDVPluginResult+HCPEvents.h"
#import "HCPApplicationConfig.h"
#import "HCPEvents.h"

#pragma mark Keys for the plugin result data.

// Used by JavaScript library to process the data, that is sent back from native side.
static NSString *const ACTION_KEY = @"action";

static NSString *const DATA_KEY = @"data";
static NSString *const DATA_USER_INFO_CONFIG = @"config";

static NSString *const ERROR_KEY = @"error";
static NSString *const ERROR_USER_INFO_CODE = @"code";
static NSString *const ERROR_USER_INFO_DESCRIPTION = @"description";

@implementation CDVPluginResult (HCPEvents)

#pragma mark Public API

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

#pragma mark Private API

/**
 *  Create error block that is send back to JavaScript
 *
 *  @param error error information
 *
 *  @return JSON dictionary with error information
 */
+ (NSDictionary *)constructErrorBlock:(NSError *)error {
    return @{ERROR_USER_INFO_CODE: @(error.code),
             ERROR_USER_INFO_DESCRIPTION: error.userInfo[NSLocalizedDescriptionKey]};
}

/**
 *  Create data block that is send back to JavaScript.
 *
 *  @param appConfig attached application config
 *
 *  @return JSON dictionary with user data
 */
+ (NSDictionary *)constructDataBlock:(HCPApplicationConfig *)appConfig {
    return @{DATA_USER_INFO_CONFIG: [appConfig toJson]};
}

@end
