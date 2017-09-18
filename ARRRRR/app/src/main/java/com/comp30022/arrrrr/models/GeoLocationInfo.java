package com.comp30022.arrrrr.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * Model for extra info for geo location
 */
@IgnoreExtraProperties
public class GeoLocationInfo implements Parcelable {
    /**
     * Time when the location was updated.
     * (UTC time & in milliseconds since January 1, 1970)
     */
    public Long time;

    public GeoLocationInfo() {
        // Default constructor required for calls to DataSnapshot.getValue(GeoLocationInfo.class)
    }

    public GeoLocationInfo(Long time) {
        this.time = time;
    }

    protected GeoLocationInfo(Parcel in) {
        this.time = in.readLong();
    }

    public static final Creator<GeoLocationInfo> CREATOR = new Creator<GeoLocationInfo>() {
        @Override
        public GeoLocationInfo createFromParcel(Parcel in) {
            return new GeoLocationInfo(in);
        }

        @Override
        public GeoLocationInfo[] newArray(int size) {
            return new GeoLocationInfo[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeLong(time);
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("time", time);

        return result;
    }
}
