//
//  HCPManifestFile.h
//
//  Created by Nikolay Demyankov on 10.08.15.
//

#import <Foundation/Foundation.h>
#import "HCPJsonConvertable.h"

/**
 *  Model holds information about file in web project.
 */
@interface HCPManifestFile : NSObject<HCPJsonConvertable>

/**
 *  Relative path to the file inside the web project.
 */
@property (nonatomic, readonly, strong) NSString *name;

/**
 * Hash of the file.
 * By this we will detect if project file has changed.
 */
@property (nonatomic, readonly, strong) NSString *md5Hash;

/**
 *  Object initializer
 *
 *  @param name    name of the file
 *  @param md5Hash file md5 hash
 *
 *  @return instance of the object
 */
- (instancetype)initWithName:(NSString *)name md5Hash:(NSString *)md5Hash;

@end
