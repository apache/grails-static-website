import Foundation

class AnnouncementsFetcher : NSObject, URLSessionDelegate {
    
    weak var delegate:AnnouncementsFetcherDelegate?
    let builder = AnnouncementBuilder()
    
    func fetchAnnouncements() {
        let url = URL(string: "\(GrailsApi.serverUrl)/\(GrailsApi.Announcements.Path)")!
        let req = NSMutableURLRequest(url:url)
        req.setValue(GrailsApi.version, forHTTPHeaderField: "Accept-Version") // <1>
        let config = URLSessionConfiguration.default
        let session = URLSession(configuration: config, delegate: self, delegateQueue: OperationQueue.main)
        
        let task = session.dataTask(with: req as URLRequest) { (data, response, error) in
            if error != nil {
                if let delegate = self.delegate {
                    delegate.announcementsFetchingFailed()
                }
                return
            }
            let announcements = self.builder.announcementsFromJSON(data)
            if let delegate = self.delegate {
                delegate.announcementsFetched(announcements)
            }
            
        }
        task.resume()
        
    }
}
