package com.comp30022.arrrrr;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;


public class MainViewActivity extends AppCompatActivity implements
        FriendsFragment.OnFragmentInteractionListener,
        MapsFragment.OnFragmentInteractionListener,
        SettingFragment.OnFragmentInteractionListener{


    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {

            switch (item.getItemId()) {
                case R.id.navigation_friends:
                    switchToFragmentFriends();
                    return true;

                case R.id.navigation_home:
                    switchToFragmentHome();
                    return true;

                case R.id.navigation_settings:
                    switchToFragmentSetting();
                    return true;
            }
            return false;
        }
    };


    private void switchToFragmentFriends() {
        FragmentManager manager = getSupportFragmentManager();
        manager.beginTransaction().replace(R.id.fragment_container,
                new FriendsFragment().newInstance("Alice", "Bob")).commit();
    }

    private void switchToFragmentHome() {
        FragmentManager manager = getSupportFragmentManager();
        manager.beginTransaction().replace(R.id.fragment_container,
                new MapsFragment().newInstance("Cindy", "Daisy")).commit();
    }

    private void switchToFragmentSetting() {
        FragmentManager manager = getSupportFragmentManager();
        manager.beginTransaction().replace(R.id.fragment_container,
                new SettingFragment().newInstance("Eddie", "Frank")).commit();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_view);
        switchToFragmentHome();
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        mOnNavigationItemSelectedListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
    }


    @Override
    public void onFragmentInteraction(Uri uri) {
    }
}
