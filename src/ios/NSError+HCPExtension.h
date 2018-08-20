//
//  NSError+HCPExtension.h
//
//  Created by Nikolay Demyankov on 12.08.15.
//

#import <Foundation/Foundation.h>

/**
 *  Domain for plugin specific errors
 */
extern NSString *const kHCPPluginErrorDomain;

/**
 *  Code for error during the download process. Means that we failed to download application config.
 */
extern NSInteger const kHCPFailedToDownloadApplicationConfigErrorCode;

/**
 *  Code for error during the download process. Indicates that web update requires higher version of the native side.
 *  User should update application through the App Store.
 */
extern NSInteger const kHCPApplicationBuildVersionTooLowErrorCode;

/**
 *  Code for error during the download process. Indicates that we failed to download manifest file.
 */
extern NSInteger const kHCPFailedToDownloadContentManifestErrorCode;

/**
 *  Code for error during the download process. Means that we failed to download new files from the server.
 */
extern NSInteger const kHCPFailedToDownloadUpdateFilesErrorCode;

/**
 *  Code for error that we failed to copy downloaded files from cache folder to the installation folder.
 */
extern NSInteger const kHCPFailedToMoveLoadedFilesToInstallationFolderErrorCode;

/**
 *  Code for error that something is wrong with the loaded update. 
 *  Maybe some files are missing, or hashes are invalid.
 */
extern NSInteger const kHCPUpdateIsInvalidErrorCode;

/**
 *  Code fore error during the installation. Indicates that we couldn't copy project files from the previous release.
 */
extern NSInteger const kHCPFailedToCopyFilesFromPreviousReleaseErrorCode;

/**
 *  Code for error during the installation. Means that we could not install loaded files.
 */
extern NSInteger const kHCPFailedToCopyNewContentFilesErrorCode;

/**
 *  Code error occures on installation/download processes on initialization phase. 
 *  Means that we could not load current version of the application config from the file system.
 */
extern NSInteger const kHCPLocalVersionOfApplicationConfigNotFoundErrorCode;

/**
 *  Code error occures on installation/download processes on initialization phase. 
 *  Means that we could not load current version of the manifest file from the file system.
 */
extern NSInteger const kHCPLocalVersionOfManifestNotFoundErrorCode;

/**
 *  Code error occures on installation process on initialization phase. 
 *  Means that we can't find loaded version of the application config in the installation folder.
 */
extern NSInteger const kHCPLoadedVersionOfApplicationConfigNotFoundErrorCode;

/**
 *  Code error occures on installation process on initialization phase.
 *  Means that we can't find loaded version of the manifest fle in the installation folder.
 */
extern NSInteger const kHCPLoadedVersionOfManifestNotFoundErrorCode;

/**
 *  Code error occures on application very first start. 
 *  At that point we need to copy www folder from bundle onto external storage.
 *  If we fail to do that - plugin won't work.
 */
extern NSInteger const kHCPFailedToInstallAssetsOnExternalStorageErrorCode;

/**
 *  Code error occures when we tried to install the update, but there is nothing to install.
 */
extern NSInteger const kHCPNothingToInstallErrorCode;

/**
 *  Code error occures when we tried to download new release, but there is nothing to download.
 */
extern NSInteger const kHCPNothingToUpdateErrorCode;

/**
 *  Code error occures when we tried to install, while update downlod is in progress.
 */
extern NSInteger const kHCPCantInstallWhileDownloadInProgressErrorCode;

/**
 *  Code error occures when we tried to download the update, while installation is in progress.
 */
extern NSInteger const kHCPCantDownloadUpdateWhileInstallationInProgressErrorCode;

/**
 *  Code error occures when we try to install the update while installation is already in progress.
 */
extern NSInteger const kHCPInstallationAlreadyInProgressErorrCode;

/**
 *  Code error occures when we try to download the update while it's already in progress.
 */
extern NSInteger const kHCPDownloadAlreadyInProgressErrorCode;

/**
 *  Code error occures when we try to call plugin API before assets are installed on external storage.
 */
extern NSInteger const kHCPAssetsNotYetInstalledErrorCode;

/**
 *  Category for NSError.
 *  Extended with plugin specific errors.
 */
@interface NSError (HCPExtension)

/**
 *  Create instance of the NSError object with the given error code and error description.
 *  Domain for the error is plugin specific.
 *
 *  @param errorCode   error code
 *  @param description error descrption; puted in the user info dictionary with NSLocalizedDescriptionKey key.
 *
 *  @return error instance
 */
+ (NSError *)errorWithCode:(NSInteger)errorCode description:(NSString *)description;

/**
 *  Create instance of the NSError object with the given error code and description that is extracted from provided error object.
 *
 *  @param errorCode error code
 *  @param error     error object from which description is extracted
 *
 *  @return error instance
 */
+ (NSError *)errorWithCode:(NSInteger)errorCode descriptionFromError:(NSError *)error;

/**
 *  Helper method to get localized description of the underlying error.
 *
 *  @return error message
 */
- (NSString *)underlyingErrorLocalizedDesription;

@end
