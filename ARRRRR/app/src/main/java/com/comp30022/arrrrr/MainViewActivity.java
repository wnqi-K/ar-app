package com.comp30022.arrrrr;

import android.app.FragmentManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import com.comp30022.arrrrr.models.User;

/**
 * Main view of the application after user has logged in. This contains three
 * fragments, friends map and settings.
 *
 * @author Wenqiang Kuang
 */

public class MainViewActivity extends AppCompatActivity implements
        MapContainerFragment.OnMapContainerFragmentInteractionListener,
        SettingFragment.OnSettingFragmentInteractionListener,
        UsersFragment.OnListFragmentInteractionListener{

    /**
     *  Users management like getting all user.
     */
    private UsersManagement mUsersManagment;
    private GetAllUsersFromFirebase mGetAllUsersFromFirebase;


    /**
     * Sets OnNavigationItemSelectedListener for the bottom navigation.
     */
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

    /**
     * Switch the current fragment to friends fragment.
     */
    private void switchToFragmentFriends() {
        FragmentManager manager = getFragmentManager();
        manager.beginTransaction().replace(R.id.fragment_container,
                UsersFragment.newInstance()).commit();
    }

    /**
     * Switch the current fragment to maps fragment.
     */
    private void switchToFragmentHome() {
        FragmentManager manager = getFragmentManager();
        manager.beginTransaction().replace(R.id.fragment_container,
                MapContainerFragment.newInstance()).commit();
    }

    /**
     * Switch the current fragment to settings fragment.
     */
    private void switchToFragmentSetting() {
        FragmentManager manager = getFragmentManager();
        manager.beginTransaction().replace(R.id.fragment_container,
                SettingFragment.newInstance()).commit();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_view);

        /*Set up the bottom navigation bar*/
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setSelectedItemId(R.id.navigation_home);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        /* Set the default fragment to be map */
        switchToFragmentHome();

        /* Get all users from database */
        this.mUsersManagment = new UsersManagement();
        this.mGetAllUsersFromFirebase = new GetAllUsersFromFirebase(mUsersManagment);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.action_bar_menu, menu);
        return true;
    }

    @Override
    public void onListFragmentInteraction(User user) {
        // TODO: Probably need to do use this in the future
    }

    @Override
    public void onMapContainerFragmentInteraction(Uri uri) {
        // TODO: Probably need to do use this in the future
    }

    @Override
    public void onSettingFragmentInteraction(Uri uri) {
        // TODO: Probably need to do use this in the future
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // If current fragment is map container, then pass arguments to its onActivityResult
        MapContainerFragment fragment = (MapContainerFragment) getFragmentManager()
                .findFragmentById(R.id.map_container);
        if (fragment != null) {
            fragment.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // If current fragment is map container, then pass arguments to its onRequestPermissionsResult
        MapContainerFragment fragment = (MapContainerFragment) getFragmentManager()
                .findFragmentById(R.id.map_container);
        if (fragment != null) {
            fragment.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    public UsersManagement getmUsersManagment() {
        return mUsersManagment;
    }
}