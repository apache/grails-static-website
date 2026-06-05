protocol AnnouncementFetcherDelegate: class {
    
    func announcementFetchingFailed()
    
    func announcementFetched(_ announcement: Announcement)
}
