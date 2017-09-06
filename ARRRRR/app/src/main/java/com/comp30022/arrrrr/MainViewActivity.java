package com.comp30022.arrrrr;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.comp30022.arrrrr.models.User;


public class MainViewActivity extends AppCompatActivity implements
        FriendsFragment.OnFragmentInteractionListener,
        MapsFragment.OnFragmentInteractionListener,
        SettingFragment.OnFragmentInteractionListener,
        UsersFragment.OnListFragmentInteractionListener {


    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {

            switch (item.getItemId()) {
                case R.id.navigation_friends:
                    switchToFragmentUsers();
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


    private void switchToFragmentUsers() {
        UserListing();
        FragmentManager manager = getSupportFragmentManager();
        manager.beginTransaction().replace(R.id.fragment_container,
                new UsersFragment().newInstance(1)).commit();
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
        navigation.setSelectedItemId(R.id.navigation_home);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }

    private void UserListing() {
        // set the register screen fragment
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.list,
                UsersFragment.newInstance(1),
                UsersFragment.class.getSimpleName());
        fragmentTransaction.commit();
    }

    @Override
    public void onFragmentInteraction (Uri uri){
    }

    @Override
    public void onListFragmentInteraction(User user) {

    }
}