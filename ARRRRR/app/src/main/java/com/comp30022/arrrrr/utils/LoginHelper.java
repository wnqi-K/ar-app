package com.comp30022.arrrrr.utils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.widget.EditText;

import com.comp30022.arrrrr.MainViewActivity;
import com.google.firebase.auth.FirebaseUser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Rondo on 9/19/2017.
 */

public class LoginHelper {

    private final static int PASSWORD_LEN = 6;
    private final static int USER_NAME_LEN = 8;
    private final static int PHONE_NUM_LEN = 10;
    /**
     * check validation of email/password input
     * */
    public static boolean validateForm(EditText mEmailField,
                                       EditText mPasswordField) {
        boolean valid = true;

        String email = mEmailField.getText().toString();
        if (TextUtils.isEmpty(email)) {
            mEmailField.setError("Required.");
            valid = false;
        } else if (!isValidEmailAddress(email)){
            valid = false;
        } else {
            mEmailField.setError(null);
        }

        String password = mPasswordField.getText().toString();
        if (TextUtils.isEmpty(password)) {
            mPasswordField.setError("Required.");
            valid = false;
        } else if (password.length() < PASSWORD_LEN){
            mPasswordField.setError("Minimum 6 digits.");
            valid = false;
        } else {
            mPasswordField.setError(null);
        }

        return valid;
    }

    /**
     * check validation of email/password/username/phone number/address input
     * */
    public static boolean validateForm2(EditText mEmailField,
                                       EditText mPasswordField,
                                       EditText mUsernameField,
                                       EditText mPhoneNumField,
                                       EditText mAddressField) {
        boolean valid = true;
        valid = validateForm(mEmailField,mPasswordField);

        String username = mUsernameField.getText().toString();
        if (TextUtils.isEmpty(username)) {
            mUsernameField.setError("Required.");
            valid = false;
        } else if (!isValidUsername(username)){
            mUsernameField.setError("Invalid Username(maximum 8 digits without empty space)");
            valid = false;
        } else {
            mUsernameField.setError(null);
        }

        String phoneNum = mPhoneNumField.getText().toString();
        if (TextUtils.isEmpty(phoneNum)) {
            mPhoneNumField.setError("Required.");
            valid = false;
        } else if (!isValidPhoneNum(phoneNum)){
            mPhoneNumField.setError("Invalid Phone Number");
            valid = false;
        }else {
            mPhoneNumField.setError(null);
        }

        String address = mAddressField.getText().toString();
        if (TextUtils.isEmpty(address)) {
            mAddressField.setError("Required.");
            valid = false;
        } else {
            mAddressField.setError(null);
        }

        return valid;
    }

    /**
     * phone number validation
     * */
    private static boolean isValidPhoneNum(String phoneNum) {
        boolean valid = true;
        if(phoneNum.length() != PHONE_NUM_LEN){
            valid = false;
        }
        return valid;
    }

    /**
     * username validation, a string with maximum 8 digits without empty space
     * */
    private static boolean isValidUsername(String username) {
        boolean valid = true;
        if(username.length() > USER_NAME_LEN){
            valid = false;
        }else{
            //check white space
            for(int i = 0;i < username.length() && valid; i++){
                if(Character.isWhitespace(username.charAt(i))){
                    valid = false;
                }
            }
        }
        return valid;
    }

    /**
     * email validation
     * */
    private static boolean isValidEmailAddress(String email) {
        boolean result = true;
        if (null != email) {
            String regex = "^([_a-zA-Z0-9-]+(\\.[_a-zA-Z0-9-]+)*@[a-zA-Z0-9-]" +
                    "+(\\.[a-zA-Z0-9-]+)*(\\.[a-zA-Z]{1,6}))?$";
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(email);
            if (!matcher.matches()) {
                result = false;
            }
        }
        return result;
    }
}
