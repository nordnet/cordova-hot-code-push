//
//  NSFileManager+HCPExtension.h
//
//  Created by Nikolay Demyankov on 20.08.15.
//

#import <Foundation/Foundation.h>

/**
 *  Category for NSFileManager class.
 */
@interface NSFileManager (HCPExtension)

/**
 *  Get URL to the Application Support directory.
 *  Basically, it's a working directory for the plugin, where we put all our files.
 *
 *  @return URL to the directory.
 */
- (NSURL *)applicationSupportDirectory;

/**
 *  Get URL to the application Cache directory.
 *  We will use this folder for update download process.
 *
 *  @return URL to the directory.
 */
- (NSURL *)applicationCacheDirectory;

@end
