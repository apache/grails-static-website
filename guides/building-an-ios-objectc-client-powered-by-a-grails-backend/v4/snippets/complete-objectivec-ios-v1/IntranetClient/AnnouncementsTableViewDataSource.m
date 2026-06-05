#import "AnnouncementsTableViewDataSource.h"
#import "Announcement.h"

@implementation AnnouncementsTableViewDataSource

#pragma mark - UITableViewDataSource

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    return [self.announcements count];
}


- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    UITableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:@"Cell" forIndexPath:indexPath];
    
    if ( [self.announcements count] > indexPath.row ) {
        id obj = self.announcements[indexPath.row];
        if ( [obj isKindOfClass:[Announcement class]]) {
            Announcement *announcement = (Announcement *)obj;
            cell.textLabel.text = announcement.title;
        }
    }
    
    return cell;
}


@end
