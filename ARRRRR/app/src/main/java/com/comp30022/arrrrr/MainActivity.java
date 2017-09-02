package com.comp30022.arrrrr;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);
        Intent intent = new Intent(this, MapsActivity.class);
        startActivity(intent);
    }

    public void emailLogin(View view) {
        Intent intent = new Intent(this, EmailLoginActivity.class);
        startActivity(intent);
    }

    public void googleLogin(View view) {
        Intent intent = new Intent(this, GoogleLoginActivity.class);
        startActivity(intent);
    }

//    public void facebookLogin(View view) {
//        Intent intent = new Intent(this, EmailLoginActivity.class);
//        startActivity(intent);
//    }
}
