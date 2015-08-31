//
//  HCPConfigStorage.h
//
//  Created by Nikolay Demyankov on 12.08.15.
//

#import <Foundation/Foundation.h>
#import "HCPJsonConvertable.h"

/**
 *  Protocol describes objects for storing/restoring different config instances.
 */
@protocol HCPConfigFileStorage <NSObject>

/**
 *  Save object into the folder. Object will be stored as JSON string.
 *  Actual path to the file in the folder, where object is putted is determined by the implementation class.
 *
 *  @param config    config to save
 *  @param folderURL absolute URL to folder, where to save the object
 *  @return <code>YES</code> if config is saved; <code>NO</code> if failed
 *  @see HCPJsonConvertable
 */
- (BOOL)store:(id<HCPJsonConvertable>)config inFolder:(NSURL *)folderURL;

/**
 *  Load object from folder.
 *
 *  @param folderURL absolute URL to folder, where to save the object
 *
 *  @return instance of the object, loaded from the provided folder.
 */
- (id<HCPJsonConvertable>)loadFromFolder:(NSURL *)folderURL;

@end
