package com.comp30022.arrrrr.utils;

import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.util.GeoUtils;
import com.google.android.gms.maps.model.LatLng;

/**
 * Utility class dealing with geologicalfriends location  locations
 *
 * @author Dafu Ai
 */

public class GeoUtil {

    /**
     * Convert a GeoLocation object to a LatLng object (to make it parcelable)
     * We can do this since geolocation contains essentially the same information with LatLng.
     */
    public static LatLng geoToLatLng(GeoLocation geoLocation) {
        return new LatLng(geoLocation.latitude, geoLocation.longitude);
    }

    /**
     * Calculate distance between two LatLng positions
     * @param x one position
     * @param y the other position
     * @return the distance in meters
     */
    public static double distanceBetween(LatLng x, LatLng y) {
        // Using GeoFire utility function
        return GeoUtils.distance(x.latitude, x.longitude, y.latitude, y.longitude);
    }

    /**
     * Given a distance (in meters), convert to a user friendly string for display.
     * @param dist distance in meters
     * @return distance in string
     */
    public static String distanceToReadable(double dist) {
        int distInt = (int) dist;
        if (distInt < 1000) {
            return String.valueOf(distInt) + "m";
        } else {
            return String.valueOf(distInt/1000) + "km";
        }
    }

}
