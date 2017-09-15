package com.comp30022.arrrrr.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.comp30022.arrrrr.models.GeoLocationInfo;
import com.comp30022.arrrrr.services.LocationSharingService;
import com.firebase.geofire.GeoLocation;

import java.util.HashMap;

/**
 * Created by Jay on 14/9/17.
 */

public class ServerLocationsReceiver extends BroadcastReceiver {

    public static final String ACTION_LOCATIONS_FROM_SERVER =
            "com.comp30022.arrrrr.intent.action.LOCATIONS_FROM_SERVER";

    @Override
    public void onReceive(Context context, Intent intent) {
        // Get information in the intent
        LocationSharingService.GeoQueryEventType type = intent
                .getParcelableExtra(LocationSharingService.PARAM_OUT_REFER_EVENT);

        String key = intent
                .getStringExtra(LocationSharingService.PARAM_OUT_REFER_KEY);

        HashMap<String, GeoLocation> geoLocations = intent
                .getParcelableExtra(LocationSharingService.PARAM_OUT_LOCATIONS);

        HashMap<String, GeoLocationInfo> geoLocationInfos = intent
                .getParcelableExtra(LocationSharingService.PARAM_OUT_LOCATION_INFOS);

    }
}
