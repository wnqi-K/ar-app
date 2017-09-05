package com.comp30022.arrrrr.models;

import java.util.ArrayList;

/**
 * Created by delaroy on 4/13/17.
 */
public class User {
    public String uid;
    public String email;
    public String firebaseToken;
    public ArrayList<String> friendsIDs;

    public User(){

    }

    public User(String uid, String email, String firebaseToken){
        this.uid = uid;
        this.email = email;
        this.firebaseToken = firebaseToken;
    }

    /* by Rondo*/
    public void addFriend(User friend){
        friendsIDs.add(friend.uid);
    }
}
