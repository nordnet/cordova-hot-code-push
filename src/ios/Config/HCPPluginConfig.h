//
//  HCPPluginConfig.h
//
//  Created by Nikolay Demyankov on 10.08.15.
//

#import <Foundation/Foundation.h>
#import "HCPJsonConvertable.h"

/**
 *  Model for plugin preferences, that can be changed during runtime.
 *  Using this you can disable/enable updates download and installation,
 *  and even change application config file url (by default it is set in config.xml).
 *
 *  Also, it stores current build version of the application,
 *  so we could determine if it has been updated through the App Store.
 */
@interface HCPPluginConfig : NSObject<HCPJsonConvertable>

/**
 *  Build version of the app which was detected on the last launch.
 *  Using it we can determine if application has been updated through the App Store.
 */
@property (nonatomic) NSInteger appBuildVersion;

/**
 *  URL where application config is stored on the server.
 *  Can be changed on runtime.
 */
@property (nonatomic, strong) NSURL *configUrl;

/**
 *  Flag that indicates if updates auto download is allowed. By default - <code>YES</code>.
 *  
 *  @return <code>YES</code> if auto download is allowed; <code>NO</code> if auto download is disabled
 */
@property (nonatomic, getter=isUpdatesAutoDowloadAllowed) BOOL allowUpdatesAutoDownload;

/**
 *  Flag that indicates if updates auto installation is allowed. By default - <code>YES</code>.
 *  
 *  @return <code>YES</code> if auto installation is allowed; <code>NO</code> if auto installation is disabled
 */
@property (nonatomic, getter=isUpdatesAutoInstallationAllowed) BOOL allowUpdatesAutoInstallation;

/**
 *  Apply and save options that has been send from the web page.
 *  Using this we can change plugin config from JavaScript.
 *
 *  @param jsOptions options that are sent from web side.
 */
- (void)mergeOptionsFromJS:(NSDictionary *)jsOptions;

/**
 *  Create default plugin config.
 *
 *  @return default config instance
 */
+ (HCPPluginConfig *)defaultConfig;


@end
