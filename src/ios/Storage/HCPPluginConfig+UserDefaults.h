//
//  HCPPluginConfig+UserDefaults.h
//  TestIosCHCP
//
//  Created by Nikolay Demyankov on 13.08.15.
//
//

#import "HCPPluginConfig.h"

@interface HCPPluginConfig (UserDefaults)

- (void)saveToUserDefaults;

+ (HCPPluginConfig *)loadFromUserDefaults;

@end
