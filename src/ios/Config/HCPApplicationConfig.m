//
//  HCPApplicationConfig.m
//
//  Created by Nikolay Demyankov on 10.08.15.
//

#import "HCPApplicationConfig.h"

@interface HCPApplicationConfig() {
    NSString *_storeUrl;
}

@property (nonatomic, strong) NSString *storeIdentifier;
@property (nonatomic, strong, readwrite) HCPContentConfig *contentConfig;

@end

#pragma mark JSON keys declaration

static NSString *const STORE_PACKAGE_IDENTIFIER_JSON_KEY = @"ios_identifier";

#pragma mark Local constants

static NSString *const STORE_URL_TEMPLATE = @"https://itunes.apple.com/app/%@";

@implementation HCPApplicationConfig

#pragma mark Public API

- (NSString *)storeUrl {
    if (self.storeIdentifier.length == 0) {
        return nil;
    }
    
    if (_storeUrl == nil) {
        if ([self.storeIdentifier containsString:@"http://"] || [self.storeIdentifier containsString:@"https://"]) {
            _storeUrl = self.storeIdentifier;
        } else {
            _storeUrl = [NSString stringWithFormat:STORE_URL_TEMPLATE, self.storeIdentifier];
        }
    }
    
    return _storeUrl;
}

#pragma mark HCPJsonConvertable implementation

- (id)toJson {
    NSMutableDictionary *jsonObject;
    NSDictionary *contentConfigJsonObject = [self.contentConfig toJson];
    
    if (contentConfigJsonObject) {
        jsonObject = [[NSMutableDictionary alloc] initWithDictionary:contentConfigJsonObject];
    } else {
        jsonObject = [[NSMutableDictionary alloc] init];
    }
    
    if (self.storeIdentifier) {
        jsonObject[STORE_PACKAGE_IDENTIFIER_JSON_KEY] = self.storeIdentifier;
    }
    
    return jsonObject;
}

+ (instancetype)instanceFromJsonObject:(id)json {
    if (![json isKindOfClass:[NSDictionary class]]) {
        return nil;
    }
    NSDictionary *jsonObject = json;
    
    HCPApplicationConfig *appConfig = [[HCPApplicationConfig alloc] init];
    appConfig.storeIdentifier = jsonObject[STORE_PACKAGE_IDENTIFIER_JSON_KEY];
    appConfig.contentConfig = [HCPContentConfig instanceFromJsonObject:jsonObject];
    
    return appConfig;
}

@end
