package com.comp30022.arrrrr.FriendManagement;

import android.text.TextUtils;
import android.util.Log;

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
 * To get the friend list, using getFriendManagement.getFriendList.
 * This is also responsible for searching users from the firebase database.
 * Created by Wenqiang Kuang on 9/17/17.
 */

public class requestFirebaseUsers {
    private FriendManagement mFriendManagement;
    private FriendManagement mAdminFriends;
    private DatabaseReference userlistReference;

    public FriendManagement getFriendManagement() {
        return mFriendManagement;
    }

    public FriendManagement getAdminFriends() {
        return mAdminFriends;
    }

    public requestFirebaseUsers(FriendManagement friendManagement, FriendManagement adminFriends){
        mFriendManagement = friendManagement;
        mAdminFriends = adminFriends;
        init();
    }

    public void init() {
        userlistReference = FirebaseDatabase.getInstance().getReference().child(Constants.ARG_USERS);
        userlistReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // get all the children of users from database
                Iterator<DataSnapshot> dataSnapshots = dataSnapshot.getChildren().iterator();
                //an arrayList stores users
                List<User> users = new ArrayList<>();
                List<User> admins = new ArrayList<>();

                // iterate each child
                while (dataSnapshots.hasNext()) {
                    DataSnapshot dataSnapshotChild = dataSnapshots.next();
                    User user = dataSnapshotChild.getValue(User.class);
                    if (!TextUtils.equals(user.getUid(), FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                        if ((user.getAdmin() != null)&&(TextUtils.equals(user.getAdmin(), "True"))) {
                            admins.add(user);
                            //Log.d("printf","adminssssssssss.");
                        }
                        users.add(user);
                        //Log.d("printf","Normals.");
                    }
                }
                mFriendManagement.addingAllUsers(users);
                mAdminFriends.addingAllUsers(admins);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                mFriendManagement.getUsersUnsuccessfully(databaseError.getMessage());
                mAdminFriends.getUsersUnsuccessfully(databaseError.getMessage());
            }
        });
    }
}
