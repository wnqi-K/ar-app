package com.comp30022.arrrrr.models;

import java.util.ArrayList;

public class User {
    private String uid;
    private String email;
    private String firebaseToken;
    private String username;
    private String phoneNum;
    private String gender;
    private String address;
    private ArrayList<String> friendsIDs;
    private String admin;

    public User(){}

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

    public String getUid() { return uid; }

    public String getEmail() { return email; }

    public String getFirebaseToken() { return firebaseToken; }

    public String getAdmin() { return admin; }

    public void setAdmin(String admin) { this.admin = admin; }
}
