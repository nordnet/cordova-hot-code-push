//
//  HCPLocalDevOptions.h
//
//  Created by Nikolay Demyankov on 07.08.15.
//

#import <Foundation/Foundation.h>

/**
 *  Model for local development options.
 *  By "local development" we mean, that you use https://github.com/nordnet/cordova-hot-code-push-cli to start local server.
 *  If so - plugin will try to connect to it via socket and listen for updates in web content.
 *  On every change plugin will trigger update download.
 *
 *  This can help you to speed up development process, so you would not have to re-build application after each change.
 */
@interface HCPLocalDevOptions : NSObject

/**
 *  Flag that indicates if local development is enabled.
 *
 *  @return <code>YES</code> if enabled, <code>NO</code> otherwise.
 */
@property (nonatomic, getter=isEnabled) BOOL enabled;

@end
