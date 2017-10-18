package com.comp30022.arrrrr;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * The logIn page for the app, providing three login option, email account
 * google and facebook account.
 *
 * Created by Wenqiang Kuang on 26/08/2017.
 */

public class MainActivity extends AppCompatActivity{
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //hide Action bar in Login Activity
        getSupportActionBar().hide();
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null && mAuth != null) {
            updateUIWithoutChecking();
        }
    }

    public void emailLogin(View view) {
        Intent intent = new Intent(this, EmailLoginActivity.class);
        startActivity(intent);
    }

    /*public void googleLogin(View view) {
        Intent intent = new Intent(this, GoogleLoginActivity.class);
        startActivity(intent);
    }*/

   /*public void facebookLogin(View view) {
        Intent intent = new Intent(this, EmailLoginActivity.class);
        startActivity(intent);
   }*/

    /**
     * This method is Update the interface directly using current mAuth.
     */
    private void updateUIWithoutChecking() {
        Intent intent = new Intent(this, MainViewActivity.class);
        startActivity(intent);
    }
}
