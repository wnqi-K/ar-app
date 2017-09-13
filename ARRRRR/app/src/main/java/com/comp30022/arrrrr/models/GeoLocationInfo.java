package com.comp30022.arrrrr.models;

import com.comp30022.arrrrr.UserLocationController;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Jay on 13/9/17.
 */
@IgnoreExtraProperties
public class GeoLocationInfo {
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
        result.put(, time);

        return result;
    }
}
