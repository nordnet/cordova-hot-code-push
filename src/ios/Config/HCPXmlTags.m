//
//  HCPXmlTags.m
//
//  Created by Nikolay Demyankov on 03.09.15.
//

#import "HCPXmlTags.h"

NSString *const kHCPMainXmlTag = @"chcp";

// Keys for processing application config location on the server
NSString *const kHCPConfigFileXmlTag = @"config-file";
NSString *const kHCPConfigFileUrlXmlAttribute = @"url";

// Keys for processing auto download options
NSString *const kHCPAutoDownloadXmlTag = @"auto-download";
NSString *const kHCPAutoDownloadEnabledXmlAttribute = @"enabled";

// Keys for processing auto install options
NSString *const kHCPAutoInstallXmlTag = @"auto-install";
NSString *const kHCPAutoInstallEnabledXmlAttribute = @"enabled";

// Keys for processing native interface version
NSString *const kHCPNativeInterfaceXmlTag = @"native-interface";
NSString *const kHCPNativeInterfaceVersionXmlAttribute = @"version";

// Keys for processing force install options
NSString *const kHCPForceInstallXmlTag = @"force-install";
NSString *const kHCPForceInstallEnabledXmlAttribute = @"enabled";

@implementation HCPXmlTags

@end
