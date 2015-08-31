//
//  NSJSONSerialization+HCPExtension.m
//
//  Created by Nikolay Demyankov on 11.08.15.
//

#import "NSJSONSerialization+HCPExtension.h"
#import "NSError+HCPExtension.h"

@implementation NSJSONSerialization (HCPExtension)

+ (id)JSONObjectWithContentsFromFileURL:(NSURL *)fileURL error:(NSError **)error {
    *error = nil;
    NSData *fileData = [NSData dataWithContentsOfURL:fileURL];
    if (fileData == nil) {
        *error = [NSError errorWithCode:0 description:@"Failed to read data from file"];
        return nil;
    }
    
    return [NSJSONSerialization JSONObjectWithData:fileData options:kNilOptions error:error];
}

+ (id)JSONObjectWithContentsFromString:(NSString *)jsonString error:(NSError **)error {
    *error = nil;
    NSData *jsonData = [jsonString dataUsingEncoding:NSUTF8StringEncoding];
    
    return [NSJSONSerialization JSONObjectWithData:jsonData options:kNilOptions error:error];
}

@end
