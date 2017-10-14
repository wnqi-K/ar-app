package com.comp30022.arrrrr.ar;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xiaoyuguo on 19/09/2017.
 */

/**
 * This is a helper class that
 * calculates azimuthal angle of the device
 * */

public class AziCalculator {

    private static double AZIMUTH_ACCURACY = 15;

    /**
     * this is a function returns a list
     * that contain the  the */
    public List<Double> calculateAzimuthAccuracy(double azimuth) {
        double minAngle = azimuth - AZIMUTH_ACCURACY;
        double maxAngle = azimuth + AZIMUTH_ACCURACY;
        List<Double> minMax = new ArrayList<Double>();

        if (minAngle < 0)
            minAngle += 360;

        if (maxAngle >= 360)
            maxAngle -= 360;

        minMax.clear();
        minMax.add(minAngle);
        minMax.add(maxAngle);

        return minMax;
    }

    /**
     * this is a function returns boolean value
     * if azimuth angle within the range returns 1
     * else,
     * if smaller than min return -1
     * if larger than max return 1
     * called by ArViewActivity in onAzimuthChanged(float azimuthChangedFrom, float azimuthChangedTo)
     * */
    public int isBetween(double standard, double minAngle, double maxAngle, double azimuth) {

        //user's azimuth minus 180 degree
        double half = standard + 180;
        if (half > 360){
            half -= 360;
        }

        //with the range
        if (minAngle > maxAngle) {
            if (isBetween(standard, 0, maxAngle, azimuth)==0 || isBetween(standard, minAngle, 360, azimuth)==0)
                return 0;
        }

        else {
            if (azimuth > minAngle && azimuth < maxAngle)
                return 0;
        }

        //should turn left to see the icon
        if (azimuth >= maxAngle && azimuth <= half ){
            return 1;
        }

        //else, should turn right to see the icon
        return -1;
    }
}
