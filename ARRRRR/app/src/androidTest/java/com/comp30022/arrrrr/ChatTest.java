package com.comp30022.arrrrr;

import android.support.test.rule.ActivityTestRule;
import android.support.v7.widget.RecyclerView;

import com.comp30022.arrrrr.adapters.ChatRecyclerAdapter;
import com.comp30022.arrrrr.models.Chat;
import com.comp30022.arrrrr.utils.Constants;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.ValueEventListener;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mockito;

import static junit.framework.Assert.assertEquals;

/**
 * Test for {@link ChatActivity}
 *
 * Tests included:
 * - sending messages successfully
 * - receiving messages successfully
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
    private ChatRecyclerAdapter mChatRecyclerAdapter;
    private ValueEventListener mGetValueEventListener,mSendValueEventListener;
    private Chat mChat;
    private String room_type_1;
    private String room_type_2;
    private int num_chats;

    @Before
    public void setup(){
        mChatActivity = mChatActivityRule.getActivity();
        mChatRoomManager = mChatActivity.getmChatRoomManager();
        mChatRecyclerAdapter = mChatActivity.getmChatRecyclerAdapter();
        mGetValueEventListener = mChatRoomManager.getmGetValueEventListener();
        mSendValueEventListener = mChatRoomManager.getmSendValueEventListener();
        mChat = Mockito.mock(Chat.class);
        room_type_1 = mChat.senderUid + "_" + mChat.receiverUid;
        room_type_2 = mChat.receiverUid + "_" + mChat.senderUid;
        num_chats = mChatRecyclerAdapter.getItemCount();
    }

    /**
     * Test the case of sending and receiving messages by verifying the number of chat
     * after the action of message delivery
     * */
    @Test
    public void SendReceiveMessageTest() {
        //mock a message event
        DataSnapshot snapshot = Mockito.mock(DataSnapshot.class);
        Mockito.when(snapshot.hasChild(room_type_1)).thenReturn(true);
        Mockito.when(snapshot.hasChild(room_type_2)).thenReturn(true);

        //send the message
        mSendValueEventListener.onDataChange(snapshot);
        // receive the message and show it on the screen
        mGetValueEventListener.onDataChange(snapshot);


        // number of chats should increment by 1 after sending message
        int current_num_chats = num_chats + 1;
        assertEquals(current_num_chats,mChatRecyclerAdapter.getItemCount());
    }




}
