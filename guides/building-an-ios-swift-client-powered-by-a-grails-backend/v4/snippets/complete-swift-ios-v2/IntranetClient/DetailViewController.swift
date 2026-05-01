import UIKit

class DetailViewController: UIViewController, UIWebViewDelegate, AnnouncementFetcherDelegate {

    @IBOutlet weak var titleLabel: UILabel!
    @IBOutlet weak var webView: UIWebView!
    @IBOutlet weak var activityIndicatorView: UIActivityIndicatorView!
    
    let fetcher = AnnouncementFetcher()
    
    // MARK: - LifeCycle
    
    override func viewDidLoad() {
        super.viewDidLoad()
        self.fetcher.delegate = self
        self.configureView()
    }
    
    func configureView() {
        // Update the user interface for the detail item.
        if let announcement = self.announcement {
            if let label = self.titleLabel {
                label.text = announcement.title
            }
            if let webView = self.webView, let body = announcement.body {
                self.hideActivityIndicator()
                webView.loadHTMLString(body, baseURL: nil)
            } else {
                self.hideActivityIndicator()
            }
        }
    }
    
    var announcement: Announcement? {
        didSet {
            self.configureView()
        }
    }

    var announcementPrimaryKey: Int? {
        didSet {
            self.showActivityIndicator()
            self.fetcher.fetchAnnouncement(self.announcementPrimaryKey!)
        }
    }
    
    // MARK: - Private Methods
    func showActivityIndicator() {
        if let activityIndicatorView  = self.activityIndicatorView {
            activityIndicatorView.startAnimating()
        }
    }
    
    func hideActivityIndicator() {
        if let activityIndicatorView  = self.activityIndicatorView {
            activityIndicatorView.stopAnimating()
        }
    }
    
    
    // MARK: - UIWebViewDelegate
    
    func webViewDidFinishLoad(_ webView: UIWebView) {
        self.hideActivityIndicator()
    }
    
    func webView(_ webView: UIWebView, didFailLoadWithError error: Error) {
        self.hideActivityIndicator()
    }
    
    // Mark: - AnnouncementFetcherDelegate
    
    func announcementFetchingFailed() {
        
    }
    
    func announcementFetched(_ announcement: Announcement) {
        self.announcement = announcement
    }
}

