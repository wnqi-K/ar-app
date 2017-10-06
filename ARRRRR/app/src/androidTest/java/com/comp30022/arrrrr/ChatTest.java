package com.comp30022.arrrrr;

import android.support.test.rule.ActivityTestRule;
import android.support.v7.widget.RecyclerView;

import com.comp30022.arrrrr.adapters.ChatRecyclerAdapter;
import com.google.firebase.database.ValueEventListener;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

/**
 * Test for {@link ChatActivity}
 *
 * Tests included:
 * - sending messages successfully
 * - receiving messages successfully
 *
 *
 * @author zijie shen
 */

public class ChatTest {
    /**
     * You will need a ActivityTestRule for testing involving any activity.
     * See https://developer.android.com/reference/android/support/test/rule/ActivityTestRule.html
     * for more details
     */
    @Rule
    public ActivityTestRule<ChatActivity> mChatActivityRule =
            new ActivityTestRule<>(ChatActivity.class);

    private ChatActivity mChatActivity;
    private ChatRoomManager mChatRoomManager;
    private RecyclerView mRecyclerViewChat;
    private ChatRecyclerAdapter mChatRecyclerAdapter;
    private ValueEventListener mGetValueEventListener,mSendValudEventListener;

    @Before
    public void setup(){
        mChatActivity = mChatActivityRule.getActivity();
        mChatRoomManager = mChatActivity.getmChatRoomManager();
        mRecyclerViewChat = mChatActivity.getmRecyclerViewChat();
        mChatRecyclerAdapter = mChatActivity.getmChatRecyclerAdapter();
        mGetValueEventListener = mChatRoomManager.getmGetValueEventListener();
        mSendValudEventListener = mChatRoomManager.getmSendValueEventListener();
    }

    @Test
    public void test() {

    }



}
