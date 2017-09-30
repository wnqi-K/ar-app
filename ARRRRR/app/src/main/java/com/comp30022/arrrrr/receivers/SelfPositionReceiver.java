package com.comp30022.arrrrr.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.support.annotation.RestrictTo;

import com.comp30022.arrrrr.services.PositioningService;
import com.comp30022.arrrrr.utils.Constants;

/**
 * A {@link BroadcastReceiver} that specifically designed for receiving device location from
 * {@link PositioningService}
 *
 * @author Dafu Ai
 */

public class SelfPositionReceiver extends BroadcastReceiver {
    public static final String ACTION_SELF_POSITION =
            Constants.ACTION_PACKAGE + ".SELF_POSITION_RECEIVED";

    /**
     * The context that is using broadcast receiver
     */
    private SelfLocationListener mListener;

    public SelfPositionReceiver(SelfLocationListener listener) {
        this.mListener = listener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Location location = intent.getParcelableExtra(PositioningService.PARAM_OUT_LOCATION);

        if (location == null) {
            throw new UnknownError("Location not sent/received properly.");
        }

        // Notify listener.
        mListener.onSelfLocationChanged(location);
    }

    /**
     * The interface that a context must implement in order to use this broadcast receiver.
     */
    public interface SelfLocationListener {
        /**
         * Handles when listener recieves new location update
         * @param location New location
         */
        void onSelfLocationChanged(Location location);
    }

    /**
     * Provides receiver registration functionality.
     * @param context context which registration is attached
     * @param listener {@link SelfLocationListener}
     * @return a SelfPositionReceiver object which has been registered
     */
    public static SelfPositionReceiver register(Context context,
                                                SelfLocationListener listener) {
        SelfPositionReceiver receiver = new SelfPositionReceiver(listener);
        IntentFilter filter = new IntentFilter(ACTION_SELF_POSITION);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        context.registerReceiver(receiver, filter);
        return receiver;
    }
}
