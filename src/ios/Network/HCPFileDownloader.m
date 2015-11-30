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


- (void) downloadDataFromUrl:(NSURL*) url completionBlock:(HCPDataDownloadCompletionBlock) block {
    
    NSURLSessionConfiguration *configuration = [NSURLSessionConfiguration defaultSessionConfiguration];
    NSURLSession              *session       = [NSURLSession sessionWithConfiguration:configuration];
    
    NSURLSessionDataTask* dowloadTask = [session dataTaskWithURL:url completionHandler:^(NSData * _Nullable data, NSURLResponse * _Nullable response, NSError * _Nullable error) {
            block(data, error);
    }];
    
    [dowloadTask resume];
}

- (void) downloadFiles:(NSArray *)filesList fromURL:(NSURL *)contentURL toFolder:(NSURL *)folderURL completionBlock:(HCPFileDownloadCompletionBlock)block {
    NSURLSessionConfiguration *configuration = [NSURLSessionConfiguration defaultSessionConfiguration];
    NSURLSession              *session       = [NSURLSession sessionWithConfiguration:configuration];
    
    __block NSMutableSet* startedTasks = [NSMutableSet set];
    __block BOOL canceled = NO;
    for (HCPManifestFile *file in filesList)
    {
        NSURL *url = [contentURL URLByAppendingPathComponent:file.name];
        __block NSURLSessionDataTask *downloadTask = [session dataTaskWithURL:url completionHandler:^(NSData *data, NSURLResponse *response, NSError *error) {
            NSError* operationError = nil;
            if (error) {
                operationError = error;
            }
            
            if (!error) {
                if (![self isDataCorrupted:data checksum:file.md5Hash error:&error]) {
                    NSURL *finalPath = [folderURL URLByAppendingPathComponent:file.name];
                    [self prepareFileForSaving:finalPath];
                    
                    BOOL success = [data writeToURL:finalPath options:kNilOptions error:&error];
                    if (success) {
                        NSLog(@"Loaded file %@", file.name);
                        [startedTasks removeObject:downloadTask];
                    } else {
                        operationError = error;
                    }
                } else {
                    NSString *message = [NSString stringWithFormat:@"Failed to load file: %@", url];
                    operationError = [NSError errorWithCode:0 description:message];
                }
            }
            
            if (operationError) {
                [session invalidateAndCancel];
                [startedTasks removeAllObjects];
            }
            // operations finishes
            if (!canceled && (startedTasks.count == 0 || operationError)) {
                if (operationError) {
                    canceled = YES; // do not dispatch any other error
                }
                dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
                    block(operationError);
                });
            }
            
        }];
        
        [startedTasks addObject:downloadTask];
        [downloadTask resume];
    }
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
