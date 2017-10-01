package com.comp30022.arrrrr.services;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RestrictTo;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.comp30022.arrrrr.MainActivity;
import com.comp30022.arrrrr.MainViewActivity;
import com.comp30022.arrrrr.MapContainerFragment;
import com.comp30022.arrrrr.R;
import com.comp30022.arrrrr.models.GeoLocationInfo;
import com.comp30022.arrrrr.receivers.SelfPositionReceiver;
import com.comp30022.arrrrr.receivers.GeoQueryLocationsReceiver;
import com.comp30022.arrrrr.receivers.SingleUserLocationReceiver;
import com.comp30022.arrrrr.utils.GeoUtil;
import com.comp30022.arrrrr.utils.TimeUtil;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.firebase.geofire.core.GeoHash;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A backend location sharing service:
 * - Runs in the background.
 * - Constantly listen for location updates from other users from the server
 * -    when there is a update, it will be broadcasted to the receivers.
 * - Constantly listen for self location change and,
 * -    when there is a change, it will send new self location to the server.
 *
 * @author Dafu Ai
 */

public class LocationSharingService extends Service implements
        GeoQueryEventListener,
        SelfPositionReceiver.SelfLocationListener {

    /**
     * Type of database reference for the user location records.
     */
    private enum RefType {
        GEO_LOCATION, GEO_INFO
    }

    @RestrictTo(RestrictTo.Scope.TESTS)
    private final IBinder mBinder = new LocationSharingBinder();

    private final static String DB_REF_GEO_INFO = "userlocations_info";
    private final static String DB_REF_GEO = "userlocations_geo";
    private final static String INFO_LABEL_TIME = "time";

    public static final String PARAM_IN_REQUEST_TYPE = "IN_REQUEST_TYPE";
    public static final String REQUEST_ADD_LISTENER = "REQUEST_ADD_LISTENER";
    public static final String REQUEST_REMOVE_LISTENER = "REQUEST_REMOVE_LISTENER";
    public static final String PARAM_IN_REFER_KEY = "IN_REFER_KEY";

    public static final String PARAM_OUT_REFER_EVENT = "OUT_REFER_TO_EVENT";
    public static final String PARAM_OUT_REFER_KEY = "OUT_REFER_TO_KEY";
    public static final String PARAM_OUT_LOCATIONS = "OUT_LOCATIONS";
    public static final String PARAM_OUT_LOCATION_INFOS = "OUT_LOCATION_INFOS";

    public static final String PARAM_OUT_UID = "OUT_UID";
    public static final String PARAM_OUT_DISTANCE = "OUT_DISTANCE";
    public static final String PARAM_OUT_TIME = "OUT_TIME";

    // Indicates the type of GeoQueryEvent for broadcasting purposes.
    public static final String ON_KEY_ENTERED = "ON_KEY_ENTERED";
    public static final String ON_KEY_EXITED = "ON_KEY_EXITED";
    public static final String ON_KEY_MOVED = "ON_KEY_MOVED";

    /**
     * TODO: Consider change radius to dynamic
     */
    private final static Double GEO_QUERY_RADIUS = 2.0;

    private final String TAG = LocationSharingService.class.getSimpleName();

    private SelfPositionReceiver mSelfPositionReceiver;
    private DatabaseReference mRootRef;
    private GeoFire mGeoFire;

    /**
     * Current location of user's device.
     */
    private Location mSelfLocation;

    /**
     * GeoFire location query. Will only start when the device location has been detected.
     */
    private GeoQuery mGeoQuery;

    /**
     * Contains all current geo locations of other users.
     * We use LatLng instead since it is parcelable.
     */
    private HashMap<String, LatLng> mGeoLocations;

    /**
     * Contains all current detailed information about a geo location
     */
    private HashMap<String, GeoLocationInfo> mGeoInfos;

    /**
     * Contains all database references to user location
     */
    private HashMap<String, DatabaseReference> mUserLocationRefs;

    /**
     * Contains all listeners to user location updates.
     */
    private HashMap<String, ValueEventListener> mUserLocationListeners;

    /**
     * Information buffer for send broadcast.
     */
    private HashMap<String, GeoLocationInfo> mInfoBuffer;

    /**
     * Intentionally made public to be used in the method registerLocationListenerForUser().
     */
    public ValueEventListener mSingleGeoLocationListener;

    // TEST ONLY
    @RestrictTo(RestrictTo.Scope.TESTS)
    public Boolean mFirebaseQueryExecuted = false;
    @RestrictTo(RestrictTo.Scope.TESTS)
    public Boolean mQueryResultSent = false;

    public LocationSharingService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();

        this.mRootRef = FirebaseDatabase.getInstance().getReference();
        this.mGeoFire = new GeoFire(FirebaseDatabase.getInstance().getReference(DB_REF_GEO));

        if (mGeoLocations == null) {
            mGeoLocations = new HashMap<>();
            mGeoInfos = new HashMap<>();
        }

        mUserLocationRefs = new HashMap<>();
        mInfoBuffer = new HashMap<>();
        mUserLocationListeners = new HashMap<>();

        createSingleGeoLocationListener();
        registerReceivers();

        String testUser = "4yP0T1QjH3ZIraUIHIuoQpYhOkU2";
        registerLocationListenerForUser(testUser);
    }


    @RestrictTo(RestrictTo.Scope.TESTS)
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }


    @RestrictTo(RestrictTo.Scope.TESTS)
    public class LocationSharingBinder extends Binder {
        public LocationSharingService getService() {
            return LocationSharingService.this;
        }
    }

    @RestrictTo(RestrictTo.Scope.TESTS)
    public boolean isUserLocationListenerRegistered(String uid) {
        return mUserLocationListeners.containsKey(uid);
    }

    /**
     * TEST ONLY
     */
    @RestrictTo(RestrictTo.Scope.TESTS)
    public boolean containsUserLocation(String uid) {
        return mGeoLocations.containsKey(uid);
    }

    @RestrictTo(RestrictTo.Scope.TESTS)
    public LatLng getGeoLocationByUid(String uid) {
        return mGeoLocations.get(uid);
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        if (intent != null) {
            String requestType = intent.getStringExtra(PARAM_IN_REQUEST_TYPE);
            if (requestType != null && requestType.equals(REQUEST_ADD_LISTENER)) {
                String key = intent.getStringExtra(PARAM_IN_REFER_KEY);
                if (key == null) {
                    Log.v(TAG, "Failed registering listener. Missing parameter in intent: PARAM_OUT_REFER_KEY.");
                } else {
                    registerLocationListenerForUser(key);
                }
            }
            if (requestType != null && requestType.equals(REQUEST_REMOVE_LISTENER)) {
                String key = intent.getStringExtra(PARAM_IN_REFER_KEY);
                if (key == null) {
                    Log.v(TAG, "Failed unregistering listener. Missing parameter in intent: PARAM_OUT_REFER_KEY.");
                } else {
                    unregisterLocationListenerForUser(key);
                }
            }
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.v(TAG, "Service stopped.");
        unregisterReceivers();
    }

    @Override
    public void onKeyEntered(String key, GeoLocation location) {
        // Only report information that has not expire
        //Log.v(TAG, "GeoQueryEvent: Key entered." + key);
        if (key.equals(getCurrentUserUID())) {
            // No need to know self location
            return;
        }
        mGeoLocations.put(key, GeoUtil.geoToLatLng(location));
        updateGeoLocationInfo(key);
    }

    @Override
    public void onKeyExited(String key) {
        Log.v(TAG, "GeoQueryEvent: Key exited." + key);
        if (key.equals(getCurrentUserUID())) {
            // No need to know self location
            return;
        }
        mQueryResultSent = false;
        mGeoLocations.remove(key);
        mGeoInfos.remove(key);
        broadcastGeoLocationsUpdate(ON_KEY_EXITED, key);
    }

    @Override
    public void onKeyMoved(String key, GeoLocation location) {
        Log.v(TAG, "GeoQueryEvent: Key moved." + key);
        if (key.equals(getCurrentUserUID())) {
            // No need to know self location
            return;
        }
        mGeoLocations.put(key, GeoUtil.geoToLatLng(location));
        updateGeoLocationInfo(key);
    }

    @Override
    public void onGeoQueryReady() {
        Log.v(TAG, "GeoQuery has been initialized.");
    }

    @Override
    public void onGeoQueryError(DatabaseError error) {
        Log.v(TAG, "GeoQuery error." + error.getMessage());
        // TODO: Consider notifying UI to display error
    }

    /**
     * When self location has changed:
     * - Change query configuration
     * - Send location update the server
     * @param location New location
     */
    @Override
    public void onSelfLocationChanged(Location location) {
        GeoLocation geoLocation = new GeoLocation(location.getLatitude(), location.getLongitude());
        Double radius = GEO_QUERY_RADIUS;

        if (mGeoQuery == null) {
            mGeoQuery = mGeoFire.queryAtLocation(geoLocation, radius);
            mGeoQuery.addGeoQueryEventListener(this);
        } else {
            mGeoQuery.setLocation(geoLocation, radius);
        }

        // Send new location to server
        sendNewSelfLocation(location);
        mFirebaseQueryExecuted = true;
        mSelfLocation = location;
    }

    /**
     * Create listener for single geo location value event
     */
    public void createSingleGeoLocationListener() {
        // Child listener
        mSingleGeoLocationListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String uid = dataSnapshot.getKey();
                GeoLocation geoLocation = getLocationValue(dataSnapshot);
                broadcastSingleUserLocation(uid, geoLocation);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.v(TAG, "Unable to retrieve detailed location info from a location update.");
            }
        };
    }

    /**
     * Retrieve and update geo location info with the given key.
     * @param key
     */
    public void updateGeoLocationInfo(String key) {
        mRootRef.child(getUserRefPath(RefType.GEO_INFO, key)).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        GeoLocationInfo geoLocationInfo = dataSnapshot.getValue(GeoLocationInfo.class);
                        String key = dataSnapshot.getKey();
                        String type;

                        // Determine which query event was fired
                        type = mGeoInfos.containsKey(key) ? ON_KEY_MOVED : ON_KEY_ENTERED;

                        if (geoLocationInfo != null && isGeoInfoExpired(geoLocationInfo)) {
                            // Throw away a expired location info
                            mGeoLocations.remove(key);
                        } else {
                            Log.v(TAG, "GeoQueryEvent: new location update");
                            mQueryResultSent = false;
                            // Only send broadcast if geo info is not expired
                            mGeoInfos.put(key, geoLocationInfo);
                            broadcastGeoLocationsUpdate(type, key);
                            if (type.equals(ON_KEY_ENTERED)
                                    || TimeUtil.isTimeCloseToNow(geoLocationInfo.time) ) {
                                // Only send location notification if the time is close to now.
                                sendLocationNotification(key);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.v(TAG, "Error retriving extra information for a geo location record. "
                                + databaseError.getMessage());
                    }
                }
        );
    }

    /**
     * Get human-readable distance between the user and a friend's locations.
     * @param uid friend's uid
     */
    private String getFriendDistanceReadable(String uid) {
        // Return empty string if self location is unknown.
        if (mSelfLocation == null) {
            return "";
        }

        LatLng latLngSelf = GeoUtil.locationToLatLng(mSelfLocation);
        LatLng latLngFriend = mGeoLocations.get(uid);

        // Return empty string if friend's location is unknown.
        if (latLngFriend == null) {
            return "";
        }

        Log.v(TAG, "Distance from " + latLngSelf.toString() + " to " + latLngFriend.toString());
        double distance = GeoUtil.distanceBetween(latLngSelf, latLngFriend);
        return GeoUtil.distanceToReadable(distance);
    }

    /**
     * Get human-readable time of friend's last location update.
     * @param uid friend's uid
     */
    private String getFriendLastLocationTime(String uid) {
        GeoLocationInfo geoLocationInfo = mGeoInfos.get(uid);

        // Return empty string if friend's location is unknown.
        if (geoLocationInfo == null) {
            return "";
        }

        return TimeUtil.getFriendlyTime(geoLocationInfo.time);
    }

    /**
     * Send friend's location update to the system.
     * @param uid friend's uid
     */
    private void sendLocationNotification(String uid) {
        if (MapContainerFragment.sIsMapOpen == true) {
            return;
        }

        String distance = getFriendDistanceReadable(uid);
        String time = getFriendLastLocationTime(uid);

        Intent intent = new Intent(this, MainViewActivity.class);

        // Remove an older notification if there is
        PendingIntent pendingIntent =
                PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                .setContentTitle(getResources().getString(R.string.app_name))
                .setContentText("Your friend " + uid + " is " + distance + " away " + time)
                .setSmallIcon(R.drawable.logo)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        int notificationId = (int) System.currentTimeMillis();

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(notificationId, mBuilder.build());
    }

    /**
     * Send location updates broadcast to receivers.
     * @param eventType indicates which type of GeoQueryEvent is fired
     * @param key indicates the key to referred to
     */
    public void broadcastGeoLocationsUpdate(String eventType, @Nullable String key) {
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(GeoQueryLocationsReceiver.ACTION_GEOQUERY_LOCATIONS);

        broadcastIntent.putExtra(PARAM_OUT_REFER_EVENT, eventType);
        broadcastIntent.putExtra(PARAM_OUT_REFER_KEY, key);
        broadcastIntent.putExtra(PARAM_OUT_LOCATIONS, mGeoLocations);
        broadcastIntent.putExtra(PARAM_OUT_LOCATION_INFOS, mGeoInfos);

        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
        sendBroadcast(broadcastIntent);
        mQueryResultSent = true;
    }

    public void registerReceivers() {
        registerPositioningReceiver();
    }

    public void registerPositioningReceiver() {
        mSelfPositionReceiver = SelfPositionReceiver.register(this, this);
    }

    /**
     * Unregister all receives. Should be called in onPause().
     */
    public void unregisterReceivers() {
        unregisterReceiver(mSelfPositionReceiver);
    }

    /**
     * Send over the data to the database server.
     * @param location New location update
     */
    public void sendNewSelfLocation(@NonNull Location location){
        String uid = getCurrentUserUID();
        Double latitude = location.getLatitude();
        Double longitude = location.getLongitude();

        GeoHash geoHash = new GeoHash(new GeoLocation(latitude, longitude));

        Map<String, Object> updates = new HashMap<>();

        // Update two refs at the same time.
        updates.put(getUserRefPath(RefType.GEO_INFO, uid) + "/" + INFO_LABEL_TIME, location.getTime());
        //updates.put(getUserRefPath(RefType.GEO_INFO, uid) + "/" + INFO_LABEL_UID, user.getUid());

        // Follows GeoFire implementation.
        updates.put(getUserRefPath(RefType.GEO_LOCATION, uid)  + "/g", geoHash.getGeoHashString());
        updates.put(getUserRefPath(RefType.GEO_LOCATION, uid)  + "/l", Arrays.asList(latitude, longitude));

        mRootRef.updateChildren(updates, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (databaseError != null) {
                    Log.v(TAG, "Failure sending device location. Message: " + databaseError.getMessage());
                } else {
                    Log.v(TAG, "New self location has been successfully updated on ther server.");
                }
            }
        });
    }

    /**
     * Register location update listener for a given user.
     * @param uid user id
     */
    public void registerLocationListenerForUser(String uid) {
        DatabaseReference ref = mRootRef.child(getUserRefPath(RefType.GEO_INFO, uid));

        // Parent listener
        ValueEventListener geoInfoListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String uid = dataSnapshot.getKey();
                GeoLocationInfo info = dataSnapshot.getValue(GeoLocationInfo.class);

                // Check for expiry of geoinfo
                if(info != null && !isGeoInfoExpired(info)) {
                    mInfoBuffer.put(uid, info);

                    mRootRef.child(getUserRefPath(RefType.GEO_LOCATION, uid))
                            .addListenerForSingleValueEvent(mSingleGeoLocationListener);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.v(TAG, "Unable to retrieve location.");
            }
        };

        // Register geo info listener with the reference corresponding to the uid
        ref.addValueEventListener(geoInfoListener);
        // Save reference object
        mUserLocationRefs.put(uid, ref);
        mUserLocationListeners.put(uid, geoInfoListener);
        Log.v(TAG, "Now listening user location update for uid = " + String.valueOf(uid));
    }

    /**
     * Broadcast a single user location update to the receivers.
     * @param uid user id
     * @param geoLocation updated geolocation
     */
    public void broadcastSingleUserLocation(String uid, GeoLocation geoLocation) {
        GeoLocationInfo info = mInfoBuffer.get(uid);

        if (info == null) {
            Log.v(TAG, "You are trying to broadcast a location with no information. Aborted.");
            return;
        }

        // Construct and send broadcast.
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(SingleUserLocationReceiver.ACTION_SINGE_USER_LOCATION);

        broadcastIntent.putExtra(PARAM_OUT_UID, uid);
        // Pass LatLng since it is parcelable.
        broadcastIntent.putExtra(PARAM_OUT_DISTANCE, getFriendDistanceReadable(uid));
        broadcastIntent.putExtra(PARAM_OUT_TIME, getFriendLastLocationTime(uid));

        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
        sendBroadcast(broadcastIntent);

        Log.v(TAG, "Broadcast sent with a new location update from user " + String.valueOf(uid));
    }

    /**
     * Unregister location update listener for a given user.
     * @param uid user id
     */
    public void unregisterLocationListenerForUser(String uid) {
        DatabaseReference ref = mUserLocationRefs.get(uid);
        // Safety check
        if (ref == null) {
            Log.v(TAG, "Failed registering listener: cannot find listener for userID = " + uid);
            return;
        }

        // IMPORTANT: Remove event listener to stop receiving update
        ref.removeEventListener(mUserLocationListeners.get(uid));

        // Not as important but necessary enough to remove
        mUserLocationRefs.remove(uid);
        mUserLocationListeners.remove(uid);
    }

    /**
     * Retrieve current user's uid.
     * @return user uid (null if current user doesn't exist)
     */
    private String getCurrentUserUID(){
        // Get current user('s id).
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            return null;
        } else {
            return user.getUid();
        }
    }

    /**
     * Get specific reference path (of UserLocation data) by user id
     * @param refType   Type of the database reference
     * @param uid       User id
     * @return          Full path of the database reference
     */
    public static String getUserRefPath(RefType refType, String uid) {
        String path = null;
        switch (refType) {
            case GEO_LOCATION:
                path = DB_REF_GEO + "/" + uid;
                break;
            case GEO_INFO:
                path = DB_REF_GEO_INFO + "/" + uid;
                break;
        }
        return path;
    }

    /**
     * Determines whether geo information has expired.
     */
    public static Boolean isGeoInfoExpired(@NonNull GeoLocationInfo info) {
        // We check whether the timestamp of the geo information is earlier than today.
        return TimeUtil.getTimeDiffUntilNow(info.time, TimeUtil.TimeUnit.Day) >= 1;
    }

    /*
     * Firebase GeoFire Java Library
     *
     * Copyright © 2014 Firebase - All Rights Reserved
     * https://www.firebase.com
     *
     * Redistribution and use in source and binary forms, with or without
     * modification, are permitted provided that the following conditions are met:
     *
     * 1. Redistributions of source code must retain the above copyright notice, this
     * list of conditions and the following disclaimer.
     *
     * 2. Redistributions in binaryform must reproduce the above copyright notice,
     * this list of conditions and the following disclaimer in the documentation
     * and/or other materials provided with the distribution.
     *
     * THIS SOFTWARE IS PROVIDED BY FIREBASE AS IS AND ANY EXPRESS OR
     * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
     * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
     * EVENT SHALL FIREBASE BE LIABLE FOR ANY DIRECT,
     * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
     * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
     * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
     * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
     * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
     * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
     */
    private GeoLocation getLocationValue(DataSnapshot dataSnapshot) {
        try {
            GenericTypeIndicator<Map<String, Object>> typeIndicator = new GenericTypeIndicator<Map<String, Object>>() {};
            Map<String, Object> data = dataSnapshot.getValue(typeIndicator);
            List<?> location = (List<?>) data.get("l");
            Number latitudeObj = (Number) location.get(0);
            Number longitudeObj = (Number) location.get(1);
            double latitude = latitudeObj.doubleValue();
            double longitude = longitudeObj.doubleValue();
            if (location.size() == 2 && GeoLocation.coordinatesValid(latitude, longitude)) {
                return new GeoLocation(latitude, longitude);
            } else {
                return null;
            }
        } catch (NullPointerException e) {
            return null;
        } catch (ClassCastException e) {
            return null;
        }
    }
}
