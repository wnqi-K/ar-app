package com.comp30022.arrrrr;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;

/**
 * A {@link BroadcastReceiver} that specifically designed for receiving device location from
 * {@link PositioningService}
 */
public class SelfPositionReceiver extends BroadcastReceiver {
    public static final String ACTION_SELF_POSITION =
            "com.comp30022.arrrrr.intent.action.SELF_POSITION_RECEIVED";

    /**
     * The context that is using broadcast receiver
     */
    SelfPositionUpdateListener mContext;

    public SelfPositionReceiver(SelfPositionUpdateListener mContext) {
        this.mContext = mContext;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Location location = intent.getParcelableExtra(PositioningService.PARAM_OUT_LOCATION);
        if (location == null) {
            throw new UnknownError("Location not sent/received properly.");
        }
        // Notify listener.
        mContext.onSelfLocationUpdate(location);
    }

    /**
     * The interface that a context must implement in order to use this broadcast receiver.
     */
    public interface SelfPositionUpdateListener {
        /**
         * Handles when listener recieves new location update
         * @param location New location
         */
        void onSelfLocationUpdate(Location location);
    }
}
