package com.comp30022.arrrrr.database;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.support.annotation.RestrictTo;
import android.text.TextUtils;
import android.util.Base64;
import android.widget.Toast;

import com.comp30022.arrrrr.R;
import com.comp30022.arrrrr.models.FriendLocation;
import com.comp30022.arrrrr.models.User;
import com.comp30022.arrrrr.utils.Constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * This class is responsible for adding and deleting friends for the current user.
 *
 * @author Wenqiang Kuang, Dafu Ai, Zijie Shen
 */

public class UserManagement {
    private static UserManagement mInstance;

    private List<User> mUserList = new ArrayList<>();
    private List<User> mFriendList =  new ArrayList<>();
    private List<User> mAdminList = new ArrayList<>();
    private User mCurrentUser;
    private HashMap<String, Bitmap> mProfileImages = new HashMap<>();
    private HashMap<String, FriendLocation> mFriendLocations = new HashMap<>();
    private Location mCurrUserLocation = null;

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

    /**
     * Add a friend's location info to the list
     * @param uid friend's uid
     * @param friendLocation location info
     */
    public void addFriendLocation(String uid, FriendLocation friendLocation) {
        mFriendLocations.put(uid, friendLocation);
    }

    /**
     * Retrieve the location by a friend's uid.
     * @param uid friend's uid
     * @return friend's location info
     */
    public FriendLocation getFriendLocation(String uid) {
        return mFriendLocations.get(uid);
    }

    public Location getCurrUserLocation() {
        return mCurrUserLocation;
    }

    public void setCurrUserLocation(Location currUserLocation) {
        this.mCurrUserLocation = currUserLocation;
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
        String imageUrl64;
        User user = getUserByUID(uid);

        if (user == null) {
            return null;
        }

        imageUrl64 = user.getImageUrl();
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
     * Using user id to find this user's FirebaseToken
     * */
    public String getReceiverFirebaseToken(String receiverUid,Context context) {
        User usr = getUserByUID(receiverUid);
        String firebaseToken = null;
        if(usr == null){
            Toast.makeText(context, Constants.GET_TOKEN_ERROR, Toast.LENGTH_SHORT).show();
        }else{
            firebaseToken = usr.getFirebaseToken();
        }
        return firebaseToken;
    }

    /**
     * Using user id to find this user's user name
     * */
    public String getUserName(String receiverUid,Context context){
        User usr = getUserByUID(receiverUid);
        String username = null;
        if(usr == null){
            Toast.makeText(context, Constants.GET_RECEIVER_ERROR, Toast.LENGTH_SHORT).show();
        }else{
            username = usr.getUsername();
        }
        return username;
    }


    /**
     * Retrieve the User object given a user's uid
     * @param uid user's uid
     * @return User object if found; null if not found
     */
    public User getUserByUID(String uid) {
        User userFound = null;
        for (User user: getUserList()) {
            if (user.getUid().equals(uid)) {
                userFound = user;
                break;
            }
        }
        if (getCurrentUser().getUid().equals(uid)) {
            userFound = getCurrentUser();
        }
        return userFound;
    }

    /**
     * Define the standard method to get a user's display name.
     * @param uid user's uid
     * @return display name in String; empty string if uid not found
     */
    public String getUserDisplayName(String uid) {
        User user = getUserByUID(uid);
        if (user == null) {
            return "";
        }
        return user.getEmail();
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