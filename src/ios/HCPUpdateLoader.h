//
//  HCPUpdateLoader.h
//
//  Created by Nikolay Demyankov on 11.08.15.
//

#import <Foundation/Foundation.h>
#import "HCPUpdateRequest.h"

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
 *  @param request update download parameters
 *  @param error   error object reference; filled with data when we failed to launch the update task
 *
 *  @return YES if download task is launched; NO - otherwise
 */
- (BOOL)executeDownloadRequest:(HCPUpdateRequest *)request error:(NSError **)error;

/**
 *  Flag to check if we are doing any downloads at the moment.
 *
 *  @return <code>YES</code> if download is running, <code>NO</code> otherwise.
 */
@property (nonatomic, readonly, getter=isDownloadInProgress) BOOL isDownloadInProgress;

@end
