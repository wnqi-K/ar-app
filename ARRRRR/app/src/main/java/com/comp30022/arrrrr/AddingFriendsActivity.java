package com.comp30022.arrrrr;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.comp30022.arrrrr.adapters.ListViewAdapter;
import com.comp30022.arrrrr.database.UserManagement;
import com.comp30022.arrrrr.models.User;

import java.util.ArrayList;

/**
 * Adding new friends by searching the precise user email account.
 * Created by Wenqiang Kuang on 16/09/2017.
 */
public class AddingFriendsActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {

    UserManagement mFriendManagement = UserManagement.getInstance();
    ListView mListView;
    CardView mCardView;
    ListViewAdapter mViewAdapter;
    SearchView mSearchView;
    ArrayList<User> allUsers = (ArrayList<User>)mFriendManagement.getUserList();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adding_friends);

        mCardView = (CardView) findViewById(R.id.select_user_card);
        mCardView.setVisibility(View.GONE);
        mListView = (ListView) findViewById(R.id.search_result_list);

        // Pass results to ListViewAdapter Class
        mViewAdapter = new ListViewAdapter(this, allUsers);

        // Binds the Adapter to the ListView
        mListView.setAdapter(mViewAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                User onClickUser = (User)parent.getAdapter().getItem(position);
                mListView.setVisibility(View.GONE);
                mCardView.setVisibility(View.VISIBLE);


                TextView userName = (TextView)mCardView.findViewById(R.id.user_name);
                TextView userEmail = (TextView)mCardView.findViewById(R.id.user_email);
                ImageView userAvatar = (ImageView) mCardView.findViewById(R.id.user_avatar);
                Button addFriend = (Button)mCardView.findViewById(R.id.add_friend_button);

                userName.setText(onClickUser.getUsername());
                userEmail.setText(onClickUser.getEmail());
                userAvatar.setImageDrawable(getResources().getDrawable(R.drawable.avatar));
                addFriend.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(getBaseContext(), "Friend request has been sent, " +
                                "please wait for confirmation. ", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        // Locate the EditText in listview_main.xml
        mSearchView = (SearchView) findViewById(R.id.search_view);
        mSearchView.setOnQueryTextListener(this);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        String text = newText;
        mViewAdapter.filter(text);
        return false;
    }
}
