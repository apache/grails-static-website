#import <Foundation/Foundation.h>

@protocol AnnouncementsFetcherDelegate <NSObject>

- (void)announcementsFetchingFailed;

- (void)announcementsFetched:(NSArray *)announcements;


@end
