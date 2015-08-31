//
//  CDVPluginResult+HCPEvents.h
//
//  Created by Nikolay Demyankov on 13.08.15.
//

#import <Cordova/CDV.h>

/**
 *  Category for CDVPluginResult class.
 *  Helps us to construct plugin specific results that are send back to the JavaScript.
 *
 *  @see HCPEvents
 */
@interface CDVPluginResult (HCPEvents)

/**
 *  Generate instance of the object from the captured notification.
 *
 *  @param notification notification with event information
 *
 *  @return plugin result instance
 */
+ (CDVPluginResult *)pluginResultForNotification:(NSNotification *)notification;

@end
