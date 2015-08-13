//
//  HCPXmlConfig.m
//  TestIosCHCP
//
//  Created by Nikolay Demyankov on 07.08.15.
//
//

#import "HCPXmlConfig.h"
#import "HCPXmlConfigParser.h"

@interface HCPXmlConfig()

@property (nonatomic, strong, readwrite) NSURL *configUrl;
@property (nonatomic, strong, readwrite) HCPLocalDevOptions *devOptions;

@end

@implementation HCPXmlConfig

- (instancetype)initWithConfigUrl:(NSURL *)configUrl developerOptions:(HCPLocalDevOptions *)devOptions {
    self = [super init];
    if (self) {
        self.configUrl = configUrl;
        self.devOptions = devOptions;
    }
    
    return self;
}

+ (instancetype)loadFromCordovaConfigXml {
    return [HCPXmlConfigParser parse];
}

@end
