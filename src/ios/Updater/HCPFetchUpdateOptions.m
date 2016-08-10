//
//  HCPFetchUpdateOptions.m
//
//  Created by Nikolay Demyankov on 24.05.16.
//

#import "HCPFetchUpdateOptions.h"

static NSString *const CONFIG_URL_JSON_KEY = @"config-file";
static NSString *const REQUEST_HEADERS_JSON_KEY = @"request-headers";

@implementation HCPFetchUpdateOptions

- (instancetype)initWithConfigURL:(NSURL *)configFileURL requestHeaders:(NSDictionary<NSString *, NSString *> *)requestHeaders {
    self = [super init];
    if (self) {
        _configFileURL = configFileURL;
        _requestHeaders = requestHeaders;
    }
    
    return self;
}

- (instancetype)initWithDictionary:(NSDictionary *)dictionary {
    self = [super init];
    if (self) {
        _configFileURL = dictionary[CONFIG_URL_JSON_KEY] ? [NSURL URLWithString:dictionary[CONFIG_URL_JSON_KEY]] : nil;
        _requestHeaders = dictionary[REQUEST_HEADERS_JSON_KEY];
    }
    
    return self;
}

@end
