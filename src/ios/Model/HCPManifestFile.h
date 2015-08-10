//
//  HCPManifestFile.h
//  TestIosCHCP
//
//  Created by Nikolay Demyankov on 10.08.15.
//
//

#import <Foundation/Foundation.h>
#import "HCPJsonConvertable.h"

@interface HCPManifestFile : NSObject<HCPJsonConvertable>

@property (nonatomic, readonly, strong) NSString *name;
@property (nonatomic, readonly, strong) NSString *md5Hash;

- (instancetype)initWithName:(NSString *)name md5Hash:(NSString *)md5Hash;

@end
