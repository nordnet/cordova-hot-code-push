//
//  HCPUpdateInstaller.h
//
//  Created by Nikolay Demyankov on 12.08.15.
//

#import <Foundation/Foundation.h>
#import "HCPFilesStructure.h"

/**
 *  Utility class to perform update installation.
 *  Class is a singleton.
 *
 *  @see HCPInstallationWorker
 */
@interface HCPUpdateInstaller : NSObject

/**
 *  Get sharer instance of the object.
 *
 *  @return instance of the object
 */
+ (HCPUpdateInstaller *)sharedInstance;

/**
 *  Flag that indicatse if any update is currently performed.
 *  
 *  @return <code>YES</code> if installation in progress; <code>NO</code> otherwise
 */
@property (nonatomic, readonly, getter=isInstallationInProgress) BOOL isInstallationInProgress;

/**
 *  Launch update installation process.
 *  
 *  @param installVersion version to install
 *  @param currentVersion current version of web content
 *  @param error error details if we failed to launch the installation worker
 *
 *  @return <code>YES</code> if installation is launched; <code>NO</code> - otherwise
 */
- (BOOL)installVersion:(NSString *)newVersion
        currentVersion:(NSString *)currentVersion
                 error:(NSError **)error;

@end
