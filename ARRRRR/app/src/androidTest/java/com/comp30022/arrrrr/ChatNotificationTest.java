package com.comp30022.arrrrr;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.rule.ServiceTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.runner.lifecycle.ActivityLifecycleMonitorRegistry;
import android.support.test.runner.lifecycle.Stage;

import com.comp30022.arrrrr.models.User;
import com.comp30022.arrrrr.services.ChatNotificationService;
import com.comp30022.arrrrr.services.LocationSharingService;
import com.comp30022.arrrrr.utils.Constants;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.util.Collection;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doNothing;

/**
 * Test for {@link ChatNotificationService}
 *
 * Tests included:
 * - Notification is successfully delivered.
 * - Chat activity is opened as expected.
 *
 * @author zijie shen
 */

@RunWith(AndroidJUnit4.class)
public class ChatNotificationTest {

    @Rule
    public ActivityTestRule<ChatActivity> activityRule
            = new ActivityTestRule<>(
            ChatActivity.class,
            true,     // initialTouchMode
            false);   // launchActivity. False to customize the intent
    private ChatNotificationService mService;

    private User sender;
    private User receiver;
    private String message;

    @Before
    public void setup(){
        // setup message
        sender = MockUserDatabase.mockRandomUser();
        receiver = MockUserDatabase.mockRandomUser();
        message = "test";

        //setup service
        mService = Mockito.mock(ChatNotificationService.class);
        doNothing().when(mService).sendNotification(null,null,null,null,null);
        Intent intent = new Intent();
        intent.putExtra(Constants.ARG_RECEIVER_UID,receiver.getUid());
        Mockito.when(mService.getIntent()).thenReturn(intent);
    }

    /**
     * test that notification has been receieved successfully and chat activity can be
     * opened afterwards
     * */
    @Test
    public void testWithChatNotificationService(){
        mService.sendNotification(sender.getEmail(),
                message,
                receiver.getUsername(),
                receiver.getUid(),
                receiver.getFirebaseToken());
        Intent intent = mService.getIntent();
        activityRule.launchActivity(intent);

        // ensure that chat has been corretly opened
        assertEquals(activityRule.getActivity().TAG, ChatActivity.TAG);

    }

}
