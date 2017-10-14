package com.comp30022.arrrrr;

import android.app.Activity;
import android.app.Instrumentation;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.widget.ListView;
import android.widget.TextView;

import com.comp30022.arrrrr.adapters.ListViewAdapter;
import com.comp30022.arrrrr.database.UserManagement;
import com.comp30022.arrrrr.models.User;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import java.util.ArrayList;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.anything;
import static org.junit.Assert.assertEquals;

/**
 * Test for addFriendActivity.
 * Tests included:
 * - check if the correct user profile interface is loaded when clicking on the user in search view.
 *
 * @author Wenqiang Kuang
 */

@RunWith(AndroidJUnit4.class)
public class AddingFriendTest {

    @Rule
    public ActivityTestRule<AddingFriendsActivity> mAddingFriendsActivityTestRule =
            new ActivityTestRule<>(AddingFriendsActivity.class);

    @Mock
    private UserManagement mUserManagement;
    private ArrayList<User> mUsers;
    private ListView mListView;
    private ListViewAdapter mListViewAdapter;

    @Before
    public void setUp() throws Exception {
        mUserManagement = MockUserDatabase.mockUserManagement();
        String uid1 = "testUid1";
        String uid2 = "testUid2";
        String email1 = "email1@test";
        String email2 = "email2@test";

        User user1 = MockUserDatabase.mockUser(uid1, email1);
        User user2 = MockUserDatabase.mockUser(uid2, email2);

        mUsers = new ArrayList<>();
        mUsers.add(user1);
        mUsers.add(user2);
    }

    /**
     * Check if the list view list correct mUsers in the list.
     */
    @Test
    public void testListView() {
        // Wait for fragment to be loaded
        getInstrumentation().waitForIdle(new Runnable() {
            @Override
            public void run() {
                ListView listView = (ListView) mAddingFriendsActivityTestRule.getActivity()
                        .findViewById(R.id.search_result_list);

                // Test if the list view lists all the mUsers.
                int listCount = listView.getAdapter().getCount();
                int userCount = UserManagement.getInstance().getUserList().size();
                assertEquals(listCount, userCount);
            }
        });
    }

    /**
     * Check if performing click on the first user in the list, whether or not the corresponding
     * user profile would appear.
     */
    @Test
    public void testUserSelected() throws Exception {
        Instrumentation instrumentation = getInstrumentation();
        mListView = (ListView) mAddingFriendsActivityTestRule.getActivity()
                .findViewById(R.id.search_result_list);
        mListViewAdapter = new ListViewAdapter(getInstrumentation().getContext(),mUsers);
        mListView.setAdapter(mListViewAdapter);

        instrumentation.runOnMainSync(new Runnable() {
            @Override
            public void run() {
                onData(anything()).inAdapterView(withId(R.id.search_result_list)).atPosition(0).perform(click());
            }
        });

        Instrumentation.ActivityMonitor monitor = instrumentation.addMonitor(AddingFriendsActivity.class.getName(), null, false);
        Activity addingFriendActivityResult = instrumentation.waitForMonitorWithTimeout(monitor, 5000);

        TextView firstUserEmail = (TextView) addingFriendActivityResult.findViewById(R.id.user_email);
        assertEquals(firstUserEmail.getText().toString(), "email1@test");
    }
}