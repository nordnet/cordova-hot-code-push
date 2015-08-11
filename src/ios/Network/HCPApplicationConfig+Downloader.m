//
//  HCPApplicationConfig+Downloader.m
//  TestIosCHCP
//
//  Created by Nikolay Demyankov on 10.08.15.
//
//

#import "HCPApplicationConfig+Downloader.h"
#import "HCPJsonDownloader.h"

@implementation HCPApplicationConfig (Downloader)

+ (void)downloadFromURL:(NSString *)url withComplitionBlock:(HCPApplicationConfigDownloadComplitionBlock)block {
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

@end
