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
 *  @param fileStructure plugins file structure
 *
 *  @return instance of the object
 */
- (instancetype)initWithFileStructure:(id<HCPFilesStructure>)fileStructure;

@end
