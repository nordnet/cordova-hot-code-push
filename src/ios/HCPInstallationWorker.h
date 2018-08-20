//
//  HCPInstallationWorker.h
//
//  Created by Nikolay Demyankov on 12.08.15.
//

#import <Foundation/Foundation.h>
#import "HCPWorker.h"
#import "HCPFilesStructure.h"

/**
 *  Worker, that implements installation logic.
 *  During the installation process events are dispatched to notify the subscribers about the progress.
 *
 *  @see HCPWorker
 */
@interface HCPInstallationWorker : NSObject<HCPWorker>

/**
 *  Initialize the worker.
 *
 *  @param newVersion new version of web content, that needs to be installed
 *  @param currentVersion current version of the web content
 *
 *  @return instance of the object
 */
- (instancetype)initWithNewVersion:(NSString *)newVersion currentVersion:(NSString *)currentVersion;

@end
