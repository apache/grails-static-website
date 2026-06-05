#import <UIKit/UIKit.h>

static NSString *kAnnouncementTappedNotification = @"AnnouncementTappedNotification";

@interface AnnouncementsTableViewDelegate : NSObject <UITableViewDelegate>

@property (nonatomic, strong) NSArray *announcements;

@end
