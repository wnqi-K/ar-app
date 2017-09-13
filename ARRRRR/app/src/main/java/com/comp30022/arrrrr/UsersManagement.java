package com.comp30022.arrrrr;

import com.comp30022.arrrrr.models.User;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by rondo on 9/12/17.
 */

class UsersManagement {
    public List<User> mUsers =  new ArrayList<User>();

    public UsersManagement(){}

    public void getUsersSuccessfully(List<User> users){
        for(User user:users){
            this.mUsers.add(user);
        }
    }

    public void getUsersUnsuccessfully(String message){

    }


    public List<User> getAllUsers() {
        return mUsers;
    }

}
