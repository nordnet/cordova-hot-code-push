//
//  HCPConfigStorageImpl.h
//
//  Created by Nikolay Demyankov on 12.08.15.
//

#import <Foundation/Foundation.h>
#import "HCPConfigFileStorage.h"

/**
 *  Base implementation of the HCPConfigFileStorage protocol.
 *  
 *  @see HCPConfigFileStorage
 */
@interface HCPConfigStorageImpl : NSObject<HCPConfigFileStorage>

/**
 *  Getter for absolute url to the file from which we need to load a config instance.
 *  Used internally by the subclasses.
 *
 *  @param folder absoule url to the folder where config file is stored
 *
 *  @return absoulte url to the config file
 */
- (NSURL *)getFullUrlToFileInFolder:(NSURL *)folder;

/**
 *  Create instance of the object from the JSON object.
 *
 *  @param jsonObject JSON object to process
 *
 *  @return instance of the created object
 *  @see HCPJsonConvertable
 */
- (id<HCPJsonConvertable>)getInstanceFromJson:(id)jsonObject;

@end
