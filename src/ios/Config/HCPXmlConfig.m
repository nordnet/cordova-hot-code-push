//
//  HCPXmlConfig.m
//
//  Created by Nikolay Demyankov on 07.08.15.
//

#import "HCPXmlConfig.h"
#import "HCPXmlConfigParser.h"
#import "HCPXmlTags.h"

@interface HCPXmlConfig()
@property (nonatomic, strong, readwrite) HCPLocalDevOptions *devOptions;
@end

@implementation HCPXmlConfig

- (instancetype)init {
    self = [super init];
    if (self) {
        _allowUpdatesAutoDownload = YES;
        _allowUpdatesAutoInstallation = YES;
        _configUrl = nil;
        _devOptions = [[HCPLocalDevOptions alloc] init];
    }
    
    return self;
}

- (void)mergeOptionsFromJS:(NSDictionary *)jsOptions {
    if (jsOptions[kHCPConfigFileXmlTag]) {
        self.configUrl = [NSURL URLWithString:jsOptions[kHCPConfigFileXmlTag]];
    }
    
    if (jsOptions[kHCPAutoInstallXmlTag]) {
        self.allowUpdatesAutoInstallation = [(NSNumber *)jsOptions[kHCPAutoInstallXmlTag] boolValue];
    }
    
    if (jsOptions[kHCPAutoDownloadXmlTag]) {
        self.allowUpdatesAutoDownload = [(NSNumber *)jsOptions[kHCPAutoDownloadXmlTag] boolValue];
    }
}


+ (instancetype)loadFromCordovaConfigXml {
    return [HCPXmlConfigParser parse];
}

@end