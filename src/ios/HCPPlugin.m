//
//  HCPPlugin.m
//  TestIosCHCP
//
//  Created by Nikolay Demyankov on 07.08.15.
//
//

#import "HCPPlugin.h"
#import "HCPDirectories.h"

@interface HCPPlugin() {
    HCPDirectories *_directories;
}

@end

@implementation HCPPlugin

-(void)pluginInitialize {
    NSLog(@"Doing CHCP plugin initialization");
    
    _directories = [[HCPDirectories alloc] init];
    
    
}

@end
