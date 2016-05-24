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

- (void) downloadDataFromUrl:(NSURL*) url requestHeaders:(NSDictionary *)headers completionBlock:(HCPDataDownloadCompletionBlock) block {
    NSURLSessionConfiguration *configuration = [NSURLSessionConfiguration defaultSessionConfiguration];
    configuration.requestCachePolicy = NSURLRequestReloadIgnoringLocalCacheData;
    if (headers) {
        [configuration setHTTPAdditionalHeaders:headers];
    }
    NSURLSession *session = [NSURLSession sessionWithConfiguration:configuration];
    
    NSURLSessionDataTask* dowloadTask = [session dataTaskWithURL:url completionHandler:^(NSData * _Nullable data, NSURLResponse * _Nullable response, NSError * _Nullable error) {
        block(data, error);
    }];
    
    [dowloadTask resume];
}

- (void) downloadFiles:(NSArray *)filesList fromURL:(NSURL *)contentURL toFolder:(NSURL *)folderURL requestHeaders:(NSDictionary *)headers completionBlock:(HCPFileDownloadCompletionBlock)block {
    NSURLSessionConfiguration *configuration = [NSURLSessionConfiguration defaultSessionConfiguration];
    configuration.requestCachePolicy = NSURLRequestReloadIgnoringLocalCacheData;
    if (headers) {
        [configuration setHTTPAdditionalHeaders:headers];
    }
    NSURLSession *session = [NSURLSession sessionWithConfiguration:configuration];
    
    __block NSMutableSet* startedTasks = [NSMutableSet set];
    __block BOOL canceled = NO;
    for (HCPManifestFile *file in filesList) {
        NSURL *url = [contentURL URLByAppendingPathComponent:file.name];
        __block NSURLSessionDataTask *downloadTask = [session dataTaskWithURL:url completionHandler:^(NSData *data, NSURLResponse *response, NSError *error) {
            __weak __typeof(self) weakSelf = self;
            if (!error) {
                if ([weakSelf saveData:data forFile:file toFolder:folderURL error:&error]) {
                    NSLog(@"Loaded file %@ from %@", file.name, url.absoluteString);
                    [startedTasks removeObject:downloadTask];
                }
            }
            
            if (error) {
                [session invalidateAndCancel];
                [startedTasks removeAllObjects];
            }
            
            // operations finishes
            if (!canceled && (startedTasks.count == 0 || error)) {
                if (error) {
                    canceled = YES; // do not dispatch any other error
                }
                // we should already be in the background thread
                block(error);
            }
        }];
        
        [startedTasks addObject:downloadTask];
        [downloadTask resume];
    }
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
- (BOOL)saveData:(NSData *)data forFile:(HCPManifestFile *)file toFolder:(NSURL *)folderURL error:(NSError **)error {
    if ([self isDataCorrupted:data forFile:file error:error]) {
        return NO;
    }
    
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
    return [data writeToURL:filePath options:kNilOptions error:error];
}

@end
