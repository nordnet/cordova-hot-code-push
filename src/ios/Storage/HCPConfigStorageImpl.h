//
//  HCPConfigStorageImpl.h
//  TestIosCHCP
//
//  Created by Nikolay Demyankov on 12.08.15.
//
//

#import <Foundation/Foundation.h>
#import "HCPConfigFileStorage.h"

@interface HCPConfigStorageImpl : NSObject<HCPConfigFileStorage>

- (NSURL *)getFullUrlToFileInFolder:(NSURL *)folder;

- (id<HCPJsonConvertable>)getInstanceFromJson:(id)jsonObject;

@end
