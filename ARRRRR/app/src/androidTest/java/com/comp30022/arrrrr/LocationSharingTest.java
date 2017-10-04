package com.comp30022.arrrrr;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.IBinder;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ServiceTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.comp30022.arrrrr.database.UserManagement;
import com.comp30022.arrrrr.models.User;
import com.comp30022.arrrrr.services.LocationSharingService;
import com.firebase.geofire.GeoLocation;
import com.google.firebase.auth.FirebaseAuth;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.util.concurrent.TimeoutException;

/**
 * Test suite for {@link LocationSharingService}
 *
 * Tests included:
 * - GeoQuery event handling (enter, move, exit)
 * - Event handling for register/unregister listener for single user's location update
 * - Event handling for self location updates
 *
 * @author Dafu Ai
 */

@RunWith(AndroidJUnit4.class)
@LargeTest
public class LocationSharingTest {
    @Rule
    public final ServiceTestRule mServiceRule = new ServiceTestRule();

    private String testUID = "testUID";

    private Context mContext;
    private LocationSharingService mService;
    private GeoLocation geoLocation = new GeoLocation(1, 1);
    private GeoLocation newGeoLocation = new GeoLocation(2, 2);
    private UserManagement userManagement;
    private FirebaseAuth firebaseAuth;

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

        userManagement = MockUserDatabase.mockUserManagement();
        firebaseAuth = MockUserDatabase.mockFirebaseAuth();

        mService.setTestUserManagement(userManagement);
        mService.setTestAuth(firebaseAuth);
    }

    @Test
    public void testRegisterLocationListenerForUser() throws InterruptedException {
        Intent intent = new Intent(mContext, LocationSharingService.class);

        intent.putExtra(LocationSharingService.PARAM_IN_REFER_KEY, testUID);
        intent.putExtra(LocationSharingService.PARAM_IN_REQUEST_TYPE,
                LocationSharingService.REQUEST_ADD_LISTENER);

        mContext.startService(intent);
        Thread.sleep(100);
        Assert.assertTrue(mService.isUserLocationListenerRegistered(testUID));
    }

    @Test
    public void testRemoveLocationListenerForUser() throws InterruptedException {
        Intent intent = new Intent(mContext, LocationSharingService.class);

        intent.putExtra(LocationSharingService.PARAM_IN_REFER_KEY, testUID);
        intent.putExtra(LocationSharingService.PARAM_IN_REQUEST_TYPE,
                LocationSharingService.REQUEST_REMOVE_LISTENER);

        mContext.startService(intent);
        Thread.sleep(100);
        Assert.assertFalse(mService.isUserLocationListenerRegistered(testUID));
    }

    /**
     * Check the case where we will NOT need the query result associated with the key.
     */
    @Test
    public void testGeoQueryKeyNotNeeded() throws InterruptedException {
        String key = userManagement.getCurrentUser().getUid();
        testOnKeyEntered(key);
        testOnKeyExited(key);
        testOnKeyMoved(key);

        key = "must_be_non_friend";
        testOnKeyEntered(key);
        testOnKeyExited(key);
        testOnKeyMoved(key);
    }

    /**
     * Check the case where we will need the query result associated with the key.
     */
    @Test
    public void testGeoQueryKeyNeeded() throws InterruptedException {
        String key = userManagement.getFriendList().get(0).getUid();
        testOnKeyEntered(key);
        testOnKeyExited(key);
        testOnKeyMoved(key);
    }


    public void testOnKeyEntered(String uid) throws InterruptedException {
        mService.onKeyEntered(uid, geoLocation);
        if (uid.equals(userManagement.getCurrentUser().getUid()) || !userManagement.isUserFriend(uid)) {
            Assert.assertFalse(mService.containsUserLocation(uid));
        } else {
            Assert.assertTrue(mService.containsUserLocation(uid));
        }
    }

    public void testOnKeyExited(String uid) throws InterruptedException {
        mService.onKeyExited(uid);
        if (uid.equals(userManagement.getCurrentUser().getUid()) || !userManagement.isUserFriend(uid)) {
            Assert.assertFalse(mService.containsUserLocation(uid));
            Assert.assertFalse(mService.mQueryResultSent);
        } else {
            Assert.assertFalse(mService.containsUserLocation(uid));
            Thread.sleep(100);
            Assert.assertTrue(mService.mQueryResultSent);
        }
    }

    public void testOnKeyMoved(String uid) throws InterruptedException {
        mService.onKeyMoved(uid, newGeoLocation);
        if (uid.equals(userManagement.getCurrentUser().getUid()) || !userManagement.isUserFriend(uid)) {
            Assert.assertFalse(mService.containsUserLocation(uid));
        } else {
            Assert.assertTrue(mService.containsUserLocation(uid));
            Assert.assertEquals(mService.getGeoLocationByUid(uid).latitude, newGeoLocation.latitude);
        }
    }

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
