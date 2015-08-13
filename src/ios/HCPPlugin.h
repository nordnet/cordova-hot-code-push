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
- (void)initPluginFromJS:(CDVInvokedUrlCommand *)command;
- (void)configure:(CDVInvokedUrlCommand *)command;
- (void)fetchUpdate:(CDVInvokedUrlCommand *)command;
- (void)installUpdate:(CDVInvokedUrlCommand *)command;

@end
