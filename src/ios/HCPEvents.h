//
//  HCPEvents.h
//
//  Created by Nikolay Demyankov on 13.08.15.
//

#import <Foundation/Foundation.h>
#import "HCPApplicationConfig.h"

/**
 *  Event is dispatched when some error has happened during the update download.
 */
extern NSString *const kHCPUpdateDownloadErrorEvent;

/**
 *  Event is dispathed when there is nothing new to download from the server. 
 *  Web content is up-to-date.
 */
extern NSString *const kHCPNothingToUpdateEvent;

/**
 *  Event is dispatched when we finished loading the update and ready for installation process.
 */
extern NSString *const kHCPUpdateIsReadyForInstallationEvent;

/**
 *  Event is dispatched when installation is about to begin
 */
extern NSString *const kHCPBeforeInstallEvent;

/**
 *  Event is dispatched we failed to install the update.
 */
extern NSString *const kHCPUpdateInstallationErrorEvent;

/**
 *  Event is dispatched when update is successfully installed.
 */
extern NSString *const kHCPUpdateIsInstalledEvent;

/**
 *  Event is dispatched when there is nothing to install.
 */
extern NSString *const kHCPNothingToInstallEvent;

/**
 *  Event is dispatched right before plugin will start installing application assets on the external storage.
 */
extern NSString *const kHCPBeforeBundleAssetsInstalledOnExternalStorageEvent;

/**
 *  Event is dispatched when we successfully installed web content from bundle onto the external storage.
 */
extern NSString *const kHCPBundleAssetsInstalledOnExternalStorageEvent;

/**
 *  Event is dispatched when error occured during the installation of the web content from the bundle.
 */
extern NSString *const kHCPBundleAssetsInstallationErrorEvent;

/**
 *  Key for error object in the user info dictionary that is attached to the event.
 */
extern NSString *const kHCPEventUserInfoErrorKey;

/**
 *  Key for the worker id object in the user info dictionary that is attached to the event.
 */
extern NSString *const kHCPEventUserInfoTaskIdKey;

/**
 *  Key for the applpication config object in the user info dictionary that is attached to the event.
 */
extern NSString *const kHCPEventUserInfoApplicationConfigKey;

/**
 *  Helper class tor create plugin specific notifications about work process (download or installation).
 */
@interface HCPEvents : NSObject

/**
 *  Create instance of the NSNotification.
 *  Object is then dispatched through the NSNotificationCenter.
 *
 *  @param name      namve of the event
 *  @param appConfig application config that is attached to the event
 *  @param taskId    id of the worker which generated the event
 *  @param error     error that is attached to the event
 *
 *  @return instance of the NSNotification
 */
+ (NSNotification *)notificationWithName:(NSString *)name applicationConfig:(HCPApplicationConfig *)appConfig taskId:(NSString *)taskId error:(NSError *)error;

/**
 *  Create instance of the NSNotification.
 *  Object is then dispatched through the NSNotificationCenter.
 *
 *  @param name      namve of the event
 *  @param appConfig applicationn config that is attached to the event
 *  @param taskId    id of the worker which generated the event
 *
 *  @return instance of the NSNotification
 */
+ (NSNotification *)notificationWithName:(NSString *)name applicationConfig:(HCPApplicationConfig *)appConfig taskId:(NSString *)taskId;

@end
