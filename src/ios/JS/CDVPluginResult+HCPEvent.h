//
//  CDVPluginResult+HCPEvent.h
//  TestIosCHCP
//
//  Created by Nikolay Demyankov on 13.08.15.
//
//

#import <Cordova/CDV.h>

@interface CDVPluginResult (HCPEvent)

+ (CDVPluginResult *)pluginResultForNotification:(NSNotification *)notification;

@end
