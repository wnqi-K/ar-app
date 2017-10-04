package com.comp30022.arrrrr.database;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.RestrictTo;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import com.comp30022.arrrrr.R;
import com.comp30022.arrrrr.models.User;
import com.google.firebase.auth.FirebaseAuth;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
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
    private HashMap<String, Bitmap> mProfileImages = new HashMap<>();

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
        return false;
    }

    /**
     * Retrieve user profile image. Lazy loading.
     * @param uid user's uid
     * @return profile image in {@link Bitmap}
     */
    public Bitmap getUserProfileImage(String uid, Context context) {
        String imageUrl64 = null;

        // Retrieve url first
        for (User user : getUserList()) {
            if (user.getUid().equals(uid)) {
                imageUrl64 = user.getImageUrl();
            }
        }

        // Also checks the uid is the current user
        if (getCurrentUser().getUid().equals(uid)) {
            imageUrl64 = getCurrentUser().getImageUrl();
        }

        if (imageUrl64 == null) {
            // User does not have profile image, so return default profile image
            return BitmapFactory.decodeResource(context.getResources(),
                                                             R.drawable.portrait_photo);
        }

        // Return loaded profile image
        if (mProfileImages.containsKey(uid)) {
            return mProfileImages.get(uid);
        }

        // Load profile image now
        try {
            byte[] decodedByteArray = Base64.decode(imageUrl64, Base64.DEFAULT);
            Bitmap img = BitmapFactory.decodeByteArray(decodedByteArray, 0, decodedByteArray.length);
            // Save image
            mProfileImages.put(uid, img);
            return img;
        } catch (Exception e) {
            // Return null if decoding fails.
            return null;
        }
    }

    /**
     * FOR TEST ONLY.
     * Check whether a user exists using its uid
     * @param uid user's uid
     */
    @RestrictTo(RestrictTo.Scope.TESTS)
    public boolean isUserExist(String uid) {
        for (User user : getUserList()) {
            if (user.getUid().equals(uid)) {
                return true;
            }
        }
        return false;
    }

    /**
     * FOR TEST ONLY.
     * Add user to the local user list
     * @param user new user
     */
    @RestrictTo(RestrictTo.Scope.TESTS)
    public void addUser(User user) {
        if (getCurrentUser().getUid().equals(user.getUid()) || isUserExist(user.getUid())) {
            return;
        }
        if (user.getAdmin().equals("True")) {
            mAdminList.add(user);
        }
        mUserList.add(user);
    }

}
