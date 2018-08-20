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

// 릴리즈 버전으로 초기화
- (instancetype)initWithReleaseVersion:(NSString *)releaseVersion {
    self = [super init];
    if (self) {
        [self localInitWithReleaseVersion:releaseVersion];
    }
    
    return self;
}

+ (NSURL *)pluginRootFolder {
    // static declaration gets executed only once
    static NSURL *_pluginRootFolder = nil;
    if (_pluginRootFolder != nil) {
        return _pluginRootFolder;
    }

    // construct path to the folder, where we will store our plugin's files
    NSFileManager *fileManager = [NSFileManager defaultManager];
    // 서포트 디렉토리의 경로를 가져옴
    NSURL *supportDir = [fileManager applicationSupportDirectory];
    _pluginRootFolder = [supportDir URLByAppendingPathComponent:CHCP_FOLDER isDirectory:YES];

    // 플러그인 루트폴더(서포트 디렉토리에 CHCP_FOLDER) 의 경로에 파일이 없으면
    if (![fileManager fileExistsAtPath:_pluginRootFolder.path]) {
        // 해당 경로에 폴더 만들기
        [fileManager createDirectoryAtURL:_pluginRootFolder withIntermediateDirectories:YES attributes:nil error:nil];
    }

    // 플러그인 루트 폴더를 iCloud backup에서 제외함. 어플리케이션이 너무 커지면 애플이 어플을 거절 할 수 있음
    // https://developer.apple.com/library/ios/qa/qa1719/_index.html
    NSError *error = nil;
    BOOL success = [_pluginRootFolder setResourceValue:[NSNumber numberWithBool:YES] forKey:NSURLIsExcludedFromBackupKey error:&error];
    if (!success) {
        NSLog(@"Error excluding %@ from backup %@", [_pluginRootFolder lastPathComponent], error);
    }
    
    return _pluginRootFolder;
}

// 릴리즈버전의 content folder 주소 저장
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

+ (NSString *)defaultConfigFileName {
    return CHCP_JSON_FILE_PATH;
}

+ (NSString *)defaultManifestFileName {
    return CHCP_MANIFEST_FILE_PATH;
}

@end