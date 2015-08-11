//
//  HCPApplicationConfig.m
//  TestIosCHCP
//
//  Created by Nikolay Demyankov on 10.08.15.
//
//

#import "HCPApplicationConfig.h"

@interface HCPApplicationConfig() {
    NSString *_storeUrl;
}

@property (nonatomic, strong) NSString *storeIdentifier;
@property (nonatomic, strong, readwrite) HCPContentConfig *contentConfig;

@end

static NSString *const STORE_PACKAGE_IDENTIFIER_JSON_KEY = @"ios_identifier";

@implementation HCPApplicationConfig

#pragma mark Public API

- (NSString *)storeUrl {
    if (_storeUrl == nil) {
        if ([self.storeIdentifier containsString:@"http://"] || [self.storeIdentifier containsString:@"https://"]) {
            _storeUrl = self.storeIdentifier;
        } else {
            _storeUrl = [NSString stringWithFormat:@"https://itunes.apple.com/%@", self.storeIdentifier];
        }
    }
    
    return _storeUrl;
}

#pragma mark HCPJsonConvertable implementation

- (id)toJson {
    NSMutableDictionary *jsonObject = [[NSMutableDictionary alloc] initWithDictionary:[self.contentConfig toJson]];
    jsonObject[STORE_PACKAGE_IDENTIFIER_JSON_KEY] = self.storeIdentifier;
    
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
