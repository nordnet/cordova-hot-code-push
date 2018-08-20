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
        // 백업 삭제의 초기값은 NO임
        _allowRemoveBackup = NO;
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

    // NOTE: remove-backup 옵션이 있으면 기존 옵션에 추가함
    if (jsOptions[kHCPRemoveBackupXmlTag]) {
        self.allowRemoveBackup = [(NSNumber *)jsOptions[kHCPRemoveBackupXmlTag] boolValue];
    }
}


// HCPXmlConfigParser의 parse 메소드 호출
+ (instancetype)loadFromCordovaConfigXml {
    return [HCPXmlConfigParser parse];
}

@end