//
//  HCPUpdateLoaderWorker.h
//
//  Created by Nikolay Demyankov on 11.08.15.
//

#import <Foundation/Foundation.h>
#import "HCPFilesStructure.h"
#import "HCPWorker.h"

/**
 *  Worker, that implements update download logic.
 *  During the download process events are dispatched to notify the subscribers about the progress.
 *  @see HCPWorker
 */
@interface HCPUpdateLoaderWorker : NSObject<HCPWorker>

/**
 *  Worker initializer.
 *
 *  @param configURL     url to the application config on the server
 *  @param currentVersion current version of the web content
 *
 *  @return instance of the object
 *  @see HCPFilesStructure
 */
- (instancetype)initWithConfigUrl:(NSURL *)configURL currentWebVersion:(NSString *)currentWebVersion nativeInterfaceVersion:(NSUInteger)currentNativeVersion;

@end
