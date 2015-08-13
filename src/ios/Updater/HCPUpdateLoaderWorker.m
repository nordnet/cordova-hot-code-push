//
//  HCPUpdateLoaderWorker.m
//  TestIosCHCP
//
//  Created by Nikolay Demyankov on 11.08.15.
//
//

#import "HCPUpdateLoaderWorker.h"
#import "HCPContentManifest+Downloader.h"
#import "HCPApplicationConfig+Downloader.h"
#import "NSJSONSerialization+HCPExtension.h"
#import "NSBundle+HCPExtension.h"
#import "HCPManifestDiff.h"
#import "HCPManifestFile.h"
#import "HCPApplicationConfigStorage.h"
#import "HCPContentManifestStorage.h"
#import "HCPFileDownloader.h"
#import "HCPEvents.h"
#import "NSError+HCPExtension.h"

@interface HCPUpdateLoaderWorker() {
    NSURL *_configURL;
    id<HCPFilesStructure> _pluginFiles;
    id<HCPConfigFileStorage> _appConfigStorage;
    id<HCPConfigFileStorage> _manifestStorage;
}

@property (nonatomic, strong, readwrite) NSString *workerId;

@end

@implementation HCPUpdateLoaderWorker

#pragma mark Public API

- (instancetype)initWithConfigUrl:(NSURL *)configURL filesStructure:(id<HCPFilesStructure>)fileStructure {
    self = [super init];
    if (self) {
        _configURL = configURL;
        _workerId = [self generateWorkerId];
        _pluginFiles = fileStructure;
        _appConfigStorage = [[HCPApplicationConfigStorage alloc] initWithFileStructure:fileStructure];
        _manifestStorage = [[HCPContentManifestStorage alloc] initWithFileStructure:fileStructure];
    }
    
    return self;
}

- (void)run {
    NSError *error = nil;
    // TODO: wait for installation to finish
    
    
    HCPApplicationConfig *oldAppConfig = [_appConfigStorage loadFromFolder:_pluginFiles.wwwFolder];
    if (oldAppConfig == nil) {
        [self notifyWithError:[NSError errorWithCode:0 description:@"Failed to load current application config"]
            applicationConfig:nil];
        return;
    }
    
    HCPContentManifest *oldManifest = [_manifestStorage loadFromFolder:_pluginFiles.wwwFolder];
    if (oldManifest == nil) {
        [self notifyWithError:[NSError errorWithCode:0 description:@"Failed to load current manifest file"]
            applicationConfig:nil];
        return;
    }
    
    // download new application config
    HCPApplicationConfig *newAppConfig = [HCPApplicationConfig downloadSyncFromURL:_configURL error:&error];
    if (error) {
        [self notifyWithError:error applicationConfig:newAppConfig];
        return;
    }
    
    if ([newAppConfig.contentConfig.releaseVersion isEqualToString:oldAppConfig.contentConfig.releaseVersion]) {
        [self notifyNothingToUpdate:newAppConfig];
        return;
    }
    
    // check if current native version supports new content
    if (newAppConfig.contentConfig.minimumNativeVersion > [NSBundle applicationBuildVersion]) {
        [self notifyWithError:[NSError errorWithCode:-2 description:@"Application build version is too low for this update"]
            applicationConfig:newAppConfig];
        return;
    }
    
    // download new content manifest
    NSURL *manifestFileURL = [newAppConfig.contentConfig.contentURL URLByAppendingPathComponent:_pluginFiles.manifestFileName];
    HCPContentManifest *newManifest = [HCPContentManifest downloadSyncFromURL:manifestFileURL error:&error];
    if (error) {
        [self notifyWithError:error applicationConfig:newAppConfig];
        return;
    }
    
    // find files that were updated
    NSArray *updatedFiles = [oldManifest calculateDifference:newManifest].updateFileList;
    if (updatedFiles.count == 0) {
        [_manifestStorage store:newManifest inFolder:_pluginFiles.wwwFolder];
        [_appConfigStorage store:newAppConfig inFolder:_pluginFiles.wwwFolder];
        [self notifyNothingToUpdate:newAppConfig];
        
        return;
    }
    
    [self recreateDownloadFolder:_pluginFiles.downloadFolder];
    
    // download files
    HCPFileDownloader *downloader = [[HCPFileDownloader alloc] init];
    [downloader downloadFilesSync:updatedFiles fromURL:newAppConfig.contentConfig.contentURL toFolder:_pluginFiles.downloadFolder error:&error];
    if (error) {
        [[NSFileManager defaultManager] removeItemAtURL:_pluginFiles.downloadFolder error:&error];
        [self notifyWithError:error applicationConfig:newAppConfig];
        return;
    }
    
    // store configs
    [_manifestStorage store:newManifest inFolder:_pluginFiles.downloadFolder];
    [_appConfigStorage store:newAppConfig inFolder:_pluginFiles.downloadFolder];
    
    
    // notify that we are done
    [self notifyUpdateDownloadSuccess:newAppConfig];
}

#pragma mark Private API

- (void)notifyWithError:(NSError *)error applicationConfig:(HCPApplicationConfig *)config {
    NSNotification *notification = [HCPEvents notificationWithName:kHCPUpdateDownloadErrorEvent applicationConfig:config taskId:self.workerId error:error];
    [[NSNotificationCenter defaultCenter] postNotification:notification];
}

- (void)notifyNothingToUpdate:(HCPApplicationConfig *)config {
    NSNotification *notification = [HCPEvents notificationWithName:kHCPNothingToUpdateEvent applicationConfig:config taskId:self.workerId];
    [[NSNotificationCenter defaultCenter] postNotification:notification];
}

- (void)notifyUpdateDownloadSuccess:(HCPApplicationConfig *)config {
    NSNotification *notification = [HCPEvents notificationWithName:kHCPUpdateIsReadyForInstallationEvent applicationConfig:config taskId:self.workerId];
    [[NSNotificationCenter defaultCenter] postNotification:notification];
}

- (void)recreateDownloadFolder:(NSURL *)downloadFolder {
    NSFileManager *fileManager = [NSFileManager defaultManager];
    
    NSError *error = nil;
    if ([fileManager fileExistsAtPath:downloadFolder.absoluteString]) {
        [fileManager removeItemAtURL:downloadFolder error:&error];
    }
    
    [fileManager createDirectoryAtURL:downloadFolder withIntermediateDirectories:YES attributes:nil error:&error];
}

- (NSString *)generateWorkerId {
    NSTimeInterval millis = [[NSDate date] timeIntervalSince1970];
    
    return [NSString stringWithFormat:@"%f",millis];
}

@end
