//
//  HCPContentManifestStorage.h
//
//  Created by Nikolay Demyankov on 12.08.15.
//

#import "HCPConfigStorageImpl.h"
#import "HCPFilesStructure.h"

/**
 *  Utility class to save and load content manifest file from the certain folder.
 * 
 *  @see HCPContentManifest
 */
@interface HCPContentManifestStorage : HCPConfigStorageImpl

/**
 *  Object initializer
 *
 *  @param fileStructure plugins file structure
 *
 *  @return instance of the object
 */
- (instancetype)initWithFileStructure:(HCPFilesStructure *)fileStructure;

@end
