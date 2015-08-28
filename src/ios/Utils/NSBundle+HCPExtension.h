//
//  NSBundle+Extension.h
//  TestIosCHCP
//
//  Created by Nikolay Demyankov on 10.08.15.
//
//

#import <Foundation/Foundation.h>

@interface NSBundle (HCPExtension)

+ (NSInteger)applicationBuildVersion;

+ (NSString *)pathToWwwFolder;

+ (NSString *)pathToCordovaConfigXml;

+ (void)installWwwFolderToExternalStorageFolder:(NSURL *)externalFolderURL;

@end
