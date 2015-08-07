//
//  HCPLocalDevOptions.m
//  TestIosCHCP
//
//  Created by Nikolay Demyankov on 07.08.15.
//
//

#import "HCPLocalDevOptions.h"

@interface HCPLocalDevOptions()

@property (nonatomic, readwrite) NSArray *jsCodeForInjection;
@property (nonatomic, readwrite) NSArray *jsScriptsForInjection;

@end

@implementation HCPLocalDevOptions

- (instancetype)initWithJsCode:(NSArray *)jsCodeForInjection jsScrips:(NSArray *)jsScriptsForInjection {
    self = [super init];
    if (self) {
        self.jsCodeForInjection = jsCodeForInjection;
        self.jsScriptsForInjection = jsScriptsForInjection;
    }
    
    return self;
}

- (NSString *)toJsonString {
    NSDictionary *objectAsJsonDictionary = @{@"js_code": self.jsCodeForInjection, @"js_scripts": self.jsScriptsForInjection};
    NSError *error = nil;
    NSData *jsonData = [NSJSONSerialization dataWithJSONObject:objectAsJsonDictionary options:0 error:&error];
    if (error) {
        NSLog(@"Failed to convert developer options object into json");
        return nil;
    }
    
    return [[NSString alloc] initWithData:jsonData encoding:NSUTF8StringEncoding];
}

+ (instancetype)fromJsonString:(NSString *)json {
    [[[NSException alloc] initWithName:@"JsonConvertionError" reason:@"Convertion from JSON to the object of this type is not supported" userInfo:nil] raise];
    return nil;
}

@end
