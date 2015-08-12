//
//  NSJSONSerialization+HCPExtension.m
//  TestIosCHCP
//
//  Created by Nikolay Demyankov on 11.08.15.
//
//

#import "NSJSONSerialization+HCPExtension.h"

@implementation NSJSONSerialization (HCPExtension)

+ (id)JSONObjectWithContentsFromFileURL:(NSURL *)fileURL error:(NSError **)error {
    NSData *fileData = [NSData dataWithContentsOfURL:fileURL];
    if (fileData == nil) {
        *error = [[NSError alloc] initWithDomain:@"Failed to read data from file" code:0 userInfo:nil];
        return nil;
    }
    
    return [NSJSONSerialization JSONObjectWithData:fileData options:kNilOptions error:error];
}

@end
