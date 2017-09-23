package com.comp30022.arrrrr;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ServiceTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.v4.content.LocalBroadcastManager;

import com.comp30022.arrrrr.models.GeoLocationInfo;
import com.comp30022.arrrrr.receivers.GeoQueryLocationsReceiver;
import com.comp30022.arrrrr.services.LocationSharingService;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.maps.model.LatLng;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;
import java.util.concurrent.TimeoutException;

@RunWith(AndroidJUnit4.class)
public class LocationSharingServiceTest {
    @Rule
    public final ServiceTestRule mServiceRule = new ServiceTestRule();

    private Context mContext;

    private LocationSharingService mService;

    private String testUid = "4yP0T1QjH3ZIraUIHIuoQpYhOkU2";

    private GeoLocation location = new GeoLocation(1, 1);

    private GeoLocation newLocation = new GeoLocation(2, 2);

    private Boolean geoQueryLocationsReceived = false;
    private String typeReceived;

    private GeoQueryLocationsReceiver.GeoQueryLocationsListener geoQueryLocationsListener
            = new GeoQueryLocationsReceiver.GeoQueryLocationsListener() {
        @Override
        public void onGeoQueryEvent(String type, String key, HashMap<String, LatLng> geoLocations, HashMap<String, GeoLocationInfo> geoLocationInfos) {
            geoQueryLocationsReceived = true;
            typeReceived = type;
        }
    };

    @Before
    public void setUp() throws TimeoutException {
        mContext = InstrumentationRegistry.getTargetContext();
        GeoQueryLocationsReceiver receiver = new GeoQueryLocationsReceiver(geoQueryLocationsListener);
        IntentFilter filter = new IntentFilter(GeoQueryLocationsReceiver.ACTION_GEOQUERY_LOCATIONS);
        LocalBroadcastManager.getInstance(mContext).registerReceiver(receiver, filter);

        Intent intent = new Intent(mContext, LocationSharingService.class);

        IBinder binder = mServiceRule.bindService(intent);
        Assert.assertNotNull(binder);
        mService = ((LocationSharingService.LocationSharingBinder) binder).getService();
    }

    @After
    public void tearDown() throws Exception {
        Intent intent = new Intent(mContext, LocationSharingService.class);
        mContext.stopService(intent);
    }

    @Test
    public void testRegisterLocationListenerForUser() throws TimeoutException {
        Intent intent = new Intent(mContext, LocationSharingService.class);

        intent.putExtra(LocationSharingService.PARAM_IN_REFER_KEY, testUid);
        intent.putExtra(LocationSharingService.PARAM_IN_REQUEST_TYPE,
                LocationSharingService.REQUEST_ADD_LISTENER);

        mContext.startService(intent);

        Assert.assertTrue(mService.isUserLocationListenerRegistered(testUid));
    }

    @Test
    public void testRemoveLocationListenerForUser() throws TimeoutException {
        Intent intent = new Intent(mContext, LocationSharingService.class);

        String uid = "4yP0T1QjH3ZIraUIHIuoQpYhOkU2";
        intent.putExtra(LocationSharingService.PARAM_IN_REFER_KEY, uid);
        intent.putExtra(LocationSharingService.PARAM_IN_REQUEST_TYPE,
                LocationSharingService.REQUEST_REMOVE_LISTENER);

        Assert.assertFalse(mService.isUserLocationListenerRegistered(uid));
    }

    @Test
    public void testOnKeyEntered() throws TimeoutException, InterruptedException {
        mService.onKeyEntered(testUid, location);
        Assert.assertTrue(mService.containsUserLoation(testUid));

        mService.broadcastGeoLocationsUpdate(LocationSharingService.ON_KEY_ENTERED, testUid);
        Thread.sleep(2000);
        Assert.assertTrue(geoQueryLocationsReceived);
        Assert.assertEquals(LocationSharingService.ON_KEY_ENTERED, typeReceived);
    }

    @Test
    public void testOnKeyExited() throws TimeoutException, InterruptedException {
        mService.onKeyExited(testUid);
        Assert.assertFalse(mService.containsUserLoation(testUid));

        mService.broadcastGeoLocationsUpdate(LocationSharingService.ON_KEY_EXITED, testUid);
        Thread.sleep(2000);
        Assert.assertTrue(geoQueryLocationsReceived);
        Assert.assertEquals(LocationSharingService.ON_KEY_EXITED, typeReceived);
    }

    @Test
    public void testOnKeyMoved() throws TimeoutException, InterruptedException {
        mService.onKeyMoved(testUid, newLocation);
        Assert.assertTrue(mService.containsUserLoation(testUid));
        Assert.assertEquals(mService.getGeoLocationByUid(testUid).latitude, newLocation.latitude);

        mService.broadcastGeoLocationsUpdate(LocationSharingService.ON_KEY_MOVED, testUid);
        Thread.sleep(2000);
        Assert.assertTrue(geoQueryLocationsReceived);
        Assert.assertEquals(LocationSharingService.ON_KEY_MOVED, typeReceived);
    }
}
