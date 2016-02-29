//
//  HCPPlugin.h
//
//  Created by Nikolay Demyankov on 07.08.15.
//

#import <Cordova/CDVPlugin.h>
#import <Cordova/CDV.h>

#ifndef HCP_CORDOVA_VERSION
#define HCP_CORDOVA_VERSION 4
#endif

/**
 *  Plugin main class
 */
@interface HCPPlugin : CDVPlugin

// methods, invoked from JavaScript

/**
 *  Initialize application with callback from web side.
 *
 *  @param command command with which the method is called
 */
- (void)jsInitPlugin:(CDVInvokedUrlCommand *)command;

/**
 *  Set plugin options.
 *
 *  @param command command with which the method is called
 */
- (void)jsConfigure:(CDVInvokedUrlCommand *)command;

/**
 *  Perform update availability check.
 *  Basically, queue update task.
 *
 *  @param command command with which the method is called
 */
- (void)jsFetchUpdate:(CDVInvokedUrlCommand *)command;

/**
 *  Install update if any available.
 *
 *  @param command command with which the method is called
 */
- (void)jsInstallUpdate:(CDVInvokedUrlCommand *)command;

/**
 *  Show dialog with request to update the application through the App Store.
 *
 *  @param command command with which the method is called
 */
- (void)jsRequestAppUpdate:(CDVInvokedUrlCommand *)command;

@end
