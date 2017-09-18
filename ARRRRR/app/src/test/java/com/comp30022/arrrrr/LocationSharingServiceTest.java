package com.comp30022.arrrrr;

import com.comp30022.arrrrr.models.GeoLocationInfo;
import com.comp30022.arrrrr.services.LocationSharingService;

import junit.framework.Assert;

import org.junit.Test;

/**
 * Test location sharing service functions
 * @author Dafu Ai
 */

public class LocationSharingServiceTest {

    @Test
    public void testLocationInfoExpiry() {
        long today = System.currentTimeMillis();
        long oneHourAgo = today - 1000*60*60;
        long twoDaysAgo = today - 1000*60*60*24*2;

        GeoLocationInfo infoOneHourAgo = new GeoLocationInfo(oneHourAgo);
        GeoLocationInfo infoTwoDaysAgo = new GeoLocationInfo(twoDaysAgo);

        Assert.assertTrue(LocationSharingService.isGeoInfoExpired(infoTwoDaysAgo));
        Assert.assertFalse(LocationSharingService.isGeoInfoExpired(infoOneHourAgo));
    }
}
