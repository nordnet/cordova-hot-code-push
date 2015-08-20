//
//  NSFileManager+HCPExtension.h
//  TestIosCHCP
//
//  Created by Nikolay Demyankov on 20.08.15.
//
//

#import <Foundation/Foundation.h>

@interface NSFileManager (HCPExtension)

- (NSURL *)applicationSupportDirectory;
- (NSURL *)applicationCacheDirectory;

@end
