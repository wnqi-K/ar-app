package com.comp30022.arrrrr;

import android.content.Context;
import android.renderscript.Sampler;
import android.util.Log;
import android.widget.Toast;

import com.comp30022.arrrrr.models.Chat;
import com.comp30022.arrrrr.services.FcmNotificationBuilder;
import com.comp30022.arrrrr.utils.Constants;
import com.comp30022.arrrrr.utils.SharedPrefUtil;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * Created by rondo on 10/4/17.
 */

public class ChatRoomManager {

    private static final String TAG = "ChatRoomManager";
    private static final String LOG_INFO_1 = "sendMessageToFirebaseUser: ";
    private static final String LOG_INFO_2 = " exists";
    private static final String LOG_INFO_3 = "getMessageFromFirebaseUser: ";
    private static final String LOG_INFO_4 = "getMessageFromFirebaseUser: no such room available";

    private String room_type_1 = null;
    private String room_type_2 = null;
    private Context mContext;
    private String mReceiverFirebaseToken  = null;
    private DatabaseReference mRef = null;
    private Chat mChat = null;

    public ChatRoomManager(Context context){
        this.mContext = context;
        mRef = FirebaseDatabase.getInstance().getReference();

    }

    public void init(Chat chat, String receiverFirebaseToken){
        this.room_type_1 = chat.senderUid + "_" + chat.receiverUid;
        this.room_type_2 = chat.receiverUid + "_" + chat.senderUid;
        this.mReceiverFirebaseToken = receiverFirebaseToken;
        this.mChat = chat;

    }

    /**
     * create chat rooms if it does not exist otherwise add message(chat class) to the chat room
     * using time stamp
     * */
    public void sendMessageToFirebaseUser() {
        mRef.child(Constants.ARG_CHAT_ROOMS).getRef().
                addListenerForSingleValueEvent(this.mSendValueEventListener);
    }

    private ValueEventListener mSendValueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            if (dataSnapshot.hasChild(room_type_1)) {
                // if chat room exists for senderUid_receiverUid, add chat class
                Log.e(TAG, LOG_INFO_1 + room_type_1 + LOG_INFO_2);
                mRef.child(Constants.ARG_CHAT_ROOMS).child(room_type_1).
                        child(String.valueOf(mChat.timestamp)).setValue(mChat);
            } else if (dataSnapshot.hasChild(room_type_2)) {
                // if chat room exists for receiverUid_senderUid, add chat class
                Log.e(TAG, LOG_INFO_1 + room_type_2 + LOG_INFO_2);
                mRef.child(Constants.ARG_CHAT_ROOMS).child(room_type_2).
                        child(String.valueOf(mChat.timestamp)).setValue(mChat);
            } else {
                // if chat room doesnt exist create one using senderUid_receiverUid,
                // then add chat class
                Log.e(TAG, LOG_INFO_1 + Constants.ARG_SUCCESS);
                mRef.child(Constants.ARG_CHAT_ROOMS).child(room_type_1).
                        child(String.valueOf(mChat.timestamp)).setValue(mChat);
                getMessageFromFirebaseUser();
            }
            // send push notification to the receiver
            sendPushNotificationToReceiver(mChat.sender,
                    mChat.message,
                    mChat.senderUid,
                    new SharedPrefUtil(mContext).getString(Constants.ARG_FIREBASE_TOKEN),
                    mReceiverFirebaseToken);
            Toast.makeText(mContext, Constants.ARG_SUCCESS, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            Toast.makeText(mContext, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
        }
    };


    /**
     * send notification to another user via Firebase
     * */
    private void sendPushNotificationToReceiver(String username,
                                                String message,
                                                String uid,
                                                String firebaseToken,
                                                String receiverFirebaseToken) {
        FcmNotificationBuilder.initialize()
                .title(username)
                .message(message)
                .username(username)
                .uid(uid)
                .firebaseToken(firebaseToken)
                .receiverFirebaseToken(receiverFirebaseToken)
                .send();
    }

    /**
     * retrieve message from chat rooms
     * */
    public void getMessageFromFirebaseUser() {
        mRef.child(Constants.ARG_CHAT_ROOMS).getRef().
                addListenerForSingleValueEvent(mGetValueEventListener);
    }

    private ValueEventListener mGetValueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            // get chat class from chat rooms
            if (dataSnapshot.hasChild(room_type_1)) {
                Log.e(TAG, LOG_INFO_3 + room_type_1 + LOG_INFO_2);
                FirebaseDatabase.getInstance()
                        .getReference()
                        .child(Constants.ARG_CHAT_ROOMS)
                        .child(room_type_1).addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        Chat chat = dataSnapshot.getValue(Chat.class);
                        Toast.makeText(mContext, Constants.ARG_SUCCESS, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {}

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {}

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {}

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText(mContext, databaseError.getMessage(), Toast.LENGTH_SHORT)
                                .show();
                    }
                });
            } else if (dataSnapshot.hasChild(room_type_2)) {
                Log.e(TAG, LOG_INFO_3 + room_type_2 + LOG_INFO_2);
                FirebaseDatabase.getInstance()
                        .getReference()
                        .child(Constants.ARG_CHAT_ROOMS)
                        .child(room_type_2).addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        Chat chat = dataSnapshot.getValue(Chat.class);
                        Toast.makeText(mContext, Constants.ARG_SUCCESS, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {}

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {}

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {}

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText(mContext, databaseError.getMessage(), Toast.LENGTH_SHORT)
                                .show();                    }
                });
            } else {
                // if chat room doesnt exist, print error
                Log.e(TAG, LOG_INFO_4);
            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            Toast.makeText(mContext, databaseError.getMessage(), Toast.LENGTH_SHORT)
                    .show();
        }
    };

}
