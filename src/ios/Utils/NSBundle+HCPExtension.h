//
//  NSBundle+Extension.h
//
//  Created by Nikolay Demyankov on 10.08.15.
//

#import <Foundation/Foundation.h>

/**
 *  Category for the NSBundle class.
 */
@interface NSBundle (HCPExtension)

/**
 *  Getter for the current build version of the application.
 *
 *  @return build version of the app
 */
+ (NSInteger)applicationBuildVersion;

/**
 *  Path to the www folder in the application bundle.
 *
 *  @return path to www folder
 */
+ (NSString *)pathToWwwFolder;

/**
 *  Path to the config.xml file in the project.
 *
 *  @return path to the config file
 */
+ (NSString *)pathToCordovaConfigXml;

/**
 *  Install www folder from bunlde onto the external storage.
 *
 *  @param externalFolderURL URL to the folder where to install web content; usually it is a folder in Application Support directory.
 */
+ (void)installWwwFolderToExternalStorageFolder:(NSURL *)externalFolderURL;

@end
