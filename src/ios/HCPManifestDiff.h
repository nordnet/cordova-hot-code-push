//
//  HCPManifestDiff.h
//
//  Created by Nikolay Demyankov on 10.08.15.
//

#import <Foundation/Foundation.h>
#import "HCPManifestFile.h"

/**
 *  Model describes difference between two manifest files.
 */
@interface HCPManifestDiff : NSObject

/**
 *  Getter for the combined list of added and changed files.
 *
 *  @return array of HCPManifestFile objects
 *  @see HCPManifestFile
 */
@property (nonatomic, strong, readonly) NSArray *updateFileList;

/**
 *  Check if there is no defference between the manifests.
 */
@property (nonatomic, readonly, getter=isEmpty) BOOL isEmpty;

/**
 *  Getter for the list of new files, that were added to the project.
 *
 *  @return array of HCPManifestFile objects
 *  @see HCPManifestFile
 */
@property (nonatomic, strong, readonly) NSArray *addedFiles;

/**
 *  Getter for the list of existing files that has been changed.
 *
 *  @return array of HCPManifestFile objects
 *  @see HCPManifestFile
 */
@property (nonatomic, strong, readonly) NSArray *changedFiles;

/**
 *  Getter for the list of deleted files.
 * 
 *  @return array of HCPManifestFile objects
 *  @see HCPManifestFile
 */
@property (nonatomic, strong, readonly) NSArray *deletedFiles;

/**
 *  Object initializer
 *
 *  @param addedFiles   list of added files
 *  @param changedFiles list of changed files
 *  @param deletedFiles list of deleted files
 *
 *  @return instance of the object
 *  @see HCPManifestFile
 */
- (instancetype)initWithAddedFiles:(NSArray *)addedFiles changedFiles:(NSArray *)changedFiles deletedFiles:(NSArray *)deletedFiles;

@end
