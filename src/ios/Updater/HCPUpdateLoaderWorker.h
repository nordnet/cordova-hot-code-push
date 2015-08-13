//
//  HCPUpdateLoaderWorker.h
//  TestIosCHCP
//
//  Created by Nikolay Demyankov on 11.08.15.
//
//

#import <Foundation/Foundation.h>
#import "HCPFilesStructure.h"
#import "HCPWorker.h"

@interface HCPUpdateLoaderWorker : NSObject<HCPWorker>

- (instancetype)initWithConfigUrl:(NSURL *)configURL filesStructure:(id<HCPFilesStructure>)fileStructure;

@end
