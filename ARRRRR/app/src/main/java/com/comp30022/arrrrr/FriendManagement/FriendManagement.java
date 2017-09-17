package com.comp30022.arrrrr.FriendManagement;

import com.comp30022.arrrrr.models.User;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is responsible for adding and deleting friends for the current user.
 * Created by Wenqiang Kuang on 17/09/2017.
 */

public class FriendManagement {
    public List<User> mFriendList =  new ArrayList<>();

    // required empty constructor.
    public FriendManagement(){}

    // adding all users from firebase database.
    public void addingAllUsers(List<User> users){
        for(User user:users){
            this.mFriendList.add(user);
        }
    }

    public void getUsersUnsuccessfully(String message){}

    // getter for all friends.
    public List<User> getFriendList() {
        return mFriendList;
    }
}
