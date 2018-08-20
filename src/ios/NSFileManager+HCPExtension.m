//
//  NSFileManager+HCPExtension.m
//
//  Created by Nikolay Demyankov on 20.08.15.
//

#import "NSFileManager+HCPExtension.h"

@implementation NSFileManager (HCPExtension)

- (NSURL *)applicationSupportDirectory {
    NSError *error = nil;
    /*
        NSURL 정보 가져오는 법
        @see https://developer.apple.com/documentation/foundation/nsfilemanager/1407693-urlfordirectory

        NSApplicationSupportDirectory는 다운로드 컨텐츠등을 위해 데이터를 추가로 보관해야 할 필요가 있을 때 사용하는 폴더
        @see http://verysimple.org/wiki/moin.cgi/iPhone/InternalFolders#A.3CApplication_Home.3E.2BAC8-Library.2BAC8-Application_Support
    */
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
