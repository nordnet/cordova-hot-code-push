//
//  HCPXmlTags.m
//  TestProject
//
//  Created by Nikolay Demyankov on 03.09.15.
//
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

@implementation HCPXmlTags

@end
