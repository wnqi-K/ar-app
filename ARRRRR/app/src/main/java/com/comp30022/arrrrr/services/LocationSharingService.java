package com.comp30022.arrrrr.services;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.comp30022.arrrrr.models.GeoLocationInfo;
import com.comp30022.arrrrr.receivers.SelfPositionReceiver;
import com.comp30022.arrrrr.receivers.ServerLocationsReceiver;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.firebase.geofire.core.GeoHash;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Dafu Ai
 * A backend location sharing service:
 * - Runs in the background.
 * - Constantly listen for location updates from other users from the server
 * -    when there is a update, it will be broadcasted to the receivers.
 * - Constantly listen for self location change and,
 * -    when there is a change, it will send new self location to the server.
 */

public class LocationSharingService extends Service implements
        GeoQueryEventListener,
        SelfPositionReceiver.SelfLocationListener {

    /**
     * Indicates the type of GeoQueryEvent for broadcasting purposes.
     */
    public enum GeoQueryEventType {
        ON_KEY_ENTERED, ON_KEY_EXITED, ON_KEY_MOVED
    }

    /**
     * Type of database reference for the user location records.
     */
    private enum RefType {
        GEO_LOCATION, GEO_INFO
    }

    private final static String DB_REF_GEO_INFO = "userlocations_info";
    private final static String DB_REF_GEO = "userlocations_geo";
    private final static String INFO_LABEL_TIME = "time";

    public static final String PARAM_OUT_REFER_EVENT = "OUT_REFER_TO_EVENT";
    public static final String PARAM_OUT_REFER_KEY = "OUT_REFER_TO_KEY";
    public static final String PARAM_OUT_LOCATIONS = "OUT_LOCATIONS";
    public static final String PARAM_OUT_LOCATION_INFOS = "OUT_LOCATION_INFOS";


    /**
     * TODO: Consider change radius to dynamic
     */
    private final static Double GEO_QUERY_RADIUS = 1.0;

    private final String TAG = LocationSharingService.class.getSimpleName();

    private SelfPositionReceiver mSelfPositionReceiver;
    private DatabaseReference mRootRef;
    private GeoFire mGeoFire;

    /**
     * GeoFire location query. Will only start when the device location has been detected.
     */
    private GeoQuery mGeoQuery;

    private HashMap<String, GeoLocation> mGeoLocations;
    private HashMap<String, GeoLocationInfo> mGeoInfos;
    private HashMap<String, DatabaseReference> mUserLocationRefs;
    private HashMap<String, ValueEventListener> mUserLocationListeners;

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
        mUserLocationListeners = new HashMap<>();
        registerReceivers();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        unregisterReceivers();
    }

    @Override
    public void onKeyEntered(String key, GeoLocation location) {
        mGeoLocations.put(key, location);
        updateGeoLocationInfo(key);
    }

    @Override
    public void onKeyExited(String key) {
        mGeoLocations.remove(key);
        mGeoInfos.remove(key);
        broadcastGeoLocationsUpdate(GeoQueryEventType.ON_KEY_EXITED, key);
    }

    @Override
    public void onKeyMoved(String key, GeoLocation location) {
        mGeoLocations.put(key, location);
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
        } else {
            mGeoQuery.setLocation(geoLocation, radius);
        }

        // Send new location to server
        sendNewSelfLocation(location);
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
                        GeoQueryEventType type;

                        // Determine which query event was fired
                        if (mGeoInfos.containsKey(key)) {
                            type = GeoQueryEventType.ON_KEY_MOVED;
                        } else {
                            type = GeoQueryEventType.ON_KEY_ENTERED;
                        }

                        mGeoInfos.put(key, geoLocationInfo);
                        broadcastGeoLocationsUpdate(type, key);
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
     * Send location updates broadcast to receivers.
     * @param eventType indicates which type of GeoQueryEvent is fired
     * @param key indicates the key to referred to
     */
    public void broadcastGeoLocationsUpdate(GeoQueryEventType eventType, @Nullable String key) {
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(ServerLocationsReceiver.ACTION_LOCATIONS_FROM_SERVER);

        broadcastIntent.putExtra(PARAM_OUT_REFER_EVENT, eventType);
        broadcastIntent.putExtra(PARAM_OUT_REFER_KEY, key);
        broadcastIntent.putExtra(PARAM_OUT_LOCATIONS, mGeoLocations);
        broadcastIntent.putExtra(PARAM_OUT_LOCATION_INFOS, mGeoInfos);

        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
        sendBroadcast(broadcastIntent);
    }

    public void registerReceivers() {
        registerPositioningReceiver();
    }

    public void registerPositioningReceiver() {
        IntentFilter filter = new IntentFilter(SelfPositionReceiver.ACTION_SELF_POSITION);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        mSelfPositionReceiver = new SelfPositionReceiver(this);
        registerReceiver(mSelfPositionReceiver, filter);
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
        // Get current user('s id).
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        // Safety check
        if (user == null) {
            Log.v(TAG, "Failure: CurrentUser is null.");
            return;
        }

        String uid = user.getUid();
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
        ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // TODO
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.v(TAG, "Unable to retrieve location.");
            }
        };
        ref.addValueEventListener(listener);
        mUserLocationRefs.put(uid, ref);
        mUserLocationListeners.put(uid, listener);
    }

    /**
     * Unregister location update listener for a given user.
     * @param uid user id
     */
    public void unragisterLocationListenerForUser(String uid) {
        DatabaseReference ref = mUserLocationRefs.get(uid);
        // Safety check
        if (ref == null) {
            Log.v(TAG, "Cannot find listener for userID=" + uid);
            return;
        }
        ref.removeEventListener(mUserLocationListeners.get(uid));
        mUserLocationListeners.remove(uid);
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
}
