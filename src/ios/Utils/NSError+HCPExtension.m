//
//  NSError+HCPExtension.m
//  TestIosCHCP
//
//  Created by Nikolay Demyankov on 12.08.15.
//
//

#import "NSError+HCPExtension.h"

NSString *const kHCPPluginErrorDomain = @"HCPPluginError";

NSInteger const kHCPFailedToDownloadApplicationConfigErrorCode = -1;
NSInteger const kHCPApplicationBuildVersionTooLowErrorCode = -2;
NSInteger const kHCPFailedToDownloadContentManifestErrorCode = -3;
NSInteger const kHCPFailedToDownloadUpdateFilesErrorCode = -4;
NSInteger const kHCPFailedToMoveLoadedFilesToInstallationFolderErrorCode = -5;
NSInteger const kHCPUpdateIsInvalidErrorCode = -6;
NSInteger const kHCPFailedToCreateBackupErrorCode = -7;
NSInteger const kHCPFailedToCopyNewContentFilesErrorCode = -8;


@implementation NSError (HCPExtension)

+ (NSError *)errorWithCode:(NSInteger)errorCode description:(NSString *)description {
    NSDictionary *userInfo = @{NSLocalizedDescriptionKey: description};
    
    return [NSError errorWithDomain:kHCPPluginErrorDomain code:errorCode userInfo:userInfo];
}

@end
