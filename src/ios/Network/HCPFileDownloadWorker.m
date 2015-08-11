//
//  HCPFileDownloadWorker.m
//  TestIosCHCP
//
//  Created by Nikolay Demyankov on 11.08.15.
//
//

#import "HCPFileDownloadWorker.h"
#import "NSData+MD5.h"

@interface HCPFileDownloadWorker() <NSURLConnectionDataDelegate> {
    BOOL _isLoading;
    NSFileManager *_fileManager;
    NSMutableData *_downloadedData;
    void (^_resultBlock)(NSError *error);
}

@end

@implementation HCPFileDownloadWorker

#pragma mark Public API

- (instancetype)initWithUrl:(NSURL *)url saveToFilePath:(NSURL *)filePath checksum:(NSString *)checksum {
    self = [super init];
    if (self) {
        _downloadURL = url;
        _filePath = filePath;
        _checksum = checksum;
        _fileManager = [NSFileManager defaultManager];
    }
    
    return self;
}

- (void)downloadWithComplitionBlock:(void (^)(NSError *error))block {
    if (_isLoading) {
        return;
    }
    _isLoading = true;
    _resultBlock = block;
    
    NSURLRequest *request = [NSURLRequest requestWithURL:self.downloadURL cachePolicy:NSURLRequestReloadIgnoringCacheData timeoutInterval:30];
    
    NSURLConnection *connection = [[NSURLConnection alloc] initWithRequest:request delegate:self];
    [connection start];
}

#pragma mark Private API

- (void)prepareFileForSaving {
    if ([_fileManager fileExistsAtPath:self.filePath.path]) {
        [_fileManager removeItemAtURL:self.filePath error:nil];
    }
    
    [_fileManager createDirectoryAtPath:[self.filePath.path stringByDeletingLastPathComponent]
           withIntermediateDirectories:YES
                            attributes:nil
                                 error:nil];
}

- (void)executeCallbackBlock:(NSError *)error {
    _isLoading = NO;
    _downloadedData = nil;
    _resultBlock(error);
    _resultBlock = nil;
}

#pragma mark NSURLConnectionDataDelegate

- (void)connection:(NSURLConnection *)connection didFailWithError:(NSError *)error {
    [self executeCallbackBlock:error];
}

- (void)connection:(NSURLConnection *)connection didReceiveResponse:(NSURLResponse *)response {
    _downloadedData = [[NSMutableData alloc] init];
}

- (void)connection:(NSURLConnection *)connection didReceiveData:(NSData *)data {
    [_downloadedData appendData:data];
}

- (void)connectionDidFinishLoading:(NSURLConnection *)connection {
    NSError *error = nil;
    NSString *loadedHash = [_downloadedData md5];
    if (![loadedHash isEqualToString:self.checksum]) {
        NSString *errorMsg = [NSString stringWithFormat:@"Hash %@ of the loaded file doesn't match the checksum %@", loadedHash, self.checksum];
        error = [[NSError alloc] initWithDomain:@"Checksum error" code:0 userInfo:@{@"description": errorMsg}];
    } else {
        [_downloadedData writeToURL:self.filePath options:kNilOptions error:&error];
    }
    
    [self executeCallbackBlock:error];
}


@end
