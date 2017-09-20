package com.comp30022.arrrrr.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.comp30022.arrrrr.models.GeoLocationInfo;
import com.comp30022.arrrrr.services.LocationSharingService;
import com.comp30022.arrrrr.utils.Constants;
import com.google.android.gms.maps.model.LatLng;

import java.util.HashMap;

/**
 * A {@link BroadcastReceiver} which receives a multiple location updates from
 * {@link LocationSharingService} by a GeoQueryEvent
 *
 * @author Dafu Ai
 */

public class GeoQueryLocationsReceiver extends BroadcastReceiver {

    public static final String ACTION_GEOQUERY_LOCATIONS =
            Constants.ACTION_PACKAGE + ".GEOQUERY_LOCATIONS";

    public GeoQueryLocationsListener mListener;

    public GeoQueryLocationsReceiver(GeoQueryLocationsListener listener) {
        this.mListener = listener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // Get information in the intent
        String type = intent
                .getStringExtra(LocationSharingService.PARAM_OUT_REFER_EVENT);

        String key = intent
                .getStringExtra(LocationSharingService.PARAM_OUT_REFER_KEY);

        HashMap<String, LatLng> geoLocations = (HashMap)intent
                .getSerializableExtra(LocationSharingService.PARAM_OUT_LOCATIONS);

        HashMap<String, GeoLocationInfo> geoLocationInfos = (HashMap)intent
                .getSerializableExtra(LocationSharingService.PARAM_OUT_LOCATION_INFOS);

        // Notify listener.
        mListener.onGeoQueryEvent(type, key, geoLocations, geoLocationInfos);
    }

    /**
     * Required interface in order to use this receiver.
     */
    public interface GeoQueryLocationsListener {
        void onGeoQueryEvent(String type,
                             String key,
                             HashMap<String, LatLng> geoLocations,
                             HashMap<String, GeoLocationInfo> geoLocationInfos);
    }
}
