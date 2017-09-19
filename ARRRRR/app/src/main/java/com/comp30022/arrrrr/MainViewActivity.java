package com.comp30022.arrrrr;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.comp30022.arrrrr.FriendManagement.FriendManagement;
import com.comp30022.arrrrr.FriendManagement.requestFirebaseUsers;
import com.comp30022.arrrrr.database.DatabaseManager;
import com.comp30022.arrrrr.models.User;
import com.comp30022.arrrrr.utils.Constants;
import com.comp30022.arrrrr.utils.SharedPrefUtil;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Main view of the application after user has logged in. This contains three
 * fragments, friends map and settings.
 *
 * Created by Wenqiang Kuang on 1/09/2017.
 */

public class MainViewActivity extends AppCompatActivity implements
        MapContainerFragment.OnMapContainerFragmentInteractionListener,
        SettingFragment.OnSettingFragmentInteractionListener,
        FriendsFragment.OnListFragmentInteractionListener{

    private DatabaseManager mDatabaseManager;
    private requestFirebaseUsers mRequestUsers;
    private FirebaseAuth mAuth;

    public requestFirebaseUsers getRequestUsers() {
        return mRequestUsers;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_view);

        // Set up the bottom navigation bar
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setSelectedItemId(R.id.navigation_home);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        // Set the default fragment to be map
        switchToFragmentHome();

        // add user to database
        mAuth = FirebaseAuth.getInstance();
        addUserToDatabase(mAuth.getCurrentUser());
        // Get all users from database
        mDatabaseManager = new DatabaseManager(this,null,null,1);
        this.mRequestUsers = new requestFirebaseUsers(mDatabaseManager);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.action_bar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.adding_friends:
                addingNewFriend();
                break;
            case R.id.quick_ar_entry:
                quickArEntry();
                break;
        }
        return true;
    }

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
                FriendsFragment.newInstance()).commit();
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

    /**
     * Quickly start the AR mode, to be done.
     */
    private void quickArEntry() {
        Intent intent = new Intent(this, ArViewActivity.class);
        startActivity(intent);
    }

    /**
     * Switch to addingFriendActivity.
     */
    private void addingNewFriend() {
        Intent intent = new Intent(this, AddingNewFriendsActivity.class);
        startActivity(intent);
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

    public DatabaseManager getmDatabaseManager() {
        return mDatabaseManager;
    }


    /**
     * add user to Firebase database
     * */
    private void addUserToDatabase(FirebaseUser firebaseUser){
        String uid = firebaseUser.getUid();
        User user = new User(uid,
                firebaseUser.getEmail(),
                new SharedPrefUtil(this).getString(Constants.ARG_FIREBASE_TOKEN));
        DatabaseReference mRef = FirebaseDatabase.getInstance().getReference();
        mRef.child(Constants.ARG_USERS)
                .child(uid)
                .setValue(user)
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (!task.isSuccessful()) {
                            Toast.makeText(getApplicationContext(),
                                    Constants.ARG_FAILURE, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getApplicationContext(),
                                    Constants.ARG_SUCCESS, Toast.LENGTH_SHORT).show();

                        }
                    }
                });
    }


}