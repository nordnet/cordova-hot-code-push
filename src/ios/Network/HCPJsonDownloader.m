//
//  HCPJsonDownloader.m
//  TestIosCHCP
//
//  Created by Nikolay Demyankov on 11.08.15.
//
//

#import "HCPJsonDownloader.h"

@implementation HCPJsonDownloader

- (instancetype)initWithUrl:(NSString *)url {
    self = [super init];
    if (self) {
        _url = url;
    }
    
    return self;
}

- (void)downloadWithComplitionBlock:(HCPJsonDownloadComplitionBlock)block {
    if (block == nil) {
        return;
    }
    
    NSURL *requestURL = [NSURL URLWithString:self.url];
    NSURLRequest *request = [NSURLRequest requestWithURL:requestURL];
    
    [NSURLConnection sendAsynchronousRequest:request
                                       queue:[NSOperationQueue currentQueue]
                           completionHandler:^(NSURLResponse *response, NSData *data, NSError *connectionError) {
        if (connectionError) {
            block(connectionError, nil);
            return;
        }
        
        NSError *jsonError = nil;
        id json = [NSJSONSerialization JSONObjectWithData:data options:NSJSONReadingAllowFragments error:&jsonError];
        if (jsonError) {
            block(jsonError, nil);
            return;
        }
        
        block(nil, json);
    }];
}


@end
