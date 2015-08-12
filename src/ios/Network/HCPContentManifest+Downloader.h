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

+ (void)downloadFromURL:(NSURL *)url withComplitionBlock:(HCPContentManifestDownloadComplitionBlock)block;

+ (HCPContentManifest *)downloadSyncFromURL:(NSURL *)url error:(NSError **)error;

@end
