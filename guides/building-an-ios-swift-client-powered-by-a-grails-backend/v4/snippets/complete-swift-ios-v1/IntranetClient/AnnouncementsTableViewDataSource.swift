import UIKit

class AnnouncementsTableViewDataSource : NSObject, UITableViewDataSource {

    var announcements = [Announcement]()

    
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return announcements.count
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        
        let cell = tableView.dequeueReusableCell(withIdentifier: "Cell", for: indexPath)
        
        let announcement = announcements[indexPath.row]
        cell.textLabel!.text = announcement.title
        return cell
    }
}
