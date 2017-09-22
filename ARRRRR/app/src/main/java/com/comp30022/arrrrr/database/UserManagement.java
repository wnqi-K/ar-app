package com.comp30022.arrrrr.database;

import android.text.TextUtils;
import android.util.Log;

import com.comp30022.arrrrr.models.User;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is responsible for adding and deleting friends for the current user.
 * Created by Wenqiang Kuang on 17/09/2017.
 */

public class UserManagement {
    private static UserManagement instance;
    private List<User> mFriendList =  new ArrayList<>();
    private List<User> mAdminList = new ArrayList<>();

    // required empty constructor.
    private UserManagement(){}

    public static synchronized UserManagement getInstance(){
        if(instance == null){
            instance = new UserManagement();
        }
        return instance;
    }

    // adding all users from firebase database.
    public void addingAllUsers(List<User> users){
        for(User user:users) {
            if ((user.getAdmin() != null)&&(TextUtils.equals(user.getAdmin(), "True"))) {
                mAdminList.add(user);
            }
            mFriendList.add(user);
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