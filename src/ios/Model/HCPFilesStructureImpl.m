//
//  HCPFilesStructureImpl.m
//  TestIosCHCP
//
//  Created by Nikolay Demyankov on 12.08.15.
//
//

#import "HCPFilesStructureImpl.h"

static NSString *const CHCP_FOLDER = @"cordova-hot-code-push-plugin";
static NSString *const DOWNLOAD_FOLDER = @"www_tmp";
static NSString *const BACKUP_FOLDER = @"www_backup";
static NSString *const WWWW_FOLDER = @"www";
static NSString *const CHCP_JSON_FILE_PATH = @"chcp.json";
static NSString *const CHCP_MANIFEST_FILE_PATH = @"chcp.manifest";

@interface HCPFilesStructureImpl()

@property (nonatomic, strong, readwrite) NSURL *contentFolder;
@property (nonatomic, strong, readwrite) NSURL *downloadFolder;
@property (nonatomic, strong, readwrite) NSURL *backupFolder;
@property (nonatomic, strong, readwrite) NSURL *wwwFolder;
@property (nonatomic, strong) NSURL *contentConfigFileURL;
@property (nonatomic, strong) NSURL *manifestFileURL;

@end

@implementation HCPFilesStructureImpl

- (NSURL *)contentFolder {
    if (_contentFolder) {
        return _contentFolder;
    }
    
    NSFileManager *fileManager = [NSFileManager defaultManager];
    
    NSArray *possibleURLs = [fileManager URLsForDirectory:NSApplicationSupportDirectory inDomains:NSUserDomainMask];
    NSURL *appSupportDir = nil;
    
    if ([possibleURLs count] >= 1) {
        appSupportDir = [possibleURLs objectAtIndex:0];
    }
    
    if (appSupportDir) {
        NSString *appBundleID = [[NSBundle mainBundle] bundleIdentifier];
        _contentFolder = [[appSupportDir URLByAppendingPathComponent:appBundleID] URLByAppendingPathComponent:CHCP_FOLDER isDirectory:YES];
    }
    
    return _contentFolder;
}

- (NSURL *)downloadFolder {
    if (_downloadFolder) {
        return _downloadFolder;
    }
    
    NSFileManager *fileManager = [NSFileManager defaultManager];
    NSArray *possibleURLs = [fileManager URLsForDirectory:NSCachesDirectory
                                                inDomains:NSUserDomainMask];
    NSURL *appCacheDirectory = nil;
    
    if ([possibleURLs count] >= 1) {
        appCacheDirectory = [possibleURLs objectAtIndex:0];
    }
    
    if (appCacheDirectory) {
        NSString* appBundleID = [[NSBundle mainBundle] bundleIdentifier];
        _downloadFolder = [[appCacheDirectory URLByAppendingPathComponent:appBundleID] URLByAppendingPathComponent:DOWNLOAD_FOLDER isDirectory:YES];
    }
    
    return _downloadFolder;
}

- (NSURL *)backupFolder {
    if (_backupFolder == nil) {
        _backupFolder = [[self contentFolder] URLByAppendingPathComponent:BACKUP_FOLDER isDirectory:YES];
    }
    
    return _backupFolder;
}

- (NSURL *)wwwFolder {
    if (_wwwFolder == nil) {
        _wwwFolder = [[self contentFolder] URLByAppendingPathComponent:WWWW_FOLDER isDirectory:YES];
    }
    
    return _wwwFolder;
}

- (NSString *)configFileName {
    return CHCP_JSON_FILE_PATH;
}

- (NSString *)manifestFileName {
    return CHCP_MANIFEST_FILE_PATH;
}

@end
