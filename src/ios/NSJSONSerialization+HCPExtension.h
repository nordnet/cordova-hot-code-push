//
//  NSJSONSerialization+HCPExtension.h
//
//  Created by Nikolay Demyankov on 11.08.15.
//

#import <Foundation/Foundation.h>

/**
 *  Category for NSJSONSerialization class.
 */
@interface NSJSONSerialization (HCPExtension)

/**
 *  Create JSON object with the contents of the given file.
 *
 *  @param fileURL url to the file whose content to use
 *  @param error object is filled with error details if any happened during the convertation
 *
 *  @return JSON object instance
 *
 */
+ (id)JSONObjectWithContentsFromFileURL:(NSURL *)fileURL error:(NSError **)error;

/**
 *  Create JSON object from the string.
 *
 *  @param jsonString JSON formatted string
 *  @param error      object is filled with error details if any happened during the convertation.
 *
 *  @return JSON object instance
 */
+ (id)JSONObjectWithContentsFromString:(NSString *)jsonString error:(NSError **)error;

@end
