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
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * Login via email account, lead to registration if no account exists.
 *
 * Created by Wenqiang Kuang on 26/08/2017.
 */
public class EmailLoginActivity extends AppCompatActivity implements View.OnClickListener{
    private static final String TAG = "EmailPassword";
    private static final String PROCESS_DIALOG_MESSAGE = "Loading...";

    private EditText mEmailField;
    private EditText mPasswordField;
    private ProgressDialog mProgressDialog;
    private FirebaseAuth mAuth;
    private Boolean mStatus;

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
        updateUI(currentUser);
    }

    private void signIn(String email, String password) {
        Log.d(TAG, "signIn:" + email);
        if (!LoginHelper.validateForm(mEmailField,mPasswordField)) {
            return;
        }

        showProgressDialog();
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, onSignInCompleteListener);
    }

    private OnCompleteListener onSignInCompleteListener = new OnCompleteListener<AuthResult>() {
        @Override
        public void onComplete(@NonNull final Task<AuthResult> task) {
            if (task.isSuccessful()) {
                Log.d(TAG, "onComplete.");
                FirebaseUser user = mAuth.getCurrentUser();
                updateUI(user);
            }
            else {
                // Does not pass the authentication, display a message to the user.
                Log.w(TAG, "signInWithEmail:failure", task.getException());
                Toast.makeText(EmailLoginActivity.this, "Authentication failed.",
                        Toast.LENGTH_SHORT).show();
                updateUI(null);
            }
            hideProgressDialog();
        }

    };

    private void checkLoginStatus(){
        Log.d(TAG, "Are you here? checking 1");
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        final DatabaseReference statusReference = firebaseDatabase.getReference().
                child(Constants.ARG_USERS).child(user.getUid()).child(Constants.ARG_STATUS);
        Log.d(TAG, "Are you here? checking 2");

        statusReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String status = dataSnapshot.getValue(String.class);
                if(status == null){
                    //mStatus = true;
                    statusReference.setValue("loggedIn");
                    Log.d(TAG, "CAN LOGIN");
                }else{
                    //mStatus = false;
                    Log.d(TAG, "CANNOT LOGIN");
                    duplicateLogin();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
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

    public void updateUI(FirebaseUser user) {
        hideProgressDialog();
        if (user != null) {
            //Toast.makeText(this,user.getUid(),Toast.LENGTH_LONG).show();

            checkLoginStatus();
            /*if(mStatus != null && mStatus == false){
                Log.d(TAG, "enter duplicateLogin");
                duplicateLogin();
                Toast.makeText(this,"This account has been logged in.", Toast.LENGTH_LONG).show();
            }*/

            Intent intent = new Intent(this, MainViewActivity.class);
            startActivity(intent);
        } else {
            findViewById(R.id.email_password_fields).setVisibility(View.VISIBLE);
        }
    }

    private void duplicateLogin() {
        mAuth.signOut();
        Intent intent = new Intent(this, EmailLoginActivity.class);
        startActivity(intent);
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
        return onSignInCompleteListener;
    }
}
