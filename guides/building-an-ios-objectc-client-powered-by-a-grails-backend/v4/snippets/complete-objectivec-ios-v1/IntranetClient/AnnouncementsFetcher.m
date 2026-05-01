#import "AnnouncementsFetcher.h"
#import "AnnouncementsFetcherDelegate.h"
#import "AnnouncementBuilder.h"


@interface AnnouncementsFetcher ()

@property ( nonatomic, weak) id<AnnouncementsFetcherDelegate> delegate;

@property ( nonatomic, strong) AnnouncementBuilder *builder;

@end

@implementation AnnouncementsFetcher

- (id)initWithDelegate:(id<AnnouncementsFetcherDelegate>)delegate {
    if(self = [super init]) {
        self.delegate = delegate;
        self.builder = [[AnnouncementBuilder alloc] init];
    }
    return self;
}

- (void)fetchAnnouncements {
    
    [self fetchAnnouncementsWithCompletionHandler:^(NSData *data, NSURLResponse *response, NSError *error) {
        if(error) {
            if ( self.delegate ) {
                [self.delegate announcementsFetchingFailed];
            }
            return;
        }
        
        NSInteger statusCode = [((NSHTTPURLResponse*)response) statusCode];
        if(statusCode != 200) {
            if ( self.delegate ) {
                [self.delegate announcementsFetchingFailed];
            }
            return;
        }
        
        NSString *objectNotation = [[NSString alloc] initWithData:data encoding:NSUTF8StringEncoding];
        NSError *builderError;
        NSArray *announcements = [self.builder announcementsFromJSON:objectNotation error:&builderError];
        if(builderError) {
            if ( self.delegate ) {
                [self.delegate announcementsFetchingFailed];
            }
            return;
        }
        if ( self.delegate ) {
            [self.delegate announcementsFetched:announcements];
        }
        
    }];
}

- (void)fetchAnnouncementsWithCompletionHandler:(void (^)(NSData * data, NSURLResponse * response, NSError * error))completionHandler {
    NSURLSessionDataTask *dataTask = [self.session dataTaskWithRequest:[self announcementsURLRequest] completionHandler:completionHandler];
    [dataTask resume];
}

- (NSURLRequest *)announcementsURLRequest {
    NSString *urlStr = [self announcementsURLString];
    return [super getURLRequestWithUrlString:urlStr
                                 cachePolicy:NSURLRequestReloadIgnoringLocalCacheData
                             timeoutInterval:FAST_TIME_INTERVAL];
}

- (NSString *)announcementsURLString {
    return [NSString stringWithFormat:@"%@/%@", kServerUrl, kAnnouncementsResourcePath];
}

@end
