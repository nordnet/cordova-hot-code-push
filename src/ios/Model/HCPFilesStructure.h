//
//  HCPFilesStructureImpl.h
//
//  Created by Nikolay Demyankov on 12.08.15.
//

#import <Foundation/Foundation.h>

/**
 *  Implementation of the HCPFileStructure protocol.
 *  
 *  @see HCPFileStructure
 */
@interface HCPFilesStructure : NSObject

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

- (void)switchToRelease:(NSString *)releaseName;

+ (NSURL *)pluginRootFolder;

@end
