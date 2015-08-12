//
//  HCPApplicationConfig+Downloader.h
//  TestIosCHCP
//
//  Created by Nikolay Demyankov on 10.08.15.
//
//

#import "HCPApplicationConfig.h"
#import "HCPApplicationConfig.h"

typedef void (^HCPApplicationConfigDownloadComplitionBlock)(NSError *error, HCPApplicationConfig *config);

@interface HCPApplicationConfig (Downloader)

+ (void)downloadFromURL:(NSURL *)url withComplitionBlock:(HCPApplicationConfigDownloadComplitionBlock)block;

+ (HCPApplicationConfig *)downloadSyncFromURL:(NSURL *)url error:(NSError **)error;

@end
