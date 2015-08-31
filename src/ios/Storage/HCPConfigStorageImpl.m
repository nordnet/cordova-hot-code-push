//
//  HCPConfigStorageImpl.m
//
//  Created by Nikolay Demyankov on 12.08.15.
//

#import "HCPConfigStorageImpl.h"

@implementation HCPConfigStorageImpl

- (BOOL)store:(id<HCPJsonConvertable>)config inFolder:(NSURL *)folderURL {
    NSError *error = nil;
    
    id jsonObject = [config toJson];
    NSData *data = [NSJSONSerialization dataWithJSONObject:jsonObject options:kNilOptions error:&error];
    if (error) {
        [self logError:error];
        return NO;
    }

    NSURL *fileURL = [self getFullUrlToFileInFolder:folderURL];
    [data writeToURL:fileURL options:kNilOptions error:&error];
    if (error) {
        [self logError:error];
    }
    
    return (error == nil);
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

#pragma mark Private API

- (void)logError:(NSError *)error {
    NSLog(@"%@", [error.userInfo[NSUnderlyingErrorKey] localizedDescription]);
}

@end
