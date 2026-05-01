#import <Foundation/Foundation.h>
@class Announcement;

@protocol AnnouncementFetcherDelegate <NSObject>

- (void)announcementFetchingFailed;

- (void)announcementFetched:(Announcement *)announcement;

@end
