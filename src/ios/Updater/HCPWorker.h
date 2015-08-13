//
//  HCPWorker.h
//  TestIosCHCP
//
//  Created by Nikolay Demyankov on 12.08.15.
//
//

#import <Foundation/Foundation.h>

@protocol HCPWorker <NSObject>

@property (nonatomic, strong, readonly) NSString *workerId;

- (void)run;

@end
