package com.comp30022.arrrrr.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

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
        LatLng latLng = intent.getParcelableExtra(LocationSharingService.PARAM_OUT_LOCATION);
        GeoLocationInfo info = intent.getParcelableExtra(LocationSharingService.PARAM_OUT_LOCATION_INFO);

        // Notify listener.
        mListener.onReceivingSingleUserLocation(uid, latLng, info);
    }

    /**
     * Required interface in order to use this receiver.
     */
    public interface SingleUserLocationListener {
        void onReceivingSingleUserLocation(String uid, LatLng latLng, GeoLocationInfo info);
    }
}
