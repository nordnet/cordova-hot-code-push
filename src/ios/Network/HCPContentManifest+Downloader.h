//
//  HCPContentManifest+Downloader.h
//  TestIosCHCP
//
//  Created by Nikolay Demyankov on 10.08.15.
//
//

#import "HCPContentManifest.h"

typedef void (^HCPContentManifestDownloadComplitionBlock)(NSError *error, HCPContentManifest *manifest);

@interface HCPContentManifest (Downloader)

+ (void)downloadFromURL:(NSString *)url withComplitionBlock:(HCPContentManifestDownloadComplitionBlock)block;

@end
