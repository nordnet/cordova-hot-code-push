//
//  HCPManifestDiff.m
//
//  Created by Nikolay Demyankov on 10.08.15.
//

#import "HCPManifestDiff.h"

@implementation HCPManifestDiff

- (instancetype)initWithAddedFiles:(NSArray *)addedFiles changedFiles:(NSArray *)changedFiles deletedFiles:(NSArray *)deletedFiles {
    self = [super init];
    if (self) {
        _addedFiles = addedFiles;
        _deletedFiles = deletedFiles;
        _changedFiles = changedFiles;
    }
    
    return self;
}

- (NSArray *)updateFileList {
    NSMutableArray *updateFileList = [[NSMutableArray alloc] init];
    [updateFileList addObjectsFromArray:_addedFiles];
    [updateFileList addObjectsFromArray:_changedFiles];
    
    return updateFileList;
}

- (BOOL)isEmpty {
    return _addedFiles.count == 0 && _changedFiles.count == 0 && _deletedFiles.count == 0;
}

@end
