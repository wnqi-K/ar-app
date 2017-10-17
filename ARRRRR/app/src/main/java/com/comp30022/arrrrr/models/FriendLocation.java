package com.comp30022.arrrrr.models;

import android.location.Location;

import com.comp30022.arrrrr.database.UserManagement;
import com.comp30022.arrrrr.utils.GeoUtil;
import com.comp30022.arrrrr.utils.TimeUtil;
import com.google.android.gms.maps.model.LatLng;

/**
 * Packs friends' location info in a single class.
 * @author Dafu Ai
 */

public class FriendLocation {

    private LatLng mPosition;
    private long mTime;

    public FriendLocation(LatLng position, long time) {
        mPosition = position;
        mTime = time;
    }

    @Override
    public String toString() {
        Location currUserLocation = UserManagement.getInstance().getCurrUserLocation();

        if (currUserLocation == null) {
            return "";
        } else {
            double distanceDouble = GeoUtil.distanceBetween(
                    GeoUtil.locationToLatLng(currUserLocation),
                    mPosition);
            String distanceString = GeoUtil.distanceToReadable(distanceDouble);
            String time = TimeUtil.getFriendlyTime(mTime);

            return distanceString + " " + time;
        }
    }
}
