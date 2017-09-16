package com.comp30022.arrrrr.models;

import java.util.ArrayList;

/**
 * Created by delaroy on 4/13/17.
 */
public class User {
    public String uid;
    public String email;
    public String firebaseToken;
    public String username;
    public String phoneNum;
    public String gender;
    public String address;
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

    public String getUsername(){ return username; }

    public void setUsername(String username){ this.username = username;}

    public String getPhoneNum(){ return phoneNum;}

    public void setPhoneNum(String phoneNo){ this.phoneNum = phoneNo; }

    public String getGender(){ return gender; }

    public void setGender(String gender){ this.gender = gender; }

    public String getAddress(){ return address; }

    public void setAddress(String address){ this.address = address; }
}
