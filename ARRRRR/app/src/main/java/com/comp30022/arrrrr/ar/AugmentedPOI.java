package com.comp30022.arrrrr.ar;

/**
 * Created by krzysztofjackowski on 24/09/15.
 * Modified by Xiaoyu Guo on 18/09/17.
 */

public class AugmentedPOI {

    /**
     * Unique POI ID for each user
     * */
    private String mId;

    /**
     * Name for the user
     * */
    private String mName;


    /**
     * user's location
     * */
    private double mLatitude;
    private double mLongitude;

    public AugmentedPOI(String newId,String newName,
                        double newLatitude, double newLongitude) {
        this.mId = newId;
        this.mName = newName;
        this.mLatitude = newLatitude;
        this.mLongitude = newLongitude;
    }

    public String getPoiId() {
        return mId;
    }

    public void setPoiId(String poiId) {
        this.mId = poiId;
    }

    public String getPoiName() {
        return mName;
    }

    public void setPoiName(String poiName) {
        this.mName = poiName;
    }

    public double getPoiLatitude() {
        return mLatitude;
    }

    public void setPoiLatitude(double poiLatitude) {
        this.mLatitude = poiLatitude;
    }

    public double getPoiLongitude() {
        return mLongitude;
    }

    public void setPoiLongitude(double poiLongitude) {
        this.mLongitude = poiLongitude;
    }
}