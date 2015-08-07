//
//  HCPXmlConfig.h
//  TestIosCHCP
//
//  Created by Nikolay Demyankov on 07.08.15.
//
//

#import <Foundation/Foundation.h>

#import "HCPLocalDevOptions.h"

@interface HCPXmlConfig : NSObject

@property (nonatomic, readonly) NSString *configUrl;
@property (nonatomic, readonly) HCPLocalDevOptions *devOptions;

- (instancetype)initWithConfigUrl:(NSString *)configUrl developerOptions:(HCPLocalDevOptions *)devOptions;

+ (instancetype)loadFromCordovaConfigXml;

@end
