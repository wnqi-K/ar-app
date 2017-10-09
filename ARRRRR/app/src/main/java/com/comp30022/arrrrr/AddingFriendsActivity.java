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
import com.comp30022.arrrrr.services.FcmNotificationBuilder;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Adding new friends by searching the precise user email account.
 * Created by Wenqiang Kuang on 16/09/2017.
 */
public class AddingFriendsActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {
 
    private ListView mListView;
    private CardView mCardView;
    private ListViewAdapter mViewAdapter;
    private SearchView mSearchView;
    private UserManagement mUserManagement = UserManagement.getInstance();
    private ArrayList<User> allUsers = (ArrayList<User>)mUserManagement.getUserList();
    private HashMap<String, String> allInfo = null;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adding_friends);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

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
                User currentUser = mUserManagement.getCurrentUser();
                allInfo = getNotificationMessage(onClickUser, currentUser);

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
                        sendRequest(allInfo);
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

/** 
* The method is to retrieve all the necessary notification message from current user and the receiver. 
*/
    private HashMap<String, String> getNotificationMessage(User onClickUser, User currentUser) {
        HashMap<String, String> info = new HashMap<>();
        info.put("email", currentUser.getEmail());
        info.put("message", "Sends you a friend request. ");
        info.put("uid", currentUser.getUid());
        info.put("senderToken", currentUser.getFirebaseToken());
        info.put("receiverToken", onClickUser.getFirebaseToken());
        return info;
    }

    /** 
    * The method is to notification message to receiver given all the info. 
    */
    private void sendRequest(HashMap<String, String> allInfo) {
        String email = allInfo.get("email");
        String message = allInfo.get("message");
        String uid = allInfo.get("uid");
        String senderToken = allInfo.get("senderToken");
        String receiverToken = allInfo.get("receiverToken");

        sendRequestToReceiver(email, message, uid, senderToken, receiverToken);
    }

    /** 
    * The method is to build notification and send. 
    */
    private void sendRequestToReceiver(String email,
                                                String message,
                                                String uid,
                                                String firebaseToken,
                                                String receiverFirebaseToken) {
        FcmNotificationBuilder.initialize()
                .title(email)
                .message(message)
                .username(email)
                .uid(uid)
                .firebaseToken(firebaseToken)
                .receiverFirebaseToken(receiverFirebaseToken)
                .send();
    }

    /**
     * Going back the the previous interface.
     */
    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return true;
    }
}
