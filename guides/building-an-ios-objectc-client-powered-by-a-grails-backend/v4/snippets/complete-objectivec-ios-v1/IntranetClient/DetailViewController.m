#import "DetailViewController.h"
#import "Announcement.h"

@interface DetailViewController () <UIWebViewDelegate>

@property ( nonatomic, weak) IBOutlet UILabel *titleLabel;
@property ( nonatomic, weak) IBOutlet UIWebView *webView;
@property (weak, nonatomic) IBOutlet UIActivityIndicatorView *activityIndicatorView;

@end

@implementation DetailViewController

- (void)configureView {
    // Update the user interface for the detail item.
    if (self.announcement) {
        self.titleLabel.text = self.announcement.title;
        [[self activityIndicatorView] startAnimating];
        [self.webView loadHTMLString:self.announcement.body baseURL:nil];
    }
}

- (void)viewDidLoad {
    [super viewDidLoad];
    self.webView.delegate = self;
    // Do any additional setup after loading the view, typically from a nib.
    [self configureView];
}

#pragma mark - Managing the detail item

- (void)setAnnouncement:(Announcement *)announcement {
    if (_announcement != announcement) {
        _announcement = announcement;
        
        // Update the view.
        [self configureView];
    }
}

#pragma mark - UIWebViewDelegate
- (void)webViewDidFinishLoad:(UIWebView *)webView {
    [[self activityIndicatorView] stopAnimating];
}
- (void)webView:(UIWebView *)webView didFailLoadWithError:(NSError *)error {
    [[self activityIndicatorView] stopAnimating];
}

@end
