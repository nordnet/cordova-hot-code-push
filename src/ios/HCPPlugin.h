//
//  HCPPlugin.h
//
//  Created by Nikolay Demyankov on 07.08.15.
//

#import <Cordova/CDVPlugin.h>
#import <Cordova/CDV.h>
#import "HCPFetchUpdateOptions.h"

/**
 *  Plugin main class
 */
@interface HCPPlugin : CDVPlugin

#pragma mark Properties

/**
 *  Fetch update preferences. Used by default if none provided from JS side.
 *  Can be used to controll plugin's workflow from the native side.
 */
@property (nonatomic, strong) HCPFetchUpdateOptions *defaultFetchUpdateOptions;

#pragma mark Methods, invoked from JavaScript

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

/**
 *  Check if new version was loaded and can be installed.
 *
 *  @param command command with which the method is called
 */
- (void)jsIsUpdateAvailableForInstallation:(CDVInvokedUrlCommand *)command;

/**
 *  Get information about app and web versions.
 *
 *  @param command command with which the method is called
 */
- (void)jsGetVersionInfo:(CDVInvokedUrlCommand *)command;

@end
