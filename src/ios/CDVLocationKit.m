/* CDVLocationKit.m Cordova plugin implementation  
   LocationKit Cordova Plugin
   c. 2015 SocialRadar Inc
   @author John Fontaine
*/
#import "CDVLocationKit.h"
#import <LocationKit/LocationKit.h>



@interface CDVLocationKit ()
@property BOOL suspended;
@end
@implementation CDVLocationKit
- (void)onReset {

    [super onReset];
}

-(CDVPlugin*)initWithWebView:(UIWebView*)theWebView
{
    self = (CDVLocationKit*)[super initWithWebView:(UIWebView*)theWebView];
    self.locationManager = [[LKLocationManager alloc] init];
    if (self) {
        _suspended = false;
    }


    return self;
}


-(void)startWithApiToken:(CDVInvokedUrlCommand*)command {
    CDVPluginResult* pluginResult = nil;
    NSString *apiToken = [command.arguments objectAtIndex:0];

    if (apiToken != nil) {
        self.locationManager.debug = YES;
        self.locationManager.apiToken = apiToken;
        self.locationManager.advancedDelegate = self;
        [self.locationManager startUpdatingLocation ];
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:@{@"msg_type" : @"start"}];
    } else {
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"Arg was null"];
    }
    [pluginResult setKeepCallbackAsBool:YES];
    _callbackId = command.callbackId;
    _suspended = false;
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(onSuspend) name:UIApplicationDidEnterBackgroundNotification object:nil];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(onResume) name:UIApplicationWillEnterForegroundNotification object:nil];

}

-(BOOL)checkStarted:(CDVInvokedUrlCommand*)command {
    if (self.locationManager.isRunning) {
        return YES;
    }
    CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"LocaitonKit is not running"];

    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    return NO;
}
-(CDVPluginResult *)makeErrorResult:(NSString *)funcName withError:(NSError *)error {
    return [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:[NSString stringWithFormat:@"%@ error: %@", funcName, error ]];
}
-(CDVPluginResult *)makeErrorResult:(NSString *)funcName withMsg:(NSString *)error {
    return [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:[NSString stringWithFormat:@"%@ error: %@", funcName, error ]];
}
-(void)getCurrentPlace:(CDVInvokedUrlCommand*)command {
    if ([self checkStarted:command]) {
        [self.commandDelegate runInBackground:^{
            [self.locationManager requestPlace:^(LKPlacemark * _Nullable place, NSError * _Nullable error) {
                CDVPluginResult *pluginResult;
                if (error) {
                    pluginResult = [self makeErrorResult:@"getCurrentPlace" withError:error];
                }   else {
                    if (place != nil) {
                       pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:[self dictionaryForPlace:place]];
                    } else {
                        pluginResult = [self makeErrorResult:@"getCurrentPlace" withMsg:@"no place found"];
                    }
                }
                [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
            }];
        }];
     }
}
         
         
-(void)getPlaceForLocation:(CDVInvokedUrlCommand*)command {
    if ([self checkStarted:command]) {
        if (command.arguments.count < 2) {
            CDVPluginResult *pluginResult = [self makeErrorResult:@"getPlaceForLocation" withMsg:@"invalid number of arguments must provide lat,lng"];
            [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
            return;
        }
        NSString *strLat = [command argumentAtIndex:0];
        NSString *strLng = [command argumentAtIndex:1];
        CLLocation *location = [[CLLocation alloc] initWithLatitude:[strLat doubleValue] longitude:[strLng doubleValue]];

        [self.commandDelegate runInBackground:^{
            [self.locationManager requestPlaceForLocation:location
                                        completionHandler:^(LKPlacemark * _Nullable place, NSError * _Nullable error) {
                                                CDVPluginResult *pluginResult;
                                                if (error) {
                                                    pluginResult = [self makeErrorResult:@"getPlaceForLocation" withError:error];
                                                }   else {
                                                    if (place != nil) {
                                                        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:[self dictionaryForPlace:place]];
                                                    } else {
                                                        pluginResult = [self makeErrorResult:@"getPlaceForLocation" withMsg:@"no place found"];
                                                    }
                                                }
                                                [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
                                        }];
        }];
    }

}
-(void)getCurrentLocation:(CDVInvokedUrlCommand*)command {
    if ([self checkStarted:command]) {
        [self.commandDelegate runInBackground:^{
            [self.locationManager requestLocation:^(CLLocation * _Nullable location, NSError * _Nullable error) {
                CDVPluginResult *pluginResult;
                if (error) {
                    pluginResult = [self makeErrorResult:@"getPlaceForLocation" withError:error];
                } else {
                    pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:[self dictionaryForLocation:location]];
                }
                [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];

            }];
        }];
    }
}
-(void)searchForPlaces:(CDVInvokedUrlCommand*)command {
    if ([self checkStarted:command]) {

        NSDictionary *search = [command argumentAtIndex:0];
        if (search == nil) {
            CDVPluginResult *pluginResult = [self makeErrorResult:@"searchForPlaces" withMsg:@"must provide search params {} "];
            [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
            return;
        }
        LKSearchRequest *searchRequest =  [self searchRequestFromJSONDict:search];
        [self.commandDelegate runInBackground:^{
            [self.locationManager searchForPlacesWithRequest:searchRequest completionHandler:^(NSArray<LKPlacemark *> * _Nullable places, NSError * _Nullable error) {
                CDVPluginResult *pluginResult;
                if (error) {
                    pluginResult = [self makeErrorResult:@"searchForPlaces" withError:error];
                } else {
                    NSMutableArray *resultsArray = [[NSMutableArray alloc] init];
                    for (LKPlacemark *place in places) {
                        [resultsArray addObject:[self dictionaryForPlace:place]];
                    }
                    pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsArray:resultsArray];
                }
                [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
            }];
        }];
    }
}
-(void)updateUserValues:(CDVInvokedUrlCommand*)command {
    if ([self checkStarted:command]) {
        NSDictionary *userDict = [command argumentAtIndex:0];
        if (userDict == nil ||userDict.allKeys.count == 0) {
            CDVPluginResult *pluginResult = [self makeErrorResult:@"updateUserValues" withMsg:@"must provide user data "];
            [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
            return;
        }
        [self.commandDelegate runInBackground:^{
            [self.locationManager setUserValues:userDict];
            CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
            [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
        }];
    }
}
-(void)pause:(CDVInvokedUrlCommand*)command {
    [self.locationManager stopUpdatingLocation];
    CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];

}
-(void)resume:(CDVInvokedUrlCommand*)command {
    [self.locationManager startUpdatingLocation];
//    NSError *error= [[LocationKit sharedInstance] resume];
    CDVPluginResult *pluginResult;
    pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}
-(void)onSuspend {
    //   _suspended = true;
}
-(void)onResume {
    _suspended = false;
}
#pragma mark -- LKLocationManager Delegate

- (void)locationManager:(LKLocationManager *)manager didUpdateLocations:(NSArray<CLLocation *> *)locations {
    for (CLLocation * location in locations) {
        NSLog(@"Your current location %@", location);
        if (_callbackId != nil && !_suspended) {
            NSDictionary *dictionary = @{
                                         @"msg_type" : @"location",
                                         @"position" : [self dictionaryForLocation:location]
                                         };
            CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:dictionary];
            [pluginResult setKeepCallbackAsBool:YES];
            [self.commandDelegate sendPluginResult:pluginResult callbackId:_callbackId];
            NSLog(@"updateLocation callbackId %@",_callbackId);
        }

    }
}
- (void)locationManager:(LKLocationManager *)manager didStartVisit:(LKVisit *)visit {
 
}
- (void)locationManager:(LKLocationManager *)manager didEndVisit:(LKVisit *)visit {
    if (_callbackId != nil && !_suspended) {
        NSDictionary *dictionary = [self dictionaryForVisit:visit];
        /* CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary::@{@"msg_type" :@"end_visit", @"visit" : dictionary}];
         */
        NSDictionary *d = @{@"msg_type" :@"end_visit", @"visit" : dictionary};
        CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:d];
        [pluginResult setKeepCallbackAsBool:YES];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:_callbackId];
    }
}

- (void)locationManager:(LKLocationManager *)manager willChangeActivityMode:(LKActivityMode)mode {
    
}

#pragma mark -- dictionary helpers

-(LKSearchRequest *)searchRequestFromJSONDict:(NSDictionary *)search {
    LKSearchRequest *request = [[LKSearchRequest  alloc] init];
    if ([search objectForKey:@"location"] != nil) {
        NSDictionary *location = [search objectForKey:@"location"];
        if (location != nil && ![location isKindOfClass:[NSNull class]]) {
            NSDictionary *coordinates = [location objectForKey:@"coordinates"];
            if (coordinates != nil && ![coordinates isKindOfClass:[NSNull class]]) {
                NSNumber *lat = [coordinates objectForKey:@"latitude"];
                NSNumber *lng = [coordinates objectForKey:@"longitude"];
                if (![lat isKindOfClass:[NSNull class]] && ![lng isKindOfClass:[NSNull class]]) {
                    request.location = [[CLLocation alloc] initWithLatitude:[lat doubleValue] longitude:[lng doubleValue]];
                }
            }
        }
    }
    if ([search objectForKey:@"radius"] !=nil ) {
        NSNumber *numRadius = [search objectForKey:@"radius"];
        if (![numRadius isKindOfClass:[NSNull class]]) {
            request.radius  = [numRadius doubleValue];
        }
    }
    if ([search objectForKey:@"limit"] !=nil) {
        NSNumber *numLimit = [search objectForKey:@"limit"];
        if (![numLimit isKindOfClass:[NSNull class]]) {
            request.limit = [numLimit integerValue];
        }
    }
    if ([search objectForKey:@"category"] !=nil) {
        NSString *strCategory = [search objectForKey:@"category"];
        if (![strCategory isKindOfClass:[NSNull class]]) {
            request.category = strCategory;
        }
    }
    if ([search objectForKey:@"query"] !=nil) {
        NSString *strQuery = [search objectForKey:@"query"];
        if (![strQuery isKindOfClass:[NSNull class]]) {
            request.query = strQuery;
        }
    }
    return request;
}

-(NSDictionary *)dictionaryForLocation:(CLLocation*)location {
    double timestamp = [location.timestamp timeIntervalSince1970] * 1000;
    return @{
            @"coords" : @{
                    @"latitude" :  [NSString stringWithFormat:@"%f",location.coordinate.latitude ],
                    @"longitude" : [NSString stringWithFormat:@"%f",location.coordinate.longitude ]
            },
            @"speed" :     [NSString stringWithFormat:@"%.2f",location.speed ],
            @"course" :    [NSString stringWithFormat:@"%.2f",location.course ],
            @"accuracy" :  [NSString stringWithFormat:@"%.2f",location.horizontalAccuracy ],
            @"altitude" :  [NSString stringWithFormat:@"%.2f",location.altitude ],
            @"timestamp" : [NSString stringWithFormat:@"%.0f",timestamp ]
    };
}
-(NSDictionary *)dictionaryForVisit:(LKVisit *)visit {
    NSMutableDictionary *dictionary = [[NSMutableDictionary alloc] init];
    NSString *arrivalDate = [NSString stringWithFormat:@"%.0f",  ([visit.arrivalDate timeIntervalSince1970] * 1000) ];

    [dictionary setObject:arrivalDate forKey:@"arrival_date"];
    if (visit.departureDate != nil) {
        NSString *departureDate = [NSString stringWithFormat:@"%.0f",  ([visit.departureDate timeIntervalSince1970] * 1000) ];
        [dictionary setObject:departureDate forKey:@"departure_date"];
    }
    [dictionary setObject:[self dictionaryForPlace:visit.place] forKey:@"place"];
    return dictionary;
}
-(NSDictionary *)dictionaryForPlace:(LKPlacemark *)place {
    NSMutableDictionary *dictionary = [[NSMutableDictionary alloc] init];
    if (place.venue.venueId.length != 0){
        [dictionary setObject:[self dictionaryForVenue:place.venue] forKey:@"venue"];
    }
    [dictionary setObject:[self dictionaryForAddress:place] forKey:@"address"];

    return dictionary;
}
-(NSDictionary *)dictionaryForVenue:(LKVenue *)venue {
    if (venue == nil) {
        return [[NSNull alloc] init]; //extra safey though this shouldn't be called.
    }
    NSDictionary *dictionary = @{
            @"venue_id" :  [self getSafeString:venue.venueId],
            @"address_id" :  [self getSafeString:venue.addressId],
            @"name" :  [self getSafeString:venue.name],
            @"category" :  [self getSafeString:venue.category],
            @"subcategory" :  [self getSafeString:venue.subcategory]
    };
    return dictionary;
}
-(NSDictionary *)dictionaryForAddress:(LKPlacemark *)address {
    NSDictionary *dictionary = @{
            @"address_id" : [self getSafeString:address.addressId],
            @"street_number" : [self getSafeString:address.subThoroughfare],
            @"street_name" : [self getSafeString:address.thoroughfare],
            @"locality" : [self getSafeString:address.locality],
            @"region" : [self getSafeString:address.region],
            @"postal_code" : [self getSafeString:address.postalCode],
            @"country_code" : [self getSafeString:address.ISOcountryCode],
            @"country" : [self getSafeString:address.country],
            @"coords" : @{
                    @"latitude" :[NSString stringWithFormat:@"%f",address.location.coordinate.latitude ],
                    @"longitude" :[NSString stringWithFormat:@"%f",address.location.coordinate.longitude ]
            }
    };
    return dictionary;
}
-(NSObject *)getSafeString:(NSString *)string {
    if (string == nil) {
        return [[NSNull alloc] init];
    } else {
        return string;
    }

}
@end

