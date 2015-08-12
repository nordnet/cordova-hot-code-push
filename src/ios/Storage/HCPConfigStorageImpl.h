//
//  HCPConfigStorageImpl.h
//  TestIosCHCP
//
//  Created by Nikolay Demyankov on 12.08.15.
//
//

#import <Foundation/Foundation.h>
#import "HCPConfigStorage.h"

@interface HCPConfigStorageImpl : NSObject<HCPConfigStorage>

- (NSURL *)getFullUrlToFileInFolder:(NSURL *)folder;

- (id<HCPJsonConvertable>)getInstanceFromJson:(id)jsonObject;

@end
