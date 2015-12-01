//
//  HCPWorker.h
//
//  Created by Nikolay Demyankov on 12.08.15.
//

#import <Foundation/Foundation.h>

/**
 *  Protocol describes installation/download worker.
 *  Those worker does all the update logic.
 */
@protocol HCPWorker <NSObject>

/**
 *  String identifier of the created worker.
 *  Since all workers run in the background - they broadcast their results via NSNotificationCenter.
 *  With this identifier we can determine who finished his work.
 */
@property (nonatomic, strong, readonly) NSString *workerId;

/**
 *  Run the worker logic.
 */
- (void)runWithComplitionBlock:(void (^)(void))updateLoaderComplitionBlock;
@end
