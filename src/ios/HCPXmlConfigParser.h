//
//  HCPXmlConfigParser.h
//
//  Created by Nikolay Demyankov on 07.08.15.
//

#import <Foundation/Foundation.h>
#import "HCPXmlConfig.h"

/**
 *  XML parser for Cordova's config.xml.
 *  Used to read plugin specific preferences.
 */
@interface HCPXmlConfigParser : NSObject

/**
 *  Parse the config and return only plugin specific preferences.
 *
 *  @return plugin preferences from config.xml
 *
 *  @see HCPXmlConfig
 */
+ (HCPXmlConfig *)parse;

@end
