//
//  HCPDataDownloader.h
//
//  Created by Nikolay Demyankov on 04.11.16.
//

#import <Foundation/Foundation.h>

/**
 *  Complition block for data download.
 *
 *  @param data  downloaded data
 *  @param error error information; <code>nil</code> - if everything is fine
 */
typedef void (^HCPDataDownloadCompletionBlock)(NSData *data, NSError *error);

/**
 *  Helper class to download data.
 */
@interface HCPDataDownloader : NSObject

/**
 *  Download data asynchronously.
 *
 *  @param url     url to the downloaded file
 *  @param headers request headers to send with the request
 *  @param block   data download completion block, called with the data when it is available.
 */
- (void) downloadDataFromUrl:(NSURL*)url
              requestHeaders:(NSDictionary *)headers
             completionBlock:(HCPDataDownloadCompletionBlock) block;

@end
