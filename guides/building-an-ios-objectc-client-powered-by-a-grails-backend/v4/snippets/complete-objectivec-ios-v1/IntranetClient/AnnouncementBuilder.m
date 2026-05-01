#import "AnnouncementBuilder.h"
#import "Announcement.h"

static NSString *kJSONKeyId = @"id";
static NSString *kJSONKeyTitle = @"title";
static NSString *kJSONKeyBody = @"body";

@implementation AnnouncementBuilder

- (NSArray *)announcementsFromJSON:(NSString *)objectNotation
                             error:(NSError **)error {
    
    return [super arrayFromJSON:objectNotation
                            key:nil
                          error:error
           invalidJSONErrorCode:kAnnouncementBuilderInvalidJSONError
          missingDataErrorCode:kAnnouncementBuilderMissingDataError
                   errorDomain:kAnnouncementBuilderErrorDomain];
}

- (id)newElementWithDictionary:(NSDictionary *)dict
                         error:(NSError **)error
          invalidJSONErrorCode:(NSInteger)invalidJSONErrorCode
          missingDataErrorCode:(NSInteger)missingDataErrorCode
                   errorDomain:(NSString *)errorDomain {
    
    Announcement *announcement = [[Announcement alloc] init];
    
    if([[dict objectForKey:kJSONKeyId] isKindOfClass:[NSNumber class]]) {
        announcement.primaryKey = (NSNumber *)[dict objectForKey:kJSONKeyId];
    } else {
        if(error != NULL) {
            *error = [self invalidJsonError];
        }
        return nil;
    }
    
    if([[dict objectForKey:kJSONKeyTitle] isKindOfClass:[NSString class]]) {
        announcement.title = (NSString *)[dict objectForKey:kJSONKeyTitle];
    } else {
        if(error != NULL) {
            *error = [self invalidJsonError];
        }
        return nil;
    }
    
    if([[dict objectForKey:kJSONKeyBody] isKindOfClass:[NSString class]]) {
        announcement.body = (NSString *)[dict objectForKey:kJSONKeyBody];
    }
    
    return announcement;
    
}

- ( NSError *)invalidJsonError {
    NSMutableDictionary *userInfo = [NSMutableDictionary dictionaryWithCapacity:1];
    return [NSError errorWithDomain:kAnnouncementBuilderErrorDomain
                               code:kAnnouncementBuilderInvalidJSONError
                           userInfo:userInfo];
}

@end

NSString *kAnnouncementBuilderErrorDomain = @"kAnnouncementBuilderErrorDomain";
