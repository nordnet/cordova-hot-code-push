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

// parser 생성후 parserConfig 호출
+ (HCPXmlConfig *)parse {
    HCPXmlConfigParser *parser = [[HCPXmlConfigParser alloc] init];

    return [parser parseConfig];
}

// config.xml 파싱 초기화
- (HCPXmlConfig *)parseConfig {
    // config.xml 패스 설정
    NSURL *cordovaConfigURL = [NSURL fileURLWithPath:[NSBundle pathToCordovaConfigXml]];
    // XML 파서 생성
    NSXMLParser *configParser = [[NSXMLParser alloc] initWithContentsOfURL:cordovaConfigURL];
    if (configParser == nil) {
        NSLog(@"Failed to initialize XML parser.");
        return nil;
    }

    _xmlConfig = [[HCPXmlConfig alloc] init];
    // XML Parser의 delegate를 설정한다
    [configParser setDelegate:self];
    [configParser parse];

    return _xmlConfig;
}

#pragma mark NSXMLParserDelegate implementation
/*
    총 4가지 메소드중 두가지를 오버라이딩
    1. parserDidStartDocument   : 해석(parse) 시작시 호출
    2. didStartElement          : 시작 태그를 만났을 때 호출
    3. foundCharacter           : 텍스트 요소를 발견하면 호출
    4. didEndElement            : 종료 태그를 만났을 때 호출

    태그명은 HCPXmlTags.m 참고
*/


- (void)parser:(NSXMLParser *)parser didStartElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName attributes:(NSDictionary *)attributeDict {
    // <chcp> 블록을 파싱하게 되면
    if (_didParseCHCPBlock) {
        return;
    }

    // <chcp>를 만나면
    if ([elementName isEqualToString:kHCPMainXmlTag]) {
        _isInCHCPBlock = YES;
        return;
    }

    // <chcp> 블록이 아니라면
    if (!_isInCHCPBlock) {
        return;
    }

    // NOTE : backup Remove 관련한 chcp config 변경은 여기에 로직을 추가하면 됨
    // <config-file>인 경우
    if ([elementName isEqualToString:kHCPConfigFileXmlTag]) {
        [self parseConfigUrl:attributeDict];
    }
    // <auto-download>인 경우
    else if ([elementName isEqualToString:kHCPAutoDownloadXmlTag]) {
        [self parseAutoDownloadOptions:attributeDict];
    }
    // <auto-install>인 경우
    else if ([elementName isEqualToString:kHCPAutoInstallXmlTag]) {
        [self parseAutoInstallOptions:attributeDict];
    }
    // <native-interface>인 경우
    else if ([elementName isEqualToString:kHCPNativeInterfaceXmlTag]) {
        [self parseNativeInterfaceOptions:attributeDict];
    }
    // NOTE: <remove-backup>인 경우
    else if ([elementName isEqualToString:kHCPRemoveBackupXmlTag]) {
        [self parseRemoveBackupOptions:attributeDict];
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

// 파싱된 데이터에 맞는 값 적용
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

// NOTE: <remove-backup>인 경우
- (void)parseRemoveBackupOptions:(NSDictionary *)attributeDict {
    _xmlConfig.allowRemoveBackup = [(NSNumber *)attributeDict[kHCPRemoveBackupXmlEnabledXmlAttribute] boolValue];
}

@end
