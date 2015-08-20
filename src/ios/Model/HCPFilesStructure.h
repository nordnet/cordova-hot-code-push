//
//  HCPFilesStructure.h
//  TestIosCHCP
//
//  Created by Nikolay Demyankov on 12.08.15.
//
//

#import <Foundation/Foundation.h>

@protocol HCPFilesStructure <NSObject>

@property (nonatomic, strong, readonly) NSURL *contentFolder;
@property (nonatomic, strong, readonly) NSURL *downloadFolder;
@property (nonatomic, strong, readonly) NSURL *installationFolder;
@property (nonatomic, strong, readonly) NSURL *backupFolder;
@property (nonatomic, strong, readonly) NSURL *wwwFolder;
@property (nonatomic, strong, readonly) NSString *configFileName;
@property (nonatomic, strong, readonly) NSString *manifestFileName;

@end
