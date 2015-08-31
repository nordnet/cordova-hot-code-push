//
//  HCPUpdateLoader.h
//
//  Created by Nikolay Demyankov on 11.08.15.
//

#import <Foundation/Foundation.h>
#import "HCPFilesStructure.h"

/**
 *  Utility class to perform update download.
 *  It only schedules the download and executes it as soon as possible.
 *
 *  Queue consists from 1 task, because we don't need to store 100 tasks for download request,
 *  we need only the last one.
 * 
 *  Class is a singleton.
 *
 *  @see HCPUpdateLoaderWorker
 */
@interface HCPUpdateLoader : NSObject

/**
 *  Get shared instance of the object.
 *
 *  @return instance of the object
 */
+ (HCPUpdateLoader *)sharedInstance;

/**
 *  Add update download task to queue. It will be executed as fast as possible.
 *
 *  @param configUrl url to the application config on the server
 *
 *  @return id of the created worker
 */
- (NSString *)addUpdateTaskToQueueWithConfigUrl:(NSURL *)configUrl;

/**
 *  Setup loader. Should be called on application startup before any real work is performed.
 *
 *  @param filesStructure plugins file structure
 *  @see HCPFilesStructure
 */
- (void)setup:(id<HCPFilesStructure>)filesStructure;

/**
 *  Flag to check if we are doing any downloads at the moment.
 *
 *  @return <code>YES</code> if download is running, <code>NO</code> otherwise.
 */
@property (nonatomic, readonly, getter=isDownloadInProgress) BOOL isDownloadInProgress;

@end
