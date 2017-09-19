package com.comp30022.arrrrr;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.comp30022.arrrrr.utils.LoginHelper;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * Login via email account, lead to registration if no account exists.
 *
 * Created by Wenqiang Kuang on 26/08/2017.
 */
public class EmailLoginActivity extends AppCompatActivity implements View.OnClickListener{

    private static final String TAG = "EmailPassword";

    private TextView mStatusTextView;
    private TextView mDetailTextView;
    private EditText mEmailField;
    private EditText mPasswordField;

    private FirebaseAuth mAuth;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_login);

        mStatusTextView = (TextView)findViewById(R.id.status);
        mDetailTextView = (TextView)findViewById(R.id.detail);
        mEmailField = (EditText)findViewById(R.id.field_email);
        mPasswordField = (EditText)findViewById(R.id.field_password);

        findViewById(R.id.email_sign_in_button).setOnClickListener(this);
        findViewById(R.id.email_create_account_button).setOnClickListener(this);
        findViewById(R.id.sign_out_button).setOnClickListener(this);
        //findViewById(R.id.verify_email_button).setOnClickListener(this);

        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        LoginHelper.updateUI(this,currentUser,null);
    }

    private void signIn(String email, String password) {
        Log.d(TAG, "signIn:" + email);
        if (!LoginHelper.validateForm(mEmailField,mPasswordField)) {
            return;
        }

        LoginHelper.showProgressDialog(null,this);

        // [START sign_in_with_email]
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            LoginHelper.updateUI(EmailLoginActivity.this,user, null);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(EmailLoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            LoginHelper.updateUI(EmailLoginActivity.this,null,null);
                        }

                        // [START_EXCLUDE]
                        if (!task.isSuccessful()) {
                            mStatusTextView.setText(R.string.auth_failed);
                        }
                        LoginHelper.hideProgressDialog(null);
                        // [END_EXCLUDE]
                    }
                });
        // [END sign_in_with_email]
    }

/*    private void sendEmailVerification() {
        // Disable button
        findViewById(R.id.verify_email_button).setEnabled(false);

        // Send verification email
        // [START send_email_verification]
        final FirebaseUser user = mAuth.getCurrentUser();
        user.sendEmailVerification()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        // [START_EXCLUDE]
                        // Re-enable button
                        findViewById(R.id.verify_email_button).setEnabled(true);

                        if (task.isSuccessful()) {
                            Toast.makeText(EmailLoginActivity.this,
                                    "Verification email sent to " + user.getEmail(),
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Log.e(TAG, "sendEmailVerification", task.getException());
                            Toast.makeText(EmailLoginActivity.this,
                                    "Failed to send verification email.",
                                    Toast.LENGTH_SHORT).show();
                        }
                        // [END_EXCLUDE]
                    }
                });
        // [END send_email_verification]
    }*/





    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.email_create_account_button) {
            RegisterActivity.startActivity(this);
        } else if (i == R.id.email_sign_in_button) {
            signIn(mEmailField.getText().toString(), mPasswordField.getText().toString());
        }

        /*else if (i == R.id.verify_email_button) {
            sendEmailVerification();
        }*/
    }

}
