//
//  HCPFileDownloader.m
//  TestIosCHCP
//
//  Created by Nikolay Demyankov on 11.08.15.
//
//

#import "HCPFileDownloader.h"
#import "HCPManifestFile.h"
#import "NSData+MD5.h"

@implementation HCPFileDownloader

#pragma mark Public API

- (void)downloadFileFromUrl:(NSURL *)url saveToFile:(NSURL *)filePath checksum:(NSString *)checksum complitionBlock:(HCPFileDownloadComplitionBlock)block {
    if (![NSThread isMainThread]) {
        [self executeFileDownloadFromURL:url saveToFile:filePath checksum:checksum complitionBlock:block];
    } else {
        dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
            [self executeFileDownloadFromURL:url saveToFile:filePath checksum:checksum complitionBlock:block];
        });
    }
}

- (void)downloadFiles:(NSArray *)filesList fromURL:(NSURL *)contentURL toFolder:(NSURL *)folderURL complitionBlock:(HCPFileDownloadComplitionBlock)block {
    if (![NSThread isMainThread]) {
        [self executeDownloadOfFiles:filesList fromURL:contentURL toFolder:folderURL complitionBlock:block];
    } else {
        dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
            [self executeDownloadOfFiles:filesList fromURL:contentURL toFolder:folderURL complitionBlock:block];
        });
    }
}

#pragma mark Private API

- (void)executeDownloadOfFiles:(NSArray *)filesList fromURL:(NSURL *)contentURL toFolder:(NSURL *)folderURL complitionBlock:(HCPFileDownloadComplitionBlock)block {
    __block NSError *loadError = nil;
    for (HCPManifestFile *file in filesList) {
        NSURL *filePathOnFileSystem = [folderURL URLByAppendingPathComponent:file.name isDirectory:NO];
        NSURL *fileUrlOnServer = [contentURL URLByAppendingPathComponent:file.name isDirectory:NO];
        [self executeFileDownloadFromURL:fileUrlOnServer saveToFile:filePathOnFileSystem checksum:file.md5Hash complitionBlock:^(NSError *error){
            loadError = error;
        }];
        if (loadError) {
            break;
        }
        
        NSLog(@"Loaded file %@", file.name);
    }
    
    block(loadError);
}

- (void)executeFileDownloadFromURL:(NSURL *)url saveToFile:(NSURL *)fileURL checksum:(NSString *)checksum complitionBlock:(HCPFileDownloadComplitionBlock)block {
    NSError *error = nil;
    NSData *downloadedContent = [NSData dataWithContentsOfURL:url];
    if (downloadedContent == nil) {
        error = [[NSError alloc] initWithDomain:@"Failed to load file" code:0 userInfo:nil];
        block(error);
        return;
    }
    
    if (![self isDataCorrupted:downloadedContent checksum:checksum error:&error]) {
        [self prepareFileForSaving:fileURL];
        [downloadedContent writeToURL:fileURL options:kNilOptions error:&error];
    }
    
    block(error);
}

- (BOOL)isDataCorrupted:(NSData *)data checksum:(NSString *)checksum error:(NSError **)error {
    NSString *dataHash = [data md5];
    if ([dataHash isEqualToString:checksum]) {
        return NO;
    }
    
    NSString *errorMsg = [NSString stringWithFormat:@"Hash %@ of the loaded file doesn't match the checksum %@", dataHash, checksum];
    *error = [NSError errorWithDomain:errorMsg code:0 userInfo:nil];
    
    return YES;
}

- (void)prepareFileForSaving:(NSURL *)filePath {
    NSFileManager *fileManager = [NSFileManager defaultManager];
    if ([fileManager fileExistsAtPath:filePath.path]) {
        [fileManager removeItemAtURL:filePath error:nil];
    }
    
    [fileManager createDirectoryAtPath:[filePath.path stringByDeletingLastPathComponent]
            withIntermediateDirectories:YES
                             attributes:nil
                                  error:nil];
}
@end
