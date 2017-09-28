package com.comp30022.arrrrr.database;

import android.text.TextUtils;
import android.util.Log;

import com.comp30022.arrrrr.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

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
        updateFriendList();
        readAllUsers();
    }

    /**
     * This method queries all admin users' information and other users' information in the
     * firebase database "users" table.
     */
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
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("Firebase Error", databaseError.toException().toString());
            }
        });
    }

    /**
     * This method gets current user's ID and query its friend list in the firebase database
     * "friends" table.
     */
    private void updateFriendList() {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        String currentUserID = firebaseAuth.getCurrentUser().getUid();

        DatabaseReference userListReference = mDatabase.getReference();

        //Listen for changes in current user's friend info updates.
        Query query = userListReference.child("friends").child(currentUserID);
        Log.d("Fatal", "on updating friend list. " + "current user id is " + currentUserID);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<String> friendsID = new ArrayList<>();
                for (DataSnapshot snapshot: dataSnapshot.getChildren()) {
                    //Friend friend = snapshot.getValue(Friend.class);
                    String uid = snapshot.getKey();
                    friendsID.add(uid);
                    Log.d("fatal", "called once. " + uid);
                }
                convertToFriendList(friendsID);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("Firebase Error", databaseError.toException().toString());
            }
        });

    }

    /**
     * This method takes a list of friends' ID as input and query them in the firebase database,
     * the list of friends would be stored in an Arraylist of user. This would substitute friend
     * list in the UserManagement.
     */
    private void convertToFriendList(ArrayList<String> friendsIdList){
        DatabaseReference userListReference = mDatabase.getReference();
        final ArrayList<User> friendList = new ArrayList<>();

        for(String friendID : friendsIdList){
            Query query = userListReference.child("users").child(friendID);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    User user = dataSnapshot.getValue(User.class);
                    friendList.add(user);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.d("Firebase Error", databaseError.toException().toString());
                }
            });
        }
        mFriendManagement.setFriendList(friendList);
    }

    /**
     * This method queries all the users of the app and would support the adding new friend feature.
     * List of candidate friends would be displayed.
     */
    private void readAllUsers(){
        DatabaseReference userListReference = mDatabase.getReference();
        Query query = userListReference.child("users");
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<User> allUsers = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    User user = snapshot.getValue(User.class);
                    if (!TextUtils.equals(user.getUid(), FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                        allUsers.add(user);
                    }else{
                        mFriendManagement.setCurrentUser(user);
                    }
                }
                mFriendManagement.setUserList(allUsers);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}