package com.comp30022.arrrrr.utils;

import com.google.android.gms.location.LocationRequest;

/**
 * A manager class that stores the a {@link LocationRequest} object (globally).
 * This is made for access convenience from multiple classes.
 *
 * @author Dafu Ai
 */

public class LocationRequestManager {
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

    private static LocationRequest mLocationRequest = null;

    /**
     * Retrieves the LocationRequest object
     */
    public static LocationRequest getRequest() {
        //if (mLocationRequest == null) {
        mLocationRequest = new LocationRequest();

        // Sets the desired interval for active location updates.
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);

        // Sets the fastest rate for active location updates.
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);

        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        //}
        return mLocationRequest;
    }
}
