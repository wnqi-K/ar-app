package com.comp30022.arrrrr;

import android.content.Context;
import android.widget.Toast;

import com.comp30022.arrrrr.database.UserManagement;
import com.comp30022.arrrrr.models.User;
import com.comp30022.arrrrr.utils.Constants;

import java.util.ArrayList;

/**
 * This class is used to find info of user using userID
 *
 * Created by Rondo on 10/5/2017.
 */

public class FindUserInfoHelper {

    /**
     * Using user id to find this user's Email
     * */
    public static String getReceiverEmail(String receiverUid,Context context) {
        User usr = getUserUsingID(receiverUid);
        String email = null;
        if(usr == null){
            Toast.makeText(context, Constants.GET_RECEIVER_ERROR, Toast.LENGTH_SHORT).show();
        }else{
            email = usr.getEmail();
        }
        return email;
    }

    /**
     * Using user id to find this user's FirebaseToken
     * */
    public static String getReceiverFirebaseToken(String receiverUid,Context context) {
        User usr = getUserUsingID(receiverUid);
        String firebaseToken = null;
        if(usr == null){
            Toast.makeText(context, Constants.GET_RECEIVER_ERROR, Toast.LENGTH_SHORT).show();
        }else{
            firebaseToken = usr.getFirebaseToken();
        }
        return firebaseToken;
    }

    /**
     * Using user id to find this user's user name
     * */
    public static String getUserName(String receiverUid,Context context){
        User usr = getUserUsingID(receiverUid);
        String username = null;
        if(usr == null){
            Toast.makeText(context, Constants.GET_RECEIVER_ERROR, Toast.LENGTH_SHORT).show();
        }else{
            username = usr.getUsername();
        }
        return username;
    }

    /**
     *  find object User using user id
     * */
    public static User getUserUsingID(String Uid){
        User usr = null;
        UserManagement friendManagement = UserManagement.getInstance();
        ArrayList<User> allUsersList = (ArrayList<User>) friendManagement.getUserList();
        for(User user:allUsersList){
            if(user.getUid().equals(Uid)){
                usr = user;
                break;
            }
        }
        return usr;
    }
}
