#import <Foundation/Foundation.h>
#import "GrailsFetcher.h"
#import "AnnouncementFetcherDelegate.h"

@interface AnnouncementFetcher : GrailsFetcher

- (id)initWithDelegate:(id<AnnouncementFetcherDelegate>)delegate;

- (void)fetchAnnouncement:(NSNumber *)primaryKey;

@end
