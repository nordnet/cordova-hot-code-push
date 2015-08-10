//
//  HCPDirectories.h
//  TestIosCHCP
//
//  Created by Nikolay Demyankov on 10.08.15.
//
//

#import <Foundation/Foundation.h>

@interface HCPDirectories : NSObject

@property (nonatomic, strong, readonly) NSURL *contentFolder;
@property (nonatomic, strong, readonly) NSURL *downloadFolder;
@property (nonatomic, strong, readonly) NSURL *backupFolder;
@property (nonatomic, strong, readonly) NSURL *wwwFolder;

@end
