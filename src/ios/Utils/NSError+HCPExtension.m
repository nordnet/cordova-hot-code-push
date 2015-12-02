//
//  NSError+HCPExtension.m
//
//  Created by Nikolay Demyankov on 12.08.15.
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
NSInteger const kHCPLocalVersionOfApplicationConfigNotFoundErrorCode = -9;
NSInteger const kHCPLocalVersionOfManifestNotFoundErrorCode = -10;
NSInteger const kHCPLoadedVersionOfApplicationConfigNotFoundErrorCode = -11;
NSInteger const kHCPLoadedVersionOfManifestNotFoundErrorCode = -12;
NSInteger const kHCPFailedToInstallAssetsOnExternalStorageErrorCode = -13;
NSInteger const kHCPNothingToInstallErrorCode = 1;
NSInteger const kHCPNothingToUpdateErrorCode = 2;

@implementation NSError (HCPExtension)

+ (NSError *)errorWithCode:(NSInteger)errorCode description:(NSString *)description {
    NSDictionary *userInfo = @{NSLocalizedDescriptionKey: description};
    
    return [NSError errorWithDomain:kHCPPluginErrorDomain code:errorCode userInfo:userInfo];
}

+ (NSError *)errorWithCode:(NSInteger)errorCode descriptionFromError:(NSError *)error {
    return [NSError errorWithDomain:kHCPPluginErrorDomain code:errorCode userInfo:error.userInfo];
}

- (NSString *)underlyingErrorLocalizedDesription {
    NSString *msg = [self.userInfo[NSUnderlyingErrorKey] localizedDescription];
    if (msg) {
        return msg;
    }
    
    return self.localizedDescription;
}

@end
