package com.comp30022.arrrrr;

import android.app.IntentService;
import android.content.Intent;
import android.location.Location;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;


/**
 * An intent service that (runs in the background)
 * and it is responsible for positioning user's device on the map.
 */
public class PositioningService extends IntentService {

    private static final String TAG = PositioningService.class.getSimpleName();
    // Keys for intent extras
    public static final String PARAM_OUT_LOCATION = "OUT_LOCATION";
    public static final String PARAM_OUT_SETTTINGS_STATUS_CODE = "OUT_SETTINGS_STATUS_CODE";
    public static final String PARAM_OUT_SETTINGS_EXCEPTION = "OUT_SETTINGS_EXCEPTION";
    public static final String PARAM_IN_PERM_GRANTED = "IN_PERMISSION_GRANTED";

    /*
     * Constant used in the location settings dialog.
     */
    private static final int REQUEST_CHECK_SETTINGS = 0x1;

    /**
     * The desired interval for location updates. Inexact. Updates may be more or less frequent.
     */
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;

    /**
     * The fastest rate for active location updates. Exact. Updates will never be more frequent
     * than this value.
     */
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;

    /**
     * Provides access to the Fused Location Provider API.
     */
    private FusedLocationProviderClient mFusedLocationClient;

    /**
     * Stores parameters for requests to the FusedLocationProviderApi.
     */
    private LocationRequest mLocationRequest;

    /**
     * Stores the types of location services the client is interested in using. Used for checking
     * settings to determine if the device has optimal location settings.
     */
    private LocationSettingsRequest mLocationSettingsRequest;

    /**
     * Provides access to the Location Settings API.
     */
    private SettingsClient mSettingsClient;

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

    public PositioningService() {
        super("PositioningService");
    }

    public PositioningService(String name) {
        super(name);
    }

    /**
     * Handles starting intent service.
     */
    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Log.v(TAG, "Starting location updates.");
        // Check for permission requests.
        // Only start location updates when permission has been granted.
        if(intent != null && intent.getBooleanExtra(PARAM_IN_PERM_GRANTED, true)) {

            // Only starts location updates if it has not been started.
            if(mRequestingLocationUpdates == null || !mRequestingLocationUpdates) {

                mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
                mSettingsClient = LocationServices.getSettingsClient(this);

                createLocationCallback();
                createLocationRequest();
                buildLocationSettingsRequest();
                startLocationUpdates();
            }
        }
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

                // Send back location data to observers
                broadcastLocation();
            }
        };
    }

    /**
     * Broadcast the updated location to receivers.
     */
    private void broadcastLocation() {
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(SelfPositionReceiver.ACTION_SELF_POSITION);
        broadcastIntent.putExtra(PARAM_OUT_LOCATION, mCurrentLocation);
        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
        sendBroadcast(broadcastIntent);
    }

    /**
     * Sets up the location request.
     */
    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();

        // Sets the desired interval for active location updates.
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);

        // Sets the fastest rate for active location updates.
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);

        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    /**
     * Uses a {@link com.google.android.gms.location.LocationSettingsRequest.Builder} to build
     * a {@link com.google.android.gms.location.LocationSettingsRequest} that is used for checking
     * if a device has the needed location settings.
     */
    private void buildLocationSettingsRequest() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        mLocationSettingsRequest = builder.build();
    }

    /**
     * Requests location updates from the FusedLocationApi. Note: we don't call this unless location
     * runtime permission has been granted.
     */
    private void startLocationUpdates() {
        mRequestingLocationUpdates = true;

        // Begin by checking if the device has the necessary location settings.
        mSettingsClient.checkLocationSettings(mLocationSettingsRequest)
                .addOnSuccessListener(new OnSuccessListener<LocationSettingsResponse>() {
                    @Override
                    public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                        Log.i(TAG, "All location settings are satisfied.");

                        //noinspection MissingPermission
                        mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                                mLocationCallback, Looper.myLooper());

                        broadcastSettingsResult(LocationSettingsStatusCodes.SUCCESS, null);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        int statusCode = ((ApiException) e).getStatusCode();
                        if (statusCode == LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE) {
                            // Location settings are inadequate, and cannot be fixed here.
                            // Fix in Settings.
                            mRequestingLocationUpdates = false;
                        }
                        // Let context activity to process other status code values.
                        broadcastSettingsResult(statusCode, e);
                    }
                });
    }

    /**
     * Broadcast check result of settings to listening receivers.
     */
    private void broadcastSettingsResult(Integer statusCode, Exception e) {
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(LocationSettingsResultReceiver.ACTION_SETTINGS_RESULT);
        broadcastIntent.putExtra(PARAM_OUT_SETTTINGS_STATUS_CODE, statusCode);
        broadcastIntent.putExtra(PARAM_OUT_SETTINGS_EXCEPTION, e);
        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
        sendBroadcast(broadcastIntent);
    }

    /**
     * Removes location updates from the FusedLocationApi.
     * TODO: This should be called if user don't want to use this service while in the background
     */
    private void stopLocationUpdates() {
        if (!mRequestingLocationUpdates) {
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

