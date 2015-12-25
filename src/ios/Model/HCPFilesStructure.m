//
//  HCPFilesStructureImpl.m
//
//  Created by Nikolay Demyankov on 12.08.15.
//

#import "HCPFilesStructure.h"
#import "NSFileManager+HCPExtension.h"

#pragma mark Predefined folders and file names of the plugin

static NSString *const CHCP_FOLDER = @"cordova-hot-code-push-plugin";
static NSString *const DOWNLOAD_FOLDER = @"update";
static NSString *const WWWW_FOLDER = @"www";
static NSString *const CHCP_JSON_FILE_PATH = @"chcp.json";
static NSString *const CHCP_MANIFEST_FILE_PATH = @"chcp.manifest";

@interface HCPFilesStructure()

@property (nonatomic, strong, readwrite) NSURL *contentFolder;
@property (nonatomic, strong, readwrite) NSURL *downloadFolder;
@property (nonatomic, strong, readwrite) NSURL *wwwFolder;

@end

@implementation HCPFilesStructure

#pragma mark Public API

- (instancetype)initWithReleaseVersion:(NSString *)releaseVersion {
    self = [super init];
    if (self) {
        [self localInitWithReleaseVersion:releaseVersion];
    }
    
    return self;
}

+ (NSURL *)pluginRootFolder {
    NSURL *supportDir = [[NSFileManager defaultManager] applicationSupportDirectory];
    return [supportDir URLByAppendingPathComponent:CHCP_FOLDER isDirectory:YES];
}

- (void)switchToRelease:(NSString *)releaseVersion {
    [self localInitWithReleaseVersion:releaseVersion];
    self.downloadFolder = nil;
    self.wwwFolder = nil;
}

- (void)localInitWithReleaseVersion:(NSString *)releaseVersion {
    _contentFolder = [[HCPFilesStructure pluginRootFolder]
                      URLByAppendingPathComponent:releaseVersion isDirectory:YES];
}

- (NSURL *)downloadFolder {
    if (_downloadFolder == nil) {
        _downloadFolder = [self.contentFolder URLByAppendingPathComponent:DOWNLOAD_FOLDER isDirectory:YES];
    }
    
    return _downloadFolder;
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