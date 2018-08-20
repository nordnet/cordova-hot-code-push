//
//  HCPAppUpdateRequestAlertDialog.m
//
//  Created by Nikolay Demyankov on 26.08.15.
//

#import "HCPAppUpdateRequestAlertDialog.h"

@interface HCPAppUpdateRequestAlertDialog()<UIAlertViewDelegate> {
    NSString *_message;
    NSString *_storeUrl;
    void (^_onSuccess)();
    void (^_onFailure)();
}

@end

@implementation HCPAppUpdateRequestAlertDialog

- (instancetype)initWithMessage:(NSString *)message storeUrl:(NSString *)storeUrl onSuccessBlock:(void (^)())onSuccess onFailureBlock:(void (^)())onFailure {
    self = [super init];
    if (self) {
        _message = message;
        _storeUrl = storeUrl;
        _onSuccess = onSuccess;
        _onFailure = onFailure;
    }
    
    return self;
}

- (void)show {
    NSString *positiveButtonTitle = NSLocalizedString(@"OK", @"");
    NSString *negativeButtontitle = NSLocalizedString(@"Cancel", @"");

    UIAlertView *alertView = [[UIAlertView alloc] initWithTitle:@"" message:_message delegate:self cancelButtonTitle:positiveButtonTitle otherButtonTitles:negativeButtontitle, nil];

    [alertView show];
}

- (void)alertView:(UIAlertView *)alertView clickedButtonAtIndex:(NSInteger)buttonIndex {
    if (buttonIndex == alertView.cancelButtonIndex) {
        _onSuccess();
        [[UIApplication sharedApplication] openURL:[NSURL URLWithString:_storeUrl]];
    } else {
        _onFailure();
    }
}

@end
