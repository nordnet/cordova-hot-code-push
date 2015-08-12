//
//  HCPApplicationConfigStorage.h
//  TestIosCHCP
//
//  Created by Nikolay Demyankov on 12.08.15.
//
//

#import <Foundation/Foundation.h>
#import "HCPConfigStorageImpl.h"
#import "HCPFilesStructure.h"

@interface HCPApplicationConfigStorage : HCPConfigStorageImpl

- (instancetype)initWithFileStructure:(id<HCPFilesStructure>)fileStructure;

@end
