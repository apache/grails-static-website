#import "MasterViewController.h"
#import "DetailViewController.h"
#import "AnnouncementsFetcherDelegate.h"
#import "AnnouncementsFetcher.h"
#import "AnnouncementsTableViewDataSource.h"
#import "AnnouncementsTableViewDelegate.h"
#import "Announcement.h"

static NSString *kSegueShowDetail = @"showDetail";

@interface MasterViewController () <AnnouncementsFetcherDelegate>

@property NSMutableArray *objects;

@property ( nonatomic, strong ) AnnouncementsFetcher *fetcher;

@property ( nonatomic, strong) id<UITableViewDataSource> tableViewDataSource;
@property ( nonatomic, strong) id<UITableViewDelegate> tableViewDelegate;

@end

@implementation MasterViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    
    self.tableViewDataSource = [[AnnouncementsTableViewDataSource alloc] init];
    self.tableViewDelegate = [[AnnouncementsTableViewDelegate alloc] init];
    self.tableView.dataSource = self.tableViewDataSource;
    self.tableView.delegate = self.tableViewDelegate;
}

- (void)viewWillAppear:(BOOL)animated {
    self.clearsSelectionOnViewWillAppear = self.splitViewController.isCollapsed;
    [super viewWillAppear:animated];
    
    [self registerNotifications];
    [self fetchAnnouncements]; // <1>
}

- (void)viewWillDisappear:(BOOL)animated {
    [super viewWillDisappear:animated];
    [self unregisterNotifications];
}

#pragma mark - Notifications

-(void)registerNotifications {
    
    NSNotificationCenter *nc = [NSNotificationCenter defaultCenter];
    [nc addObserver:self selector:@selector(announcementTapped:) name:kAnnouncementTappedNotification object:nil];
}

- (void)unregisterNotifications {
    NSNotificationCenter *nc = [NSNotificationCenter defaultCenter];
    [nc removeObserver:self name:kAnnouncementTappedNotification object:nil];
}

#pragma mark - Segues

- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender {
    if ([[segue identifier] isEqualToString:kSegueShowDetail]) {
        DetailViewController *controller = (DetailViewController *)[[segue destinationViewController] topViewController];
        if([sender isKindOfClass:[Announcement class]]) {
            Announcement *announcement = (Announcement *)sender;
            controller.announcement = announcement;
        }
        controller.navigationItem.leftBarButtonItem = self.splitViewController.displayModeButtonItem;
        controller.navigationItem.leftItemsSupplementBackButton = YES;
    }
}

#pragma mark - Private Methods

- (void)announcementTapped:(NSNotification *)notification {
    if([[notification object] isKindOfClass:[Announcement class]]) {
        Announcement *announcement = (Announcement *)[notification object];
        [self performSegueWithIdentifier:kSegueShowDetail sender:announcement];
    }
}

- (void)setNetworkActivityIndicator:(BOOL)visible {
    [[UIApplication sharedApplication] setNetworkActivityIndicatorVisible:visible];
}

- (void)fetchAnnouncements {
    [self setNetworkActivityIndicator:YES];
    [self.fetcher fetchAnnouncements];
}

#pragma mark - AnnouncementsFetcherDelegate

- (void)announcementsFetchingFailed {
    [self setNetworkActivityIndicator:NO];
}

- (void)announcementsFetched:(NSArray *)announcements {
    [self setNetworkActivityIndicator:NO];
    
    if ( [self.tableViewDataSource isKindOfClass:[AnnouncementsTableViewDataSource class]]) {
        ((AnnouncementsTableViewDataSource *)self.tableViewDataSource).announcements = announcements;
    }
    if ( [self.tableViewDelegate isKindOfClass:[AnnouncementsTableViewDelegate class]]) {
        ((AnnouncementsTableViewDelegate *)self.tableViewDelegate).announcements = announcements;
    }
    [self.tableView reloadData];  // <2>    
}

#pragma mark - Lazy

- (AnnouncementsFetcher *)fetcher {
    if(!_fetcher) {
        _fetcher = [[AnnouncementsFetcher alloc] initWithDelegate:self];
    }
    return _fetcher;
}

@end
