//
//  HCPContentManifest.h
//  TestIosCHCP
//
//  Created by Nikolay Demyankov on 10.08.15.
//
//

#import <Foundation/Foundation.h>

#import "HCPJsonConvertable.h"
#import "HCPManifestDiff.h"

@interface HCPContentManifest : NSObject<HCPJsonConvertable>

@property (nonatomic, readonly, strong) NSArray *files;

- (HCPManifestDiff *)calculateDifference:(HCPContentManifest *)comparedManifest;

@end
