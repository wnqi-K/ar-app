package com.comp30022.arrrrr;

import com.comp30022.arrrrr.utils.GeoUtil;
import com.google.android.gms.maps.model.LatLng;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Test utility functions related to geo locations
 * @author Dafu Ai
 */

@RunWith(MockitoJUnitRunner.class)
public class GeoUtilTest{

    // 28 Bouverie St, Carlton, VIC 3053 Australia
    private LatLng home = new LatLng(-37.8058120, 144.9618550);
    // 8 Franklin St, Melbourne VIC 3004 Australia
    private LatLng aldi = new LatLng(-37.8074710, 144.9620160);

    @Test
    public void testCalcDistance() {
        double dist = GeoUtil.distanceBetween(home, aldi);
        Assert.assertTrue(dist < 500);

        String distStr = GeoUtil.distanceToReadable(dist);
        System.out.println("Check this: distance between home and aldi = " + distStr);
        Assert.assertTrue(distStr.contains("m"));
    }

}
