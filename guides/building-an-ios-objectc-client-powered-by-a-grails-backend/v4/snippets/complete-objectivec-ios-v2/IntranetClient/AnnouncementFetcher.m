#import "AnnouncementFetcher.h"
#import "AnnouncementFetcherDelegate.h"
#import "AnnouncementBuilder.h"

@interface AnnouncementFetcher ()

@property ( nonatomic, weak )id<AnnouncementFetcherDelegate> delegate;
@property ( nonatomic, strong )AnnouncementBuilder *builder;

@end

@implementation AnnouncementFetcher

#pragma mark - LifeCycle

- (id)initWithDelegate:(id<AnnouncementFetcherDelegate>)delegate {
    if(self = [super init]) {
        self.delegate = delegate;
        self.builder = [[AnnouncementBuilder alloc] init];
    }
    return self;
}

#pragma mark - Public Methods

- (void)fetchAnnouncement:(NSNumber *)primaryKey {
    
    [self fetchAnnouncement:primaryKey completionHandler:^(NSData *data, NSURLResponse *response, NSError *error) {
        if(error) {
            if ( self.delegate ) {
                [self.delegate announcementFetchingFailed];
            }
            return;
        }
        
        NSInteger statusCode = [((NSHTTPURLResponse*)response) statusCode];
        if(statusCode != 200) {
            if ( self.delegate ) {
                [self.delegate announcementFetchingFailed];
            }
            return;
        }
        
        NSString *objectNotation = [[NSString alloc] initWithData:data encoding:NSUTF8StringEncoding];
        NSError *builderError;
        Announcement *announcement = [self.builder announcementFromJSON:objectNotation error:&builderError];
        if(builderError) {
            if ( self.delegate ) {
                [self.delegate announcementFetchingFailed];
            }
            return;
        }
        if ( self.delegate ) {
            [self.delegate announcementFetched:announcement];
        }
    }];
    
}

#pragma mark - Private Methods

- (void)fetchAnnouncement:(NSNumber *)primaryKey completionHandler:(void (^)(NSData * data, NSURLResponse * response, NSError * error))completionHandler {
    NSURLSessionDataTask *dataTask = [self.session dataTaskWithRequest:[self announcementURLRequest:primaryKey] completionHandler:completionHandler];
    [dataTask resume];
}

- (NSURLRequest *)announcementURLRequest:(NSNumber *)primaryKey {
    NSString *urlStr = [self announcementURLString:primaryKey];
    return [super getURLRequestWithUrlString:urlStr
                                 cachePolicy:NSURLRequestReloadIgnoringLocalCacheData
                             timeoutInterval:FAST_TIME_INTERVAL];
}

- (NSString *)announcementURLString:(NSNumber *)primaryKey {
    return [NSString stringWithFormat:@"%@/%@/%@", kServerUrl, kAnnouncementsResourcePath, primaryKey];
}

@end

