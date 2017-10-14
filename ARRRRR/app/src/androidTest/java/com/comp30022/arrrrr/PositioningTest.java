package com.comp30022.arrrrr;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.IBinder;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ServiceTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.v4.content.LocalBroadcastManager;

import com.comp30022.arrrrr.receivers.SelfPositionReceiver;
import com.comp30022.arrrrr.services.PositioningService;
import com.google.android.gms.location.LocationResult;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

/**
 * Test for {@link PositioningService}
 *
 * Tests included:
 * - Handling new self location update
 *
 * @author Dafu Ai
 */

@RunWith(AndroidJUnit4.class)
public class PositioningTest {
    @Rule
    public final ServiceTestRule mServiceRule = new ServiceTestRule();

    private Context mContext = InstrumentationRegistry.getTargetContext();

    private PositioningService mService;

    private SelfPositionReceiver mReceiver;

    @Before
    public void setUp() throws TimeoutException {
        if (mService == null) {
            Intent intent = new Intent(mContext, PositioningService.class);
            IBinder binder = mServiceRule.bindService(intent);
            while(binder == null) {
                binder = mServiceRule.bindService(intent);
            }
            mService = ((PositioningService.PositioningBinder) binder).getService();
        }
    }

    @After
    public void tearDown() throws Exception {
        if (mReceiver != null) {
            LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mReceiver);
        }
    }

    @Test
    public void testStartService() throws InterruptedException {
        Intent intent = new Intent(mContext, PositioningService.class);
        intent.putExtra(PositioningService.PARAM_IN_REQUEST_START, true);
        mContext.startService(intent);

        Thread.sleep(100);
        Assert.assertTrue(mService.isRequestingLocationUpdates());
    }

    @Test
    public void testOnLocationResult() throws InterruptedException, TimeoutException {
        testStartService();
        SelfPositionReceiver.SelfLocationListener listener = new SelfPositionReceiver.SelfLocationListener() {
            @Override
            public void onSelfLocationChanged(Location location) {
                System.out.println("Broadcast received.");
            }
        };

        Location location = new Location("test_provider");
        location.setLatitude(1);
        location.setLongitude(1);

        List<Location> locationList = new ArrayList<>();
        locationList.add(location);
        LocationResult locationResult = LocationResult.create(locationList);

        mService.getLocationCallback().onLocationResult(locationResult);

        Thread.sleep(100);
        Assert.assertTrue(mService.mUpdateSent);
    }
}