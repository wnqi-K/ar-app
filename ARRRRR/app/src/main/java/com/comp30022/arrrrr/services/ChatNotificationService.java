package com.comp30022.arrrrr.services;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.support.annotation.RestrictTo;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.comp30022.arrrrr.ChatActivity;
import com.comp30022.arrrrr.R;
import com.comp30022.arrrrr.utils.Constants;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;


import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * This class is responsible of receiving and building notifications
 *
 * Created by rondo on 9/19/17.
 */

public class ChatNotificationService extends FirebaseMessagingService {
    private static final String TAG = "FCM Service";
    private static final String TAG_INFO = "Message data payload: ";
    private static final String DATE_FORMAT = "ddHHmmss";

    /*
    * This class manages messages receiving
    * */
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, TAG_INFO + remoteMessage.getData());

            // Details used to build a notification
            String title = remoteMessage.getData().get(Constants.KEY_TITLE);
            String message = remoteMessage.getData().get(Constants.KEY_TEXT);
            String username = remoteMessage.getData().get(Constants.KEY_USERNAME);
            String uid = remoteMessage.getData().get(Constants.KEY_UID);
            String fcmToken = remoteMessage.getData().get(Constants.KEY_FCM_TOKEN);

            // Don't show notification if chat activity is open.
            if (!ChatActivity.isActivityOpen()) {
                sendNotification(title,
                        message,
                        username,
                        uid,
                        fcmToken);

            }
        }
    }


    /*
    * This class will build a notification and send it to the user
    * */
    private void sendNotification(String title,
                                  String message,
                                  String receiver,
                                  String receiverUid,
                                  String firebaseToken){


        // Add information to Chat activity
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra(Constants.ARG_RECEIVER, receiver);
        intent.putExtra(Constants.ARG_RECEIVER_UID, receiverUid);
        intent.putExtra(Constants.ARG_FIREBASE_TOKEN, firebaseToken);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT);

        // build a notification
        //Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this);
        notificationBuilder.setSmallIcon(R.drawable.logo);
        notificationBuilder.setContentTitle(title);
        notificationBuilder.setContentText(message);
        notificationBuilder.setAutoCancel(true);
        notificationBuilder.setContentIntent(pendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        int mNotificationId = createID();
        // send notification
        mNotificationManager.notify(mNotificationId, notificationBuilder.build());
    }


    /**
     * create unique id for notification id
     * */
    private int createID(){
        Date now = new Date();
        int id = Integer.parseInt(new SimpleDateFormat(DATE_FORMAT,  Locale.US).format(now));
        return id;
    }

    @RestrictTo(RestrictTo.Scope.TESTS)
    public class ChatNotificationBinder extends Binder {
        public ChatNotificationService getService() {
            return ChatNotificationService.this;
        }
    }
}


