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
 *  Download list of files asynchronously.
 *
 *  @param filesList  list of files to download. Files are instances of HCPManifestFile class.
 *  @param contentURL url on the server where all files are located. Full URL to the file is constructed from this one and the files mame.
 *  @param folderURL  url to the directory in local file system where to put all loaded files
 *  @param block      download completion block
 *
 *  @see HCPManifestFile
 */

- (instancetype)initWithFiles:(NSArray *)filesList
                    srcDirURL:(NSURL *)contentURL
                    dstDirURL:(NSURL *)folderURL
               requestHeaders:(NSDictionary *)headers;

- (void)startDownloadWithCompletionBlock:(HCPFileDownloadCompletionBlock)block;


@end
