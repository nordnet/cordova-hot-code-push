//
//  HCPApplicationConfig+Downloader.m
//
//  Created by Nikolay Demyankov on 10.08.15.
//

#import "HCPApplicationConfig+Downloader.h"
#import "HCPJsonDownloader.h"

@implementation HCPApplicationConfig (Downloader)

+ (void)downloadFromURL:(NSURL *)url withComplitionBlock:(HCPApplicationConfigDownloadComplitionBlock)block {
    if (block == nil) {
        return;
    }
    
    HCPJsonDownloader *jsonDownloader = [[HCPJsonDownloader alloc] initWithUrl:url];
    [jsonDownloader downloadWithComplitionBlock:^(NSError *error, id json) {
        HCPApplicationConfig *config = nil;
        if (error == nil) {
            config = [HCPApplicationConfig instanceFromJsonObject:json];
        }
        
        block(error, config);
    }];
}

+ (HCPApplicationConfig *)downloadSyncFromURL:(NSURL *)url error:(NSError **)error {
    HCPJsonDownloader *jsonDownloader = [[HCPJsonDownloader alloc] initWithUrl:url];
    id json = [jsonDownloader downloadSync:error];
    
    return [HCPApplicationConfig instanceFromJsonObject:json];
}

@end
