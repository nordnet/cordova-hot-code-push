//
//  HCPDataDownloader.m
//
//  Created by Nikolay Demyankov on 04.11.16.
//

#import "HCPDataDownloader.h"

@implementation HCPDataDownloader

- (void) downloadDataFromUrl:(NSURL*) url requestHeaders:(NSDictionary *)headers completionBlock:(HCPDataDownloadCompletionBlock) block {
    NSURLSessionConfiguration *configuration = [NSURLSessionConfiguration defaultSessionConfiguration];
    configuration.requestCachePolicy = NSURLRequestReloadIgnoringLocalCacheData;
    if (headers) {
        [configuration setHTTPAdditionalHeaders:headers];
    }
    NSURLSession *session = [NSURLSession sessionWithConfiguration:configuration];
    
    NSURLSessionDataTask* dowloadTask = [session dataTaskWithURL:url completionHandler:^(NSData * _Nullable data, NSURLResponse * _Nullable response, NSError * _Nullable error) {
        block(data, error);
    }];
    
    [dowloadTask resume];
}

@end
