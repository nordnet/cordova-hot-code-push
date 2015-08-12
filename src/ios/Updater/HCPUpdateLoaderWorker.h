//
//  HCPUpdateLoaderWorker.h
//  TestIosCHCP
//
//  Created by Nikolay Demyankov on 11.08.15.
//
//

#import <Foundation/Foundation.h>
#import "HCPFilesStructure.h"

@interface HCPUpdateLoaderWorker : NSObject

@property (nonatomic, strong, readonly) NSString *workerId;

- (instancetype)initWithConfigUrl:(NSURL *)configURL filesStructure:(id<HCPFilesStructure>)fileStructure;

- (void)run;

@end
