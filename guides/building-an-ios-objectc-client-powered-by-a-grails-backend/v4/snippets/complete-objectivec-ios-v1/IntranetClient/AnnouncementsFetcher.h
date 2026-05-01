#import <Foundation/Foundation.h>
#import "GrailsFetcher.h"

@protocol AnnouncementsFetcherDelegate;

@interface AnnouncementsFetcher : GrailsFetcher

- (id)initWithDelegate:(id<AnnouncementsFetcherDelegate>)delegate;

- (void)fetchAnnouncements;

@end
