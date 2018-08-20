//
//  HCPContentConfig.h
//
//  Created by Nikolay Demyankov on 10.08.15.
//

#import <Foundation/Foundation.h>
#import "HCPJsonConvertable.h"

/**
 *  Enum holds list of options, when we should perform the update.
 */
typedef NS_ENUM(NSUInteger, HCPUpdateTime){
    /**
     *  Value is undefined
     */
    HCPUpdateTimeUndefined = 0,
    /**
     *  Update should be performed as soon as possible. For example, when download is finished.
     */
    HCPUpdateNow = 1,
    /**
     *  Update should be performed on application start
     */
    HCPUpdateOnStart = 2,
    /**
     *  Update should be performed when application is resumed
     */
    HCPUpdateOnResume = 3
};

/**
 *  Model for content configuration.
 *  Holds information about current/new release, when to perform the update installation and so on.
 *  Basically, it is a part of the chcp.json file, just moved to separate class for convenience.
 */
@interface HCPContentConfig : NSObject<HCPJsonConvertable>

/**
 *  Getter for the content's version.
 *  Used to determine if the new release is available on the server.
 */
@property (nonatomic, strong, readonly) NSString *releaseVersion;

/**
 *  Getter for minimum required version of the native part.
 *  By this value we will determine if it is possible to install new version of web content
 *  into current version of the app.
 */
@property (nonatomic, readonly) NSInteger minimumNativeVersion;

/**
 *  Getter for url on the server where all content is stored.
 *  All updated/new files are loaded relative to this url.
 */
@property (nonatomic, strong, readonly) NSURL *contentURL;

/**
 *  Getter for the preference, when we should install the update.
 * 
 *  @see HCPUpdateTime
 */
@property (nonatomic, readonly) HCPUpdateTime updateTime;

@end
