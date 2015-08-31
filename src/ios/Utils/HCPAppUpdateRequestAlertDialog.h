//
//  HCPAppUpdateRequestAlertDialog.h
//
//  Created by Nikolay Demyankov on 26.08.15.
//

#import <Foundation/Foundation.h>

/**
 Helper class to construct and show alert dialog to the user with prompt for application update through the App Store.
 */
@interface HCPAppUpdateRequestAlertDialog : NSObject

/**
 *  Initialize dialog
 *
 *  @param message   message to show
 *  @param storeUrl  url to the App Store, where user should be redirected
 *  @param onSuccess block to call when user agrees to go to App Store
 *  @param onFailure block to call when user declines the redirection
 *
 *  @return instance of the object
 */
- (instancetype)initWithMessage:(NSString *)message storeUrl:(NSString *)storeUrl onSuccessBlock:(void (^)())onSuccess onFailureBlock:(void (^)())onFailure;

/**
 *  Show dialog to the user.
 */
- (void)show;

@end
