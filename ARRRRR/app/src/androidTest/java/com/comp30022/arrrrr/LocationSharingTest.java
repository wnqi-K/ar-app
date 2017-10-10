package com.comp30022.arrrrr;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.IBinder;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ServiceTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.comp30022.arrrrr.database.UserManagement;
import com.comp30022.arrrrr.models.GeoLocationInfo;
import com.comp30022.arrrrr.models.User;
import com.comp30022.arrrrr.services.LocationSharingService;
import com.comp30022.arrrrr.utils.PreferencesAccess;
import com.firebase.geofire.GeoLocation;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.util.concurrent.TimeoutException;

/**
 * Test suite for {@link LocationSharingService}
 *
 * Tests included:
 * - GeoQuery event handling (enter, move, exit) (also including nearby notification switch)
 * - Event handling for register/unregister listener for single user's location update
 * - Event handling for self location updates (including location sharing switch)
 * - GeoQuery filter distance(/radius)
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

        mService.mTestUserManagement = userManagement;
        mService.setTestAuth(firebaseAuth);
    }

    /**
     * Test event handling of location listener registration for a single user
     */
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

    /**
     * Test event handling of location listener removal for a single user
     */
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

    /**
     * Test event handling of changing device's position.
     */
    @Test
    public void testOnSelfLocationChanged() throws InterruptedException {
        Location location = new Location("test_provider");
        location.setLongitude(geoLocation.longitude);
        location.setLatitude(geoLocation.latitude);

        /////////////// TEST WHEN SWITCH IS ON

        // Create a mocked SharedPreferences where location sharing is ENABLED
        SharedPreferences mockPref = Mockito.mock(SharedPreferences.class);
        Mockito.when(mockPref.getBoolean(
                mContext.getString(R.string.PREF_KEY_ENABLE_LOCATION_SHARING),
                true)).thenReturn(true);

        mService.mTestPref = mockPref;

        mService.onSelfLocationChanged(location);
        assertFirebaseQueryExecuted();

        /////////////// TEST WHEN SWITCH IS OFF

        // Set the mocked SharedPreferences where location sharing is NOT ENABLED
        Mockito.when(mockPref.getBoolean(
                mContext.getString(R.string.PREF_KEY_ENABLE_LOCATION_SHARING),
                true)).thenReturn(false);
        assertFirebaseQueryNotExecuted();
    }

    public void assertFirebaseQueryExecuted() throws InterruptedException {
        Thread.sleep(100);
        Assert.assertTrue(mService.mFirebaseQueryExecuted);
        mService.mFirebaseQueryExecuted = false;
    }

    public void assertFirebaseQueryNotExecuted() throws InterruptedException {
        Thread.sleep(100);
        Assert.assertFalse(mService.mFirebaseQueryExecuted);
        mService.mFirebaseQueryExecuted = false;
    }

    /**
     * Test whether we have a working switch for nearby notification.
     */
    @Test
    public void testNearbyNotificationSwitch() {
        // Mock a DataSnapShot to inject to the onDataChange event
        DataSnapshot snapshot = Mockito.mock(DataSnapshot.class);
        Mockito.when(snapshot.getKey()).thenReturn(testUID);

        // Make a geolocation info object into the DataSnapshot which is guaranteed to be valid
        GeoLocationInfo geoLocationInfo = new GeoLocationInfo(System.currentTimeMillis());
        Mockito.when(snapshot.getValue(GeoLocationInfo.class)).thenReturn(geoLocationInfo);

        /////////////// TEST WHEN SWITCH IS ON

        // Create a mocked SharedPreferences where notification is ENABLED
        SharedPreferences mockPref = Mockito.mock(SharedPreferences.class);
        Mockito.when(mockPref.getBoolean(
                mContext.getString(R.string.PREF_KEY_ENABLE_NEARBY_NOTIFICATION),
                false)).thenReturn(true);
        mService.mTestPref = mockPref;

        mService.getGeoInfoSingleValueListener().onDataChange(snapshot);
        Assert.assertTrue(mService.mNearbyNotificationSent);
        mService.mNearbyNotificationSent = false;

        /////////////// TEST WHEN SWITCH IS OFF

        // Set the mocked SharedPreferences where notification is NOT ENABLED
        Mockito.when(mockPref.getBoolean(
                mContext.getString(R.string.PREF_KEY_ENABLE_NEARBY_NOTIFICATION),
                false)).thenReturn(false);
        mService.mTestPref = mockPref;

        mService.getGeoInfoSingleValueListener().onDataChange(snapshot);
        Assert.assertFalse(mService.mNearbyNotificationSent);
        mService.mNearbyNotificationSent = false;
    }

    /**
     * Test whether query filter distance can be correctly set.
     */
    @Test
    public void testQueryFilterDistance() {
        long testRadius = (long)3;

        SharedPreferences preferences = Mockito.mock(SharedPreferences.class);
        Mockito.when(preferences.getLong(mContext.getString(R.string.PREF_KEY_FILTER_DISTANCE),
                (long)(double)LocationSharingService.DEFAULT_GEO_QUERY_RADIUS)).thenReturn(testRadius);

        mService.mTestPref = preferences;

        Location location = Mockito.mock(Location.class);
        mService.onSelfLocationChanged(location);

        Assert.assertEquals(mService.getActualGeoQueryRadius(), (double)testRadius);
    }
}
