//
//  HCPContentManifest.m
//
//  Created by Nikolay Demyankov on 10.08.15.
//

#import "HCPManifestSignature.h"
#import "HCPContentManifest.h"
#import "HCPManifestFile.h"
#import <CommonCrypto/CommonDigest.h>

#pragma mark JSON keys declaration

static NSString *const ALGORITHM = @"algorithm";
static NSString *const CONTENT_SIGNATURE = @"contentSignature";
static NSString *const X509_BEGIN_MARKER = @"-----BEGIN CERTIFICATE-----";
static NSString *const X509_END_MARKER = @"-----END CERTIFICATE-----";

@implementation HCPManifestSignature

#pragma mark Public API

- (instancetype)initWithContentSignature:(NSString *)contentSignature algorithm:(NSString *)algorithm {
    self = [super init];
    if (self) {
        _contentSignature = contentSignature;
        _algorithm = algorithm;
    }
    
    return self;
}

- (BOOL)isEqual:(id)object {
    if (![object isKindOfClass:[HCPManifestSignature class]]) {
        return [super isEqual:object];
    }
    
    HCPManifestSignature *comparedSignature = object;
    
    return [comparedSignature.contentSignature isEqualToString:self.contentSignature] && [comparedSignature.algorithm isEqualToString:self.algorithm];
}

- (BOOL)isContentManifestValid:(HCPContentManifest *)manifest usingSignatureCertificate:(NSString *)signingCertificate {
    NSUInteger startMarker = [signingCertificate rangeOfString:X509_BEGIN_MARKER].location;
    NSUInteger endMarker = [signingCertificate rangeOfString:X509_END_MARKER].location;
    NSRange innerCertificateRange = NSMakeRange(startMarker + X509_BEGIN_MARKER.length, endMarker - startMarker - X509_BEGIN_MARKER.length);
    NSString *innerCertificatePEM = [signingCertificate substringWithRange:innerCertificateRange];
    innerCertificatePEM = [[innerCertificatePEM componentsSeparatedByCharactersInSet:[NSCharacterSet whitespaceAndNewlineCharacterSet]] componentsJoinedByString:@""];
    NSData *innerCertificateDER = [[NSData alloc] initWithBase64EncodedString:innerCertificatePEM options:0];

    BOOL isValid = NO;
    SecCertificateRef cert = SecCertificateCreateWithData(NULL, (CFDataRef)innerCertificateDER);
    SecPolicyRef policy = SecPolicyCreateBasicX509();
    SecTrustRef trust = NULL;
    SecTrustCreateWithCertificates((CFTypeRef)cert, policy, &trust);
    SecTrustResultType resultType;
    SecTrustEvaluate(trust, &resultType);
    SecKeyRef publicKey = SecTrustCopyPublicKey(trust);

    NSMutableString *manifestFileInfoBuilder = [NSMutableString stringWithString:@""];
    for(HCPManifestFile *manifestFile in [manifest files]) {
        [manifestFileInfoBuilder appendString:[manifestFile name]];
        [manifestFileInfoBuilder appendString:[manifestFile md5Hash]];
    }
    NSData *manifestFileInfoBytes = [manifestFileInfoBuilder dataUsingEncoding:NSUTF8StringEncoding];
    
    uint8_t digest[CC_SHA256_DIGEST_LENGTH];
    CC_SHA256([manifestFileInfoBytes bytes], (unsigned int)[manifestFileInfoBytes length], digest);
    
    NSData *signature = [[NSData alloc] initWithBase64EncodedString:_contentSignature options:0];
    OSStatus status = SecKeyRawVerify(publicKey,
                                      kSecPaddingPKCS1SHA256,
                                      digest,
                                      CC_SHA256_DIGEST_LENGTH,
                                      [signature bytes],
                                      [signature length]);
    
    isValid = status == errSecSuccess;
    
    if (trust) CFRelease(trust);
    if (policy) CFRelease(policy);
    if (cert) CFRelease(cert);
    if (publicKey) CFRelease(publicKey);
    return isValid;
}





#pragma mark HCPJsonConvertable implmenetation

- (id)toJson {
    return @{ALGORITHM: _algorithm, CONTENT_SIGNATURE: _contentSignature};
}

+ (instancetype)instanceFromJsonObject:(id)json {
    if (![json isKindOfClass:[NSDictionary class]]) {
        return nil;
    }
    
    NSDictionary *jsonObject = json;
    
    return [[HCPManifestSignature alloc] initWithContentSignature:jsonObject[CONTENT_SIGNATURE] algorithm:jsonObject[ALGORITHM]];
}

@end
