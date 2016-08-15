//
//  HCPUpdateRequest.h
//
//  Created by Nikolay Demyankov on 24.05.16.
//

#import <Foundation/Foundation.h>

@interface HCPUpdateRequest : NSObject

/**
 *  URL to the application config on the server.
 */
@property (nonatomic, strong) NSURL *configURL;

/**
 *  Current working version of the web content.
 */
@property (nonatomic, strong) NSString *currentWebVersion;

/**
 *  Current native version of the app.
 */
@property (nonatomic) NSUInteger currentNativeVersion;

/**
 *  True if update signature should be checked.
 */
@property (nonatomic) BOOL checkUpdateSigning;

/**
 *  Public key used to check update signatures.
 */
@property (nonatomic, strong) NSString *updateSigningCertificate;



/**
 *  Additional request headers.
 */
@property (nonatomic, strong) NSDictionary *requestHeaders;

@end
