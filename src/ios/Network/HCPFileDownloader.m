//
//  HCPFileDownloader.m
//  TestIosCHCP
//
//  Created by Nikolay Demyankov on 11.08.15.
//
//

#import "HCPFileDownloader.h"
#import "HCPFileDownloadWorker.h"
#import "HCPManifestFile.h"

@implementation HCPFileDownloader

#pragma mark Public API

- (void)downloadFileFromUrl:(NSURL *)url saveToFile:(NSURL *)filePath checksum:(NSString *)checksum complitionBlock:(HCPFileDownloadComplitionBlock)block {
    HCPFileDownloadWorker *downloadWorker = [[HCPFileDownloadWorker alloc] initWithUrl:url saveToFilePath:filePath checksum:checksum];
    if ([NSThread isMainThread]) {
        dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
            [downloadWorker downloadWithComplitionBlock:block];
        });
    } else {
        [downloadWorker downloadWithComplitionBlock:block];
    }
}

- (void)downloadFiles:(NSArray *)filesList fromURL:(NSURL *)contentURL toFolder:(NSURL *)folderURL complitionBlock:(HCPFileDownloadComplitionBlock)block {
    if ([NSThread isMainThread]) {
        __weak HCPFileDownloader *weakSelf = self;
        dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
            [weakSelf runDownloadOfFiles:filesList fromURL:contentURL toFolder:folderURL complitionBlock:block];
        });
    } else {
        [self runDownloadOfFiles:filesList fromURL:contentURL toFolder:folderURL complitionBlock:block];
    }
    
}

#pragma mark Private API

- (void)runDownloadOfFiles:(NSArray *)filesList fromURL:(NSURL *)contentURL toFolder:(NSURL *)folderURL complitionBlock:(HCPFileDownloadComplitionBlock)block {
    for (HCPManifestFile *file in filesList) {
        NSURL *filePathOnFileSystem = [folderURL URLByAppendingPathComponent:file.name isDirectory:NO];
        NSURL *fileUrlOnServer = [contentURL URLByAppendingPathComponent:file.name isDirectory:NO];
        
        HCPFileDownloadWorker *loadWorker = [[HCPFileDownloadWorker alloc] initWithUrl:fileUrlOnServer
                                                                        saveToFilePath:filePathOnFileSystem
                                                                              checksum:file.md5Hash];
        __block BOOL isFinished = false;
        __block NSError *loadError = nil;
        [loadWorker downloadWithComplitionBlock:^(NSError *error) {
            loadError = error;
            isFinished = YES;
        }];
        while (!isFinished) {
        }
        
        if (loadError) {
            block(loadError);
            break;
        }
    }
    
    block(nil);
}

@end
