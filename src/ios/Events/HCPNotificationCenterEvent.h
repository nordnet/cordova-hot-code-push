//
//  HCPNotificationCenterEvent.h
//  TestIosCHCP
//
//  Created by Nikolay Demyankov on 12.08.15.
//
//

#import <Foundation/Foundation.h>

@protocol HCPNotificationCenterEvent <NSObject>

@property (nonatomic, readonly) NSNotification* notification;

- (instancetype)initWithNotification:(NSNotification *)notification;

+ (id)fromNotification:(NSNotification *)notification;

@end
