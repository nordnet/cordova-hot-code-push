//
//  HCPLocalDevOptions.h
//  TestIosCHCP
//
//  Created by Nikolay Demyankov on 07.08.15.
//
//

#import <Foundation/Foundation.h>

#import "HCPJsonConvertable.h"

@interface HCPLocalDevOptions : NSObject<HCPJsonConvertable>

- (instancetype)initWithJsCode:(NSArray *)jsCodeForInjection jsScrips:(NSArray *)jsScriptsForInjection;

@property (nonatomic, getter=isEnabled) BOOL enabled;
@property (nonatomic, readonly) NSArray *jsCodeForInjection;
@property (nonatomic, readonly) NSArray *jsScriptsForInjection;

@end
