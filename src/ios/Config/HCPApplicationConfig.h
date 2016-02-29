//
//  HCPApplicationConfig.h
//
//  Created by Nikolay Demyankov on 10.08.15.
//

#import <Foundation/Foundation.h>
#import "HCPJsonConvertable.h"
#import "HCPContentConfig.h"

/**
 *  Model for application config. Holds information from chcp.json file.
 */
@interface HCPApplicationConfig : NSObject<HCPJsonConvertable>

/**
 *  Getter for url, that leeds to the applications page on the App Store.
 */
@property (nonatomic, strong, readonly) NSString *storeUrl;

/**
 *  Getter for content config.
 *
 *  @see HCPContentConfig
 */
@property (nonatomic, strong, readonly) HCPContentConfig *contentConfig;

/**
 *  Create instance of the application config from the configuration file in assets.
 *
 *  @param configFileName name of the configuration file
 *
 *  @return config instance
 */
+ (instancetype)configFromBundle:(NSString *)configFileName;

@end
