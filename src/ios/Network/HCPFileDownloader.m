//
//  HCPFileDownloader.m
//
//  Created by Nikolay Demyankov on 11.08.15.
//

#import "HCPFileDownloader.h"
#import "HCPManifestFile.h"
#import "NSData+HCPMD5.h"
#import "NSError+HCPExtension.h"

@interface HCPFileDownloader()<NSURLSessionDownloadDelegate> {
    NSArray *_filesList;
    NSURL *_contentURL;
    NSURL *_folderURL;
    NSDictionary *_headers;
    
    NSURLSession *_session;
    HCPFileDownloadCompletionBlock _complitionHandler;
    NSUInteger _downloadCounter;
}

@end

static NSUInteger const TIMEOUT = 300;

@implementation HCPFileDownloader

#pragma mark Public API

- (instancetype)initWithFiles:(NSArray *)filesList srcDirURL:(NSURL *)contentURL dstDirURL:(NSURL *)folderURL requestHeaders:(NSDictionary *)headers {
    self = [super init];
    if (self) {
        _filesList = filesList;
        _contentURL = contentURL;
        _folderURL = folderURL;
        _headers = headers;
    }
    
    return self;
}

- (NSURLSession *)sessionWithHeaders:(NSDictionary *)headers {
    NSURLSessionConfiguration *configuration = [NSURLSessionConfiguration defaultSessionConfiguration];
    configuration.requestCachePolicy = NSURLRequestReloadIgnoringLocalCacheData;
    configuration.timeoutIntervalForRequest = TIMEOUT;
    configuration.timeoutIntervalForResource = TIMEOUT;
    if (headers) {
        [configuration setHTTPAdditionalHeaders:headers];
    }
    
    return [NSURLSession sessionWithConfiguration:configuration delegate:self delegateQueue:nil];
}

- (void)startDownloadWithCompletionBlock:(HCPFileDownloadCompletionBlock)block {
    _complitionHandler = block;
    _downloadCounter = 0;
    _session = [self sessionWithHeaders:_headers];
    
    [self launchDownloadTaskForFile:_filesList[0]];
}

#pragma mark NSURLSessionTaskDelegate delegate

- (void)URLSession:(NSURLSession *)session didBecomeInvalidWithError:(NSError *)error {
    if (error && _complitionHandler) {
        _complitionHandler(error);
    }
}

- (void)URLSession:(NSURLSession *)session task:(NSURLSessionTask *)task didCompleteWithError:(NSError *)error {
    if (error) {
        [_session invalidateAndCancel];
        _complitionHandler(error);
        return;
    }
}

- (void)URLSession:(NSURLSession *)session downloadTask:(NSURLSessionDownloadTask *)downloadTask didFinishDownloadingToURL:(NSURL *)location {
    NSError *error = nil;
    if (![self moveLoadedFile:location forFile:_filesList[_downloadCounter] toFolder:_folderURL error:&error]) {
        _complitionHandler(error);
        [_session invalidateAndCancel];
        return;
    }
    
    _downloadCounter++;
    if (_downloadCounter >= _filesList.count) {
        [_session finishTasksAndInvalidate];
        _complitionHandler(nil);
        return;
    }
    
    [self launchDownloadTaskForFile:_filesList[_downloadCounter]];
}

- (void)launchDownloadTaskForFile:(HCPManifestFile *)file {
    NSURL *url = [_contentURL URLByAppendingPathComponent:file.name];
    NSLog(@"Starting file download: %@", url.absoluteString);
    
    [[_session downloadTaskWithURL:url] resume];
}

#pragma Private API

/**
 *  Check if data was corrupted during the download.
 *
 *  @param data     data to check
 *  @param checksum supposed checksum of the data
 *  @param error    error details if data corrupted; <code>nil</code> if data is valid
 *
 *  @return <code>YES</code> if data is corrupted; <code>NO</code> if data is valid
 */
- (BOOL)isDataCorrupted:(NSData *)data forFile:(HCPManifestFile *)file error:(NSError **)error {
    *error = nil;
    NSString *dataHash = [data md5];
    NSString *fileChecksum = file.md5Hash;
    if ([dataHash isEqualToString:fileChecksum]) {
        return NO;
    }
    
    NSString *errorMsg = [NSString stringWithFormat:@"Hash %@ of the file %@ doesn't match the checksum %@", dataHash, file.name, fileChecksum];
    *error = [NSError errorWithCode:kHCPFailedToDownloadUpdateFilesErrorCode description:errorMsg];
    
    return YES;
}

/**
 *  Save loaded file data to the file system.
 *
 *  @param data      loaded data
 *  @param file      file, whose data we loaded
 *  @param folderURL folder, where to save loaded data
 *  @param error     error entry; <code>nil</code> - if saved successfully;
 *
 *  @return <code>YES</code> - if data is saved; <code>NO</code> - otherwise
 */

- (BOOL)moveLoadedFile:(NSURL *)loadedFile forFile:(HCPManifestFile *)file toFolder:(NSURL *)folderURL error:(NSError **)error {
//    if ([self isDataCorrupted:data forFile:file error:error]) {
//        return NO;
//    }
//    
    NSURL *filePath = [folderURL URLByAppendingPathComponent:file.name];
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
    
    // write data
    //return [data writeToURL:filePath options:kNilOptions error:error];
    
    return [fileManager moveItemAtURL:loadedFile toURL:filePath error: error];
}


@end
