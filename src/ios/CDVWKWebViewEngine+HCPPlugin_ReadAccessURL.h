//
//  CDVWKWebViewEngine+HCPPlugin_ReadAccessURL.h
//
//  Created by Nikolay Demyankov on 04.04.16.
//

#if WK_WEBVIEW_ENGINE_IS_USED

#import "CDVWKWebViewEngine.h"

@interface CDVWKWebViewEngine (HCPPlugin_ReadAccessURL)

- (id)loadRequest:(NSURLRequest*)request;

@end

#endif