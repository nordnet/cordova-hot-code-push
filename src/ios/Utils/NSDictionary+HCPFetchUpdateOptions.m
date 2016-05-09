//
//  NSDictionary+HCPFetchUpdateOptions.m
//

#import "NSDictionary+HCPFetchUpdateOptions.h"

static NSString *const CONFIG_URL_JSON_KEY = @"config-url";
static NSString *const REQUEST_HEADERS_JSON_KEY = @"request-headers";

@implementation NSDictionary (HCPFetchUpdateOptions)

- (NSURL *)configURL {
    NSString *configPath = self[CONFIG_URL_JSON_KEY];
    
    return configPath ? [NSURL URLWithString:configPath] :nil;
}

- (NSDictionary<NSString *, NSString *> *)requestHeaders {
    return self[REQUEST_HEADERS_JSON_KEY];
}


@end
