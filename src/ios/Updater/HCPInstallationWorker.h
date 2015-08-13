//
//  HCPInstallationWorker.h
//  TestIosCHCP
//
//  Created by Nikolay Demyankov on 12.08.15.
//
//

#import <Foundation/Foundation.h>
#import "HCPWorker.h"
#import "HCPFilesStructure.h"

@interface HCPInstallationWorker : NSObject<HCPWorker>

- (instancetype)initWithFileStructure:(id<HCPFilesStructure>)fileStructure;

@end
