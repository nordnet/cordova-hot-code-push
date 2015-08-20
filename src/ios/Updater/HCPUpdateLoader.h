//
//  HCPUpdateLoader.h
//  TestIosCHCP
//
//  Created by Nikolay Demyankov on 11.08.15.
//
//

#import <Foundation/Foundation.h>
#import "HCPFilesStructure.h"

@interface HCPUpdateLoader : NSObject

+ (HCPUpdateLoader *)sharedInstance;

- (NSString *)addUpdateTaskToQueueWithConfigUrl:(NSURL *)configUrl;

- (void)setup:(id<HCPFilesStructure>)filesStructure;

@property (nonatomic, readonly, getter=isDownloadInProgress) BOOL isDownloadInProgress;

@end
