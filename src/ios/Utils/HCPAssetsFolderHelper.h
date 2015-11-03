//
//  HCPAssetsFolderHelper.h
//
//  Created by Nikolay Demyankov on 03.11.15.
//

#import <Foundation/Foundation.h>

/**
 *  Helper class to install www folder on the external storage
 */
@interface HCPAssetsFolderHelper : NSObject

/**
 *  Install www folder from bunlde onto the external storage.
 *
 *  @param externalFolderURL URL to the folder where to install web content; 
 *         usually it is a folder in Application Support directory.
 */
+ (void)installWwwFolderToExternalStorageFolder:(NSURL *)externalFolderURL;

@end
