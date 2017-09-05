package com.comp30022.arrrrr;

import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.BottomNavigationView.OnNavigationItemSelectedListener;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import java.util.HashMap;

/**
 * The activity that serves as the main view of the app after user has logged in.
 */
public class MainViewActivity extends AppCompatActivity {

    /**
     * Stores all fragments associated with the navigation items.
     */
    private HashMap<Integer, Fragment> mFragments;

    /**
     * Handles when an item is selected. Switch fragment accordingly.
     */
    private OnNavigationItemSelectedListener mOnNavigationItemSelectedListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_view);

        registerAllListeners();
        registerAllFragments();
    }

    /**
     * Register all listeners for this activity.
     */
    private void registerAllListeners() {
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);

        navigation.setOnNavigationItemSelectedListener(new OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switchFragment(item.getItemId());
                return false;
            }
        });
    }

    /**
     * Register a single fragment associated with a navigation id
     */
    private void registerFragment(Integer navId, @IdRes Integer fragId) {
        mFragments.put(navId, getSupportFragmentManager().findFragmentById(fragId));
    }

    /**
     * Register all fragments associated with each navigation button.
     */
    private void registerAllFragments() {
        registerFragment(R.id.navigation, R.id.main_view_map);
        // TODO: Add two other fragments below
    }

    /**
     * Switch the current fragment to another one.
     * @param id the fragement id.
     */
    private void switchFragment(@IdRes int id) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, mFragments.get(id));
        fragmentTransaction.commit();
    }
}
