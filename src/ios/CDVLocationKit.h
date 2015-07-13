/*
 LocationKit Cordova Plugin Implementation 
 c. 2015 SocialRadar Inc All Rights Reserved
 @author John Fontaine
 
*/
#import <LocationKit/LocationKit.h>
#import <Cordova/CDVPlugin.h>

@interface CDVLocationKit : CDVPlugin <LocationKitDelegate>
@property (nonatomic, strong) LocationKit *kit;
@property (copy) NSString *callbackId;
-(void)startWithApiToken:(CDVInvokedUrlCommand*)command;
-(void)startWithApiTokenAndInterval:(CDVInvokedUrlCommand*)command;
-(void)getCurrentPlace:(CDVInvokedUrlCommand*)command;
-(void)getPlaceForLocation:(CDVInvokedUrlCommand*)command;
-(void)getCurrentLocation:(CDVInvokedUrlCommand*)command;
-(void)searchForPlaces:(CDVInvokedUrlCommand*)command;
-(void)updateUserValues:(CDVInvokedUrlCommand*)command;
-(void)pause:(CDVInvokedUrlCommand*)command;
-(void)resume:(CDVInvokedUrlCommand*)command;
@end
