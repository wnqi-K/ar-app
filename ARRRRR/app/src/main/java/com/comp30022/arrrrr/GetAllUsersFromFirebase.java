package com.comp30022.arrrrr;

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
 * Created by rondo on 9/12/17.
 */

public class GetAllUsersFromFirebase {
    public UsersManagement mUsersManagement;

    public GetAllUsersFromFirebase(UsersManagement usersManagement){
        mUsersManagement = usersManagement;
        init();
    }

    public void init() {
        FirebaseDatabase.getInstance().getReference().child(Constants.ARG_USERS).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                // get all the children of users from database
                Iterator<DataSnapshot> dataSnapshots = dataSnapshot.getChildren().iterator();

                //an arrayList stores users
                List<User> users = new ArrayList<User>();

                // iterate each child
                while (dataSnapshots.hasNext()) {
                    DataSnapshot dataSnapshotChild = dataSnapshots.next();
                    User user = dataSnapshotChild.getValue(User.class);
                    if (!TextUtils.equals(user.uid, FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                        users.add(user);
                    }
                }

                mUsersManagement.getUsersSuccessfully(users);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                mUsersManagement.getUsersUnsuccessfully(databaseError.getMessage());
            }
        });
    }
}
