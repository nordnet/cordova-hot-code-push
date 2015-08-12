//
//  HCPJsonDownloader.m
//  TestIosCHCP
//
//  Created by Nikolay Demyankov on 11.08.15.
//
//

#import "HCPJsonDownloader.h"

@implementation HCPJsonDownloader

- (instancetype)initWithUrl:(NSURL *)url {
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

    NSURLRequest *request = [NSURLRequest requestWithURL:self.url];
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

- (id)downloadSync:(NSError **)error {
    *error = nil;
    NSData *data = [NSData dataWithContentsOfURL:self.url];
    if (data == nil) {
        *error = [NSError errorWithDomain:@"Failed to download config file from the given url" code:0 userInfo:nil];
        return nil;
    }
    
    return [NSJSONSerialization JSONObjectWithData:data options:kNilOptions error:error];
}

@end
