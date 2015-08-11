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
    
    NSString *path = [[NSBundle mainBundle] pathForResource:@"index" ofType:@"html" inDirectory:@"www"];
    
    NSLog(@"path is: %@", path);
}

@end
