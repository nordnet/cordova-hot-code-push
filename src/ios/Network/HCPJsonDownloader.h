//
//  HCPJsonDownloader.h
//
//  Created by Nikolay Demyankov on 11.08.15.
//

#import <Foundation/Foundation.h>

/**
 *  Complition block for JSON download process.
 *
 *  @param error object is not <code>nil</code> if some error has happened during the download.
 *  @param json  loaded json object
 */
typedef void (^HCPJsonDownloadComplitionBlock)(NSError *error, id json);

/**
 *  Helper class to download JSON and convert it into appropriate object.
 */
@interface HCPJsonDownloader : NSObject

/**
 *  URL from which we will download JSON.
 */
@property (nonatomic, strong, readonly) NSURL *url;

/**
 *  Initialize object.
 *
 *  @param url URL from which we should download JSON
 *
 *  @return instance of the object
 */
- (instancetype)initWithUrl:(NSURL *)url;

/**
 *  Perform download and call the provided block when finished.
 *  Download performed asynchronously.
 *
 *  @param block complition block
 */
- (void)downloadWithComplitionBlock:(HCPJsonDownloadComplitionBlock)block;

/**
 *  Download JSON synchronously.
 *
 *  @param error object will hold error information if any occur
 *
 *  @return loaded JSON object
 */
- (id)downloadSync:(NSError **)error;

@end
