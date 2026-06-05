import UIKit

class AnnouncementsTableViewDelegate : NSObject, UITableViewDelegate {

    var announcements = [Announcement]()

    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {        
        let announcement = announcements[indexPath.row]
        NotificationCenter.default.post(name: Notification.Name("AnnouncementTappedNotification"), object: announcement) // <1>
    }
    
}
