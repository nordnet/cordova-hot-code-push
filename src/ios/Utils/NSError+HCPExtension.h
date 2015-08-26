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

@interface NSError (HCPExtension)

+ (NSError *)errorWithCode:(NSInteger)errorCode description:(NSString *)description;

@end
