import UIKit

class MasterViewController: UITableViewController, AnnouncementsFetcherDelegate {
    
    var detailViewController: DetailViewController? = nil
    var objects = [Any]()
    
    var tableViewDataSource = AnnouncementsTableViewDataSource()
    var tableViewDelegate = AnnouncementsTableViewDelegate()
    let fetcher = AnnouncementsFetcher()
    
    // MARK: - LifeCycle
    
    override func viewDidLoad() {
        super.viewDidLoad()
        self.tableView.dataSource = self.tableViewDataSource
        self.tableView.delegate = self.tableViewDelegate
        self.fetcher.delegate = self
    }
    
    override func viewWillAppear(_ animated: Bool) {
        self.clearsSelectionOnViewWillAppear = self.splitViewController!.isCollapsed
        super.viewWillAppear(animated)
        self.registerNotifications()
        self.fetchAnnouncements()
    }
    
    override func viewWillDisappear(_ animated: Bool) {
        super.viewWillDisappear(animated)
        self.unregisterNotifications()
    }
    
    // MARK: - Notifications
    
    func registerNotifications() {
        NotificationCenter.default.addObserver(
            self,
            selector: #selector(self.announcementTapped),
            name: Notification.Name("AnnouncementTappedNotification"),
            object: nil)
    }
    
    func unregisterNotifications() {
        NotificationCenter.default.removeObserver(
            self,
            name: Notification.Name("AnnouncementTappedNotification"),
            object: nil)
    }
    
    // MARK: - Private Methods
    
    @objc func announcementTapped(notification: NSNotification){
        
        let announcement = notification.object as! Announcement
        self.performSegue(withIdentifier: "showDetail", sender: announcement)
    }
    
    func fetchAnnouncements() {
        self.setNetworkActivityIndicator(true)
        self.fetcher.fetchAnnouncements()
    }
    
    func setNetworkActivityIndicator(_ visible: Bool) {
        UIApplication.shared.isNetworkActivityIndicatorVisible = visible
    }
    
    // MARK: - Segues
    
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        if segue.identifier == "showDetail" {
            let controller = (segue.destination as! UINavigationController).topViewController as! DetailViewController
            if let announcement = sender as? Announcement {
                controller.announcementPrimaryKey = announcement.primaryKey
            }
            controller.navigationItem.leftBarButtonItem = self.splitViewController?.displayModeButtonItem
            controller.navigationItem.leftItemsSupplementBackButton = true            
        }
    }
    
    // MARK: - AnnouncementsFetcherDelegate
    
    func announcementsFetchingFailed() {
        self.setNetworkActivityIndicator(false)
    }
    
    func announcementsFetched(_ announcements: [Announcement]) {
        self.setNetworkActivityIndicator(false)
        
        self.tableViewDataSource.announcements = announcements
        self.tableViewDelegate.announcements = announcements
        self.tableView.reloadData()
    }
}

