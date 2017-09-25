package com.comp30022.arrrrr.database;

import android.text.TextUtils;

import com.comp30022.arrrrr.models.Friend;
import com.comp30022.arrrrr.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

/**
 * This function takes a friendManagement object as input and adding users to the object.
 * To get the friend list, using getUserManagement.getFriendList.
 * This is also responsible for searching users from the firebase database.
 * Created by Wenqiang Kuang on 9/17/17.
 */

public class RequestFirebaseUsers {

    private UserManagement mFriendManagement;
    private static FirebaseDatabase mDatabase;

    public static FirebaseDatabase getDatabase() {
        if (mDatabase == null) {
            mDatabase = FirebaseDatabase.getInstance();
        }
        return mDatabase;
    }

    public RequestFirebaseUsers(UserManagement friendManagement){
        mFriendManagement = friendManagement;
        mDatabase = getDatabase();
        loadAdminFriends();
        //updateFriendList();
    }

    private void loadAdminFriends() {
        DatabaseReference userListReference = mDatabase.getReference();

        Query query = userListReference.child("users");
        query.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                User newUser = dataSnapshot.getValue(User.class);
                if (!TextUtils.equals(newUser.getUid(), FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                    if ((newUser.getAdmin() != null)&&(TextUtils.equals(newUser.getAdmin(), "True"))) {
                        mFriendManagement.addingAdminUsers(newUser);
                    }
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {}

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {}

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {}

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }

    public void updateFriendList() {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        String currentUserID = firebaseAuth.getCurrentUser().getUid();

        DatabaseReference userListReference = mDatabase.getReference();

        //Listen for changes in current user's friend info updates.
        Query query = userListReference.child("friends").child(currentUserID);
        query.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Friend newFriend = dataSnapshot.getValue(Friend.class);

                //List<Friend> friends = mFriendManagement.getFriendList();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {}

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {}

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {}

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }
}
