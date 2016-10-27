//
//  HCPXmlConfig.m
//
//  Created by Nikolay Demyankov on 07.08.15.
//

#import "HCPXmlConfig.h"
#import "HCPXmlConfigParser.h"
#import "HCPXmlTags.h"

@implementation HCPXmlConfig

- (instancetype)init {
    self = [super init];
    if (self) {
        _allowUpdatesAutoDownload = YES;
        _allowUpdatesAutoInstallation = YES;
        _configUrl = nil;
        _nativeInterfaceVersion = 1;
        _checkUpdateSigning = NO;
        _updateSigningCertificate = @"";
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
    
    if (jsOptions[kHCPUpdateSigningXmlTag]) {
        self.checkUpdateSigning = YES;
        self.updateSigningCertificate = jsOptions[kHCPUpdateSigningXmlTag];
    }

}


+ (instancetype)loadFromCordovaConfigXml {
    return [HCPXmlConfigParser parse];
}

@end