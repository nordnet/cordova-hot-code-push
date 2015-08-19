//
//  HCPFileDownloader.m
//  TestIosCHCP
//
//  Created by Nikolay Demyankov on 11.08.15.
//
//

#import "HCPFileDownloader.h"
#import "HCPManifestFile.h"
#import "NSData+HCPMD5.h"
#import "NSError+HCPExtension.h"

@implementation HCPFileDownloader

#pragma mark Public API

- (void)downloadFileFromUrl:(NSURL *)url saveToFile:(NSURL *)filePath checksum:(NSString *)checksum complitionBlock:(HCPFileDownloadComplitionBlock)block {
    dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
        NSError *error = nil;
        [self executeFileDownloadFromURL:url saveToFile:filePath checksum:checksum error:&error];
        block(error);
    });
}

- (void)downloadFiles:(NSArray *)filesList fromURL:(NSURL *)contentURL toFolder:(NSURL *)folderURL complitionBlock:(HCPFileDownloadComplitionBlock)block {
    dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
        NSError *error = nil;
        [self executeDownloadOfFiles:filesList fromURL:contentURL toFolder:folderURL error:&error];
        block(error);
    });
}

- (void)downloadFilesSync:(NSArray *)filesList fromURL:(NSURL *)contentURL toFolder:(NSURL *)folderURL error:(NSError **)error {
    [self executeDownloadOfFiles:filesList fromURL:contentURL toFolder:folderURL error:error];
}

- (void)downloadFileSyncFromUrl:(NSURL *)url saveToFile:(NSURL *)filePath checksum:(NSString *)checksum error:(NSError **)error {
    [self executeFileDownloadFromURL:url saveToFile:filePath checksum:checksum error:error];
}

#pragma mark Private API

- (void)executeDownloadOfFiles:(NSArray *)filesList fromURL:(NSURL *)contentURL toFolder:(NSURL *)folderURL error:(NSError **)error {
    for (HCPManifestFile *file in filesList) {
        NSURL *filePathOnFileSystem = [folderURL URLByAppendingPathComponent:file.name isDirectory:NO];
        NSURL *fileUrlOnServer = [contentURL URLByAppendingPathComponent:file.name isDirectory:NO];
        BOOL isDownloaded = [self executeFileDownloadFromURL:fileUrlOnServer saveToFile:filePathOnFileSystem checksum:file.md5Hash error:error];
        if (!isDownloaded) {
            break;
        }
        
        NSLog(@"Loaded file %@", file.name);
    }
}

- (BOOL)executeFileDownloadFromURL:(NSURL *)url saveToFile:(NSURL *)fileURL checksum:(NSString *)checksum error:(NSError **)error {
    *error = nil;
    NSData *downloadedContent = [NSData dataWithContentsOfURL:url];
    if (downloadedContent == nil) {
        *error = [NSError errorWithCode:0 description:@"Failed to load file"];
        return NO;
    }
    
    if (![self isDataCorrupted:downloadedContent checksum:checksum error:error]) {
        [self prepareFileForSaving:fileURL];
        [downloadedContent writeToURL:fileURL options:kNilOptions error:error];
    }
    
    return (*error == nil);
}

- (BOOL)isDataCorrupted:(NSData *)data checksum:(NSString *)checksum error:(NSError **)error {
    NSString *dataHash = [data md5];
    if ([dataHash isEqualToString:checksum]) {
        return NO;
    }
    
    NSString *errorMsg = [NSString stringWithFormat:@"Hash %@ of the loaded file doesn't match the checksum %@", dataHash, checksum];
    *error = [NSError errorWithCode:0 description:errorMsg];
    
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
