//
//  HCPPluginConfig.h
//  TestIosCHCP
//
//  Created by Nikolay Demyankov on 10.08.15.
//
//

#import <Foundation/Foundation.h>
#import "HCPJsonConvertable.h"

@interface HCPPluginConfig : NSObject<HCPJsonConvertable>

@property (nonatomic) NSInteger appBuildVersion;
@property (nonatomic, strong) NSURL *configUrl;
@property (nonatomic, getter=isUpdatesAutoDowloadAllowed) BOOL allowUpdatesAutoDownload;
@property (nonatomic, getter=isUpdatesAutoInstallationAllowed) BOOL allowUpdatesAutoInstallation;

- (void)mergeOptionsFromJS:(NSDictionary *)jsOptions;

+ (instancetype)defaultConfig;


@end
