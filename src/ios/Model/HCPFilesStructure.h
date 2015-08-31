//
//  HCPFilesStructure.h
//
//  Created by Nikolay Demyankov on 12.08.15.
//

#import <Foundation/Foundation.h>

/**
 *  Protocol describes structure of the plugin working directories, like where
 *  to download updates, from where install them and so on.
 */
@protocol HCPFilesStructure <NSObject>

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
 *  Absolute path to the temporary folder where new update is located.
 *  Folder is created after update download. We will perform installation from it.
 */
@property (nonatomic, strong, readonly) NSURL *installationFolder;

/**
 *  Absolute path to the temporary folder where we put backup of the current web content before
 *  installing new version. If during the installation some error will happen - we will restore content
 *  from this folder.
 */
@property (nonatomic, strong, readonly) NSURL *backupFolder;

/**
 *  Getter for the name of the application config file.
 */
@property (nonatomic, strong, readonly) NSString *configFileName;

/**
 *  Getter for the name of the manifest file.
 */
@property (nonatomic, strong, readonly) NSString *manifestFileName;

@end
