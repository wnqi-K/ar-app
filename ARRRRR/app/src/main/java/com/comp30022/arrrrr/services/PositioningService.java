package com.comp30022.arrrrr.services;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Binder;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RestrictTo;
import android.util.Log;

import com.comp30022.arrrrr.receivers.SelfPositionReceiver;
import com.comp30022.arrrrr.utils.LocationRequestManager;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

/**
 * A service that (runs in the background)
 * and it is responsible for positioning current user's device and send new location
 * by broadcasting intents.
 *
 * @author Dafu Ai
 */

public class PositioningService extends Service {

    // Keys for intent extras
    public static final String PARAM_OUT_LOCATION = "OUT_LOCATION";
    public static final String PARAM_IN_PERM_GRANTED = "IN_PERMISSION_GRANTED";
    private static final String TAG = PositioningService.class.getSimpleName();

    /**
     * Provides access to the Fused Location Provider API.
     */
    private FusedLocationProviderClient mFusedLocationClient;

    /**
     * Stores parameters for requests to the FusedLocationProviderApi.
     */
    private LocationRequest mLocationRequest;

    /**
     * Callback for Location events.
     */
    private LocationCallback mLocationCallback;

    /**
     * Tracks the status of the location updates request.
     */
    private Boolean mRequestingLocationUpdates;

    /**
     * Represents a geographical location.
     */
    private Location mCurrentLocation;

    @RestrictTo(RestrictTo.Scope.TESTS)
    public boolean mUpdateSent = false;

    @RestrictTo(RestrictTo.Scope.TESTS)
    private final IBinder mBinder = new PositioningService.PositioningBinder();

    @RestrictTo(RestrictTo.Scope.TESTS)
    public class PositioningBinder extends Binder {
        public PositioningService getService() {
            return PositioningService.this;
        }
    }

    public PositioningService() {

    }

    @RestrictTo(RestrictTo.Scope.TESTS)
    public Boolean isRequestingLocationUpdates() {
        return mRequestingLocationUpdates;
    }

    @RestrictTo(RestrictTo.Scope.TESTS)
    public LocationCallback getLocationCallback() {
        return mLocationCallback;
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        // Check for permission requests.
        // Only start location updates when permission has been granted.
        if (intent != null && intent.getBooleanExtra(PARAM_IN_PERM_GRANTED, true)) {

            // Only starts location updates if it has not been started.
            if (mRequestingLocationUpdates == null || !mRequestingLocationUpdates) {

                mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
                mLocationRequest = LocationRequestManager.getRequest();

                createLocationCallback();
                startLocationUpdates();
                Log.v(TAG, "PositioningService has started.");
            }
        }

        return START_STICKY;
    }

    @RestrictTo(RestrictTo.Scope.TESTS)
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopLocationUpdates();
        Log.v(TAG, "Service stopped.");
    }

    /**
     * Creates a callback for receiving location events.
     */
    private void createLocationCallback() {
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);

                mCurrentLocation = locationResult.getLastLocation();
                mUpdateSent = false;
                // Send back location data to observers
                broadcastLocation();
            }
        };
    }

    /**
     * Broadcast the updated location to receivers.
     */
    private void broadcastLocation() {
        Log.v(TAG, "Broadcasting location.");
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(SelfPositionReceiver.ACTION_SELF_POSITION);
        broadcastIntent.putExtra(PARAM_OUT_LOCATION, mCurrentLocation);
        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
        sendBroadcast(broadcastIntent);
        mUpdateSent = true;
    }

    /**
     * Requests location updates from the FusedLocationApi. Note: we don't call this unless location
     * runtime permission has been granted.
     */
    private void startLocationUpdates() {
        mRequestingLocationUpdates = true;

        // Assume we already checked permission before starting the service.
        //noinspection MissingPermission
        mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                mLocationCallback, Looper.myLooper());

        Log.v(TAG, "Location updates started");
    }

    /**
     * Removes location updates from the FusedLocationApi.
     * TODO: This should be called if user don't want to use this service while in the background
     */
    private void stopLocationUpdates() {
        if (mRequestingLocationUpdates == null || !mRequestingLocationUpdates) {
            // "updates never requested, no-op."
            return;
        }

        // TODO: Read, understand and remove this comment
        // It is a good practice to remove location requests when the activity is in a paused or
        // stopped state. Doing so helps battery performance and is especially
        // recommended in applications that request frequent location updates.
        mFusedLocationClient.removeLocationUpdates(mLocationCallback)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        mRequestingLocationUpdates = false;
                    }
                });
    }
}
