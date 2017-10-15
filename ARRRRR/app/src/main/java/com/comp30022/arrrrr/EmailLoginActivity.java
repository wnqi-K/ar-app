package com.comp30022.arrrrr;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RestrictTo;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.comp30022.arrrrr.utils.Constants;
import com.comp30022.arrrrr.utils.LoginHelper;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.UUID;

/**
 * Login via email account, lead to registration if no account exists.
 *
 * @author Wenqiang Kuang, Dafu Ai
 */
public class EmailLoginActivity extends AppCompatActivity implements View.OnClickListener{
    private static final String TAG = "EmailPassword";
    private static final String PROCESS_DIALOG_MESSAGE = "Loading...";
    public static final String AUTHENTICATION_FAILED = "Authentication failed.";
    public static final String LOGIN_FAILED_OTHER = "Failed to logged in.";
    public static final String LOGGED_IN = "loggedIn";
    public static final String DUPLICATE_LOGIN_MESSAGE = "This account has been logged in.";

    private EditText mEmailField;
    private EditText mPasswordField;
    private ProgressDialog mProgressDialog;
    private FirebaseAuth mAuth;
    private String mFreshBuffer;


    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_login);

        mEmailField = (EditText)findViewById(R.id.field_email);
        mPasswordField = (EditText)findViewById(R.id.field_password);

        findViewById(R.id.email_sign_in_button).setOnClickListener(this);
        findViewById(R.id.email_create_account_button).setOnClickListener(this);
        findViewById(R.id.sign_out_button).setOnClickListener(this);

        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null && mAuth != null) {
            updateUIByLoginSuccess();
        }
    }

    /**
     * Do sign in action with given email and password.
     */
    private void signIn(String email, String password) {
        Log.d(TAG, "signIn:" + email);
        if (!LoginHelper.validateForm(mEmailField,mPasswordField)) {
            return;
        }

        showProgressDialog();
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(mOnValidationCompleteListener);
    }

    /**
     * Handles when a sign in (credential validation) action has completed.
     * BUT the entire login process has not finished yet.
     * We need to check for duplicate login!
     */
    private OnCompleteListener mOnValidationCompleteListener = new OnCompleteListener<AuthResult>() {
        @Override
        public void onComplete(@NonNull final Task<AuthResult> task) {
            if (task.isSuccessful()) {
                Log.d(TAG, "signInWithEmail: Credentials correct, now checking duplicate login...");
                checkLoginStatus();
            }
            else {
                // Does not pass the authentication, display a message to the user.
                Log.w(TAG, "signInWithEmail: Failure", task.getException());
                updateUIByLoginFailure(AUTHENTICATION_FAILED);
            }
        }
    };

    /**
     * Handles when we complete login status check (including adding login status lock)
     */
    private OnCompleteListener mOnLoginProtectionCompleteListener = new OnCompleteListener() {
        @Override
        public void onComplete(@NonNull Task task) {
            if (task.isSuccessful()) {
                Log.v(TAG, "Login status locked has been applied. Login success");
                updateUIByLoginSuccess();
            } else {
                Log.w(TAG, "Failed to update login status lock, forced logging out...");
                updateUIByLoginFailure(LOGIN_FAILED_OTHER);
            }
        }
    };

    private ValueEventListener mLoginStatusValueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            String freshIdentifier = (String) dataSnapshot.child(Constants.ARG_FRESH).getValue();

            if (freshIdentifier.equals(mFreshBuffer)) {
                // Only trust this data if it is fresh
                Boolean loggedIn = dataSnapshot.child(Constants.ARG_LOCK).getValue(Boolean.class);
                if(loggedIn == null || !loggedIn){
                    Log.d(TAG, "No duplicate login status found. Permit to login.");
                    getLoginStatusRef().child(Constants.ARG_LOCK).setValue(true)
                            .addOnCompleteListener(mOnLoginProtectionCompleteListener);
                    Log.d(TAG, "Now updating login security lock.");
                }else{
                    Log.d(TAG, "Another user has already logged in. Login failed.");
                    handleDuplicateLogin();
                }
            } else {
                // If data not fresh, ask for value again
                getLoginStatusRef().addListenerForSingleValueEvent(mLoginStatusValueEventListener);
            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            Log.d(TAG, "Login status check: failed to fetch data. ");

            // This is important! Still sign out user if check fails for other reaseons.
            mAuth.signOut();
            updateUIByLoginFailure(LOGIN_FAILED_OTHER);
        }
    };

    /**
     * Get common database reference to login status value field.
     */
    private DatabaseReference getLoginStatusRef() {
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        return firebaseDatabase.getReference().
                child(Constants.ARG_USERS).child(user.getUid()).child(Constants.ARG_STATUS);

    }

    private OnCompleteListener mOnKeepFreshCompleteListener = new OnCompleteListener() {
        @Override
        public void onComplete(@NonNull Task task) {

            if (task.isSuccessful()) {
                getLoginStatusRef().addListenerForSingleValueEvent(mLoginStatusValueEventListener);
            } else {
                Log.w(TAG, "Failed to use fresh lock data, login failed.");
                updateUIByLoginFailure(LOGIN_FAILED_OTHER);
            }
        }
    };

    /**
     * This method is to check whether or not current Auth user has logged in.
     * if the status attribute is not null, meaning duplicated login. Go back to the Login interface
     * (Main Activity).
     */
    private void checkLoginStatus(){
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();

        final DatabaseReference statusReference = firebaseDatabase.getReference().
                child(Constants.ARG_USERS).child(user.getUid()).child(Constants.ARG_STATUS);

        mFreshBuffer = UUID.randomUUID().toString();

        statusReference.keepSynced(true);
        statusReference.child(Constants.ARG_FRESH)
                .setValue(mFreshBuffer)
                .addOnCompleteListener(mOnKeepFreshCompleteListener);
    }

    /**
     * Update UI after entire login process is complete.
     */
    public void updateUIByLoginSuccess() {
        hideProgressDialog();
        Intent intent = new Intent(this, MainViewActivity.class);
        startActivity(intent);
    }

    /**
     * Update UI when login process fails.
     */
    public void updateUIByLoginFailure(String toastMessage) {
        hideProgressDialog();
        Toast.makeText(this, toastMessage, Toast.LENGTH_SHORT).show();
        findViewById(R.id.email_password_fields).setVisibility(View.VISIBLE);
    }

    /**
     * Force the duplicated user to logout. Go back to the MainActivity and show notification.
     */
    private void handleDuplicateLogin() {
        mAuth.signOut();
        updateUIByLoginFailure(DUPLICATE_LOGIN_MESSAGE);
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.email_create_account_button) {
            RegisterActivity.startActivity(this);
        } else if (i == R.id.email_sign_in_button) {
            signIn(mEmailField.getText().toString(), mPasswordField.getText().toString());
        }
    }

    /**
     * Display the process dialog while authenticating the account.
     */
    public void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setCancelable(false);
            mProgressDialog.setMessage(PROCESS_DIALOG_MESSAGE);
        }
        mProgressDialog.show();
    }

    public void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    public static void startActivity(Context context) {
        Intent intent = new Intent(context, EmailLoginActivity.class);
        context.startActivity(intent);
    }

    @RestrictTo(RestrictTo.Scope.TESTS)
    public void setAuth(FirebaseAuth auth) {
        this.mAuth = auth;
    }

    @RestrictTo(RestrictTo.Scope.TESTS)
    public OnCompleteListener getOnSignInCompleteListener() {
        return mOnValidationCompleteListener;
    }
}
