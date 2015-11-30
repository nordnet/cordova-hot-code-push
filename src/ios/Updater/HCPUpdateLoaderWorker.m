//
//  HCPUpdateLoaderWorker.m
//
//  Created by Nikolay Demyankov on 11.08.15.
//

#import "HCPUpdateLoaderWorker.h"
#import "NSJSONSerialization+HCPExtension.h"
#import "NSBundle+HCPExtension.h"
#import "HCPManifestDiff.h"
#import "HCPManifestFile.h"
#import "HCPApplicationConfigStorage.h"
#import "HCPContentManifestStorage.h"
#import "HCPFileDownloader.h"
#import "HCPEvents.h"
#import "NSError+HCPExtension.h"
#import "HCPUpdateInstaller.h"
#import "HCPContentManifest.h"

@interface HCPUpdateLoaderWorker() {
    NSURL *_configURL;
    id<HCPFilesStructure> _pluginFiles;
    id<HCPConfigFileStorage> _appConfigStorage;
    id<HCPConfigFileStorage> _manifestStorage;
    HCPApplicationConfig *_oldAppConfig;
    HCPContentManifest *_oldManifest;
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
    
    // initialize before the run
    if (![self loadLocalConfigs:&error]) {
        [self notifyWithError:error applicationConfig:nil];
        return;
    }
    
    HCPFileDownloader *configDownload = [[HCPFileDownloader alloc] init];
    
    // download new application config
    [configDownload downloadDataFromUrl:_configURL completionBlock:^(NSData *data, NSError *error) {
        if (error) {
            [self notifyWithError:[NSError errorWithCode:kHCPFailedToDownloadApplicationConfigErrorCode descriptionFromError:error]
                applicationConfig:nil];
            return;
        }
        NSError* jsonError = nil;
        NSDictionary* json = [NSJSONSerialization JSONObjectWithData:data options:kNilOptions error:&jsonError];
        HCPApplicationConfig* newAppConfig = [HCPApplicationConfig instanceFromJsonObject:json];
        
        if ([newAppConfig.contentConfig.releaseVersion isEqualToString:_oldAppConfig.contentConfig.releaseVersion]) {
            [self notifyNothingToUpdate:newAppConfig];
            return;
        }
        
        // check if current native version supports new content
        if (newAppConfig.contentConfig.minimumNativeVersion > [NSBundle applicationBuildVersion]) {
            [self notifyWithError:[NSError errorWithCode:kHCPApplicationBuildVersionTooLowErrorCode
                                             description:@"Application build version is too low for this update"]
                applicationConfig:newAppConfig];
            return;
        }
        
        // download new content manifest
        NSURL *manifestFileURL = [newAppConfig.contentConfig.contentURL URLByAppendingPathComponent:_pluginFiles.manifestFileName];
        [configDownload downloadDataFromUrl:manifestFileURL completionBlock:^(NSData *data, NSError *error) {
            if (error) {
                [self notifyWithError:[NSError errorWithCode:kHCPFailedToDownloadContentManifestErrorCode
                                        descriptionFromError:error]
                    applicationConfig:newAppConfig];
                return;
            }
            
            NSError* jsonError = nil;
            NSDictionary* json = [NSJSONSerialization JSONObjectWithData:data options:kNilOptions error:&jsonError];
            HCPContentManifest* newManifest = [HCPContentManifest instanceFromJsonObject:json];
            
            // find files that were updated
            NSArray *updatedFiles = [_oldManifest calculateDifference:newManifest].updateFileList;
            if (updatedFiles.count == 0) {
                [_manifestStorage store:newManifest inFolder:_pluginFiles.wwwFolder];
                [_appConfigStorage store:newAppConfig inFolder:_pluginFiles.wwwFolder];
                [self notifyNothingToUpdate:newAppConfig];
                
                return;
            }
            
            [self recreateDownloadFolder:_pluginFiles.downloadFolder];
            
            // download files
            HCPFileDownloader *downloader = [[HCPFileDownloader alloc] init];
            // todo set credentials on downloader
            
            [downloader downloadFiles:updatedFiles
                              fromURL:newAppConfig.contentConfig.contentURL
                             toFolder:_pluginFiles.downloadFolder
                      completionBlock:^(NSError * error) {
                          
                if (error) {
                    [[NSFileManager defaultManager] removeItemAtURL:_pluginFiles.downloadFolder error:&error];
                    
                    [self notifyWithError:[NSError errorWithCode:kHCPFailedToDownloadUpdateFilesErrorCode
                                            descriptionFromError:error]
                        applicationConfig:newAppConfig];
                } else {
                    // store configs
                    [_manifestStorage store:newManifest inFolder:_pluginFiles.downloadFolder];
                    [_appConfigStorage store:newAppConfig inFolder:_pluginFiles.downloadFolder];
                    
                    // move download folder to installation folder
                    [self moveDownloadedContentToInstallationFolder];
                    
                    // notify that we are done
                    [self notifyUpdateDownloadSuccess:newAppConfig];
                }
            }];
            
        }];
    }];
}

#pragma mark Private API

/**
 *  Load configuration files from the file system.
 *
 *  @param error object to fill with error data if something will go wrong
 *
 *  @return <code>YES</code> if configs are loaded; <code>NO</code> - if some of the configs not found on file system
 */
- (BOOL)loadLocalConfigs:(NSError **)error {
    *error = nil;
    _oldAppConfig = [_appConfigStorage loadFromFolder:_pluginFiles.wwwFolder];
    if (_oldAppConfig == nil) {
        *error = [NSError errorWithCode:kHCPLocalVersionOfApplicationConfigNotFoundErrorCode
                            description:@"Failed to load current application config"];
        return NO;
    }
    
    _oldManifest = [_manifestStorage loadFromFolder:_pluginFiles.wwwFolder];
    if (_oldManifest == nil) {
        *error = [NSError errorWithCode:kHCPLocalVersionOfManifestNotFoundErrorCode
                            description:@"Failed to load current manifest file"];
        return NO;
    }
    
    return YES;
}

/**
 *  Copy all loaded files from download folder to installation folder from which we will install the update.
 */
- (void)moveDownloadedContentToInstallationFolder {
    [self waitForInstallationToComplete];
    
    NSFileManager *fileManager = [NSFileManager defaultManager];
    NSError *error = nil;
    [fileManager moveItemAtURL:_pluginFiles.downloadFolder toURL:_pluginFiles.installationFolder error:&error];
}

/**
 *  If installation is in progress - we should wait for it to finish before moving newly loaded files to installation folder.
 */
- (void)waitForInstallationToComplete {
    while ([HCPUpdateInstaller sharedInstance].isInstallationInProgress) {
        [NSThread sleepForTimeInterval:0.1]; // avoid busy loop
    }
}

/**
 *  Send notification with error details.
 *
 *  @param error  occured error
 *  @param config application config that was used for download
 */
- (void)notifyWithError:(NSError *)error applicationConfig:(HCPApplicationConfig *)config {
    NSNotification *notification = [HCPEvents notificationWithName:kHCPUpdateDownloadErrorEvent
                                                 applicationConfig:config
                                                            taskId:self.workerId
                                                             error:error];
    
    [[NSNotificationCenter defaultCenter] postNotification:notification];
}

/**
 *  Send notification that there is nothing to update and we are up-to-date
 *
 *  @param config application config that was used for download
 */
- (void)notifyNothingToUpdate:(HCPApplicationConfig *)config {
    NSError *error = [NSError errorWithCode:kHCPNothingToUpdateErrorCode description:@"Nothing to update"];
    NSNotification *notification = [HCPEvents notificationWithName:kHCPNothingToUpdateEvent
                                                 applicationConfig:config
                                                            taskId:self.workerId
                                                             error:error];
    
    [[NSNotificationCenter defaultCenter] postNotification:notification];
}

/**
 *  Send notification that update is loaded and ready for installation.
 *
 *  @param config application config that was used for download
 */
- (void)notifyUpdateDownloadSuccess:(HCPApplicationConfig *)config {
    NSNotification *notification = [HCPEvents notificationWithName:kHCPUpdateIsReadyForInstallationEvent
                                                 applicationConfig:config
                                                            taskId:self.workerId];
    
    [[NSNotificationCenter defaultCenter] postNotification:notification];
}

/**
 *  Remove old version of download folder and create the new one.
 *
 *  @param downloadFolder url to the download folder
 */
- (void)recreateDownloadFolder:(NSURL *)downloadFolder {
    NSFileManager *fileManager = [NSFileManager defaultManager];
    
    NSError *error = nil;
    if ([fileManager fileExistsAtPath:downloadFolder.path]) {
        [fileManager removeItemAtURL:downloadFolder error:&error];
    }
    
    [fileManager createDirectoryAtURL:downloadFolder withIntermediateDirectories:YES attributes:nil error:&error];
}

/**
 *  Create id of the download worker.
 *
 *  @return worker id
 */
- (NSString *)generateWorkerId {
    NSTimeInterval millis = [[NSDate date] timeIntervalSince1970];
    
    return [NSString stringWithFormat:@"%f",millis];
}

@end
