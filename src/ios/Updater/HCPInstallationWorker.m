//
//  HCPInstallationWorker.m
//  TestIosCHCP
//
//  Created by Nikolay Demyankov on 12.08.15.
//
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

- (void)run {
    NSError *error = nil;
    if (![self initBeforeRun:&error] || ![self isUpdateValid:&error] ||
            ![self backupFiles:&error] || ![self deleteUnusedFiles:&error] || ![self moveDownloadedFilesToWwwFolder:&error]) {
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

- (void)dispatchEventWithError:(NSError *)error {
    NSNotification *notification = [HCPEvents notificationWithName:kHCPUpdateInstallationErrorEvent
                                                 applicationConfig:_newConfig
                                                            taskId:self.workerId error:error];
    [[NSNotificationCenter defaultCenter] postNotification:notification];
}

- (void)dispatchSuccessEvent {
    NSNotification *notification = [HCPEvents notificationWithName:kHCPUpdateIsInstalledEvent
                                                 applicationConfig:_newConfig
                                                            taskId:self.workerId];
    
    [[NSNotificationCenter defaultCenter] postNotification:notification];
}

- (BOOL)initBeforeRun:(NSError **)error {
    *error = nil;
    
    _fileManager = [NSFileManager defaultManager];
    _manifestStorage = [[HCPContentManifestStorage alloc] initWithFileStructure:_fileStructure];
    _configStorage = [[HCPApplicationConfigStorage alloc] initWithFileStructure:_fileStructure];
    
    _oldConfig = [_configStorage loadFromFolder:_fileStructure.wwwFolder];
    if (_oldConfig == nil) {
        *error = [NSError errorWithCode:0 description:@"Failed to load application config from cache folder"];
        return NO;
    }
    
    _newConfig = [_configStorage loadFromFolder:_fileStructure.downloadFolder];
    if (_newConfig == nil) {
        *error = [NSError errorWithCode:0 description:@"Failed to load application config from download folder"];
        return NO;
    }
    
    _oldManifest = [_manifestStorage loadFromFolder:_fileStructure.wwwFolder];
    if (_oldManifest == nil) {
        *error = [NSError errorWithCode:0 description:@"Failed to load content manifest from cache folder"];
        return NO;
    }
    
    _newManifest = [_manifestStorage loadFromFolder:_fileStructure.downloadFolder];
    if (_newManifest == nil) {
        *error = [NSError errorWithCode:0 description:@"Failed to load content manifest from download folder"];
        return NO;
    }
    
    _manifestDiff = [_oldManifest calculateDifference:_newManifest];
    
    return YES;
}

- (BOOL)isUpdateValid:(NSError **)error {
    *error = nil;
    NSString *errorMsg = nil;
    
    NSArray *updateFileList = _manifestDiff.updateFileList;
    for (HCPManifestFile *updatedFile in updateFileList) {
        NSURL *fileLocalURL = [_fileStructure.downloadFolder URLByAppendingPathComponent:updatedFile.name isDirectory:NO];
        if (![_fileManager fileExistsAtPath:fileLocalURL.path]) {
            errorMsg = [NSString stringWithFormat:@"Update validation error! File not found:%@", updatedFile.name];
            *error = [NSError errorWithCode:0 description:errorMsg];
            break;
        }
        
        NSString *fileMD5 = [[NSData dataWithContentsOfURL:fileLocalURL] md5];
        if (![fileMD5 isEqualToString:updatedFile.md5Hash]) {
            errorMsg = [NSString stringWithFormat:@"Update validation error! File's %@ hash %@ doesnt match the hash %@ from manifest file", updatedFile.name, fileMD5, updatedFile.md5Hash];
            *error = [NSError errorWithCode:0 description:errorMsg];
            break;
        }
    }
    
    return (*error == nil);
}

- (BOOL)backupFiles:(NSError **)error {
    *error = nil;
    
    return [_fileManager createDirectoryAtURL:[_fileStructure.backupFolder URLByDeletingLastPathComponent] withIntermediateDirectories:YES attributes:nil error:error] &&
            [_fileManager copyItemAtURL:_fileStructure.wwwFolder toURL:_fileStructure.backupFolder error:error];
}

- (BOOL)deleteUnusedFiles:(NSError **)error {
    *error = nil;
    NSArray *deletedFiles = _manifestDiff.deletedFiles;
    for (HCPManifestFile *deletedFile in deletedFiles) {
        NSURL *filePath = [_fileStructure.wwwFolder URLByAppendingPathComponent:deletedFile.name];
        if (![_fileManager removeItemAtURL:filePath error:error]) {
            break;
        }
    }
    
    return (*error == nil);
}

- (BOOL)moveDownloadedFilesToWwwFolder:(NSError **)error {
    *error = nil;
    NSFileManager *fileManager = [NSFileManager defaultManager];
    NSArray *updatedFiles = _manifestDiff.updateFileList;
    for (HCPManifestFile *manifestFile in updatedFiles) {
        NSURL *pathInDownloadFolder = [_fileStructure.downloadFolder URLByAppendingPathComponent:manifestFile.name];
        NSURL *pathInWwwFolder = [_fileStructure.wwwFolder URLByAppendingPathComponent:manifestFile.name];
        if ([fileManager fileExistsAtPath:pathInWwwFolder.path] && ![fileManager removeItemAtURL:pathInWwwFolder error:error]) {
            break;
        }
        
        if (![fileManager moveItemAtURL:pathInDownloadFolder toURL:pathInWwwFolder error:error]) {
            NSLog(@"%@", [(*error).userInfo[NSUnderlyingErrorKey] localizedDescription]);
            break;
        }
    }
    
    return (*error == nil);
}

- (void)saveNewConfigsToWwwFolder {
    [_manifestStorage store:_newManifest inFolder:_fileStructure.wwwFolder];
    [_configStorage store:_newConfig inFolder:_fileStructure.wwwFolder];
}

- (void)cleanUp {
    NSError *error = nil;
    [_fileManager removeItemAtURL:_fileStructure.downloadFolder error:&error];
    if ([_fileManager fileExistsAtPath:_fileStructure.backupFolder.path]) {
        [_fileManager removeItemAtURL:_fileStructure.backupFolder error:&error];
    }
}

- (void)rollback {
    if (![_fileManager fileExistsAtPath:_fileStructure.backupFolder.path]) {
        return;
    }
    
    NSError *error = nil;
    [_fileManager removeItemAtURL:_fileStructure.wwwFolder error:&error];
    [_fileManager createDirectoryAtURL:_fileStructure.wwwFolder withIntermediateDirectories:YES attributes:nil error:&error];
    [_fileManager copyItemAtURL:_fileStructure.backupFolder toURL:_fileStructure.wwwFolder error:&error];
}

@end
