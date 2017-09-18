package com.comp30022.arrrrr.FriendManagement;

import android.text.TextUtils;

import com.comp30022.arrrrr.models.User;
import com.comp30022.arrrrr.utils.Constants;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * This function takes a friendManagement object as input and adding users to the object.
 * To get the friend list, using getFriendManagement.getFriendList.
 * This is also responsible for searching users from the firebase database.
 * Created by Wenqiang Kuang on 9/17/17.
 */

public class requestFirebaseUsers {
    private FriendManagement mFriendManagement;

    public FriendManagement getFriendManagement() {
        return mFriendManagement;
    }

    public requestFirebaseUsers(FriendManagement friendManagement){
        mFriendManagement = friendManagement;
        init();
    }

    public void init() {
        FirebaseDatabase.getInstance().getReference().child(Constants.ARG_USERS).addListenerForSingleValueEvent(new ValueEventListener() {
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
                    if (!TextUtils.equals(user.uid, FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                        users.add(user);
                    }
                }
                mFriendManagement.addingAllUsers(users);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                mFriendManagement.getUsersUnsuccessfully(databaseError.getMessage());
            }
        });
    }
}
