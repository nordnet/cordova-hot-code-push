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
 *  Complition block for data download.
 *
 *  @param data  downloaded data
 *  @param error error information; <code>nil</code> - if everything is fine
 */
typedef void (^HCPDataDownloadCompletionBlock)(NSData *data, NSError *error);

/**
 *  Helper class to download files from the server.
 */
@interface HCPFileDownloader : NSObject

/**
 *  Download data asynchronously.
 *
 *  @param url      url to the downloaded file
 *  @param block    data download completion block, called with the data when it is available.
 */
- (void) downloadDataFromUrl:(NSURL*)url
              requestHeaders:(NSDictionary *)headers
             completionBlock:(HCPDataDownloadCompletionBlock) block;

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
- (void) downloadFiles:(NSArray *)filesList
               fromURL:(NSURL *)contentURL
              toFolder:(NSURL *)folderURL
        requestHeaders:(NSDictionary *)headers
       completionBlock:(HCPFileDownloadCompletionBlock)block;

@end
