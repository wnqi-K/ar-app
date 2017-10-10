package com.comp30022.arrrrr;

import android.content.Context;
import android.support.annotation.RestrictTo;
import android.util.Log;

import com.comp30022.arrrrr.models.Chat;
import com.comp30022.arrrrr.services.FcmNotificationBuilder;
import com.comp30022.arrrrr.utils.ChatInterface;
import com.comp30022.arrrrr.utils.Constants;
import com.comp30022.arrrrr.utils.PreferencesAccess;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * This class is responsible of managing the process of getting messages
 * and sending messages
 *
 * @author Zijie Shen
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
    private Chat newChat = null;
    private ChatInterface.Listener mListener;

    public Chat getNewChat() {
        return newChat;
    }

    public ChatRoomManager(Context context,
                           String senderUid,
                           String receiverUid,
                           String receiverFirebaseToken,
                           ChatInterface.Listener listener){
        this.mContext = context;
        this.room_type_1 = senderUid + "_" + receiverUid;
        this.room_type_2 = receiverUid + "_" + senderUid;
        this.mReceiverFirebaseToken = receiverFirebaseToken;
        mRef = FirebaseDatabase.getInstance().getReference();
        mListener = listener;

    }

    /**
     * create chat rooms if it does not exist otherwise add message(chat class) to the chat room
     * using time stamp
     * */
    public void sendMessageToFirebaseUser(Chat chat) {
        this.mChat = chat;
        mRef.child(Constants.ARG_CHAT_ROOMS).getRef().
                addListenerForSingleValueEvent(this.mSendValueEventListener);
    }

    /**
     * listener for the action of sending messages and create a new chat room if two users have
     * never communicated with each other before
     * */
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
                    PreferencesAccess.getInstance(mContext).getString(Constants.ARG_FIREBASE_TOKEN),
                    mReceiverFirebaseToken);
            mListener.onSendMessageSuccess();
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            mListener.onSendMessageFailure(databaseError.getMessage());
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

    /**
     * listener for getting all the messages between two users
     * */
    private ValueEventListener mGetValueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {

            // get chat class from chat rooms
            if (dataSnapshot.hasChild(room_type_1)) {
                Log.e(TAG, LOG_INFO_3 + room_type_1 + LOG_INFO_2);
                mRef.child(Constants.ARG_CHAT_ROOMS)
                        .child(room_type_1).addChildEventListener(mChidEvenListener);
            } else if (dataSnapshot.hasChild(room_type_2)) {
                Log.e(TAG, LOG_INFO_3 + room_type_2 + LOG_INFO_2);
                mRef.child(Constants.ARG_CHAT_ROOMS)
                        .child(room_type_2).addChildEventListener(mChidEvenListener);
            } else {
                // if chat room doesnt exist, print error
                Log.e(TAG, LOG_INFO_4);
            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            mListener.onGetMessagesFailure(databaseError.getMessage());
        }
    };

    /**
     * listener for any new message that has been sent and update chat
     * UI interface
     * */
    private ChildEventListener mChidEvenListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            newChat = dataSnapshot.getValue(Chat.class);
            mListener.onGetMessagesSuccess(newChat);
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {}

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {}

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {}

        @Override
        public void onCancelled(DatabaseError databaseError) {
            mListener.onGetMessagesFailure(databaseError.getMessage());
        }
    };

    @RestrictTo(RestrictTo.Scope.TESTS)
    public ValueEventListener getmSendValueEventListener() {
        return mSendValueEventListener;
    }

    @RestrictTo(RestrictTo.Scope.TESTS)
    public ValueEventListener getmGetValueEventListener() {
        return mGetValueEventListener;
    }

    @RestrictTo(RestrictTo.Scope.TESTS)
    public ChildEventListener getmChidEvenListener() {
        return mChidEvenListener;
    }

}
