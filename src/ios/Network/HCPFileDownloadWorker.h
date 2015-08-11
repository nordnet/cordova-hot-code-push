//
//  HCPFileDownloadWorker.h
//  TestIosCHCP
//
//  Created by Nikolay Demyankov on 11.08.15.
//
//

#import <Foundation/Foundation.h>

@interface HCPFileDownloadWorker : NSObject

@property (nonatomic, strong, readonly) NSURL *downloadURL;
@property (nonatomic, strong, readonly) NSURL *filePath;
@property (nonatomic, strong, readonly) NSString *checksum;

- (instancetype)initWithUrl:(NSURL *)url saveToFilePath:(NSURL *)filePath checksum:(NSString *)checksum;

- (void)downloadWithComplitionBlock:(void (^)(NSError *error))block;

@end
