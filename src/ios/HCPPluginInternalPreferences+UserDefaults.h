//
//  HCPPluginConfig+UserDefaults.h
//  TestIosCHCP
//
//  Created by Nikolay Demyankov on 13.08.15.
//
//

#import "HCPPluginInternalPreferences.h"

/**
 *  Category for HCPPluginConfig class.
 *  Adds methods to store/restore object to/from UserDefaults.
 *
 *  @see HCPPluginConfig
 */
@interface HCPPluginInternalPreferences (UserDefaults)

/**
 *  Save object to user defaults.
 */
- (void)saveToUserDefaults;

/**
 *  Load object from user defaults.
 *
 *  @return restored from user defaults instance of the object.
 */
+ (HCPPluginInternalPreferences *)loadFromUserDefaults;

@end
