package com.comp30022.arrrrr;

import android.location.Location;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.comp30022.arrrrr.models.GeoLocationInfo;
import com.comp30022.arrrrr.utils.MapUIManager;
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
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Dafu Ai
 */

public class UserLocationController implements
        MapUIManager.OnSelfMarkerMoveListener,
        GeoQueryEventListener {
    /**
     * Type of database reference for the user location records.
     */
    public enum RefType {
        GEO_LOCATION, GEO_INFO
    }

    private final static String DB_REF_GEO_INFO = "userlocations_info";
    private final static String DB_REF_GEO = "userlocations_geo";

    private final static String INFO_LABEL_TIME = "time";

    /**
     * TODO: Consider change radius to dynamic
     */
    private final static Double GEO_QUERY_RADIUS = 1.0;

    private final String TAG = UserLocationController.class.getSimpleName();

    private DatabaseReference mRootRef;
    private GeoFire mGeoFire;
    private GeoQuery mGeoQuery;
    private Map<String, GeoLocation> mGeoLocations;
    private Map<String, GeoLocationInfo> mGeoInfos;

    public UserLocationController() {
        this.mRootRef = FirebaseDatabase.getInstance().getReference();
        this.mGeoFire = new GeoFire(FirebaseDatabase.getInstance().getReference(DB_REF_GEO));
        this.mGeoLocations = new HashMap<>();
        this.mGeoInfos = new HashMap<>();
    }

    /**
     * Update GeoQuery configurations acoordingly when the self in the Google maps has moved.
     */
    @Override
    public void onSelfMarkerMove(LatLng position) {
        GeoLocation geoLocation = new GeoLocation(position.latitude, position.longitude);
        Double radius = UserLocationController.GEO_QUERY_RADIUS;

        if (mGeoQuery == null) {
            mGeoQuery = mGeoFire.queryAtLocation(geoLocation, radius);
        } else {
            mGeoQuery.setLocation(geoLocation, radius);
        }
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
     * Retrieve and update geo location info with the given key.
     * @param key
     */
    public void updateGeoLocationInfo(String key) {
        mRootRef.child(getUserRefPath(RefType.GEO_INFO, key)).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        GeoLocationInfo geoLocationInfo = dataSnapshot.getValue(GeoLocationInfo.class);
                        mGeoInfos.put(dataSnapshot.getKey(), geoLocationInfo);
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
     * Send over the data to the database server.
     * @param location New location update
     */
    public void sendLocationUpdate(@NonNull Location location){
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
                    Log.v(TAG, "New location has been successfully updated.");
                }

            }
        });
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
