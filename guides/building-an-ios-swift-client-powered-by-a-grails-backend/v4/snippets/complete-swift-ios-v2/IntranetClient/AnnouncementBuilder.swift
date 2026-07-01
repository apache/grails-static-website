import Foundation

class AnnouncementBuilder {
    
    func announcementsFromJSON(_ data: Data?) -> [Announcement] {
        let json = try? JSONSerialization.jsonObject(with: data!, options: [])
        var announcements = [Announcement]()
        if let array = json as? [Any] {
            for object in array {
                if let dict = object as? Dictionary<String, AnyObject> {
                    if let title = dict["title"] as? String, let primaryKey = dict["id"]  as? Int {
                        let announcement = Announcement(primaryKey: primaryKey, title: title)
                        if let body = dict["body"]  as? String {
                            announcement.body = body
                        }
                        announcements.append(announcement)
                    }
                }
            }
        }
        return announcements
    }
    
    func announcementFromJSON(_ data: Data?) -> Announcement? {
        let json = try? JSONSerialization.jsonObject(with: data!, options: [])
        if let dict = json as? Dictionary<String, AnyObject> {
            if let title = dict["title"] as? String, let primaryKey = dict["id"]  as? Int {
                let announcement = Announcement(primaryKey: primaryKey, title: title)
                if let body = dict["body"]  as? String {
                    announcement.body = body
                }
                return announcement
        
            }
        }
        return nil
    }
}
