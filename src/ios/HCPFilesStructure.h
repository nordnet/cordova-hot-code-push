//
//  HCPFilesStructure.h
//
//  Created by Nikolay Demyankov on 12.08.15.
//

#import <Foundation/Foundation.h>

/**
 *  Model for plugins file structure.
 *  Each release has it's own folder, so we need to initialize this object with release version.
 *  
 *  @see HCPFileStructure
 */
@interface HCPFilesStructure : NSObject

/**
 *  Constructor.
 *
 *  @param releaseVersion for what version this file structure
 *
 *  @return object instance
 */
- (instancetype)initWithReleaseVersion:(NSString *)releaseVersion;

/**
 *  Absolute path to plugins working directory.
 */
@property (nonatomic, strong, readonly) NSURL *contentFolder;

/**
 *  Absolute path to the folder on the external storage where all web content is placed.
 *  From this folder we will show web pages.
 *  Think of this as an bundle folder on the external storage.
 */
@property (nonatomic, strong, readonly) NSURL *wwwFolder;

/**
 *  Absolute path to the temporary folder where we will put files during the update download.
 */
@property (nonatomic, strong, readonly) NSURL *downloadFolder;

/**
 *  Getter for the name of the application config file.
 */
@property (nonatomic, strong, readonly) NSString *configFileName;

/**
 *  Getter for the name of the manifest file.
 */
@property (nonatomic, strong, readonly) NSString *manifestFileName;

/**
 *  Get root folder for this plugin. 
 *  In it all releases are located.
 *
 *  @return url on the external storage to the plugins root folder.
 */
+ (NSURL *)pluginRootFolder;

/**
 *  Default application config file name.
 *  Should be equal to the config name, that is bundled with the app.
 *
 *  @return default application config file name.
 */
+ (NSString *)defaultConfigFileName;

/**
 *  Default name of the manifest file.
 *  Should be equal to the manifest name, that is bundled with the app.
 *
 *  @return default manifest file name.
 */
+ (NSString *)defaultManifestFileName;

@end
