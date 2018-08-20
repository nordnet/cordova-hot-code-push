//
//  HCPInstallationWorker.m
//
//  Created by Nikolay Demyankov on 12.08.15.
//

#import "HCPInstallationWorker.h"
#import "HCPManifestDiff.h"
#import "HCPContentManifest.h"
#import "HCPApplicationConfig.h"
#import "HCPApplicationConfigStorage.h"
#import "HCPContentManifestStorage.h"
#import "NSError+HCPExtension.h"
#import "NSData+HCPMD5.h"
#import "HCPEvents.h"

@interface HCPInstallationWorker() {
    HCPFilesStructure *_newReleaseFS;
    HCPFilesStructure *_currentReleaseFS;
    
    id<HCPConfigFileStorage> _manifestStorage;
    id<HCPConfigFileStorage> _configStorage;
    HCPApplicationConfig *_oldConfig;
    HCPApplicationConfig *_newConfig;
    HCPContentManifest *_oldManifest;
    HCPContentManifest *_newManifest;
    HCPManifestDiff *_manifestDiff;
    NSFileManager *_fileManager;
}

@property (nonatomic, strong, readwrite) NSString *workerId;

@end

@implementation HCPInstallationWorker

#pragma mark Public API

- (instancetype)initWithNewVersion:(NSString *)newVersion currentVersion:(NSString *)currentVersion {
    self = [super init];
    if (self) {
        _newReleaseFS = [[HCPFilesStructure alloc] initWithReleaseVersion:newVersion];
        _currentReleaseFS = [[HCPFilesStructure alloc] initWithReleaseVersion:currentVersion];
    }
    
    return self;
}

- (void)runWithComplitionBlock:(void (^)(void))updateInstallationComplitionBlock {
    [self dispatchBeforeInstallEvent];

    NSError *error = nil;
    if (![self initBeforeRun:&error] ||
        ![self isUpdateValid:&error] ||
        ![self copyFilesFromCurrentReleaseToNewRelease:&error] ||
        ![self deleteUnusedFiles:&error] ||
        ![self moveDownloadedFilesToWwwFolder:&error]) {
            NSLog(@"%@. Error code %ld", [error underlyingErrorLocalizedDesription], (long)error.code);
            [self cleanUpOnFailure];
            [self dispatchEventWithError:error];
        
            return;
    }
    
    [self cleanUpOnSucess];
    [self saveNewConfigsToWwwFolder];
    [self dispatchSuccessEvent];
}

#pragma mark Private API

/**
 *  Send event that update is about to begin
 */
- (void)dispatchBeforeInstallEvent {
    NSNotification *notification = [HCPEvents notificationWithName:kHCPBeforeInstallEvent
                                                 applicationConfig:_newConfig
                                                            taskId:self.workerId];
    
    [[NSNotificationCenter defaultCenter] postNotification:notification];
}

/**
 *  Send update installation failure event with error details.
 *
 *  @param error occured error
 */
- (void)dispatchEventWithError:(NSError *)error {
    NSNotification *notification = [HCPEvents notificationWithName:kHCPUpdateInstallationErrorEvent
                                                 applicationConfig:_newConfig
                                                            taskId:self.workerId
                                                             error:error];
    
    [[NSNotificationCenter defaultCenter] postNotification:notification];
}

/**
 *  Send event that update was successfully installed
 */
- (void)dispatchSuccessEvent {
    NSNotification *notification = [HCPEvents notificationWithName:kHCPUpdateIsInstalledEvent
                                                 applicationConfig:_newConfig
                                                            taskId:self.workerId];
    
    
    [[NSNotificationCenter defaultCenter] postNotification:notification];
}

/**
 *  Initialize all required variables before executing installation logic.
 *
 *  @param error filled with information about any occured error; <code>nil</code> if initialization finished with success
 *
 *  @return <code>YES</code> if everything is ready for update; <code>NO</code> otherwise
 */
- (BOOL)initBeforeRun:(NSError **)error {
    *error = nil;
    
    _fileManager = [NSFileManager defaultManager];
    _manifestStorage = [[HCPContentManifestStorage alloc] initWithFileStructure:_newReleaseFS];
    _configStorage = [[HCPApplicationConfigStorage alloc] initWithFileStructure:_newReleaseFS];
    
    // load from file system current version of application config
    _oldConfig = [_configStorage loadFromFolder:_currentReleaseFS.wwwFolder];
    if (_oldConfig == nil) {
        *error = [NSError errorWithCode:kHCPLocalVersionOfApplicationConfigNotFoundErrorCode
                            description:@"Failed to load application config from cache folder"];
        return NO;
    }
    
    // load from file system new version of the application config
    _newConfig = [_configStorage loadFromFolder:_newReleaseFS.downloadFolder];
    if (_newConfig == nil) {
        *error = [NSError errorWithCode:kHCPLoadedVersionOfApplicationConfigNotFoundErrorCode
                            description:@"Failed to load application config from download folder"];
        return NO;
    }
    
    // load from file system old version of the manifest
    _oldManifest = [_manifestStorage loadFromFolder:_currentReleaseFS.wwwFolder];
    if (_oldManifest == nil) {
        *error = [NSError errorWithCode:kHCPLocalVersionOfManifestNotFoundErrorCode
                            description:@"Failed to load content manifest from cache folder"];
        return NO;
    }
    
    // load from file system new version of the manifest
    _newManifest = [_manifestStorage loadFromFolder:_newReleaseFS.downloadFolder];
    if (_newManifest == nil) {
        *error = [NSError errorWithCode:kHCPLoadedVersionOfManifestNotFoundErrorCode
                            description:@"Failed to load content manifest from download folder"];
        return NO;
    }
    
    // calculate difference between the old and the new manifests
    _manifestDiff = [_oldManifest calculateDifference:_newManifest];
    
    return YES;
}

/**
 *  Validate the update.
 *  We will check if all the required files are loaded and if they are not corrupted.
 *
 *  @param error filled with information about any occured error; <code>nil</code> if update is valid
 *
 *  @return <code>YES</code> if update is valid and can be installed; <code>NO</code> - update is corrupted
 */
- (BOOL)isUpdateValid:(NSError **)error {
    *error = nil;
    NSString *errorMsg = nil;
    
    NSArray *updateFileList = _manifestDiff.updateFileList;
    for (HCPManifestFile *updatedFile in updateFileList) {
        // Force the release of the memory allocated to calculate the MD5 hash
        @autoreleasepool {
            NSURL *fileLocalURL = [_newReleaseFS.downloadFolder URLByAppendingPathComponent:updatedFile.name isDirectory:NO];
            if (![_fileManager fileExistsAtPath:fileLocalURL.path]) {
                errorMsg = [NSString stringWithFormat:@"Update validation error! File not found: %@", updatedFile.name];
                break;
            }
            
            NSString *fileMD5 = [[NSData dataWithContentsOfURL:fileLocalURL] md5];
            if (![fileMD5 isEqualToString:updatedFile.md5Hash]) {
                errorMsg = [NSString stringWithFormat:@"Update validation error! File's %@ hash %@ doesnt match the hash %@ from manifest file", updatedFile.name, fileMD5, updatedFile.md5Hash];
                break;
            }
        }
    }
    
    if (errorMsg) {
        *error = [NSError errorWithCode:kHCPUpdateIsInvalidErrorCode description:errorMsg];
    }
    
    return (*error == nil);
}

- (BOOL)copyFilesFromCurrentReleaseToNewRelease:(NSError **)error {
    *error = nil;
    
    // just in case check if previous www folder exists; if it does - remove it before copying new stuff
    if ([_fileManager fileExistsAtPath:_newReleaseFS.wwwFolder.path]) {
        [_fileManager removeItemAtURL:_newReleaseFS.wwwFolder error:nil];
    }
    
    // copy items from current www folder to the new www folder
    if (![_fileManager copyItemAtURL:_currentReleaseFS.wwwFolder toURL:_newReleaseFS.wwwFolder error:error]) {
        NSLog(@"Installation error! Failed to copy files from %@ to %@", _currentReleaseFS.wwwFolder.path, _newReleaseFS.wwwFolder.path);
        *error = [NSError errorWithCode:kHCPFailedToCopyFilesFromPreviousReleaseErrorCode descriptionFromError:*error];
        return NO;
    }
    
    return YES;
}

/**
 *  Delete from the project unused files.
 *
 *  @param error filled with error information if any occured; <code>nil</code> on success
 *
 *  @return <code>YES</code> if unused files were deleted; <code>NO</code> on failure;
 */
- (BOOL)deleteUnusedFiles:(NSError **)error {
    *error = nil;
    NSArray *deletedFiles = _manifestDiff.deletedFiles;
    for (HCPManifestFile *deletedFile in deletedFiles) {
        NSURL *filePath = [_newReleaseFS.wwwFolder URLByAppendingPathComponent:deletedFile.name];
        if (![_fileManager removeItemAtURL:filePath error:error]) {
            NSLog(@"CHCP Warinig! Failed to delete file: %@", filePath.absoluteString);
        }
    }
    
    return YES;
}

/**
 *  Copy loaded from server files into the www folder on the external storage.
 *
 *  @param error filled with error information if any occured; <code>nil</code> when files are installed
 *
 *  @return <code>YES</code> if files are copied successfully; <code>NO</code> on failure
 */
- (BOOL)moveDownloadedFilesToWwwFolder:(NSError **)error {
    *error = nil;
    NSFileManager *fileManager = [NSFileManager defaultManager];
    NSArray *updatedFiles = _manifestDiff.updateFileList;
    NSString *errorMsg = nil;
    for (HCPManifestFile *manifestFile in updatedFiles) {
        // Force the release of the memory allocated during file copy
        @autoreleasepool {
            // determine paths to the file in installation and www folders
            NSURL *pathInInstallationFolder = [_newReleaseFS.downloadFolder URLByAppendingPathComponent:manifestFile.name];
            NSURL *pathInWwwFolder = [_newReleaseFS.wwwFolder URLByAppendingPathComponent:manifestFile.name];
            
            // if file already exists in www folder - remove it before copying
            if ([fileManager fileExistsAtPath:pathInWwwFolder.path] && ![fileManager removeItemAtURL:pathInWwwFolder error:error]) {
                errorMsg = [NSString stringWithFormat:@"Failed to delete old version of the file %@ : %@. Installation failed",
                            manifestFile.name, [(*error) underlyingErrorLocalizedDesription]];
                break;
            }
            
            // if needed - create subfolders for the new file
            NSURL *parentDirectoryPathInWwwFolder = [pathInWwwFolder URLByDeletingLastPathComponent];
            if (![fileManager fileExistsAtPath:parentDirectoryPathInWwwFolder.path]) {
                if (![fileManager createDirectoryAtPath:parentDirectoryPathInWwwFolder.path withIntermediateDirectories:YES attributes:nil error:error]) {
                    errorMsg = [NSString stringWithFormat:@"Failed to create folder structure for file %@ : %@. Installation failed.",
                                manifestFile.name, [(*error) underlyingErrorLocalizedDesription]];
                    break;
                }
            }
            
            // copy new file into www folder
            if (![fileManager moveItemAtURL:pathInInstallationFolder toURL:pathInWwwFolder error:error]) {
                errorMsg = [NSString stringWithFormat:@"Failed to copy file %@ into www folder: %@. Installation failed.",
                            manifestFile.name, [(*error) underlyingErrorLocalizedDesription]];
                break;
            }
        }
    }
    
    if (errorMsg) {
        *error = [NSError errorWithCode:kHCPFailedToCopyNewContentFilesErrorCode description:errorMsg];
    }
    
    return (*error == nil);
}

/**
 *  Save loaded configs to the www folder. They are now our current configs.
 */
- (void)saveNewConfigsToWwwFolder {
    [_manifestStorage store:_newManifest inFolder:_newReleaseFS.wwwFolder];
    [_configStorage store:_newConfig inFolder:_newReleaseFS.wwwFolder];
}

/**
 *  Cleanup on failed installation attempt.
 */
- (void)cleanUpOnFailure {
    [_fileManager removeItemAtURL:_newReleaseFS.contentFolder error:nil];
}

/**
 *  Cleanup on successfull installation.
 */
- (void)cleanUpOnSucess {
    [_fileManager removeItemAtURL:_newReleaseFS.downloadFolder error:nil];
}

@end
