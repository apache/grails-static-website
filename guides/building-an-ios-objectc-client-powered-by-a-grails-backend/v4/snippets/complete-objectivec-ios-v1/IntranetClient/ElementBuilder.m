#import "ElementBuilder.h"

@implementation ElementBuilder


- (NSArray *)arrayFromJSON:(NSString *)objectNotation
                       key:(NSString *)key
                     error:(NSError **)error
      invalidJSONErrorCode:(NSInteger)invalidJSONErrorCode
      missingDataErrorCode:(NSInteger)missingDataErrorCode
               errorDomain:(NSString *)errorDomain {
    
    id parsedObject = [self parseJSON:objectNotation
                                error:error
                 invalidJSONErrorCode:invalidJSONErrorCode
                          errorDomain:errorDomain];
    
    NSArray *elements = nil;
    if ( [parsedObject isKindOfClass:[NSDictionary class]]) {
        elements = [((NSDictionary *)parsedObject) objectForKey:key];
        
    } else if ( [parsedObject isKindOfClass:[NSArray class]] ) {
        elements = (NSArray *)parsedObject;
    }
    
    if (elements == nil) {
        if (error != NULL) {
            *error = [NSError errorWithDomain:errorDomain code:missingDataErrorCode userInfo:nil];
        }
        return nil;
    }

    NSMutableArray *results = [NSMutableArray arrayWithCapacity:[elements count]];
    for (NSDictionary *parsedEl in elements) {
        id el = [self newElementWithDictionary:parsedEl
                                         error:error
                          invalidJSONErrorCode:invalidJSONErrorCode
                          missingDataErrorCode:missingDataErrorCode
                                   errorDomain:errorDomain];
        // Return nil becuase there has been an error in the previous method call
        if(!el) {
            return nil;
        }
        
        if([self isElementValid:el]) {
            [results addObject:el];
        }
    }
    return [results copy];
}


- (BOOL)isElementValid:(id)el {
    // This may be overriden in a subclass
    return YES;
}

- (id)newElementWithDictionary:(NSDictionary *)dict
                         error:(NSError **)error
          invalidJSONErrorCode:(NSInteger)invalidJSONErrorCode
          missingDataErrorCode:(NSInteger)missingDataErrorCode
                   errorDomain:(NSString *)errorDomain {
    @throw [NSException exceptionWithName:NSInternalInconsistencyException
                                   reason:[NSString stringWithFormat:@"You must override %@ in a subclass", NSStringFromSelector(_cmd)]
                                 userInfo:nil];
}


- (id)parseJSON:(NSString *)objectNotation
          error:(NSError **)error
invalidJSONErrorCode:(NSInteger)invalidJSONErrorCode
    errorDomain:(NSString *)errorDomain  {
    
    NSParameterAssert(objectNotation != nil);
    id jsonObject;
    
    NSError *localError = nil;
    if(objectNotation != nil) {
        NSData *unicodeNotation = [objectNotation dataUsingEncoding:NSUTF8StringEncoding];
        jsonObject = [NSJSONSerialization JSONObjectWithData:unicodeNotation
                                                     options:0
                                                       error:&localError];
    }
    if (jsonObject == nil) {
        if (error != NULL) {
            NSMutableDictionary *userInfo = [NSMutableDictionary dictionaryWithCapacity:1];
            if (localError != nil) {
                [userInfo setObject:localError forKey:NSUnderlyingErrorKey];
            }
            *error = [NSError errorWithDomain:errorDomain code:invalidJSONErrorCode userInfo:userInfo];
        }
        return nil;
    }
    return jsonObject;
}

+ (id)objectOrNull:(id)object {
    if (!object || object == [NSNull null]) return nil;
    return object;
}

+ (BOOL)isNotNilAndNotArrayTheKey:(NSString *)key atDict:(NSDictionary *)dict {
    return [self isNotNilTheKey:key atDict:dict] && ![self isArrayTheKey:key atDict:dict];
}

+ (BOOL)isNotNilAndNotNumberTheKey:(NSString *)key atDict:(NSDictionary *)dict {
    return [self isNotNilTheKey:key atDict:dict] && ![self isNumberTheKey:key atDict:dict];
}

+ (BOOL)isNotNilAndNotStringTheKey:(NSString *)key atDict:(NSDictionary *)dict {
    
    return [self isNotNilTheKey:key atDict:dict] && ![self isStringTheKey:key atDict:dict];
}

+ (BOOL)isNotNilTheKey:(NSString *)key atDict:(NSDictionary *)dict {
    if(dict[key] != (id)[NSNull null]  && dict[key]) {
        return YES;
    }
    return NO;
}

+ (BOOL)isStringTheKey:(NSString *)key atDict:(NSDictionary *)dict {
    if(dict[key] != (id)[NSNull null]  && dict[key] && [dict[key] isKindOfClass:[NSString class]]) {
        return YES;
    }
    return NO;
}

+ (BOOL)isNumberTheKey:(NSString *)key atDict:(NSDictionary *)dict {
    if(dict[key] != (id)[NSNull null]  && dict[key] && [dict[key] isKindOfClass:[NSNumber class]]) {
        return YES;
    }
    return NO;
}
+ (BOOL)isArrayTheKey:(NSString *)key atDict:(NSDictionary *)dict {
    if(dict[key] != (id)[NSNull null]  && dict[key] && [dict[key] isKindOfClass:[NSArray class]]) {
        return YES;
    }
    return NO;
}

@end
