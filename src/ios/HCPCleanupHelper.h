//
//  HCPCleanupHelper.h
//
//  Created by Nikolay Demyankov on 23.12.15.
//

#import <Foundation/Foundation.h>

/**
 *  Helper class to cleanup after plugin work.
 *  For now it's main task is to remove content folders from the previous releases.
 */
@interface HCPCleanupHelper : NSObject

/**
 *  Remove older releases from the external storage to free space.
 *
 *  @param ignoredReleases list of releases that should be left; mainly: current and previous one
 */
+ (void)removeUnusedReleasesExcept:(NSArray *)ignoredReleases;

@end
