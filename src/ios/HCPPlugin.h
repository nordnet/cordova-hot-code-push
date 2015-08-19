//
//  HCPPlugin.h
//  TestIosCHCP
//
//  Created by Nikolay Demyankov on 07.08.15.
//
//

#import <Cordova/CDVPlugin.h>
#import <Cordova/CDV.h>

@interface HCPPlugin : CDVPlugin

// methods, invoked from JavaScript 
- (void)jsInitPlugin:(CDVInvokedUrlCommand *)command;
- (void)jsConfigure:(CDVInvokedUrlCommand *)command;
- (void)jsFetchUpdate:(CDVInvokedUrlCommand *)command;
- (void)jsInstallUpdate:(CDVInvokedUrlCommand *)command;

@end
