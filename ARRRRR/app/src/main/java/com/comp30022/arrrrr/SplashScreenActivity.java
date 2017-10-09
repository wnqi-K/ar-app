package com.comp30022.arrrrr;

import android.support.v7.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;

public class SplashScreenActivity extends AppCompatActivity {

    private final int SPLASH_DISPLAY_LENGTH = 3000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gifview);

        //hide Action bar in Login Activity
        getSupportActionBar().hide();

        Thread timerThread = new Thread() {
            public void run() {
                try {
                    sleep(SPLASH_DISPLAY_LENGTH);     //set the duration of splash screen
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    Intent intent = new Intent(SplashScreenActivity.this, MainActivity.class);
                    startActivity(intent);
                }
            }
        };
        timerThread.start();
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        finish();
    }

}
