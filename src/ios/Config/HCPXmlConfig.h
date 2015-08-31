//
//  HCPXmlConfig.h
//
//  Created by Nikolay Demyankov on 07.08.15.
//

#import <Foundation/Foundation.h>
#import "HCPLocalDevOptions.h"

/**
 *  Model for hot-code-push specific preferences in config.xml.
 */
@interface HCPXmlConfig : NSObject

/**
 *  URL to application config, that is stored on the server.
 *  This is a path to chcp.json file.
 */
@property (nonatomic, strong, readonly) NSURL *configUrl;

/**
 *  Local development options.
 *
 *  @see HCPLocalDevOptions
 */
@property (nonatomic, strong, readonly) HCPLocalDevOptions *devOptions;

/**
 *  Object initializer
 *
 *  @param configUrl  url to the config file on the server
 *  @param devOptions development options
 *
 *  @return object instance
 */
- (instancetype)initWithConfigUrl:(NSURL *)configUrl developerOptions:(HCPLocalDevOptions *)devOptions;

/**
 *  Load object from config.xml
 *
 *  @return plugin preferences from config.xml
 */
+ (HCPXmlConfig *)loadFromCordovaConfigXml;

@end
