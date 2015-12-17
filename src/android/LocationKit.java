package cordova.locationkit;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.apache.cordova.CordovaWebView;
import org.apache.cordova.LOG;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Locale;

import socialradar.locationkit.ILocationKitBinder;
import socialradar.locationkit.ILocationKitCallback;
import socialradar.locationkit.ILocationKitEventListener;
import socialradar.locationkit.LocationKitService;
import socialradar.locationkit.LocationKitServiceOptions;
import socialradar.locationkit.internal.network.LKNetworkClient;
import socialradar.locationkit.model.LKActivityMode;
import socialradar.locationkit.model.LKPlace;
import socialradar.locationkit.model.LKPowerLevel;
import socialradar.locationkit.model.LKSearchRequest;
import socialradar.locationkit.model.LKUserValues;
import socialradar.locationkit.model.LKVenueFilter;
import socialradar.locationkit.model.LKVisit;
import socialradar.locationkit.model.LKVisitCriteria;

/**
 * This class echoes a string called from JavaScript.
 */
public class LocationKit extends CordovaPlugin {
    private static String LOG_TAG = "LocationKitCordova";
    private boolean bound = false;
    private CallbackContext serviceCallbackContext;
    private ILocationKitBinder service;
    private boolean started = false;
    private Location currentLocation;
    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
//        Log.v(LOG_TAG, "initializing LocationKitPlugin");
        if (!bound) {
            Intent i = new Intent(cordova.getActivity(), LocationKitService.class);
            cordova.getActivity().getApplicationContext().bindService(i, connection, Service.BIND_AUTO_CREATE);
        }
    }

    @Override
    public void onDestroy() {
        try {
            if (bound) {
                cordova.getActivity().getApplicationContext().unbindService(connection);
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, "error unbinding service",e);
        }
//        Log.v(LOG_TAG, "destroy LocationKitPlugin");
        super.onDestroy();
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
//        Log.v(LOG_TAG, String.format(Locale.ENGLISH, "invoked Action %s", action));
        if (action.equals("startWithApiToken")) {
            if (bound && service != null) {
                try {
                    handleStartWithApiToken(args, callbackContext);
                    PluginResult result = new PluginResult(PluginResult.Status.OK, createMessage("start"));
                    result.setKeepCallback(true);
                    callbackContext.sendPluginResult(result);
                } catch (JSONException e) {
                    Log.e(LOG_TAG, "error", e);
                    PluginResult result = new PluginResult(PluginResult.Status.ERROR, "json error");
                    result.setKeepCallback(true);
                    callbackContext.sendPluginResult(result);

                }
            }
            return true;
        } else if (action.equals("getCurrentPlace")) {
            if (bound && service != null) {
                handleGetCurrentPlace(args, callbackContext);
            }
            return true;
        } else if (action.equals("getPlaceForLocation")) {
            handleGetPlaceForLocation(args, callbackContext);
            return true;
        } else if (action.equals("getCurrentLocation")) {
            if (bound && service != null) {
                handleGetCurrentLocation(args, callbackContext);
            }

            return true;
        } else if (action.equals("pause")) {
            callbackContext.sendPluginResult(createSuccessResult());
            return true;
        } else if (action.equals("resume")) {
            callbackContext.sendPluginResult(createSuccessResult());
            return true;
        } else if (action.equals("searchForPlaces")) {
            if (bound && service != null) {
                try {
                    handleSearchForPlaces(args, callbackContext);
                } catch (JSONException e) {
                    Log.e(LOG_TAG, "json error", e);
                    throw e;
                }
            }
            return true;
        } else if (action.equals("updateUserValues")) {
            if (bound && service != null) {
                handleUpdateUserValues(args,callbackContext);
            }
            return true;
        }
        return false;
    }

    private void handleGetPlaceForLocation(JSONArray args, final CallbackContext callbackContext) throws JSONException {
        service.getPlaceForLocation(args.getDouble(0), args.getDouble(1), new ILocationKitCallback<LKPlace>() {
            @Override
            public void onError(Exception e, String s) {
                callbackContext.error(s);
                Log.e(LOG_TAG, s, e);
            }

            @Override
            public void onReceivedData(LKPlace lkPlace) {
                String json = LKNetworkClient.sGson.toJson(lkPlace);
                try {
                    JSONObject object = new JSONObject(json);
                    callbackContext.success(object);
                } catch (JSONException e) {
                    callbackContext.error(e.getMessage());
                    Log.e(LOG_TAG, "json error", e);
                }
            }
        });
    }

    private void handleUpdateUserValues(JSONArray args, CallbackContext callbackContext) throws JSONException {
        LKUserValues values = new LKUserValues();
        JSONObject object = args.getJSONObject(0);
        if (object.has("age") && !object.getString("age").equals("null")) {
            values.setAge(object.getString("age"));
        }
        if (object.has("date_installed")) {
            values.setDateInstalled(object.getLong("date_installed"));
        }
        if (object.has("has_in_app_purchases")) {
            values.setHasInAppPurchases(object.getBoolean("has_in_app_purchases"));
        }
        if (object.has("gender") && !object.getString("gender").equals("null")) {
            values.setGender(object.getString("gender"));
        }
        if (object.has("identifier") && !object.getString("identifier").equals("null")) {
            values.setIdentifier(object.getString("identifier"));
        }
        if (object.has("income") && !object.getString("age").equals("income")) {
            values.setIncome(object.getString("income"));
        }
        if (object.has("marital_status") && !object.getString("marital_status").equals("null")) {
            values.setMaritalStatus(object.getString("marital_status"));
        }
        if (object.has("name") && !object.getString("name").equals("null")) {
            values.setName(object.getString("name"));
        }
        if (object.has("occupation") && !object.getString("occupation").equals("null")) {
            values.setOccupation(object.getString("occupation"));
        }
        service.updateUserValues(values);
        callbackContext.success();
    }

    /**
     * "location" : {"coordinates" : {"latitude": lat, "longitude": lng} },
     *  radius" : radius,
     *  limit" : limit,
     *  category" : category,
     *  query" : query
     *
     * @param args
     * @param callbackContext
     * @throws JSONException
     */
    private void handleSearchForPlaces(JSONArray args, final CallbackContext callbackContext) throws JSONException {
        LKSearchRequest request = new LKSearchRequest();
        JSONObject object = args.getJSONObject(0);
        if (object.has("category")) {
            if (!"null".equals(object.getString("category"))) {
                request.setCategory(object.getString("category"));
                Log.v(LOG_TAG, String.format(Locale.ENGLISH,"category is %s", request.getCategory()));
            } else {
                Log.v(LOG_TAG, "null category");
            }
        }
        if (object.has("limit")) {
            request.setLimit(object.getInt("limit"));
            Log.v(LOG_TAG, String.format(Locale.ENGLISH,"limit is %d", request.getLimit()));
        }
        if (object.has("radius")) {
            request.setRadius(object.getInt("radius"));
            Log.v(LOG_TAG, String.format(Locale.ENGLISH,"radius is %d", request.getRadius()));
        }
        if (object.has("query")) {
            if (!"null".equals(object.getString("query"))) {
                request.setQuery(object.getString("query"));
                Log.v(LOG_TAG, String.format(Locale.ENGLISH, "query is %s", request.getQuery()));
            } else {
                Log.v(LOG_TAG, "null query");
            }
        }
        if (object.has("coordinates")) {
            Location location = new Location("");
            location.setLatitude(object.getJSONObject("coordinates").getDouble("latitude"));
            location.setLongitude(object.getJSONObject("coordinates").getDouble("longitude"));
            request.setLocation(location);
            Log.v(LOG_TAG, "got coordinates");
        } else {
            request.setLocation(currentLocation);
            Log.v(LOG_TAG, "using current location");
        }
        service.searchForPlaces(request, new ILocationKitCallback<List<LKPlace>>() {
            @Override
            public void onError(Exception e, String s) {
                callbackContext.error(String.format("search places failed %s", s));
            }

            @Override
            public void onReceivedData(List<LKPlace> lkPlaces) {
                Log.v(LOG_TAG, String.format(Locale.ENGLISH, "got search for places result %d", lkPlaces.size()));
                String json = LKNetworkClient.sGson.toJson(lkPlaces);
                try {

                    Log.v(LOG_TAG, json);
                    callbackContext.success(new JSONArray(json));
                } catch (JSONException e) {
                    Log.e(LOG_TAG, "json error", e);
                    callbackContext.error("JSON error " + e.getMessage());
                }
            }
        });

    }

    private void handleGetCurrentLocation(JSONArray args, final CallbackContext callbackContext) {
        service.getCurrentLocation(new ILocationKitCallback<Location>() {
            @Override
            public void onError(Exception e, String s) {
                callbackContext.error(s);
            }

            @Override
            public void onReceivedData(Location location) {
                JSONObject object = new JSONObject();
                JSONObject coordinates = new JSONObject();
                try {
                    coordinates.put("latitude", location.getLatitude());
                    coordinates.put("longitude", location.getLongitude());
                    object.put("coords", coordinates);
                    callbackContext.success(object);
                } catch (JSONException e) {
                    callbackContext.error(e.getMessage());
                }
            }
        });
    }

    private void handleGetCurrentPlace(JSONArray args, final CallbackContext callbackContext) {
        service.getCurrentPlace(new ILocationKitCallback<LKPlace>() {
            @Override
            public void onError(Exception e, String s) {
                callbackContext.error(s);
                Log.e(LOG_TAG, s, e);
            }

            @Override
            public void onReceivedData(LKPlace lkPlace) {
               String json = LKNetworkClient.sGson.toJson(lkPlace);
                Log.v(LOG_TAG, json);
                try {
                    JSONObject object = new JSONObject(json);
                    callbackContext.success(object);
                } catch (JSONException e) {
                    callbackContext.error(e.getMessage());
                    Log.e(LOG_TAG, "json error", e);
                }

            }
        });
    }

    private PluginResult createSuccessResult() {
        PluginResult result = new PluginResult(PluginResult.Status.OK);

        return result;
    }

    private JSONObject createMessage(String msgType) throws JSONException {
        JSONObject message = new JSONObject();
        message.put("msg_type", msgType);
        return message;
    }

    private void handleStartWithApiToken(JSONArray args, CallbackContext callbackContext) throws JSONException {
        Log.v(LOG_TAG,"handleStartWithApiToken");
        String apiToken = args.getString(0);
        LocationKitServiceOptions lkServiceOptions = null;
        try {
            if (args.length() > 1 && args.get(1) != null && !args.get(1).equals("null")) {
                JSONObject serviceOptionsJson = args.getJSONObject(1);
                LocationKitServiceOptions.Builder builder = new LocationKitServiceOptions.Builder();
                if (serviceOptionsJson.has("interval")) {
                    builder.withInterval(serviceOptionsJson.getLong("interval"));
                }
                if (serviceOptionsJson.has("power_level")) {
                    builder.withPowerLevel(LKPowerLevel.valueOf(serviceOptionsJson.getString("power_level")));
                }
                if (serviceOptionsJson.has("visit_criteria")) {
                    JSONArray array = serviceOptionsJson.getJSONArray("visit_criteria");
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject object = array.getJSONObject(i);
                        LKVisitCriteria criteria = new LKVisitCriteria();
                        criteria.setAddressId(object.getString("address_id"));
                        criteria.setRadius(object.getDouble("radius"));
                        criteria.setVenueCategory(object.getString("venue_category"));
                        criteria.setVenueName(object.getString("venue_name"));
                        builder.withVisitCriteria(criteria);
                    }
                }
                lkServiceOptions = builder.build();
            }
        } catch (JSONException e) {
//            Log.e(LOG_TAG, "json error can be ignored here", e);
        }
        if (!started) {
            this.serviceCallbackContext = callbackContext;
            service.startWithApiToken(apiToken, lkServiceOptions, listener);
            Log.v(LOG_TAG, "finished call to start with api token");
            started = true;
        }
    }
    /**
     *   if (message != null && message.length() > 0) {
     callbackContext.success(message);
     } else {
     callbackContext.error("Expected one non-empty string argument.");
     }
     */
    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
//            Log.v(LOG_TAG, "service is bound");
            bound = true;
            LocationKit.this.service = (ILocationKitBinder)service;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            bound = false;
        }
    };

    private ILocationKitEventListener listener = new ILocationKitEventListener() {
        @Override
        public void onStartVisit(LKVisit lkVisit) {
//            Log.v(LOG_TAG, "visit started");
            if (serviceCallbackContext != null) {

                try {
                    String dataStr = LKNetworkClient.sGson.toJson(lkVisit);
                    JSONObject object = createMessage("start_visit");
                    JSONObject data = new JSONObject(dataStr);
                    object.put("visit", data);
                    PluginResult result = new PluginResult(PluginResult.Status.OK, object);
                    result.setKeepCallback(true);
                    serviceCallbackContext.sendPluginResult(result);
                } catch (JSONException e) {
                    Log.e(LOG_TAG, "failed",e);
                }
            }
        }

        @Override
        public void onEndVisit(LKVisit lkVisit) {
//            Log.v(LOG_TAG, "visit ended");
            if (serviceCallbackContext != null) {
                try {
                    String dataStr = LKNetworkClient.sGson.toJson(lkVisit);
                    JSONObject object = createMessage("end_visit");
                    JSONObject data = new JSONObject(dataStr);
                    object.put("visit", data);
                    PluginResult result = new PluginResult(PluginResult.Status.OK, object);
                    result.setKeepCallback(true);
                    serviceCallbackContext.sendPluginResult(result);
                } catch (JSONException e) {
                    Log.e(LOG_TAG, "failed",e);
                }
            }
        }

        @Override
        public void onNetworkUnavailable() {
//            Log.v(LOG_TAG, "network available");
        }

        @Override
        public void onNetworkAvailable() {
//            Log.v(LOG_TAG, "network unavailable");
        }

        @Override
        public void onLocationManagerDisabled() {

//            Log.v(LOG_TAG, "locationmanager disabled");
        }

        @Override
        public void onLocationManagerEnabled() {

//            Log.v(LOG_TAG, "locationmanager enabled");
        }

        @Override
        public void onChangedActivityMode(LKActivityMode lkActivityMode) {
//            Log.v(LOG_TAG, "activity changed");
            if (serviceCallbackContext != null) {
//                Log.v(LOG_TAG, "serviceCallbackContext changed");

                try {
                    JSONObject object = createMessage("activity_changed");
                    object.put("activity", lkActivityMode.toString());
                    PluginResult result = new PluginResult(PluginResult.Status.OK, object);
                    result.setKeepCallback(true);
                    serviceCallbackContext.sendPluginResult(result);
                } catch (JSONException e) {
                    Log.e(LOG_TAG, "failed",e);
                }

            }
        }

        @Override
        public void onError(Exception e, String s) {
            Log.e(LOG_TAG, s, e);
            if (serviceCallbackContext != null) {
                PluginResult result = new PluginResult(PluginResult.Status.ERROR, s);
                result.setKeepCallback(true);
                serviceCallbackContext.sendPluginResult(result);
            }
        }

        @Override
        public void onPermissionDenied(String s) {
            Log.e(LOG_TAG, s);
            if (serviceCallbackContext != null) {
                PluginResult result = new PluginResult(PluginResult.Status.ERROR, s);
                result.setKeepCallback(true);
                serviceCallbackContext.sendPluginResult(result);
            }
        }

        @Override
        public void onUnbind() {
            Log.v(LOG_TAG, "UNBIND");
        }

        @Override
        public void onLocationChanged(Location location) {
//            Log.v(LOG_TAG, "Location Changed");
            currentLocation = location;
            if (serviceCallbackContext != null) {

                try {
                    JSONObject object = createMessage("location");
                    JSONObject data = new JSONObject();
                    JSONObject coordinates = new JSONObject();
                    coordinates.put("latitude", location.getLatitude());
                    coordinates.put("longitude", location.getLongitude());
                    data.put("coords", coordinates);
                    data.put("accuracy", location.getAccuracy());
                    data.put("speed", location.getSpeed());
                    data.put("bearing", location.getBearing());
                    data.put("provider", location.getProvider());
                    object.put("position", data);
                    PluginResult result = new PluginResult(PluginResult.Status.OK, object);
                    result.setKeepCallback(true);

                    serviceCallbackContext.sendPluginResult(result);
                } catch (JSONException e) {
                    Log.e(LOG_TAG, "failed",e);
                }


            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
//            Log.v(LOG_TAG, "status changed");
        }

        @Override
        public void onProviderEnabled(String provider) {

//            Log.v(LOG_TAG,"providerEnabled");
        }

        @Override
        public void onProviderDisabled(String provider) {
//            Log.v(LOG_TAG, "providerDisabled");
        }
    };
}
