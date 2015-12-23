//
//  HCPFilesStructureImpl.m
//
//  Created by Nikolay Demyankov on 12.08.15.
//

#import "HCPFilesStructureImpl.h"
#import "NSFileManager+HCPExtension.h"

#pragma mark Predefined folders and file names of the plugin

static NSString *const CHCP_FOLDER = @"cordova-hot-code-push-plugin";
static NSString *const DOWNLOAD_FOLDER = @"www_tmp";
static NSString *const INSTALLATION_FOLDER = @"www_install";
static NSString *const BACKUP_FOLDER = @"www_backup";
static NSString *const WWWW_FOLDER = @"www";
static NSString *const CHCP_JSON_FILE_PATH = @"chcp.json";
static NSString *const CHCP_MANIFEST_FILE_PATH = @"chcp.manifest";

@interface HCPFilesStructureImpl()

@property (nonatomic, strong, readwrite) NSURL *contentFolder;
@property (nonatomic, strong, readwrite) NSURL *downloadFolder;
@property (nonatomic, strong, readwrite) NSURL *installationFolder;
@property (nonatomic, strong, readwrite) NSURL *backupFolder;
@property (nonatomic, strong, readwrite) NSURL *wwwFolder;

@end

@implementation HCPFilesStructureImpl

#pragma mark Public API

- (instancetype)initWithReleaseVersion:(NSString *)releaseVersion {
    self = [super init];
    if (self) {
        [self localInitWithReleaseVersion:releaseVersion];
    }
    
    return self;
}

- (void)switchToRelease:(NSString *)releaseVersion {
    [self localInitWithReleaseVersion:releaseVersion];
    self.downloadFolder = nil;
    self.installationFolder = nil;
    self.backupFolder = nil;
    self.wwwFolder = nil;
}

- (void)localInitWithReleaseVersion:(NSString *)releaseVersion {
    NSURL *supportDir = [[NSFileManager defaultManager] applicationSupportDirectory];
    _contentFolder = [[supportDir URLByAppendingPathComponent:CHCP_FOLDER isDirectory:YES]
                      URLByAppendingPathComponent:releaseVersion isDirectory:YES];
}

- (NSURL *)downloadFolder {
    if (_downloadFolder == nil) {
        _downloadFolder = [self.contentFolder URLByAppendingPathComponent:DOWNLOAD_FOLDER isDirectory:YES];
    }
    
    return _downloadFolder;
}

- (NSURL *)installationFolder {
    if (_installationFolder == nil) {
        _installationFolder = [self.contentFolder URLByAppendingPathComponent:INSTALLATION_FOLDER isDirectory:YES];
    }
    
    return _installationFolder;
}

- (NSURL *)backupFolder {
    if (_backupFolder == nil) {
        _backupFolder = [self.contentFolder URLByAppendingPathComponent:BACKUP_FOLDER isDirectory:YES];
    }
    
    return _backupFolder;
}

- (NSURL *)wwwFolder {
    if (_wwwFolder == nil) {
        _wwwFolder = [self.contentFolder URLByAppendingPathComponent:WWWW_FOLDER isDirectory:YES];
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