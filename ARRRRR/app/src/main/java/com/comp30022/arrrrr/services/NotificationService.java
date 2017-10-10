package com.comp30022.arrrrr.services;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.RestrictTo;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.comp30022.arrrrr.AcceptRequestActivity;
import com.comp30022.arrrrr.ChatActivity;
import com.comp30022.arrrrr.R;
import com.comp30022.arrrrr.database.UserManagement;
import com.comp30022.arrrrr.models.User;
import com.comp30022.arrrrr.utils.Constants;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * This class is responsible of receiving and building notifications.
 * Created by rondo on 9/19/17.
 */

public class NotificationService extends FirebaseMessagingService {
    private static final String TAG = "FCM Service";
    private static final String TAG_INFO = "Message data payload: ";
    private static final String DATE_FORMAT = "ddHHmmss";
    private Intent intent;

    /*
     * This method manages messages receiving
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
    * This method will build a notification and send it to the user
    * */
    private void sendNotification(String title,
                                  String message,
                                  String senderEmail,
                                  String senderUid,
                                  String firebaseToken){

        // If detecting friend_request_message, assign addingFriendsActivity
        // intent to the notification.
        if(message.equals(getString(R.string.friend_request_message))){
            UserManagement userManagement = UserManagement.getInstance();
            User sender = userManagement.getUserByUID(senderUid);

            String currentUserName = sender.getUsername();
            String currentUserEmail = sender.getEmail();
            String currentUserGender = sender.getGender();
            String currentUserAddress = sender.getAddress();

            PendingIntent pendingIntent = switchToAddFriInterface(currentUserName, currentUserEmail,
                    currentUserGender, currentUserAddress);
            buildNotification(title, message, pendingIntent);

            // Otherwise chat message, assign chatActivity intent to the notification.
        }else{
            PendingIntent pendingIntent = switchToChatActivity(senderEmail, senderUid, firebaseToken);
            buildNotification(title, message, pendingIntent);
        }
    }

    /**
     * The method is to build and customize appearance of the notification bar.
     * */
    private void buildNotification(String title, String message, PendingIntent pendingIntent) {
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this);
        notificationBuilder.setSmallIcon(R.drawable.logo);
        notificationBuilder.setContentTitle(title);
        notificationBuilder.setContentText(message);
        notificationBuilder.setAutoCancel(true);
        notificationBuilder.setContentIntent(pendingIntent);

        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // build notification
        int mNotificationId = createID();
        mNotificationManager.notify(mNotificationId, notificationBuilder.build());
    }

    /**
     * Set up an intent to go back to chatActivity and passing necessary info to resume the chat.
     * */
    private PendingIntent switchToChatActivity(String receiver, String receiverUid, String firebaseToken) {
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra(Constants.ARG_RECEIVER, receiver);
        intent.putExtra(Constants.ARG_RECEIVER_UID, receiverUid);
        intent.putExtra(Constants.ARG_FIREBASE_TOKEN, firebaseToken);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        return PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT);
    }

    /**
     * Set up an intent to go back to MainViewActivity.
     * */
    private PendingIntent switchToAddFriInterface(String userName, String userEmail,
                                                  String userGender, String userAddress) {
        Intent intent = new Intent(this, AcceptRequestActivity.class);
        intent.putExtra("SenderName", userName);
        intent.putExtra("SenderEmail", userEmail);
        intent.putExtra("SenderGender", userGender);
        intent.putExtra("SenderAddress", userAddress);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        return PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT);
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
    public Intent getIntent() {
        return intent;
    }
}


