package com.comp30022.arrrrr.database;

import android.text.TextUtils;
import android.util.Log;

import com.comp30022.arrrrr.models.Friend;
import com.comp30022.arrrrr.models.User;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is responsible for adding and deleting friends for the current user.
 * Created by Wenqiang Kuang on 17/09/2017.
 */

public class UserManagement {
    private static UserManagement instance;
    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private List<Friend> mFriendObjectList;

    private List<User> mFriendList =  new ArrayList<>();
    private List<User> mAdminList = new ArrayList<>();

    private UserManagement(){}

    public static synchronized UserManagement getInstance(){
        if(instance == null){
            instance = new UserManagement();
        }
        return instance;
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

    // getter for all friends.
    public List<User> getFriendList() {
        return mFriendList;
    }

    public List<User> getAdminList(){
        return mAdminList;
    }
}