//
//  NSError+HCPExtension.h
//  TestIosCHCP
//
//  Created by Nikolay Demyankov on 12.08.15.
//
//

#import <Foundation/Foundation.h>

extern NSString *const kHCPPluginErrorDomain;
extern NSInteger const kHCPFailedToDownloadApplicationConfigErrorCode;
extern NSInteger const kHCPApplicationBuildVersionTooLowErrorCode;
extern NSInteger const kHCPFailedToDownloadContentManifestErrorCode;
extern NSInteger const kHCPFailedToDownloadUpdateFilesErrorCode;
extern NSInteger const kHCPFailedToMoveLoadedFilesToInstallationFolderErrorCode;
extern NSInteger const kHCPUpdateIsInvalidErrorCode;
extern NSInteger const kHCPFailedToCreateBackupErrorCode;
extern NSInteger const kHCPFailedToCopyNewContentFilesErrorCode;
extern NSInteger const kHCPLocalVersionOfApplicationConfigNotFoundErrorCode;
extern NSInteger const kHCPLocalVersionOfManifestNotFoundErrorCode;
extern NSInteger const kHCPLoadedVersionOfApplicationConfigNotFoundErrorCode;
extern NSInteger const kHCPLoadedVersionOfManifestNotFoundErrorCode;

@interface NSError (HCPExtension)

+ (NSError *)errorWithCode:(NSInteger)errorCode description:(NSString *)description;
+ (NSError *)errorWithCode:(NSInteger)errorCode descriptionFromError:(NSError *)error;

@end
