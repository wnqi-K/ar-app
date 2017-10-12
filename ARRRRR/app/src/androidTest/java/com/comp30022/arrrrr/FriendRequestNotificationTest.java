package com.comp30022.arrrrr;

import android.content.Intent;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.comp30022.arrrrr.models.User;
import com.comp30022.arrrrr.services.NotificationService;
import com.comp30022.arrrrr.utils.Constants;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doNothing;

/**
 * Test for {@link NotificationService}
 *
 * Tests included:
 * - Request Notification is successfully delivered.
 * - AcceptRequest activity is opened as expected.
 *
 * @author Wenqiang Kuang
 */

@RunWith(AndroidJUnit4.class)
public class FriendRequestNotificationTest{
    @Rule
    public ActivityTestRule<AcceptRequestActivity> activityRule
            = new ActivityTestRule<>(
            AcceptRequestActivity.class,
            true,     // initialTouchMode
            false);   // launchActivity. False to customize the intent

    // Items to be mocked.
    private NotificationService mService;
    private User sender;
    private String message;

    @Before
    public void setup(){
        // setup message
        sender = MockUserDatabase.mockRandomUser();
        message = Constants.REQUEST_MESSAGE;

        //setup service
        mService = Mockito.mock(NotificationService.class);
        doNothing().when(mService).sendNotification(null,null,null,null,null);
        Intent intent = new Intent();
        intent.putExtra(Constants.SENDER_UID, sender.getUid());
        intent.putExtra(Constants.SENDER_NAME, sender.getUsername());
        intent.putExtra(Constants.SENDER_EMAIL, sender.getEmail());
        intent.putExtra(Constants.SENDER_GENDER, sender.getGender());
        intent.putExtra(Constants.SENDER_ADDRESS, sender.getAddress());
        Mockito.when(mService.getIntent()).thenReturn(intent);
    }

    /**
     * test that the request notification has been delivered successfully and acceptRequestActivity
     * intent is attached to the notification.
     * */
    @Test
    public void testWithChatNotificationService(){
        mService.sendNotification(sender.getEmail(),
                message,
                sender.getUsername(),
                sender.getUid(),
                sender.getFirebaseToken());
        Intent intent = mService.getIntent();
        activityRule.launchActivity(intent);

        // Ensure that acceptRequestActivity has been correctly opened.
        assertEquals(activityRule.getActivity().TAG, AcceptRequestActivity.TAG);
        String senderUid = intent.getExtras().getString(Constants.SENDER_UID);
        String senderName = intent.getExtras().getString(Constants.SENDER_NAME);
        String senderEmail = intent.getExtras().getString(Constants.SENDER_EMAIL);
        String senderGender = intent.getExtras().getString(Constants.SENDER_GENDER);
        String senderAddress = intent.getExtras().getString(Constants.SENDER_ADDRESS);

        // Ensure the information delivered is correct.
        assertEquals(senderUid, sender.getUid());
        assertEquals(senderName, sender.getUsername());
        assertEquals(senderEmail, sender.getEmail());
        assertEquals(senderGender, sender.getGender());
        assertEquals(senderAddress, sender.getAddress());
    }
}
