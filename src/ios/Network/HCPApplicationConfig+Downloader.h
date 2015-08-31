//
//  HCPApplicationConfig+Downloader.h
//
//  Created by Nikolay Demyankov on 10.08.15.
//

#import "HCPApplicationConfig.h"
#import "HCPApplicationConfig.h"

/**
 *  Application config download complition block.
 *
 *  @param error  holds information about occured error; <code>nil</code> if no error happened.
 *  @param config loaded application config
 */
typedef void (^HCPApplicationConfigDownloadComplitionBlock)(NSError *error, HCPApplicationConfig *config);

/**
 *  Category for HCPApplicationConfig.
 *  Adds methods to download application config from the server.
 */
@interface HCPApplicationConfig (Downloader)

/**
 *  Download application config asynchronously from the certain url.
 *
 *  @param url   url from which application config is loaded
 *  @param block download complition block
 */
+ (void)downloadFromURL:(NSURL *)url withComplitionBlock:(HCPApplicationConfigDownloadComplitionBlock)block;

/**
 *  Download application config synchronously from the certain url.
 *
 *  @param url   url from which application config is loaded
 *  @param error holds information about occured error; <code>nil</code> if no error happened.
 *
 *  @return loaded application config
 */
+ (HCPApplicationConfig *)downloadSyncFromURL:(NSURL *)url error:(NSError **)error;

@end
