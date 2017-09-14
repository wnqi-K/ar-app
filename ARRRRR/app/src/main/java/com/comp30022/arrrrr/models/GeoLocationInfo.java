package com.comp30022.arrrrr.models;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * Model for extra info for geo location
 */
@IgnoreExtraProperties
public class GeoLocationInfo {
    /**
     * Time when the location was updated.
     */
    public Long time;

    public GeoLocationInfo() {
        // Default constructor required for calls to DataSnapshot.getValue(GeoLocationInfo.class)
    }

    public GeoLocationInfo(Long time) {
        this.time = time;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("time", time);

        return result;
    }
}
