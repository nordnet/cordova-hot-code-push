//
//  HCPApplicationConfigStorage.h
//
//  Created by Nikolay Demyankov on 12.08.15.
//

#import <Foundation/Foundation.h>
#import "HCPConfigStorageImpl.h"
#import "HCPFilesStructure.h"

/**
 *  Utility class to save and load application config from the certain folder.
 *
 *  @see HCPConfigFileStorage
 */
@interface HCPApplicationConfigStorage : HCPConfigStorageImpl

/**
 *  Initialize object.
 *
 *  @param fileStructure plugins file structure
 *
 *  @return instance of the object
 *  @see HCPFilesStructure
 */
- (instancetype)initWithFileStructure:(HCPFilesStructure *)fileStructure;

@end
