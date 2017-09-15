package com.comp30022.arrrrr;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    private static boolean sIsChatActivityOpen = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void emailLogin(View view) {
        Intent intent = new Intent(this, EmailLoginActivity.class);
        startActivity(intent);
    }

    public void googleLogin(View view) {
        Intent intent = new Intent(this, GoogleLoginActivity.class);
        startActivity(intent);
    }

    public static boolean isChatActivityOpen() {
        return sIsChatActivityOpen;
    }

    public static void setChatActivityOpen(boolean isChatActivityOpen) {
        MainActivity.sIsChatActivityOpen = isChatActivityOpen;
    }

//    public void facebookLogin(View view) {
//        Intent intent = new Intent(this, EmailLoginActivity.class);
//        startActivity(intent);
//    }
}
