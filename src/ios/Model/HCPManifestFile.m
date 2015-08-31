//
//  HCPManifestFile.m
//
//  Created by Nikolay Demyankov on 10.08.15.
//

#import "HCPManifestFile.h"

#pragma mark JSON keys declaration

static NSString *const FILE_PATH = @"file";
static NSString *const FILE_HASH = @"hash";

@implementation HCPManifestFile

#pragma mark Public API

- (instancetype)initWithName:(NSString *)name md5Hash:(NSString *)md5Hash {
    self = [super init];
    if (self) {
        _name = name;
        _md5Hash = md5Hash;
    }
    
    return self;
}

- (BOOL)isEqual:(id)object {
    if (![object isKindOfClass:[HCPManifestFile class]]) {
        return [super isEqual:object];
    }
    
    HCPManifestFile *comparedFile = object;
    
    return [comparedFile.name isEqualToString:self.name] && [comparedFile.md5Hash isEqualToString:self.md5Hash];
}

#pragma mark HCPJsonConvertable implementation

- (id)toJson {
    return @{FILE_PATH: _name, FILE_HASH: _md5Hash};
}

+ (instancetype)instanceFromJsonObject:(id)json {
    if (![json isKindOfClass:[NSDictionary class]]) {
        return nil;
    }
    
    NSDictionary *jsonObject = json;
    
    return [[HCPManifestFile alloc] initWithName:jsonObject[FILE_PATH] md5Hash:jsonObject[FILE_HASH]];
}

@end
