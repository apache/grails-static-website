protocol AnnouncementsFetcherDelegate: class {
   
    func announcementsFetchingFailed()
    
    func announcementsFetched(_ announcements: [Announcement])
}
