//
//  NSDictionary+HCPFetchUpdateOptions.h
//

#import <Foundation/Foundation.h>

@interface NSDictionary (HCPFetchUpdateOptions)

- (NSURL *)configURL;

- (NSDictionary<NSString *, NSString *> *)requestHeaders;

@end
