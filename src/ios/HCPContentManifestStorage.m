//
//  HCPContentManifestStorage.m
//
//  Created by Nikolay Demyankov on 12.08.15.
//

#import "HCPContentManifestStorage.h"
#import "HCPContentManifest.h"

@interface HCPContentManifestStorage() {
    NSString *_fileName;
}

@end

@implementation HCPContentManifestStorage

- (instancetype)initWithFileStructure:(HCPFilesStructure *)fileStructure {
    self = [super init];
    if (self) {
        _fileName = fileStructure.manifestFileName;
    }
    
    return self;
}

- (NSURL *)getFullUrlToFileInFolder:(NSURL *)folder {
    return [folder URLByAppendingPathComponent:_fileName];
}

- (id<HCPJsonConvertable>)getInstanceFromJson:(id)jsonObject {
    return [HCPContentManifest instanceFromJsonObject:jsonObject];
}

@end
