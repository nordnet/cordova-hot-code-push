//
//  NSFileManager+HCPExtension.m
//
//  Created by Nikolay Demyankov on 20.08.15.
//

#import "NSFileManager+HCPExtension.h"

@implementation NSFileManager (HCPExtension)

- (NSURL *)applicationSupportDirectory {
    NSError *error = nil;
    NSURL *appSupportDir = [self URLForDirectory:NSApplicationSupportDirectory
                                               inDomain:NSUserDomainMask
                                      appropriateForURL:nil
                                                 create:YES
                                                  error:&error];
    if (error) {
        return nil;
    }
    
    NSString *appBundleID = [[NSBundle mainBundle] bundleIdentifier];
    
    return [appSupportDir URLByAppendingPathComponent:appBundleID isDirectory:YES];
}

- (NSURL *)applicationCacheDirectory {
    NSError *error = nil;
    NSURL *appCacheDirectory = [self URLForDirectory:NSCachesDirectory
                                                   inDomain:NSUserDomainMask
                                          appropriateForURL:nil
                                                     create:YES
                                                      error:&error];
    if (error) {
        return nil;
    }

    NSString *appBundleID = [[NSBundle mainBundle] bundleIdentifier];
    
    return [appCacheDirectory URLByAppendingPathComponent:appBundleID isDirectory:YES];
}


@end
