package com.comp30022.arrrrr.ar;

/**
 * Created by krzysztofjackowski on 24/09/15.
 * Modified by Xiaoyu Guo on 18/09/17.
 */

public class AugmentedPOI {
    private int mId;
    private String mName;
    private String mdescription;
    private double mLatitude;
    private double mLongitude;

    public AugmentedPOI(String newName, String newDescription,
                        double newLatitude, double newLongitude) {
        this.mName = newName;
        this.mdescription = newDescription;
        this.mLatitude = newLatitude;
        this.mLongitude = newLongitude;
    }

    public int getPoiId() {
        return mId;
    }
    public void setPoiId(int poiId) {
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