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
@interface HCPPluginInternalPreferences : NSObject<HCPJsonConvertable>

/**
 *  Build version of the app which was detected on the last launch.
 *  Using it we can determine if application has been updated through the App Store.
 */
@property (nonatomic, strong) NSString *appBuildVersion;

/**
 *  Flag to check, if www folder from the bundle is installed on external storage.
 */
@property (nonatomic, getter=isWwwFolderInstalled) BOOL wwwFolderInstalled;

/**
 *  Previous version of the content. Can be used to rollback.
 */
@property (nonatomic, strong) NSString *previousReleaseVersionName;

/**
 *  Current version of the content, that is displayed to the user.
 */
@property (nonatomic, strong) NSString *currentReleaseVersionName;

/**
 *  Name of the new version, that was loaded and ready to be installed. 
 */
@property (nonatomic, strong) NSString *readyForInstallationReleaseVersionName;

/**
 *  Create default plugin config.
 *
 *  @return default config instance
 */
+ (HCPPluginInternalPreferences *)defaultConfig;


@end
