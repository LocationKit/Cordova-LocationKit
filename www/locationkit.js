var exec = require('cordova/exec');


_logMessage = function(message){
    return console.log(message);
}
var LocationKit = {
    startWithApiToken: function(apiToken, success,error) {
        success = success || _logMessage;
        error = error || _logMessage;
        exec(success, error, "LocationKit", "startWithApiToken", [apiToken]);
    },
    startWithApiTokenAndInterval: function(apiToken, interval, success, error) {
        success = success || _logMessage;
        error = error || _logMessage;
        exec(success, error, "LocationKit", "startWithApiToken", [apiToken, interval]);
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
                 "location" : {"coordinates" : {"latitude": lat, "longitude": lng} },
                 "radius" : radius,
                 "limit" : limit,
                 "category" : category,
                 "query" : query
             };
         }
    }

};

module.exports = LocationKit;
