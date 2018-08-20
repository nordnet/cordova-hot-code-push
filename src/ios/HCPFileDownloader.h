//
//  HCPFileDownloader.h
//
//  Created by Nikolay Demyankov on 11.08.15.
//

#import <Foundation/Foundation.h>

/**
 *  Complition block for file download process.
 *
 *  @param error holds information about occured error; <code>nil</code> if everything is fine
 */
typedef void (^HCPFileDownloadCompletionBlock)(NSError *error);

/**
 *  Helper class to download files from the server.
 */
@interface HCPFileDownloader : NSObject

/**
 * Constructor.
 * 
 * @param filesList  list of files to download
 * @param contentURL url on the server where files are located
 * @param folderURL  where loaded files should be placed
 * @param headers    headers to attach to the requests
 */
- (instancetype)initWithFiles:(NSArray *)filesList
                    srcDirURL:(NSURL *)contentURL
                    dstDirURL:(NSURL *)folderURL
               requestHeaders:(NSDictionary *)headers;

/**
 * Start download task.
 * 
 * @param block complition block
 */
- (void)startDownloadWithCompletionBlock:(HCPFileDownloadCompletionBlock)block;

@end
