#import "GrailsFetcher.h"

@interface GrailsFetcher () <NSURLSessionDelegate>

@end

@implementation GrailsFetcher

#pragma mark - LifeCycle

- (id)init {
    if(self = [super init]) {
        NSURLSessionConfiguration* configuration = [NSURLSessionConfiguration defaultSessionConfiguration];
        self.session = [NSURLSession sessionWithConfiguration:configuration
                                                     delegate:self
                                                delegateQueue:[NSOperationQueue mainQueue]];
    }
    return self;
}


#pragma mark - Public


- (NSURLRequest *)getURLRequestWithUrlString:(NSString *)urlString
                                 cachePolicy:(NSURLRequestCachePolicy)cachePolicy
                             timeoutInterval:(NSTimeInterval)timeoutInterval {
    NSURL *url = [NSURL URLWithString:urlString];
    NSMutableURLRequest *urlRequest = [NSMutableURLRequest requestWithURL:url
                                                              cachePolicy:NSURLRequestReloadIgnoringLocalCacheData
                                                          timeoutInterval:FAST_TIME_INTERVAL];
    [urlRequest setHTTPMethod:@"GET"];
    
    [[self headers] enumerateKeysAndObjectsUsingBlock:^(id key, id obj, BOOL * stop) {
        [urlRequest setValue:obj forHTTPHeaderField:key];
    }];
    return urlRequest;
}

#pragma mark - Private

- (NSDictionary *)headers {
    return @{@"Accept-Version": kApiVersion}; // <1>
}

@end
