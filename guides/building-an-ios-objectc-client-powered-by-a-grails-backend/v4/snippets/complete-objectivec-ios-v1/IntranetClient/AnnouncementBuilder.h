#import <Foundation/Foundation.h>

#import "ElementBuilder.h"

extern NSString *kAnnouncementBuilderErrorDomain;

enum {
    kAnnouncementBuilderInvalidJSONError,
    kAnnouncementBuilderMissingDataError,
};

@interface AnnouncementBuilder : ElementBuilder

- (NSArray *)announcementsFromJSON:(NSString *)objectNotation
                             error:(NSError **)error;

@end
