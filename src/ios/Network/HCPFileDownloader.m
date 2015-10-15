//
//  HCPFileDownloader.m
//
//  Created by Nikolay Demyankov on 11.08.15.
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

- (BOOL)downloadFilesSync:(NSArray *)filesList fromURL:(NSURL *)contentURL toFolder:(NSURL *)folderURL error:(NSError **)error {
    [self executeDownloadOfFiles:filesList fromURL:contentURL toFolder:folderURL error:error];
    
    return (*error == nil);
}

- (BOOL)downloadFileSyncFromUrl:(NSURL *)url saveToFile:(NSURL *)filePath checksum:(NSString *)checksum error:(NSError **)error {
    [self executeFileDownloadFromURL:url saveToFile:filePath checksum:checksum error:error];
    
    return (*error == nil);
}

#pragma mark Private API

/**
 *  Perform download of the list of files
 *
 *  @param filesList  list of files to download
 *  @param contentURL base url for all the loaded files
 *  @param folderURL  where to put loaded files on the file system
 *  @param error      error information if any occure; <code>nil</code> if all files are loaded
 */
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

/**
 *  Perform download of the file from the provided url
 *
 *  @param url      url from which to downlaod the file
 *  @param fileURL  where to save file on the external storage
 *  @param checksum file checksum to validate it after the download
 *  @param error    error information if any occure; <code>nil</code> on download success
 *
 *  @return <code>YES</code> if file is downloaded; <code>NO</code> if we failed to download
 */
- (BOOL)executeFileDownloadFromURL:(NSURL *)url saveToFile:(NSURL *)fileURL checksum:(NSString *)checksum error:(NSError **)error {
    *error = nil;
    NSData *downloadedContent = [NSData dataWithContentsOfURL:url];
    if (downloadedContent == nil) {
        NSString *message = [NSString stringWithFormat:@"Failed to load file: %@", url];
        *error = [NSError errorWithCode:0 description:message];
        return NO;
    }
    
    if (![self isDataCorrupted:downloadedContent checksum:checksum error:error]) {
        [self prepareFileForSaving:fileURL];
        [downloadedContent writeToURL:fileURL options:kNilOptions error:error];
    }
    
    return (*error == nil);
}

/**
 *  Check if data was corrupted during the download.
 *
 *  @param data     data to check
 *  @param checksum supposed checksum of the data
 *  @param error    error details if data corrupted; <code>nil</code> if data is valid
 *
 *  @return <code>YES</code> if data is corrupted; <code>NO</code> if data is valid
 */
- (BOOL)isDataCorrupted:(NSData *)data checksum:(NSString *)checksum error:(NSError **)error {
    NSString *dataHash = [data md5];
    if ([dataHash isEqualToString:checksum]) {
        return NO;
    }
    
    NSString *errorMsg = [NSString stringWithFormat:@"Hash %@ of the loaded file doesn't match the checksum %@", dataHash, checksum];
    *error = [NSError errorWithCode:0 description:errorMsg];
    
    return YES;
}

/**
 *  Prepare file system for file download
 *
 *  @param filePath url to the file where it should be placed in the file system after download
 */
- (void)prepareFileForSaving:(NSURL *)filePath {
    NSFileManager *fileManager = [NSFileManager defaultManager];
    
    // remove old version of the file
    if ([fileManager fileExistsAtPath:filePath.path]) {
        [fileManager removeItemAtURL:filePath error:nil];
    }
    
    // create storage directories
    [fileManager createDirectoryAtPath:[filePath.path stringByDeletingLastPathComponent]
            withIntermediateDirectories:YES
                             attributes:nil
                                  error:nil];
}
@end
