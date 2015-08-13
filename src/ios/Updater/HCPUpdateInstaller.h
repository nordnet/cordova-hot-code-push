//
//  HCPUpdateInstaller.h
//  TestIosCHCP
//
//  Created by Nikolay Demyankov on 12.08.15.
//
//

#import <Foundation/Foundation.h>
#import "HCPFilesStructure.h"

@interface HCPUpdateInstaller : NSObject

+ (HCPUpdateInstaller *)sharedInstance;

@property (nonatomic, readonly, getter=isInstallationInProgress) BOOL isInstallationInProgress;

- (void)setup:(id<HCPFilesStructure>)filesStructure;

- (BOOL)launchUpdateInstallation:(NSError **)error;

@end
