package com.comp30022.arrrrr;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    private Button mBtnMap = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mBtnMap = (Button)findViewById(R.id.activity_main_btn_map);
        mBtnMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(view.getContext(), MapsActivity.class));
            }
        });
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
