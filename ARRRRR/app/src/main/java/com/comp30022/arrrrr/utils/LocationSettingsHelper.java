package com.comp30022.arrrrr.utils;

import android.app.Fragment;
import android.content.Context;
import android.support.annotation.NonNull;

import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

/**
 * A helper class for location settings checking.
 *
 * @author Dafu Ai
 */

public class LocationSettingsHelper {
    /**
     * Provides access to the Location Settings API.
     */
    private SettingsClient mSettingsClient;

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
     * Root context (can be a context of the fragment or the context itself)
     */
    private Context mRootContext;

    /**
     * Context of the helper class.
     */
    private OnLocationSettingsResultListener mListener;

    /**
     * Constructor for the case where {@link Context} is using this helper.
     */
    public LocationSettingsHelper(Context rootContext) {
        mLocationRequest = LocationRequestManager.getRequest();
        mSettingsClient = LocationServices.getSettingsClient(rootContext);
        mRootContext = rootContext;

        checkForInterfaceRequirement(rootContext);
        buildLocationSettingsRequest();
    }

    /**
     * Constructor for the case where {@link Fragment} is using this helper
     * and it should have a root context of course.
     */
    public LocationSettingsHelper(Fragment fragmentContext,
                                  Context rootContext) {
        mLocationRequest = LocationRequestManager.getRequest();
        mSettingsClient = LocationServices.getSettingsClient(rootContext);
        mRootContext = rootContext;

        checkForInterfaceRequirement(fragmentContext);
        buildLocationSettingsRequest();
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
     * Check location settings and pass result to the corresponding listener in the context.
     */
    public void checkLocationSettings() {
        // Begin by checking if the device has the necessary location settings.
        mSettingsClient.checkLocationSettings(mLocationSettingsRequest)
                .addOnSuccessListener(new OnSuccessListener<LocationSettingsResponse>() {
                    @Override
                    public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                        mListener.onLocationSettingsResultSuccess(locationSettingsResponse);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        mListener.onLocationSettingsResultFailure(e);
                    }
                });
    }

    /**
     * Check for whether context has implemented the required interface.
     */
    public void checkForInterfaceRequirement(Object object) {
        if (object instanceof OnLocationSettingsResultListener) {
            mListener = (OnLocationSettingsResultListener) object;
        } else {
            throw new RuntimeException(object.toString()
                    + " must implement OnLocationResultListener");
        }
    }

    public interface OnLocationSettingsResultListener {
        void onLocationSettingsResultSuccess(LocationSettingsResponse locationSettingsResponse);

        void onLocationSettingsResultFailure(@NonNull Exception e);
    }
}
