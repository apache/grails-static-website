#import "AnnouncementsTableViewDelegate.h"
#import "Announcement.h"

@implementation AnnouncementsTableViewDelegate

#pragma mark - UITableViewDelegate

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
    
    if ( [self.announcements count] > indexPath.row ) {
        id obj = self.announcements[indexPath.row];
        if ( [obj isKindOfClass:[Announcement class]]) {
            Announcement *announcement = (Announcement *)obj;
            [[NSNotificationCenter defaultCenter] postNotificationName:kAnnouncementTappedNotification // <1>
                                                                object:announcement
                                                              userInfo:nil];
        }
    }
}


@end
