package com.comp30022.arrrrr;

import android.app.Application;

import com.google.firebase.database.FirebaseDatabase;

/**
 * Application class used to enable the Firebase offline functionality.
 * Created by Ricky_KUANG on 24/09/2017.
 */

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }
}
