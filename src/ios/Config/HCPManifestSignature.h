//
//  HCPContentManifest.h
//
//  Created by Nikolay Demyankov on 10.08.15.
//

#import <Foundation/Foundation.h>

#import "HCPJsonConvertable.h"
#import "HCPContentManifest.h"

/**
 *  Model for content manifest signature.
 *  The manifest signature contains a signed hash of the filenames and hashes in the content manifest.
 *  Used to cryptographically verify the integrity of the update.
 */
@interface HCPManifestSignature : NSObject<HCPJsonConvertable>

/**
 *  Signature encryption algorithm.
 */
@property (nonatomic, readonly, strong) NSString *algorithm;

/**
 * Content signature string.
 */
@property (nonatomic, readonly, strong) NSString *contentSignature;

/**
 *  Object initializer
 *
 *  @param contentSignature content signature string
 *  @param algorithm signature encryption algorithm
 *
 *  @return instance of the object
 */
- (instancetype)initWithContentSignature:(NSString *)contentSignature algorithm:(NSString *)algorithm;

- (BOOL)isContentManifestValid:(HCPContentManifest *)manifest usingSignatureCertificate:(NSString *)signingCertificate;

@end
