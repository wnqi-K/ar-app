package com.comp30022.arrrrr;

import android.app.FragmentManager;
import android.support.test.annotation.UiThreadTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.comp30022.arrrrr.adapters.RecyclerFriendListAdapter;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.InstrumentationRegistry.getTargetContext;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.assertThat;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static junit.framework.Assert.assertEquals;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.core.IsNull.notNullValue;

/**
 * Test for the friends fragment
 *
 * Tests included:
 * Friend list recycler view is loaded when fragment boots, and adapter is set.
 * Test if the adding_new_friend menu starts a new AddingFriendActivity.
 *
 * @author Wenqiang Kuang
 */

@RunWith(AndroidJUnit4.class)
public class FriendsFragmentTest {
    @Rule
    public ActivityTestRule<MainViewActivity> mActivityTestRule =
            new ActivityTestRule<>(MainViewActivity.class);

    private FriendsFragment mFragment;
    private MainViewActivity mActivity;

    @Before
    public void setUp() throws Exception {
        mFragment = FriendsFragment.newInstance();

        mActivity = mActivityTestRule.getActivity();
        FragmentManager manager = mActivity.getFragmentManager();
        manager.beginTransaction().replace(R.id.fragment_container,
                mFragment).commit();
    }

    /**
     * Test if the recycler view is loaded when fragment boots and adapter is correctly set.
     */
    @Test
    @UiThreadTest
    public void testRecyclerViewLoaded() {
        // Wait for fragment to be loaded
        getInstrumentation().waitForIdle(new Runnable() {
            @Override
            public void run() {
                View viewById = mActivityTestRule.getActivity().findViewById(R.id.recycler_view_friend_list);
                assertThat(viewById,notNullValue());
                assertThat(viewById, instanceOf(RecyclerView.class));
                RecyclerView recyclerView = (RecyclerView) viewById;
                RecyclerFriendListAdapter adapter = (RecyclerFriendListAdapter) recyclerView.getAdapter();
                assertThat(adapter, instanceOf(RecyclerFriendListAdapter.class));
                assertEquals(adapter.getItemCount(), 0);
            }
        });
    }

    /**
     * Test if the adding_new_friend menu starts a new AddingFriendActivity.
     */
    @Test
    public void testClickAddingFriend() {
        openActionBarOverflowOrOptionsMenu(getTargetContext());
        onView(withText(R.string.adding_new_friend)).perform(click());

        // Views that only belongs to the new created activity, check if they are displayed.
        onView(withId(R.id.search_result_list)).check(matches(isDisplayed()));
        onView(withId(R.id.search_view)).check(matches(isDisplayed()));
    }
}
