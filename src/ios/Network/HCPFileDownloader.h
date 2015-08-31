//
//  HCPFileDownloader.h
//
//  Created by Nikolay Demyankov on 11.08.15.
//

#import <Foundation/Foundation.h>

/**
 *  Complition block for file download process.
 *
 *  @param holds information about occured error; <code>nil</code> if everything is fine
 */
typedef void (^HCPFileDownloadComplitionBlock)(NSError *error);

/**
 *  Helper class to download files from the server.
 */
@interface HCPFileDownloader : NSObject

/**
 *  Download file asynchronously.
 *
 *  @param url      url to the downloaded file
 *  @param filePath url in local file system where to put loaded file
 *  @param checksum hash of the file to check if it's not corrupted
 *  @param block    download complition block
 */
- (void)downloadFileFromUrl:(NSURL *)url saveToFile:(NSURL *)filePath checksum:(NSString *)checksum complitionBlock:(HCPFileDownloadComplitionBlock)block;

/**
 *  Download list of files asynchronously.
 *
 *  @param filesList  list of files to download. Files are instances of HCPManifestFile class.
 *  @param contentURL url on the server where all files are located. Full URL to the file is constructed from this one and the files mame.
 *  @param folderURL  url to the directory in local file system where to put all loaded files
 *  @param block      download complition block
 *
 *  @see HCPManifestFile
 */
- (void)downloadFiles:(NSArray *)filesList fromURL:(NSURL *)contentURL toFolder:(NSURL *)folderURL complitionBlock:(HCPFileDownloadComplitionBlock)block;

/**
 * Download list of files synchronously.
 *
 *  @param filesList  list of files to download. Files are instances of HCPManifestFile class.
 *  @param contentURL url on the server where all files are located. Full URL to the file is constructed from this one and the files mame.
 *  @param folderURL  url to the directory in local file system where to put all loaded files
 *  @param error      holds information about occured error; <code>nil</code> if everything is fine
 *  @return <code>YES</code> when all files are loaded; <code>NO</code> on download error
 *  @see HCPManifestFile
 */
- (BOOL)downloadFilesSync:(NSArray *)filesList fromURL:(NSURL *)contentURL toFolder:(NSURL *)folderURL error:(NSError **)error;

/**
 * Download file synchronously.
 *
 *  @param url      url to the downloaded file
 *  @param filePath url in local file system where to put loaded file
 *  @param checksum hash of the file to check if it's not corrupted
 *  @param error holds information about occured error; <code>nil</code> if everything is fine
 *
 *  @return <code>YES</code> when file is loaded; <code>NO</code> on download error
 */
- (BOOL)downloadFileSyncFromUrl:(NSURL *)url saveToFile:(NSURL *)filePath checksum:(NSString *)checksum error:(NSError **)error;

@end
