//
//  HCPUpdateDownloadError.h
//  TestIosCHCP
//
//  Created by Nikolay Demyankov on 12.08.15.
//
//

#import <Foundation/Foundation.h>
#import "HCPUpdateProgressEvent.h"

extern NSString *const kHCPUpdateDownloadErrorEventName;
extern NSString *const kHCPUpdateDownloadErrorUserInfoKey;

@interface HCPUpdateDownloadErrorEvent : HCPUpdateProgressEvent

@property (nonatomic, strong, readonly) NSError *error;

- (instancetype)initWithError:(NSError *)error taskId:(NSString *)taskId applicationConfig:(HCPApplicationConfig *)config;

@end
