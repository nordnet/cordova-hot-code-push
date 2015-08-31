//
//  HCPContentManifest+Downloader.h
//
//  Created by Nikolay Demyankov on 10.08.15.
//

#import "HCPContentManifest.h"

/**
 *  Complition block for manifest download process.
 *
 *  @param error    holds information about the occured error; <code>nil</code> if everything is fine
 *  @param manifest loaded manifest file
 *  @see HCPContentManifest
 */
typedef void (^HCPContentManifestDownloadComplitionBlock)(NSError *error, HCPContentManifest *manifest);

/**
 *  Category for HCPContentManifest. 
 *  Adds methods to download manifest from server.
 */
@interface HCPContentManifest (Downloader)

/**
 *  Download manifest asynchronously from the certain url.
 *
 *  @param url   url from which manifest should be loaded
 *  @param block download complition block
 */
+ (void)downloadFromURL:(NSURL *)url withComplitionBlock:(HCPContentManifestDownloadComplitionBlock)block;

/**
 *  Download manifest file synchronously.
 *
 *  @param url   url from which manifest file is loaded
 *  @param error holds information about occured error; <code>nil</code> on success
 *
 *  @return loaded manifest instance; <code>nil</code> on error
 */
+ (HCPContentManifest *)downloadSyncFromURL:(NSURL *)url error:(NSError **)error;

@end
