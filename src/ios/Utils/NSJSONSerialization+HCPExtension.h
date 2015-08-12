//
//  NSJSONSerialization+HCPExtension.h
//  TestIosCHCP
//
//  Created by Nikolay Demyankov on 11.08.15.
//
//

#import <Foundation/Foundation.h>

@interface NSJSONSerialization (HCPExtension)

+ (id)JSONObjectWithContentsFromFileURL:(NSURL *)fileURL error:(NSError **)error;

@end
