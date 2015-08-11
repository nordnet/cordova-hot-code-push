//
//  HCPContentManifest+Downloader.m
//  TestIosCHCP
//
//  Created by Nikolay Demyankov on 10.08.15.
//
//

#import "HCPContentManifest+Downloader.h"
#import "HCPJsonDownloader.h"

@implementation HCPContentManifest (Downloader)

+ (void)downloadFromURL:(NSString *)url withComplitionBlock:(HCPContentManifestDownloadComplitionBlock)block {
    if (block == nil) {
        return;
    }
    
    HCPJsonDownloader *jsonDownloader = [[HCPJsonDownloader alloc] initWithUrl:url];
    [jsonDownloader downloadWithComplitionBlock:^(NSError *error, id json) {
        HCPContentManifest *manifest = nil;
        if (error != nil) {
            manifest = [HCPContentManifest instanceFromJsonObject:json];
        }
        
        block(error, manifest);
    }];
}

@end
