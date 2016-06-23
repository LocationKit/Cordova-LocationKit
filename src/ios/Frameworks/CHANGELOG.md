## 3.0.1 (December 15, 2015)
Bugfixes

## 3.0.1-beta2 (December 9, 2015)

Misc:
  - improve documentation for Cocoapods.org

## 3.0.0 (December 1, 2015)

After a month in private beta and 3 weeks in public beta, we're happy to announce the final, non-beta release of LocationKit 3.0.0!

We extend a hearty thanks to all the developers who tried our public beta and gave us feedback, your help and support allowed us to ship this final, stable version.

The bullet points are below, but the major change with LocationKit 3.0 is that we now inherit from Apple's CLLocationManager which:

1. Makes migrations from CoreLocation to LocationKit far easier
2. Allows us to offer significant location updates, heading updates, region monitoring (geofences), beacons, and more that CLLocationManager had already. We don't enhance all of these features (yet) but we'll enhance some of them and pass through the rest we don't
3. Means developers using CoreLocation features LocationKit 2.x didn't have will no longer have to maintain 2 different location managers -- now a single LocationKit 3.x location manager will suffice!

Improved:
* Easier migrations from Apple's CoreLocation
* Now a dynamic framework meaning you can enable bitcode and take advantage of app thinning
* Now including all Nullable, Nonnull, flags added to Objective-C meaning Swift autocomplete is now much nicer

Fixed:
* We received feedback on LocationKit 2.x that the location update interval behavior was wonky. We've entirely rewritten it for this release so it's now rock solid

Added:
* Our featureset now includes: significant location updates, heading updates, region monitoring (geofences), beacons, and more
* New and improved venue detection using our new proprietary storefront and entrance database
* Debug logging can now be enabled to log pertinent functionality to the console
* Enhanced Geofences are now much simpler to use

## 2.3.7 (October 16, 2015)
* Fix a bug where people nearby was reporting both the latitude and longitude
  as the latitude of the device

## 2.3.6 (October 14, 2015)
* Bugfixes
    * Fix an issue where some compilation errors could occur referencing a
      library called `z` which we use for compression when installing from
      CocoaPods. Update to the Podspec fixed this
    * Fix a warning that was appearing when building an app with LocationKit
      in it with a build target < iOS 9.0. These warnings looked like:
      "ld: warning: object file (/Users/victor/Development/Positus/Pods/LocationKit/LocationKit.framework/LocationKit(LocationManagerStrategy.o)) was built for newer iOS version (9.0) than being linked (7.1)" and they should now be gone.

## 2.3.5 (October 13, 2015)

* Bugfixes
    * Fix an issue where a file was not correctly included in the build causing
      exceptions like the following to the thrown when LocationKit entered power
      reserve mode: `[LKLocationAnalyzerContext addToPowerReserveTime:mode:endDate:]: unrecognized selector sent to instance 0x7faaa8c3bc40`
    * Fix an issue where, in some circumstances, the request body could get too
      large and cause requests to our backend for resolving a venue to fail

## 2.3.4 (October 7, 2015)

* Now with better `getPeopleNearby`, now returns:
    * The user's name (if you have sent it to us with `updateUserValues`)
    * Custom identifier (also if you have sent it to us with `updateUserValues`)
    * Current latitude/longitude of user
    * Distance to user
    * Bearing (compass direction) to the user
* Now with new API call, `optOut`. This will opt the user out of both
    `getPeopleNearby`, so calling it ensures they don't appear there, but also
    purges all of that user's data from our backend for your location analytics.
* Now with some more analytics (sparingly) sent to our backend so we can detect
    and resolve future issues
* Some new battery failsafes have been added to help ensure LocationKit is not
    draining more battery than it ought to

## 2.3.3 (September 30, 2015)

* Bugfixes
    * Include some public headers accidentally left out of the last build
      including LKVisitCriteria
    * Fix an issue where the version reported by LocationKit would be `nil`
    * Bugfix to help prevent large battery drain while the user is walking
      around in a large building or campus

## 2.3.2 (September 29, 2015)

* Minor bugfixes
    * One of these affected battery life. We had a debug flag set which caused
      the distance filter to have a higher value than it should have while the
      device was at rest. This caused code run in response to a location update
      to be running more than it ought to. This has been resolved, so while at
      rest the battery drain should be far better now.
* Tweaks to some internal constants affecting visit detection accuracy and time

## 2.3.1 (September 25, 2015)

* Fix for building on Xcode 7 without warnings
* Revert back change in 2.3.0 to move to a Dynamic Framework until we can figure
    out why it was misbehaving (We were getting reports of code signing issues
    when developers were installing 2.3.0 on their devices. Until we can figure
    out the solution to those issues, we've rolled back to a Static Library.)
* Some minor bugfixes we rolled up during this week.

## 2.3.0 (September 22, 2015)

* Minor version bump due to a few decent changes:
    * **Update** As of 2.3.1, this dynamic framework change has been rolled
      back pending further review.
    * With the release of iOS 9 and the iOS 8/9 marketshare upwards of 95% we
      have converted LocationKit to a Dynamic Framework. It was previously a
      Static Library. More details on the difference [here.](http://blog.cocoapods.org/Pod-Authors-Guide-to-CocoaPods-Frameworks/)
    * Dynamic Frameworks are not supported in iOS 7.x so we are dropping support
      for that platform.
    * In summary, this is good, meaning bitcode is enabled, LocationKit plays
      nicer with the new app thinning, and the library size (once installed in
      your app) has shrunk significantly.
* Possibly breaking API changes:
    * `applyOperationMode` changed to `setOperationMode`
    * Now requiring visit criteria to be specified in order to trigger visit
      start/end. This is to accommodate our coming improvements, but could
      impact existing applications relying on visit start/end. More info
      forthcoming, in the meantime contact us if you have questions.
      See [Set Visit Criteria](#set-visit-criteria) in our docs for more details
* Bugfixes
    * Fixed a crash that was reported where a LocationKitDelegate existed but
      did not have the `didStartVisit` method implemented
    * The move to a Dynamic Framework fixes hundreds of warnings which were
      appearing in Xcode 7 when building past versions of LocationKit.

## 2.2.6 (September 17, 2015)

* Handful of minor iOS 9 bugfixes to ensure LocationKit is more resilient on
  Apple's newest OS update.

## 2.2.5 (September 15, 2015)

* Lower battery drain for custom operation overrides. Previously, the [custom
  operation modes](https://docs.locationkit.io/#custom-operation-mode) would
  alter the GPS settings, but the compass/accelerometer/altimeter and other
  sensors would follow our internal Auto mode settings. Now in custom operation
  modes (modes other than `LKSettingAuto`, the default) these other sensors are
  disabled resulting in lower battery drain while in those modes.
* Bugfixes
    * Fixed an issue which could cause a stale place to be provided in response
      to the `getCurrentPlaceWithHandler` call.
    * Widen the radius of `getPeopleNearby` to fix a bug where people were
      sometimes not appearing in that call when they should have
    * Fix an issue which likely affected accuracy of venue visits due to lack
      of our internal burst mode firing upon noticing a venue entry.

## 2.2.4 (September 4, 2015)

* Major refactor to the way we handle state changes internally. This means
  LocationKit should now be far more robust with respect to activity mode
  changes and developers overriding the settings with custom operation modes
* Bugfixes
    * Fixed a bug where custom operation modes would sometimes override and
      get the phone stuck in a higher battery drain mode than intended

## 2.2.3 (August 25, 2015)

* Miscellaneous Bugfixes

## 2.2.2 (August 21, 2015)

* Sorry we skipped version `2.2.1`! This was because we accidentally pushed the
  wrong version to CocoaPods and they don't allow you to overwrite without a
  version bump.
* New features
    * Now with the ability to set a venue filter to only receive visit callbacks
      in response to certain categories of venue or name of venues. For example
      this would allow you to tell LocationKit you only want updates whenever
      a user enters a Coffee Shop or a Brewery or a place named
      "Copperwood Tavern" by supplying an `LKFilter` to a new operation option
      called `LKOptionFilter`
* Bugfixes
    * Fixed an issue which resulted in higher than normal battery drain when
      switching out of the default operation mode and then back
    * Fixed an issue which could cause LocationKit to stop updating until
      the device moved a certain distance when it detected a place without an
      address in our proprietary venue database

## 2.2.0 (August 11, 2015)

* Now LocationKit can also run in foreground only mode not requiring permissions
  from the user to always run in the background! More details documented above
  under launching [LocationKit with options](#start-locationkit-with-options)
  Note: this will reduce the usefulness of the location analytics and metrics
  we serve as well as make all background location updates stop. It will also
  diminish accuracy so we don't recommend it if you want most of our
  enhancements. However, it is available for developers who want this
  functionality and many have requested it.
  [Contact us for more details](mailto:support@locationkit.io)
* Minor version bump due to breaking initialization API change.
    * Change to initialization:
        * Was: `[LocationKit startWithApiToken:@"your_token_here" andDelegate:myDelegate]`
        * Now: `[LocationKit startWithApiToken:@"your_token_here" delegate:myDelegate]`
    * More details will come in next round of documentation updates

## 2.1.1 (August 8, 2015)

* Fixed a bug where end visit would sometimes not be reported correctly when a
  user moved far away from a venue as it should, so the end visit handler would
  be delayed until the next visit. Now is correctly called upon leaving a venue

## 2.1.0 (August 7, 2015)

* New minor version due to the addition of significant new features. Please
  update your Podfile to `pod 'LocationKit', '~>2.1.0'` to ensure you get this
  and subsequent releases!
* New Features
    * Now with significantly less battery drain under normal usage. LocationKit
      will now, more than ever, keep battery drain low by monitoring user behavior
      and throttling down to lower battery drain mode when possible without
      sacrificing accuracy when the user is moving. We call this "best for venue
      detection" mode and will drain the smallest amount of battery while
      accurately identifying user visits
    * Addition of new delegate method `willChangeActivityMode` which will be
      called when the user's activity mode will change. This currently has 4
      possible values for which we have constants: `LKActivityModeUnknown`,
      `LKActivityModeStationary`, `LKActivityModeWalking`, and
      `LKActivityModeAutomotive`. These can be used to trigger anything, but is
      ideal paired with our next new feature. This is something we had and used
      internally to LocationKit and are now exposing externally to developers.
    * Addition of `applyOperationMode` which can be used to override the location
      settings. This allows for great customization. Paired with the previous
      feature, this would allow you to say "when driving, set the mode to high
      rather than the default." It can be paired with the activityModes, but can
      also be used on its own however you see fit. We suggest changing back to
      Auto which is our standard mode of operation whenever possible.
    * New readonly property `deviceId` to get the id of this device as referenced
      by LocationKit. Important for the "people nearby" functionality as this
      deviceId is referenced there.
* Note: All of these new features will be better documented shortly!

## 2.0.9 (July 29, 2015)

* Bugfixes
    * Fix an issue causing higher battery drain than expected due to continued
      use of the magnetic compass while the device was at rest
    * Fix a bug where queued network requests generated when the device had no
      network connection would sometimes not be dequeued until app launch
* New Features
    * Now the first location point is computed instantly, causing the first call
      to `getCurrentLocationWithHandler` to return much more quickly
    * New method `getPriorVisits` added to retrieve the user's prior visits, up
      to 100
    * New method `getPeopleAtCurrentVenue` added to retrieve the device ids of
      other users of your app currently in the same venue
    * New method `getPeopleNearby` added to retrieve other users nearby (but not
      necessarily in the same venue)
    * Now with better international support -- the LocationKit venue database
      only covers the US, now seamlessly falling back to Apple reverse geocoding
      if a location is detected outside the US so visit callbacks should work
      fine internationally


## 2.0.8 (July 21, 2015)

* Fix a bug in the 2.0.7 CocoaPod which caused linker errors when trying to
  build a project with LocationKit

## 2.0.7 (July 21, 2015)

* DO NOT INSTALL, CocoaPod error fixed in 2.0.8
* Major rework to the place detection algorithm which should make it perform far
    better, particulacly in urban environments. Now:
    * Using more more sensors on the phone (accelerometer, compass) to refine
      detection of the place the user entered
    * Even quicker place detection, generally resolves a place in under 90
      seconds upon the user's arrival
    * With an even greater accuracy of place detection
    * Using more intelligence server-side to refine place detection including
      past history and machine learning

## 2.0.6 (July 6, 2015

* Moar bugfixes:
    * Fix LKVisit to make it encodable
    * Fix an issue where, under some circumstances, a visit could be started but not properly ended
    * Fix a bug where the average speed calculation could be off if too few samples were present
* Rework the way all network calls are handled in LocationKit:
    * If the network is unavailable at the time of the request, queue it and retry when it is available
    * This may result in the didStartVisit and didEndVisit callbacks being delayed (in the event there is no network available when the visit is detected as starting or ending), but they should be fired with the correct start and end time for the visit
    * Having these handlers delayed is better than the prior behavior of losing a visit start or end if the network was unavailable at the time of visit start
    * This includes network handling of not only visits, but reverse geocoding, search, and all other requests which should also help with momentary network blips

## 2.0.5 (June 26, 2015)

* As always, some bugfixes:
    * Fix the coordinate on the LKAddress to return a proper CLLocationCoordinate2D rather than a dictionary
    * Remove an NSLog statement which accidentally made its way into the prod build
    * Fix a typo in the LocationKit API (`searchForPlacseWithRequest` -> `searchForPlacesWithRequest`)

## 2.0.4 (June 23, 2015)

* More bugfixes
* Addition of `didFailWithError` LocationKitDelegate method so developer can now get errors rather than having it fail silently
* Now place search has the ability to take another location (rather than only using the current location of the device)
* This release includes the following new features:
    * Ability to start LocationKit and receive location updates on a specified time interval with new static method `startWithApiToken:withTimeInterval:andDelegate`
    * Ability to reverse geocode (resolve place from lat/lng) at a place other than the device's current location with `getPlaceForLocation:withHandler`

## 2.0.3 (June 18, 2015)

* Fixes a critical bug in 2.0.2

## 2.0.2 (June 18, 2015)

* Bugfixes
* This release includes the following new features:
    * Ability to search for places nearby
    * Support for sending additional demographic data to server for analytics

## 2.0.1 (June 17, 2015)

* Bugfixes

## 2.0.0 (June 15, 2015)

* After months in private beta, this is the first public release of LocationKit!
* Initial release includes the following features:
    * Single filtered higher accuracy location point request
    * Single place request to find user's current address and venue
    * Streaming, filtered location updates for greater accuracy
    * Streaming visit requests to receive notification when user starts and ends a visit
    * Reporting and analytics sent to LocationKit developer portal
