//
//  HCPApplicationConfig.m
//
//  Created by Nikolay Demyankov on 10.08.15.
//

#import "HCPApplicationConfig.h"
#import "NSBundle+HCPExtension.h"
#import "NSError+HCPExtension.h"

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
        if ([self.storeIdentifier hasPrefix:@"http://"] || [self.storeIdentifier hasPrefix:@"https://"]) {
            _storeUrl = self.storeIdentifier;
        } else {
            _storeUrl = [NSString stringWithFormat:STORE_URL_TEMPLATE, self.storeIdentifier];
        }
    }
    
    return _storeUrl;
}

+ (instancetype)configFromBundle:(NSString *)configFileName {
    NSURL *wwwFolderURL = [NSURL fileURLWithPath:[NSBundle pathToWwwFolder] isDirectory:YES];
    NSURL *chcpJsonFileURLFromBundle = [wwwFolderURL URLByAppendingPathComponent:configFileName];
    
    NSData *jsonData = [NSData dataWithContentsOfURL:chcpJsonFileURLFromBundle];
    if (jsonData == nil) {
        return nil;
    }
    
    NSError *error = nil;
    id json = [NSJSONSerialization JSONObjectWithData:jsonData options:kNilOptions error:&error];
    if (error) {
        NSLog(@"Can't read application config from bundle. %@", [error underlyingErrorLocalizedDesription]);
        return nil;
    }
    
    return [HCPApplicationConfig instanceFromJsonObject:json];
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
    if (!json || ![json isKindOfClass:[NSDictionary class]]) {
        return nil;
    }
    NSDictionary *jsonObject = json;
    
    HCPApplicationConfig *appConfig = [[HCPApplicationConfig alloc] init];
    appConfig.storeIdentifier = jsonObject[STORE_PACKAGE_IDENTIFIER_JSON_KEY];
    appConfig.contentConfig = [HCPContentConfig instanceFromJsonObject:jsonObject];
    
    return appConfig;
}

@end
