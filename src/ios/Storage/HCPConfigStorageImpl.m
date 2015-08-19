//
//  HCPConfigStorageImpl.m
//  TestIosCHCP
//
//  Created by Nikolay Demyankov on 12.08.15.
//
//

#import "HCPConfigStorageImpl.h"

@implementation HCPConfigStorageImpl

- (void)store:(id<HCPJsonConvertable>)config inFolder:(NSURL *)folderURL {
    NSURL *fileURL = [self getFullUrlToFileInFolder:folderURL];
    
    id jsonObject = [config toJson];
    NSError *error = nil;
    NSData *data = [NSJSONSerialization dataWithJSONObject:jsonObject options:kNilOptions error:&error];
    if (error != nil) {
        return;
    }
    
    [data writeToURL:fileURL options:kNilOptions error:&error];
    if (error) {
        NSLog(@"%@", [error.userInfo[NSUnderlyingErrorKey] localizedDescription]);
    }
}

- (id<HCPJsonConvertable>)loadFromFolder:(NSURL *)folderURL {
    NSURL *fileURL = [self getFullUrlToFileInFolder:folderURL];
    if (![[NSFileManager defaultManager] fileExistsAtPath:fileURL.path]) {
        return nil;
    }
    
    
    NSData *data = [NSData dataWithContentsOfURL:fileURL];
    NSError *error = nil;
    id json = [NSJSONSerialization JSONObjectWithData:data options:kNilOptions error:&error];
    if (error) {
        return nil;
    }
    
    return [self getInstanceFromJson:json];
}

#pragma mark Methods to Override

// should be overriden by the child class
- (NSURL *)getFullUrlToFileInFolder:(NSURL *)folder {
    return nil;
}

// should be overriden by the child class
- (id<HCPJsonConvertable>)getInstanceFromJson:(id)jsonObject {
    return nil;
}


@end
