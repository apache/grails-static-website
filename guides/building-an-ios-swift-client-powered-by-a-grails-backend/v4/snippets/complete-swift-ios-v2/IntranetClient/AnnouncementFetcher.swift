import Foundation

class AnnouncementFetcher : NSObject, URLSessionDelegate {
    
    weak var delegate: AnnouncementFetcherDelegate?
    let builder = AnnouncementBuilder()
    
    func fetchAnnouncement(_ primaryKey: Int) {
        let url = URL(string: "\(GrailsApi.serverUrl)/\(GrailsApi.Announcements.Path)/\(primaryKey)")!
        let req = NSMutableURLRequest(url:url)
        req.setValue(GrailsApi.version, forHTTPHeaderField: "Accept-Version") // <1>
        let config = URLSessionConfiguration.default
        let session = URLSession(configuration: config, delegate: self, delegateQueue: OperationQueue.main)
        
        let task = session.dataTask(with: req as URLRequest) { (data, response, error) in
            if error != nil {
                if let delegate = self.delegate {
                    delegate.announcementFetchingFailed()
                }
                return
            }
            if let announcement = self.builder.announcementFromJSON(data) {
                if let delegate = self.delegate {
                    delegate.announcementFetched(announcement)
                }
            } else {
                if let delegate = self.delegate {
                    delegate.announcementFetchingFailed()
                }
            }
            
        }
        task.resume()
        
    }
}
