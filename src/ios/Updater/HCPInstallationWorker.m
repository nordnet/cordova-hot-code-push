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
    id<HCPFilesStructure> _fileStructure;
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

- (instancetype)initWithFileStructure:(id<HCPFilesStructure>)fileStructure {
    self = [super init];
    if (self) {
        _fileStructure = fileStructure;
    }
    
    return self;
}

- (void)runWithComplitionBlock:(void (^)(void))updateInstallationComplitionBlock {
    NSError *error = nil;
    if (![self initBeforeRun:&error] ||
        ![self isUpdateValid:&error] ||
        ![self backupFiles:&error] ||
        ![self deleteUnusedFiles:&error] ||
        ![self moveDownloadedFilesToWwwFolder:&error]) {
            NSLog(@"%@", error.localizedDescription);
            [self rollback];
            [self cleanUp];
            [self dispatchEventWithError:error];
        
            return;
    }
    
    [self saveNewConfigsToWwwFolder];
    [self cleanUp];
    [self dispatchSuccessEvent];
}

#pragma mark Private API

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
    _manifestStorage = [[HCPContentManifestStorage alloc] initWithFileStructure:_fileStructure];
    _configStorage = [[HCPApplicationConfigStorage alloc] initWithFileStructure:_fileStructure];
    
    // load from file system current version of application config
    _oldConfig = [_configStorage loadFromFolder:_fileStructure.wwwFolder];
    if (_oldConfig == nil) {
        *error = [NSError errorWithCode:kHCPLocalVersionOfApplicationConfigNotFoundErrorCode
                            description:@"Failed to load application config from cache folder"];
        return NO;
    }
    
    // load from file system new version of the application config
    _newConfig = [_configStorage loadFromFolder:_fileStructure.installationFolder];
    if (_newConfig == nil) {
        *error = [NSError errorWithCode:kHCPLoadedVersionOfApplicationConfigNotFoundErrorCode
                            description:@"Failed to load application config from download folder"];
        return NO;
    }
    
    // load from file system old version of the manifest
    _oldManifest = [_manifestStorage loadFromFolder:_fileStructure.wwwFolder];
    if (_oldManifest == nil) {
        *error = [NSError errorWithCode:kHCPLocalVersionOfManifestNotFoundErrorCode
                            description:@"Failed to load content manifest from cache folder"];
        return NO;
    }
    
    // load from file system new version of the manifest
    _newManifest = [_manifestStorage loadFromFolder:_fileStructure.installationFolder];
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
        NSURL *fileLocalURL = [_fileStructure.installationFolder URLByAppendingPathComponent:updatedFile.name isDirectory:NO];
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
    
    if (errorMsg) {
        *error = [NSError errorWithCode:kHCPUpdateIsInvalidErrorCode description:errorMsg];
    }
    
    return (*error == nil);
}

/**
 *  Create backup of the current www folder.
 *  If something will go wrong during the update we will rollback to it.
 *
 *  @param error filled with any occured error; <code>nil</code> if backup created
 *
 *  @return <code>YES</code> if backup created; <code>NO</code> on error
 */
- (BOOL)backupFiles:(NSError **)error {
    *error = nil;
    
    // create backup folder
    NSURL *backupParentFolderURL = [_fileStructure.backupFolder URLByDeletingLastPathComponent];
    if (![_fileManager createDirectoryAtURL:backupParentFolderURL withIntermediateDirectories:YES attributes:nil error:error]) {
        *error = [NSError errorWithCode:kHCPFailedToCreateBackupErrorCode descriptionFromError:*error];
        return NO;
    }
    
    // copy items from www to backup
    if (![_fileManager copyItemAtURL:_fileStructure.wwwFolder toURL:_fileStructure.backupFolder error:error]) {
        *error = [NSError errorWithCode:kHCPFailedToCreateBackupErrorCode descriptionFromError:*error];
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
        NSURL *filePath = [_fileStructure.wwwFolder URLByAppendingPathComponent:deletedFile.name];
        if (![_fileManager removeItemAtURL:filePath error:error]) {
            NSLog(@"CHCP Warinig! Failed to delete file: %@", filePath.absoluteString);
            //break;
        }
    }
    
    // Since we are deleting files and some doesn't exist - probably it's not important and we can skip that kind of error in this situation.
    // If not - we should uncomment that line
    //return (*error == nil);
    
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
        // determine paths to the file in installation and www folders
        NSURL *pathInInstallationFolder = [_fileStructure.installationFolder URLByAppendingPathComponent:manifestFile.name];
        NSURL *pathInWwwFolder = [_fileStructure.wwwFolder URLByAppendingPathComponent:manifestFile.name];
        
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
    
    if (errorMsg) {
        *error = [NSError errorWithCode:kHCPFailedToCopyNewContentFilesErrorCode description:errorMsg];
    }
    
    return (*error == nil);
}

/**
 *  Save loaded configs to the www folder. They are now our current configs.
 */
- (void)saveNewConfigsToWwwFolder {
    [_manifestStorage store:_newManifest inFolder:_fileStructure.wwwFolder];
    [_configStorage store:_newConfig inFolder:_fileStructure.wwwFolder];
}

/**
 *  Remove all temporary files and folders.
 */
- (void)cleanUp {
    NSError *error = nil;
    [_fileManager removeItemAtURL:_fileStructure.installationFolder error:&error];
    if ([_fileManager fileExistsAtPath:_fileStructure.backupFolder.path]) {
        [_fileManager removeItemAtURL:_fileStructure.backupFolder error:&error];
    }
}

/**
 *  Restore content from the backup.
 */
- (void)rollback {
    if (![_fileManager fileExistsAtPath:_fileStructure.backupFolder.path]) {
        return;
    }
    
    NSError *error = nil;
    [_fileManager removeItemAtURL:_fileStructure.wwwFolder error:&error];
    if (error) {
        NSLog(@"Failed to rollback: %@", [error underlyingErrorLocalizedDesription]);
    }
    
    [_fileManager copyItemAtURL:_fileStructure.backupFolder toURL:_fileStructure.wwwFolder error:&error];
    if (error) {
        NSLog(@"Failed to rollback: %@", [error underlyingErrorLocalizedDesription]);
    }
}

@end
