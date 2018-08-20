//
//  HCPJsonConvertable.h
//
//  Created by Nikolay Demyankov on 07.08.15.
//

#import <Foundation/Foundation.h>

/**
 *  Protocol describes objects that can be converted to/from JSON.
 */
@protocol HCPJsonConvertable <NSObject>

/**
 *  Convert this object instnace into JSON object
 *
 *  @return JSON object
 */
- (id)toJson;

/**
 *  Create instance of the object from the JSON object
 *
 *  @param json JSON object to convert from
 *
 *  @return created instance
 */
+ (instancetype)instanceFromJsonObject:(id)json;

@end
