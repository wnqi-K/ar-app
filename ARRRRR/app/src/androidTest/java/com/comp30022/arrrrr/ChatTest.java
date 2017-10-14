package com.comp30022.arrrrr;

import android.content.Intent;
import android.support.test.rule.ActivityTestRule;

import com.comp30022.arrrrr.adapters.ChatRecyclerAdapter;
import com.comp30022.arrrrr.models.Chat;
import com.comp30022.arrrrr.models.User;
import com.comp30022.arrrrr.utils.Constants;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mockito;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.doNothing;

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
            new ActivityTestRule<>(ChatActivity.class,
                    true,    // initialTouchMode
                    false);  // Lazy launching

    private ChatActivity mChatActivity;
    private ChatRoomManager mChatRoomManager;
    private ChatRecyclerAdapter mChatRecyclerAdapter;

    private User receiver;

    @Before
    public void setup(){
        receiver = MockUserDatabase.mockRandomUser();
        Intent intent = new Intent();
        intent.putExtra(Constants.ARG_RECEIVER_UID,receiver.getUid());
        mChatActivityRule.launchActivity(intent);
        mChatActivity = mChatActivityRule.getActivity();

        mChatRoomManager = Mockito.mock(ChatRoomManager.class);
        mChatRecyclerAdapter = Mockito.mock(ChatRecyclerAdapter.class);
        //Mockito.when(mChatRecyclerAdapter.getItemCount()).thenReturn(0);
        mChatActivity.setChatRecyclerAdapter(mChatRecyclerAdapter);
        mChatActivity.setChatRoomManager(mChatRoomManager);
    }

    /**
     * Test the case of sending and receiving messages by verifying content of message
     * after the action of message delivery
     * */
    @Test
    public void SendReceiveMessageTest() {
        // create a message
        String message = "test only";
        Chat chat = new Chat(null,null,null,null,message,1);
        doNothing().when(mChatRoomManager).sendMessageToFirebaseUser(chat);
        doNothing().when(mChatRoomManager).getMessageFromFirebaseUser();

        // send message
        mChatActivity.setMessage(message);
        mChatActivity.sendMessage();

        // verify message
        assertEquals(message,mChatActivity.getChat().message);
    }
}
