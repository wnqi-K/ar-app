package com.comp30022.arrrrr.utils;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.comp30022.arrrrr.services.LocationSharingService;
import com.comp30022.arrrrr.services.PositioningService;

/**
 * Manager class that provides functionality to start/stop any service
 */

public class ServiceManager {
    private static final String TAG = ServiceManager.class.getSimpleName();

    /**
     * This method can ONLY BE CALLED AFTER :
     * 1. location permission has been granted
     * 2. location settings are okay.
     */
    public static void startPositioningService(Context context) {
        Intent intent = new Intent(context, PositioningService.class);
        intent.putExtra(PositioningService.PARAM_IN_REQUEST_START, true);
        context.startService(intent);
        Log.v(TAG, "Starting positioning service.");
    }

    /**
     * This method should be called before startPositioningService().
     * We want location sharing service to be started first.
     */
    public static void startLocationSharingService(Context context) {
        Intent intent = new Intent(context, LocationSharingService.class);
        context.startService(intent);
        Log.v(TAG, "Starting location sharing service.");
    }
}
