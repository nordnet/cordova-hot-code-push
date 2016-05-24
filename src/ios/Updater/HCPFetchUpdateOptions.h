//
//  HCPFetchUpdateOptions.h
//
//  Created by Nikolay Demyankov on 24.05.16.
//

#import <Foundation/Foundation.h>

/**
 *  Model for fetch update options.
 */
@interface HCPFetchUpdateOptions : NSObject

/**
 *  URL to the config file (chcp.json).
 */
@property (nonatomic, strong) NSURL *configFileURL;

/**
 *  Additional request headers.
 */
@property (nonatomic, strong) NSDictionary<NSString *, NSString *> *requestHeaders;

/**
 *  Constructor.
 *  Used internally in the plugin.
 *
 *  @param dictionary dictionary with options from the JS side
 *
 *  @return object instance
 */
- (instancetype)initWithDictionary:(NSDictionary *)dictionary;

@end
