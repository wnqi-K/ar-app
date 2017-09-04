package com.comp30022.arrrrr;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.TextView;

import com.comp30022.arrrrr.models.User;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.comp30022.arrrrr.UsersFragment.OnListFragmentInteractionListener;

public class MainViewActivity extends AppCompatActivity implements UsersFragment.OnListFragmentInteractionListener {

    private TextView mTextMessage;



    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_friends:
                    UserListing();
                    //UserListingActivity.startActivity(MainViewActivity.this);
                    return true;
                case R.id.navigation_home:
                    mTextMessage.setText(R.string.title_home);
                    return true;
                case R.id.navigation_settings:
                    mTextMessage.setText(R.string.title_settings);
                    return true;
            }
            return false;
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_view);

        mTextMessage = (TextView) findViewById(R.id.message);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
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
    public void onListFragmentInteraction(User user) {

    }


}
