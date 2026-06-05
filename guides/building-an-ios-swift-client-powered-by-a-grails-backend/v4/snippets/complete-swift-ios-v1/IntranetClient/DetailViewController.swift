import UIKit

class DetailViewController: UIViewController, UIWebViewDelegate {

    @IBOutlet weak var titleLabel: UILabel!
    @IBOutlet weak var webView: UIWebView!
    @IBOutlet weak var activityIndicatorView: UIActivityIndicatorView!
    
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
        } else {
            self.hideActivityIndicator()
        }
    }
    
    func hideActivityIndicator() {
        if let activityIndicatorView  = self.activityIndicatorView {
            activityIndicatorView.stopAnimating()
        }
    }

    override func viewDidLoad() {
        super.viewDidLoad()
        // Do any additional setup after loading the view, typically from a nib.
        self.configureView()
    }

    var announcement: Announcement? {
        didSet {
            // Update the view.
            self.configureView()
        }
    }
    
    
    // MARK: - UIWebViewDelegate
    
    func webViewDidFinishLoad(_ webView: UIWebView) {
        self.hideActivityIndicator()
    }
    
    func webView(_ webView: UIWebView, didFailLoadWithError error: Error) {
        self.hideActivityIndicator()
    }
}

