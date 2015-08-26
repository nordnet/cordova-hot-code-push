//
//  HCPAppUpdateRequestAlertDialog.h
//  TestIosCHCP3
//
//  Created by Nikolay Demyankov on 26.08.15.
//
//

#import <Foundation/Foundation.h>

@interface HCPAppUpdateRequestAlertDialog : NSObject

- (instancetype)initWithMessage:(NSString *)message storeUrl:(NSString *)storeUrl onSuccessBlock:(void (^)())onSuccess onFailureBlock:(void (^)())onFailure;

- (void)show;

@end
