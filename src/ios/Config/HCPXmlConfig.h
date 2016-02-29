//
//  HCPXmlConfig.h
//
//  Created by Nikolay Demyankov on 07.08.15.
//

#import <Foundation/Foundation.h>

/**
 *  Model for hot-code-push specific preferences in config.xml.
 */
@interface HCPXmlConfig : NSObject

/**
 *  URL to application config, that is stored on the server.
 *  This is a path to chcp.json file.
 */
@property (nonatomic, strong) NSURL *configUrl;

/**
 *  Flag that indicates if updates auto download is allowed. By default - <code>YES</code>.
 *
 *  @return <code>YES</code> if auto download is allowed; <code>NO</code> if auto download is disabled
 */
@property (nonatomic, getter=isUpdatesAutoDownloadAllowed) BOOL allowUpdatesAutoDownload;

/**
 *  Flag that indicates if updates auto installation is allowed. By default - <code>YES</code>.
 *
 *  @return <code>YES</code> if auto installation is allowed; <code>NO</code> if auto installation is disabled
 */
@property (nonatomic, getter=isUpdatesAutoInstallationAllowed) BOOL allowUpdatesAutoInstallation;

/**
 *  Current native interface version of the application.
 */
@property (nonatomic) NSUInteger nativeInterfaceVersion;

/**
 *  Apply and save options that has been send from the web page.
 *  Using this we can change plugin config from JavaScript.
 *
 *  @param jsOptions options that are sent from web side.
 */
- (void)mergeOptionsFromJS:(NSDictionary *)jsOptions;

/**
 *  Load object from config.xml
 *
 *  @return plugin preferences from config.xml
 */
+ (HCPXmlConfig *)loadFromCordovaConfigXml;

@end
