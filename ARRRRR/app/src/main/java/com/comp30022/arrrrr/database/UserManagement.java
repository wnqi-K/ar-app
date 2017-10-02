package com.comp30022.arrrrr.database;

import android.text.TextUtils;
import android.util.Log;

import com.comp30022.arrrrr.models.User;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is responsible for adding and deleting friends for the current user.
 * Created by Wenqiang Kuang on 17/09/2017.
 */

public class UserManagement {
    private static UserManagement mInstance;
    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

    private List<User> mUserList = new ArrayList<>();
    private List<User> mFriendList =  new ArrayList<>();
    private List<User> mAdminList = new ArrayList<>();
    private User mCurrentUser;

    // getters and setters.
    public List<User> getFriendList() {
        return mFriendList;
    }

    public List<User> getAdminList(){
        return mAdminList;
    }

    public List<User> getUserList() {
        return mUserList;
    }

    public void setFriendList(List<User> friendList) {
        mFriendList = friendList;
    }

    public void setUserList(List<User> userList) {
        mUserList = userList;
    }

    public User getCurrentUser() {
        return mCurrentUser;
    }

    public void setCurrentUser(User currentUser) {
        mCurrentUser = currentUser;
    }

    private UserManagement(){}

    public static synchronized UserManagement getInstance(){
        if(mInstance == null){
            mInstance = new UserManagement();
        }
        return mInstance;
    }

    // adding new admin user from firebase database.
    public void addingAdminUsers(User user){
        boolean couldAdd = true;
        if (mAdminList.isEmpty()){
            mAdminList.add(user);
        }else{
            for(User users : mAdminList){
                if (TextUtils.equals(users.getUid(), user.getUid())){
                    couldAdd = false;
                }
            }
            if(couldAdd){
                mAdminList.add(user);
            }
        }
    }

    public void getUsersUnsuccessfully(String message){
        Log.d("Firebase", message);
    }

    /**
     * Given a user uid, determines whether this user is a friend of the device user.
     */
    public boolean isUserFriend(String uid) {
        List<User> friends = getFriendList();
        for (User user : friends) {
            if (user.getUid().equals(uid)) {
                return true;
            }
        }
        return true;
    }
}
