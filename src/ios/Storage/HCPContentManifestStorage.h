//
//  HCPContentManifestStorage.h
//  TestIosCHCP
//
//  Created by Nikolay Demyankov on 12.08.15.
//
//

#import "HCPConfigStorageImpl.h"
#import "HCPFilesStructure.h"

@interface HCPContentManifestStorage : HCPConfigStorageImpl

- (instancetype)initWithFileStructure:(id<HCPFilesStructure>)fileStructure;

@end
