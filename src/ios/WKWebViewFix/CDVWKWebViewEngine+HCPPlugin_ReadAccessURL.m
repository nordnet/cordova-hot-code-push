//
//  CDVWKWebViewEngine+HCPPlugin_ReadAccessURL.m
//
//  Created by Nikolay Demyankov on 04.04.16.
//

#if WK_WEBVIEW_ENGINE_IS_USED

#import "CDVWKWebViewEngine+HCPPlugin_ReadAccessURL.h"
#import <objc/message.h>
#import "HCPFilesStructure.h"

#define CDV_WKWEBVIEW_FILE_URL_LOAD_SELECTOR @"loadFileURL:allowingReadAccessToURL:"

@implementation CDVWKWebViewEngine (HCPPlugin_ReadAccessURL)

- (id)loadRequest:(NSURLRequest*)request
{
    if ([self canLoadRequest:request]) { // can load, differentiate between file urls and other schemes
		CDVViewController* vc = (CDVViewController*)self.viewController;                

		//if its contains localhost, the application use local web server
        if([vc.startPage containsString: @"localhost"]) {
			NSURLComponents *newUrl = [NSURLComponents componentsWithString:vc.startPage];

			//Case: Navigation after update install complete
			if (request.URL.fileURL) {
			    newUrl.path = [@"/local-filesystem" stringByAppendingString: request.URL.path];            

			    return [(WKWebView*)self.engineWebView loadRequest:[NSURLRequest requestWithURL:newUrl.URL]];
			} 
			//Case: load application start page
			else {
				NSURL *wwwPath = [NSURL URLWithString:vc.wwwFolderName];
			    if(wwwPath.fileURL) {
			        newUrl.path = [@"/local-filesystem" stringByAppendingString:[wwwPath.path stringByAppendingString:@"/index.html"] ];

			        return [(WKWebView*)self.engineWebView loadRequest:[NSURLRequest requestWithURL:newUrl.URL]];
			    }
			    else {
			        return [(WKWebView*)self.engineWebView loadRequest:request];
			    }
			}
		}
		//Original way
		else {
			if (request.URL.fileURL) {
			    SEL wk_sel = NSSelectorFromString(CDV_WKWEBVIEW_FILE_URL_LOAD_SELECTOR);
			    
			    // by default we set allowingReadAccessToURL property to the plugin's root folder,
			    // so the WKWebView would load our updates from it.
			    NSURL* readAccessUrl = [HCPFilesStructure pluginRootFolder];
			    
			    // if we are loading index page from the bundle - we need to go up in the folder structure, so the next load from the external storage would work
			    if (![request.URL.absoluteString containsString:readAccessUrl.absoluteString]) {
			        readAccessUrl = [[[request.URL URLByDeletingLastPathComponent] URLByDeletingLastPathComponent] URLByDeletingLastPathComponent];
			    }
			    
			    return ((id (*)(id, SEL, id, id))objc_msgSend)(self.engineWebView, wk_sel, request.URL, readAccessUrl);
			} else {
			    return [(WKWebView*)self.engineWebView loadRequest:request];
			}
		}
    } else { // can't load, print out error
        NSString* errorHtml = [NSString stringWithFormat:
								@"<!doctype html>"
								@"<title>Error</title>"
								@"<div style='font-size:2em'>"
								@"   <p>The WebView engine '%@' is unable to load the request: %@</p>"
								@"   <p>Most likely the cause of the error is that the loading of file urls is not supported in iOS %@.</p>"
								@"</div>",							   
								NSStringFromClass([self class]),
								[request.URL description],
								[[UIDevice currentDevice] systemVersion]
                               ];

        return [self loadHTMLString:errorHtml baseURL:nil];
    }
}

@end

#endif
