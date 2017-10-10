package com.comp30022.arrrrr;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RestrictTo;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.comp30022.arrrrr.models.User;
import com.comp30022.arrrrr.utils.Constants;
import com.comp30022.arrrrr.utils.LoginHelper;
import com.comp30022.arrrrr.utils.PreferencesAccess;
import com.comp30022.arrrrr.utils.SharedPrefUtil;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * This class will perform registration and adding user to Firebase database
 * functionality.
 *
 * @author Zijie Shen
 */
public class RegisterActivity extends AppCompatActivity implements View.OnClickListener{

    private static final String TAG = RegisterActivity.class.getSimpleName();
    private EditText mETxtEmail, mETxtPassword,mETxtPhonenum,
            mETxtUsername,mETxtAddress;
    private Button mBtnRegister;
    private RadioGroup mRadioGroup;
    private ProgressDialog mProgressDialog;
    private FirebaseAuth mAuth;
    private DatabaseReference mRef;
    private String gender = null;
    private CharSequence toast_text;


    public static void startActivity(Context context) {
        Intent intent = new Intent(context, RegisterActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mETxtEmail = (EditText) findViewById(R.id.edit_text_email_id);
        mETxtPassword = (EditText) findViewById(R.id.edit_text_password);
        mETxtUsername = (EditText) findViewById(R.id.edit_text_username);
        mETxtPhonenum = (EditText) findViewById(R.id.edit_text_phonenum);
        mETxtAddress = (EditText) findViewById(R.id.edit_text_address);
        mBtnRegister = (Button) findViewById(R.id.button_register);
        mRadioGroup = (RadioGroup) findViewById(R.id.radioGroup);

        mAuth = FirebaseAuth.getInstance();
        mRef = FirebaseDatabase.getInstance().getReference();

        mBtnRegister.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        int viewId = view.getId();

        switch (viewId) {
            case R.id.button_register:
                onRegister(view);
                break;
        }
    }

    /**
     * peform Firebase Registration and check whether input is valid or not*/
    private void onRegister(View view) {
        String emailId = mETxtEmail.getText().toString();
        String password = mETxtPassword.getText().toString();

        if (mRadioGroup.getCheckedRadioButtonId() == R.id.radioButton_male) {
            this.gender = Constants.ARG_MALE;
        } else if (mRadioGroup.getCheckedRadioButtonId() == R.id.radioButton_female) {
            this.gender = Constants.ARG_FEMALE;
        }

        if (gender == null) {
            toast_text = "gender required";
            Toast.makeText(RegisterActivity.this, toast_text, Toast.LENGTH_SHORT).show();
        } else if (!LoginHelper.validateForm2(mETxtEmail,
                mETxtPassword,
                mETxtUsername,
                mETxtPhonenum,
                mETxtAddress)) {
            // if input is not valid
            toast_text = "Error occurs, please check your input";
            Toast.makeText(RegisterActivity.this, toast_text, Toast.LENGTH_SHORT).show();
        } else {
            performFirebaseRegistration(RegisterActivity.this, emailId, password);
        }
    }

    /**
     * create a Firebase User using email and password
     * */
    private void performFirebaseRegistration(Activity activity,
                                             final String email,
                                             String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(activity, onRegisterCompleteListener);
    }

    /**
     * check whether registration fails or succeeds
     * */
    private OnCompleteListener onRegisterCompleteListener = new OnCompleteListener<AuthResult>() {
        @Override
        public void onComplete(@NonNull Task<AuthResult> task) {
            Log.e(TAG, "performFirebaseRegistration:onComplete:" + task.isSuccessful());
            // If sign in fails, display a message to the user. If sign in succeeds
            // the auth state listener will be notified and logic to handle the
            // signed in user can be handled in the listener.
            if (!task.isSuccessful()) {
                toast_text = Constants.REGISTER_FAILURE;
                Toast.makeText(RegisterActivity.this, toast_text, Toast.LENGTH_SHORT).show();
                //Toast.makeText(getActivity(),task.getException().getMessage(), Toast.LENGTH_SHORT).show();

            } else {
                toast_text = Constants.REGISTER_SUCCESS;
                addUserToDatabase(RegisterActivity.this,task.getResult().getUser(),gender);
            }
        }
    };

    /**
     * add user info to Firebase database
     * */
    private void addUserToDatabase(Activity activity, FirebaseUser firebaseUser, String gender){
        String uid = firebaseUser.getUid();
        User user = User.createUserWithoutAdmin(uid,
                firebaseUser.getEmail(),
                new SharedPrefUtil(activity).getString(Constants.ARG_FIREBASE_TOKEN),
                mETxtUsername.getText().toString(),
                mETxtPhonenum.getText().toString(),
                gender,
                mETxtAddress.getText().toString(),
                null);
        mRef.child(Constants.ARG_USERS)
                .child(uid)
                .setValue(user)
                .addOnCompleteListener(activity,onAddUserToDatabaseCompleteListener);
    }

    /**
     * check whether adding class user to Firebase fails or succeeds
     * */
    private OnCompleteListener onAddUserToDatabaseCompleteListener = new OnCompleteListener<Void>() {
        @Override
        public void onComplete(@NonNull Task<Void> task) {
            if (!task.isSuccessful()) {
                toast_text = Constants.ADD_TO_DATABASE_FAILURE;
                Toast.makeText(RegisterActivity.this,
                        toast_text, Toast.LENGTH_SHORT).show();
            } else {
                toast_text = Constants.ADD_TO_DATABASE_SUCCESS;
                Toast.makeText(RegisterActivity.this,
                        toast_text, Toast.LENGTH_SHORT).show();
                EmailLoginActivity.startActivity(RegisterActivity.this);

            }
        }
    };

    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return true;
    }

    @RestrictTo(RestrictTo.Scope.TESTS)
    public OnCompleteListener getOnRegisterCompleteListener() {
        return onRegisterCompleteListener;
    }

    @RestrictTo(RestrictTo.Scope.TESTS)
    public OnCompleteListener getOnAddUserToDatabaseCompleteListener() {
        return onAddUserToDatabaseCompleteListener;
    }

    @RestrictTo(RestrictTo.Scope.TESTS)
    public void setAuth(FirebaseAuth auth) {
        this.mAuth = auth;
    }

    @RestrictTo(RestrictTo.Scope.TESTS)
    public CharSequence getToastText() {
        return toast_text;
    }
}
