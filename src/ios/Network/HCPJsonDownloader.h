//
//  HCPJsonDownloader.h
//  TestIosCHCP
//
//  Created by Nikolay Demyankov on 11.08.15.
//
//

#import <Foundation/Foundation.h>

typedef void (^HCPJsonDownloadComplitionBlock)(NSError *error, id json);

@interface HCPJsonDownloader : NSObject

@property (nonatomic, strong, readonly) NSString *url;

- (instancetype)initWithUrl:(NSString *)url;

- (void)downloadWithComplitionBlock:(HCPJsonDownloadComplitionBlock)block;

@end
