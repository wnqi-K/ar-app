package com.comp30022.arrrrr.database;

import android.text.TextUtils;

import com.comp30022.arrrrr.models.User;
import com.comp30022.arrrrr.utils.Constants;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * This function takes a friendManagement object as input and adding users to the object.
 * To get the friend list, using getUserManagement.getFriendList.
 * This is also responsible for searching users from the firebase database.
 * Created by Wenqiang Kuang on 9/17/17.
 */

public class RequestFirebaseUsers {

    private UserManagement mFriendManagement;
    private static FirebaseDatabase mDatabase;
    private DatabaseReference userlistReference;

    public static FirebaseDatabase getDatabase() {
        if (mDatabase == null) {
            mDatabase = FirebaseDatabase.getInstance();
            //mDatabase.setPersistenceEnabled(true);
        }
        return mDatabase;
    }

    public RequestFirebaseUsers(UserManagement friendManagement){
        mFriendManagement = friendManagement;
        mDatabase = getDatabase();
        updateUserList();
    }

    public void updateUserList() {
        userlistReference = mDatabase.getReference().child(Constants.ARG_USERS);
        userlistReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // get all the children of users from database
                Iterator<DataSnapshot> dataSnapshots = dataSnapshot.getChildren().iterator();
                //an arrayList stores users
                List<User> users = new ArrayList<>();

                // iterate each child
                while (dataSnapshots.hasNext()) {
                    DataSnapshot dataSnapshotChild = dataSnapshots.next();
                    User user = dataSnapshotChild.getValue(User.class);
                    if (!TextUtils.equals(user.getUid(), FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                        users.add(user);
                    }
                }
                mFriendManagement.addingAllUsers(users);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                mFriendManagement.getUsersUnsuccessfully("Fail to fetch user data.");
            }
        });
    }
}
