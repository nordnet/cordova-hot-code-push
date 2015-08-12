//
//  HCPFileDownloader.h
//  TestIosCHCP
//
//  Created by Nikolay Demyankov on 11.08.15.
//
//

#import <Foundation/Foundation.h>

typedef void (^HCPFileDownloadComplitionBlock)(NSError *error);

@interface HCPFileDownloader : NSObject

- (void)downloadFileFromUrl:(NSURL *)url saveToFile:(NSURL *)filePath checksum:(NSString *)checksum complitionBlock:(HCPFileDownloadComplitionBlock)block;

- (void)downloadFiles:(NSArray *)filesList fromURL:(NSURL *)contentURL toFolder:(NSURL *)folderURL complitionBlock:(HCPFileDownloadComplitionBlock)block;

- (void)downloadFilesSync:(NSArray *)filesList fromURL:(NSURL *)contentURL toFolder:(NSURL *)folderURL error:(NSError **)error;

- (void)downloadFileSyncFromUrl:(NSURL *)url saveToFile:(NSURL *)filePath checksum:(NSString *)checksum error:(NSError **)error;

@end
