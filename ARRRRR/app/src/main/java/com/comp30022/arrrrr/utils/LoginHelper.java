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
    /**
     * check validation of email/password input
     * */
    public static boolean validateForm(EditText mEmailField,EditText mPasswordField) {
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



    /**
     * update ui interface after registration
     * */
    public static void updateUI(Activity activity, FirebaseUser user,
                         ProgressDialog mProgressDialog) {
        hideProgressDialog(mProgressDialog);
        if (user != null) {
            MainViewActivity.startActivity(activity);

            //findViewById(R.id.verify_email_button).setEnabled(!user.isEmailVerified());
        } else {
            Log.d("fail", "error getting user");
        }
    }


    public static void showProgressDialog(ProgressDialog mProgressDialog,Context context) {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(context);
            mProgressDialog.setCancelable(false);
            mProgressDialog.setMessage("Loading...");
        }

        mProgressDialog.show();
    }

    public static void hideProgressDialog(ProgressDialog mProgressDialog) {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }
}
