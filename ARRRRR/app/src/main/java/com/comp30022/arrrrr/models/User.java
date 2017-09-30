package com.comp30022.arrrrr.models;

import java.io.Serializable;

public class User implements Serializable {
    private String uid;
    private String email;
    private String firebaseToken;
    private String username;
    private String phoneNum;
    private String gender;
    private String address;
    private String imageUrl;
    private String admin;

    public User(){}

    public User(String uid, String email, String firebaseToken){
        this.uid = uid;
        this.email = email;
        this.firebaseToken = firebaseToken;
    }

    public User(String uid, String email, String firebaseToken,String username,
                String phoneNum,String gender,String address,String admin){
        this.uid = uid;
        this.email = email;
        this.firebaseToken = firebaseToken;
        this.username = username;
        this.phoneNum = phoneNum;
        this.gender = gender;
        this.address = address;
        this.admin = admin;
    }

    public String getUsername(){ return username; }

    public void setUsername(String username){ this.username = username;}

    public String getPhoneNum(){ return phoneNum;}

    public void setPhoneNum(String phoneNo){ this.phoneNum = phoneNo; }

    public String getGender(){ return gender; }

    public void setGender(String gender){ this.gender = gender; }

    public String getAddress(){ return address; }

    public void setAddress(String address){ this.address = address; }

    public String getImageUrl() { return imageUrl; }

    public String getUid() { return uid; }

    public String getEmail() { return email; }

    public String getFirebaseToken() { return firebaseToken; }

    public String getAdmin() { return admin; }
}
