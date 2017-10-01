package com.comp30022.arrrrr.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.comp30022.arrrrr.models.GeoLocationInfo;
import com.comp30022.arrrrr.services.LocationSharingService;
import com.comp30022.arrrrr.utils.Constants;
import com.google.android.gms.maps.model.LatLng;

/**
 * A {@link BroadcastReceiver} which receives a single user location update from
 * {@link LocationSharingService}
 *
 * @author Dafu Ai
 */

public class SingleUserLocationReceiver extends BroadcastReceiver {

    public static final String ACTION_SINGE_USER_LOCATION =
            Constants.ACTION_PACKAGE + ".ACTION_SINGE_USER_LOCATION";

    public SingleUserLocationListener mListener;

    public SingleUserLocationReceiver(SingleUserLocationListener listener) {
        this.mListener = listener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String uid = intent.getStringExtra(LocationSharingService.PARAM_OUT_UID);
        String distance = intent.getParcelableExtra(LocationSharingService.PARAM_OUT_DISTANCE);
        String time = intent.getParcelableExtra(LocationSharingService.PARAM_OUT_TIME);

        // Notify listener.
        mListener.onReceivingSingleUserLocation(uid, distance, time);
    }

    /**
     * Required interface in order to use this receiver.
     */
    public interface SingleUserLocationListener {
        void onReceivingSingleUserLocation(String uid, String distance, String time);
    }

    /**
     * Provides receiver registration functionality.
     * @param context context which registration is attached
     * @param listener {@link SingleUserLocationListener}
     * @return a SingleUserLocationReceiver object which has been registered
     */
    public static SingleUserLocationReceiver register(Context context,
                                                      SingleUserLocationListener listener) {
        SingleUserLocationReceiver receiver = new SingleUserLocationReceiver(listener);
        IntentFilter filter = new IntentFilter(ACTION_SINGE_USER_LOCATION);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        context.registerReceiver(receiver, filter);
        return receiver;
    }
}
