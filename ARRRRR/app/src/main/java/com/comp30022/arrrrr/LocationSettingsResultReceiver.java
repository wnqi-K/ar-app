package com.comp30022.arrrrr;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.util.Log;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationSettingsStatusCodes;

/**
 * A {@link BroadcastReceiver} that specifically designed for receiving result of checking
 * location settings from {@link PositioningService}
 */

public class LocationSettingsResultReceiver extends BroadcastReceiver {

    private static final String TAG = LocationSettingsResultReceiver.class.getSimpleName();

    public static final String ACTION_SETTINGS_RESULT =
            "com.comp30022.arrrrr.intent.action.LOCATION_SETTINGS_RESULT_RECEIVED";

    private LocationSettingsResultListener mContext;

    public LocationSettingsResultReceiver(LocationSettingsResultListener mContext) {
        this.mContext = mContext;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Integer statusCode = intent.getIntExtra(
                PositioningService.PARAM_OUT_SETTTINGS_STATUS_CODE,
                LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE);
        Exception e = intent.getParcelableExtra(
                PositioningService.PARAM_OUT_SETTINGS_EXCEPTION);

        mContext.onLocationSettingsResult(statusCode, e);
    }

    /**
     * The interface that a context must implement in order to use this broadcast receiver.
     */
    public interface LocationSettingsResultListener {
        /**
         * Handles when result of checking location settings have been received
         * @param statusCode
         * @param e
         */
        void onLocationSettingsResult(Integer statusCode, Exception e);
    }
}
