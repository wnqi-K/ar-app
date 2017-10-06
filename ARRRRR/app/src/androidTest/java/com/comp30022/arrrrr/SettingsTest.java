package com.comp30022.arrrrr;

import android.app.Activity;
import android.app.FragmentManager;
import android.location.Address;
import android.location.Location;
import android.support.test.InstrumentationRegistry;
import android.support.test.annotation.UiThreadTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import android.view.View;
import android.widget.TextView;

import com.comp30022.arrrrr.services.LocationSharingService;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import static junit.framework.Assert.*;

/**
 * Test for user settings
 *
 * Tests included:
 * - (Internal) Request result handling for removing current user's location records
 * - event handling for getting user's last location
 * @author Dafu Ai
 */

@RunWith(AndroidJUnit4.class)
public class SettingsTest {

    @Rule
    public ActivityTestRule<MainViewActivity> mActivityTestRule =
            new ActivityTestRule<>(MainViewActivity.class);

    private View.OnClickListener onClickListener;

    private SettingFragment mFragment;

    @Before
    public void setUp() throws Exception {
        mFragment = SettingFragment.newInstance();

        Activity activity = mActivityTestRule.getActivity();

        FragmentManager manager = mActivityTestRule.getActivity().getFragmentManager();
        manager.beginTransaction().replace(R.id.fragment_container,
                mFragment).commit();
    }

    /**
     * Button must be reset to disabled when action has started
     */
    @Test
    @UiThreadTest
    public void testStartClearLocationRecords() {
        // Wait for fragment to be loaded
        InstrumentationRegistry.getInstrumentation().waitForIdle(new Runnable() {
            @Override
            public void run() {
                mFragment.getOnClearRecordsClickListener().onClick(mFragment.getView());
                assertFalse(mFragment.getButtonClearRecords().isEnabled());
            }
        });
    }

    /**
     * Button must be reset to enabled after action has completed,
     * NO MATTER WHAT RESULT THE CONTEXT RECEIVES
     */
    @Test
    @UiThreadTest
    public void testFinishClearLocationRecords() {
        // Wait for fragment to be loaded
        InstrumentationRegistry.getInstrumentation().waitForIdle(new Runnable() {
            @Override
            public void run() {
                mFragment.onReceivingSimpleRequestResult(
                        LocationSharingService.REQUEST_CLEAR_LOCATION_RECORDS,
                        true
                );
                assertTrue(mFragment.getButtonClearRecords().isEnabled());

                mFragment.onReceivingSimpleRequestResult(
                        LocationSharingService.REQUEST_CLEAR_LOCATION_RECORDS,
                        false
                );
                assertTrue(mFragment.getButtonClearRecords().isEnabled());
            }
        });
    }

    /**
     * Test when we failed to get user's last location
     */
    @Test
    @UiThreadTest
    public void testGetLastLocationFail() {
        // Wait for fragment to be loaded
        InstrumentationRegistry.getInstrumentation().waitForIdle(new Runnable() {
            @Override
            public void run() {
                mFragment.onAddressFetchFailure(Mockito.mock(Location.class));

                TextView textView = (TextView)mFragment.getView().findViewById(R.id.text_view_last_location);

                // Check textView has shown the error text
                assertTrue(textView.getText().toString().contains(mFragment.getString(R.string.text_unknown)));
            }
        });
    }

    /**
     * Test when we successfully got user's last location
     */
    @Test
    @UiThreadTest
    public void testGetLastLocationSuccess() {
        // Wait for fragment to be loaded
        InstrumentationRegistry.getInstrumentation().waitForIdle(new Runnable() {
            @Override
            public void run() {
                mFragment.onAddressFetchSuccess(Mockito.mock(Address.class),
                        Mockito.mock(Location.class));

                TextView textView = (TextView)mFragment.getView().findViewById(R.id.text_view_last_location);

                // Check textView will not show the error text
                assertFalse(textView.getText().toString().contains(mFragment.getString(R.string.text_unknown)));
            }
        });
    }
}
