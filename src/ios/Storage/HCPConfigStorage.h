//
//  HCPConfigStorage.h
//  TestIosCHCP
//
//  Created by Nikolay Demyankov on 12.08.15.
//
//

#import <Foundation/Foundation.h>
#import "HCPJsonConvertable.h"

@protocol HCPConfigStorage <NSObject>

- (void)store:(id<HCPJsonConvertable>)config inFolder:(NSURL *)folderURL;

- (id<HCPJsonConvertable>)loadFromFolder:(NSURL *)folderURL;

@end
