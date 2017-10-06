package com.comp30022.arrrrr;

import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ServiceTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.comp30022.arrrrr.services.ChatNotificationService;
import com.comp30022.arrrrr.services.LocationSharingService;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.TimeoutException;

/**
 * Created by Rondo on 10/6/2017.
 */

@RunWith(AndroidJUnit4.class)
public class ReceiveNotificationTest {
    @Rule
    public final ServiceTestRule mServiceRule = new ServiceTestRule();

    private ChatNotificationService mService;

    @Before
    public void setup() throws TimeoutException{
        // Create the service Intent.
        Intent intent = new Intent(InstrumentationRegistry.getTargetContext(),
                ChatNotificationService.class);

        // Bind the service and grab a reference to the binder.
        if (mService == null) {
            IBinder binder = mServiceRule.bindService(intent);
            while(binder == null) {
                binder = mServiceRule.bindService(intent);
            }
            mService = ((ChatNotificationService.ChatNotificationBinder) binder).getService();
        }
    }
    @Test
    public void testWithChatNotificationService(){


    }

}
