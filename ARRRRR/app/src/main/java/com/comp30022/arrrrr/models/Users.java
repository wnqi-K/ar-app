package com.comp30022.arrrrr.models;

import android.location.Address;

/**
 * Created by delaroy on 4/13/17.
 */
public class Users {

    private String emailId;
    public String username;
    public String phoneNum;
    public String gender;
    public String address;
    private String lastMessage;
    private int notifCount;

    public String getEmailId(){ return emailId; }

    public void setEmailId(String emailId ){ this.emailId = emailId; }

    public String getUsername(){ return username; }

    public void setUsername(String username){ this.username = username;}

    public String getPhoneNum(){ return phoneNum;}

    public void setPhoneNum(String phoneNo){ this.phoneNum = phoneNo; }

    public String getGender(){ return gender; }

    public void setGender(String gender){ this.gender = gender; }

    public String getAddress(){ return address; }

    public void setAddress(String address){ this.address = address; }

    public String getLastMessage(){ return lastMessage; }

    public void setLastMessage(){ this.lastMessage = lastMessage; }

    public int getNotifCount(){ return notifCount; }

    public void setNotifCount(int notifCount){ this.notifCount = notifCount; }
}
