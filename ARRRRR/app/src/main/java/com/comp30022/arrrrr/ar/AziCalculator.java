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

    private static double AZIMUTH_ACCURACY = 5;

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
     * if azimuth angle within the range returns true
     * else, false
     * called by ArViewActivity in onAzimuthChanged(float azimuthChangedFrom, float azimuthChangedTo)
     * */
    public boolean isBetween(double minAngle, double maxAngle, double azimuth) {
        if (minAngle > maxAngle) {
            if (isBetween(0, maxAngle, azimuth) && isBetween(minAngle, 360, azimuth))
                return true;
        } else {
            if (azimuth > minAngle && azimuth < maxAngle)
                return true;
        }
        return false;
    }
}
