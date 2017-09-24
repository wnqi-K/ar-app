package com.comp30022.arrrrr;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.IBinder;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ServiceTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.comp30022.arrrrr.services.LocationSharingService;
import com.firebase.geofire.GeoLocation;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.TimeoutException;

/**
 * Test for location sharing service
 *
 * @author Dafu Ai
 */

@RunWith(AndroidJUnit4.class)
public class LocationSharingTest {
    @Rule
    public final ServiceTestRule mServiceRule = new ServiceTestRule();

    private Context mContext;

    private LocationSharingService mService;

    private String testUid = "4yP0T1QjH3ZIraUIHIuoQpYhOkU2";

    private GeoLocation geoLocation = new GeoLocation(1, 1);
    private GeoLocation newGeoLocation = new GeoLocation(2, 2);

    @Before
    public void setUp() throws TimeoutException {
        mContext = InstrumentationRegistry.getTargetContext();

        Intent intent = new Intent(mContext, LocationSharingService.class);

        if (mService == null) {
            IBinder binder = mServiceRule.bindService(intent);
            while(binder == null) {
                binder = mServiceRule.bindService(intent);
            }
            mService = ((LocationSharingService.LocationSharingBinder) binder).getService();
        }
    }

    @Test
    public void testRegisterLocationListenerForUser() throws InterruptedException {
        Intent intent = new Intent(mContext, LocationSharingService.class);

        intent.putExtra(LocationSharingService.PARAM_IN_REFER_KEY, testUid);
        intent.putExtra(LocationSharingService.PARAM_IN_REQUEST_TYPE,
                LocationSharingService.REQUEST_ADD_LISTENER);

        mContext.startService(intent);
        Thread.sleep(100);
        Assert.assertTrue(mService.isUserLocationListenerRegistered(testUid));
    }

    @Test
    public void testRemoveLocationListenerForUser() throws InterruptedException {
        Intent intent = new Intent(mContext, LocationSharingService.class);

        intent.putExtra(LocationSharingService.PARAM_IN_REFER_KEY, testUid);
        intent.putExtra(LocationSharingService.PARAM_IN_REQUEST_TYPE,
                LocationSharingService.REQUEST_REMOVE_LISTENER);

        mContext.startService(intent);
        Thread.sleep(100);
        Assert.assertFalse(mService.isUserLocationListenerRegistered(testUid));
    }

    @Test
    public void testOnKeyEntered() throws InterruptedException {
        mService.onKeyEntered(testUid, geoLocation);
        Assert.assertTrue(mService.containsUserLocation(testUid));
    }

    @Test
    public void testOnKeyExited() throws InterruptedException {
        mService.onKeyExited(testUid);
        Assert.assertFalse(mService.containsUserLocation(testUid));
        Thread.sleep(100);
        Assert.assertTrue(mService.mQueryResultSent);
    }

    @Test
    public void testOnKeyMoved() throws InterruptedException {
        mService.onKeyMoved(testUid, newGeoLocation);
        Assert.assertTrue(mService.containsUserLocation(testUid));
        Assert.assertEquals(mService.getGeoLocationByUid(testUid).latitude, newGeoLocation.latitude);
    }

    @Test
    public void testOnSelfLocationChanged() throws InterruptedException {
        Location location = new Location("test_provider");
        location.setLongitude(geoLocation.longitude);
        location.setLatitude(geoLocation.latitude);

        mService.onSelfLocationChanged(location);
        assertFirebaseQueryExecuted();
    }

    public void assertFirebaseQueryExecuted() throws InterruptedException {
        Thread.sleep(100);
        Assert.assertTrue(mService.mFirebaseQueryExecuted);
        mService.mFirebaseQueryExecuted = false;
    }
}
