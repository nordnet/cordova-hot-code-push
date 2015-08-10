//
//  HCPManifestDiff.h
//  TestIosCHCP
//
//  Created by Nikolay Demyankov on 10.08.15.
//
//

#import <Foundation/Foundation.h>

#import "HCPManifestFile.h"

@interface HCPManifestDiff : NSObject

@property (nonatomic, strong, readonly) NSArray *updateFileList;

@property (nonatomic, strong, readonly) NSArray *addedFiles;
@property (nonatomic, strong, readonly) NSArray *changedFiles;
@property (nonatomic, strong, readonly) NSArray *deletedFiles;

- (instancetype)initWithAddedFiles:(NSArray *)addedFiles changedFiles:(NSArray *)changedFiles deletedFiles:(NSArray *)deletedFiles;

@end
