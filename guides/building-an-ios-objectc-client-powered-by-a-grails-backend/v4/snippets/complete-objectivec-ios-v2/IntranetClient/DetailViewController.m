#import "DetailViewController.h"
#import "Announcement.h"
#import "AnnouncementFetcher.h"

@interface DetailViewController () <UIWebViewDelegate, AnnouncementFetcherDelegate>

@property ( nonatomic, weak) IBOutlet UILabel *titleLabel;
@property ( nonatomic, weak) IBOutlet UIWebView *webView;
@property (weak, nonatomic) IBOutlet UIActivityIndicatorView *activityIndicatorView;

@property ( nonatomic, strong ) AnnouncementFetcher *fetcher;

@property (nonatomic, strong) Announcement *announcement;

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
    
    if ( self.announcementPrimaryKey ) {
        [self.fetcher fetchAnnouncement:self.announcementPrimaryKey];
    }
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

#pragma mark - AnnouncementFetcherDelegate

- (void)announcementFetchingFailed {
    [[self activityIndicatorView] stopAnimating];
}

- (void)announcementFetched:(Announcement *)announcement {
    [[self activityIndicatorView] stopAnimating];
    self.announcement = announcement;
}


#pragma mark - Lazy

- (AnnouncementFetcher *)fetcher {
    if(!_fetcher) {
        _fetcher = [[AnnouncementFetcher alloc] initWithDelegate:self];
    }
    return _fetcher;
}

@end
