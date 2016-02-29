//
//  HCPXmlConfigParser.m
//
//  Created by Nikolay Demyankov on 07.08.15.
//

#import "HCPXmlConfigParser.h"
#import "NSBundle+HCPExtension.h"
#import "HCPXmlTags.h"

@interface HCPXmlConfigParser() <NSXMLParserDelegate> {
    BOOL _didParseCHCPBlock;
    BOOL _isInCHCPBlock;
    
    HCPXmlConfig *_xmlConfig;
}

@end

@implementation HCPXmlConfigParser

#pragma mark Public API

+ (HCPXmlConfig *)parse {
    HCPXmlConfigParser *parser = [[HCPXmlConfigParser alloc] init];
    
    return [parser parseConfig];
}

- (HCPXmlConfig *)parseConfig {
    NSURL *cordovaConfigURL = [NSURL fileURLWithPath:[NSBundle pathToCordovaConfigXml]];
    NSXMLParser *configParser = [[NSXMLParser alloc] initWithContentsOfURL:cordovaConfigURL];
    if (configParser == nil) {
        NSLog(@"Failed to initialize XML parser.");
        return nil;
    }
    
    _xmlConfig = [[HCPXmlConfig alloc] init];
    [configParser setDelegate:self];
    [configParser parse];
    
    return _xmlConfig;
}

#pragma mark NSXMLParserDelegate implementation

- (void)parser:(NSXMLParser *)parser didStartElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName attributes:(NSDictionary *)attributeDict {
    if (_didParseCHCPBlock) {
        return;
    }
    
    if ([elementName isEqualToString:kHCPMainXmlTag]) {
        _isInCHCPBlock = YES;
        return;
    }
    
    if (!_isInCHCPBlock) {
        return;
    }
    
    if ([elementName isEqualToString:kHCPConfigFileXmlTag]) {
        [self parseConfigUrl:attributeDict];
    } else if ([elementName isEqualToString:kHCPAutoDownloadXmlTag]) {
        [self parseAutoDownloadOptions:attributeDict];
    } else if ([elementName isEqualToString:kHCPAutoInstallXmlTag]) {
        [self parseAutoInstallOptions:attributeDict];
    } else if ([elementName isEqualToString:kHCPNativeInterfaceXmlTag]) {
        [self parseNativeInterfaceOptions:attributeDict];
    }
}

- (void)parser:(NSXMLParser *)parser didEndElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName {
    if (_didParseCHCPBlock || !_isInCHCPBlock) {
        return;
    }
    
    if ([elementName isEqualToString:kHCPMainXmlTag]) {
        _didParseCHCPBlock = YES;
        return;
    }
}

#pragma mark Private API

- (void)parseConfigUrl:(NSDictionary *)attributeDict {
    _xmlConfig.configUrl = [NSURL URLWithString:attributeDict[kHCPConfigFileUrlXmlAttribute]];
}

- (void)parseAutoDownloadOptions:(NSDictionary *)attributeDict {
   _xmlConfig.allowUpdatesAutoDownload = [(NSNumber *)attributeDict[kHCPAutoDownloadEnabledXmlAttribute] boolValue];
}

- (void)parseAutoInstallOptions:(NSDictionary *)attributeDict {
    _xmlConfig.allowUpdatesAutoInstallation = [(NSNumber *)attributeDict[kHCPAutoInstallEnabledXmlAttribute] boolValue];
}

- (void)parseNativeInterfaceOptions:(NSDictionary *)attributeDict {
    _xmlConfig.nativeInterfaceVersion = [(NSString *)attributeDict[kHCPNativeInterfaceVersionXmlAttribute] integerValue];
}

@end
