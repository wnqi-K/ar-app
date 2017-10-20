package com.comp30022.arrrrr;

import android.app.FragmentManager;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

/**
 * Test for the Ar View
 * Tests included:
 * Test if the dialog popped up in the map fragment would start a new ArViewActivity.
 * @author Wenqiang Kuang
 */

@RunWith(AndroidJUnit4.class)
public class ArViewTest {
    @Rule
    public ActivityTestRule<MainViewActivity> mActivityTestRule =
            new ActivityTestRule<>(MainViewActivity.class);

    private MapContainerFragment mFragment;
    private MainViewActivity mActivity;

    @Before
    public void setUp() throws Exception {
        mFragment = MapContainerFragment.newInstance();

        mActivity = mActivityTestRule.getActivity();
        FragmentManager manager = mActivity.getFragmentManager();
        manager.beginTransaction().replace(R.id.fragment_container,
                mFragment).commit();
    }

    /**
     * Test if the quick_ar_entry menu item starts a new ArViewActivity.
     */
    @Test
    public void testQuickArNavigation() {
        openActionBarOverflowOrOptionsMenu(getTargetContext());
        onView(withText(R.string.quick_ar_entry)).perform(click());

        // Views that only belongs to the new created activity, check if they are displayed.
        onView(withId(R.id.cameraTextView)).check(matches(isDisplayed()));
        onView(withId(R.id.msg)).check(matches(isDisplayed()));
        onView(withId(R.id.distance)).check(matches(isDisplayed()));
    }
}
