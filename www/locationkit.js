var exec = require('cordova/exec');

_logMessage = function(message){
    return console.log(message);
}
var LocationKit = {
    startWithApiToken: function(apiToken, serviceOptions, success,error) {
        success = success || _logMessage;
        error = error || _logMessage;
        exec(success, error, "LocationKit", "startWithApiToken", [apiToken, serviceOptions]);
    },

    getCurrentPlace :function(success,error) {
        success = success || _logMessage;
        error = error || _logMessage;
        exec(success, error, "LocationKit", "getCurrentPlace", []);
    },
    getPlaceForLocation : function(lat,lng,success,error) {
        success = success || _logMessage;
        error = error || _logMessage;
        exec(success, error, "LocationKit", "getPlaceForLocation", [lat,lng]);
    },

    getCurrentLocation : function(success,error) {
        success = success || _logMessage;
        error = error || _logMessage;
        exec(success, error, "LocationKit", "getCurrentLocation", []);
    },

    pause : function(success,error) {
        success = success || _logMessage;
        error = error || _logMessage;
        exec(success, error, "LocationKit", "pause", []);
    },

    resume : function(success,error) {
        success = success || _logMessage;
        error = error || _logMessage;
        exec(success, error, "LocationKit", "resume", []);
    },

    searchForPlaces : function(search_request, success,error) {
        success = success || _logMessage;
        error = error || _logMessage;
        exec(success, error, "LocationKit", "searchForPlaces", [search_request]);
    },
    updateUserValues : function(user_values, success,error) {
        success = success || _logMessage;
        error = error || _logMessage;
        exec(success, error, "LocationKit", "updateUserValues", [user_values]);
    },
    makeLKSearchRequest : function(lat, lng, radius, limit, category, query)  {

         if (lat && lng) {
            return  {
                "location" : {"coordinates" : {"latitude": lat, "longitude": lng} },
                "radius" : radius,
                "limit" : limit,
                "category" : category,
                "query" : query
            };
         } else {
             return  {
                 "radius" : radius,
                 "limit" : limit,
                 "category" : category,
                 "query" : query
             };
         }
    },
    makeLocationKitServiceOptions : function(interval, power_level, visit_criteria) {
        return {
            "interval" : interval,
            "power_level" : power_level,
            "visit_criteria":  visit_criteria
        };
    },
    makeVisitCriteria : function(venue_name, venue_category, address_id, radius ) {
        return {
            "venue_name" : venue_name,
            "venue_category" : venue_category,
            "address_id" : address_id,
            "radius" : radius
        };
    }
};

module.exports = LocationKit;

});



