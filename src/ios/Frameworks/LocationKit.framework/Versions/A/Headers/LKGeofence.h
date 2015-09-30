//
// Created by SocialRadar on 9/9/15.
// Copyright (c) 2015 SocialRadar. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>
#import "LKVenue.h"
#import "LKVisitCriteria.h"

@class LKAddress;

@interface LKGeofence : NSObject<NSCoding>

@property(nonatomic) CLLocationCoordinate2D center;

@property(nonatomic, strong) LKVenue *venue;

@property(nonatomic, strong) LKAddress *address;
@property(nonatomic, strong) NSDate *enterDate;
@property(nonatomic, strong) NSDate *exitDate;
@property(nonatomic, strong) LKVisitCriteria *criteria;
@end