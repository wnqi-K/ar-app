package com.comp30022.arrrrr;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import com.comp30022.arrrrr.utils.Constants;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Adding new friends by searching the precise user email account.
 * Created by Wenqiang Kuang on 16/09/2017.
 */
public class AddingFriendsActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {

    public static final String PROMPT_MESSAGE = "Friend request has been sent, please wait for confirmation.";
    private ListView mListView;
    private CardView mCardView;
    private ListViewAdapter mViewAdapter;
    private SearchView mSearchView;
    private UserManagement mUserManagement = UserManagement.getInstance();
    private ArrayList<User> allUsers = (ArrayList<User>) mUserManagement.getUserList();
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
                User onClickUser = (User) parent.getAdapter().getItem(position);
                User currentUser = mUserManagement.getCurrentUser();
                allInfo = getNotificationMessage(onClickUser, currentUser);

                mListView.setVisibility(View.GONE);
                mCardView.setVisibility(View.VISIBLE);

                TextView userName = (TextView) mCardView.findViewById(R.id.user_name);
                TextView userEmail = (TextView) mCardView.findViewById(R.id.user_email);
                TextView userGender = (TextView) mCardView.findViewById(R.id.user_gender);
                TextView userAddress = (TextView) mCardView.findViewById(R.id.user_address);

                ImageView userAvatar = (ImageView) mCardView.findViewById(R.id.friend_list_user_avatar);
                Button addFriend = (Button) mCardView.findViewById(R.id.add_friend_button);

                userName.setText(Constants.NAME_PREFIX + onClickUser.getUsername());
                userEmail.setText(Constants.EMAIL_PREFIX + onClickUser.getEmail());
                userGender.setText(Constants.GENDER_PREFIX + onClickUser.getGender());
                userAddress.setText(Constants.ADDRESS_PREFIX + onClickUser.getAddress());

                String userID = onClickUser.getUid();
                Bitmap avatar;
                try {
                    avatar = UserManagement.getInstance().getUserProfileImage(userID, getBaseContext());
                } catch (Exception e) {
                    avatar = BitmapFactory.decodeResource(getResources(), R.drawable.portrait_photo);
                }

                userAvatar.setImageBitmap(avatar);
                addFriend.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        sendRequest(allInfo);
                        Toast.makeText(getBaseContext(), PROMPT_MESSAGE, Toast.LENGTH_SHORT).show();
                        goBackToMainView();
                    }
                });
            }
        });

        // Locate the EditText in list_view_main.xml
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
        mViewAdapter.getFilter().filter(text);
        return false;
    }

    /**
     * After sending request, go back to the main view.
     */
    private void goBackToMainView() {
        Intent intent = new Intent(this, MainViewActivity.class);
        startActivity(intent);
        finish();
    }

    /**
     * The method is to retrieve all the necessary notification message from current user
     * and the receiver.
     */
    private HashMap<String, String> getNotificationMessage(User onClickUser, User currentUser) {
        HashMap<String, String> info = new HashMap<>();
        info.put(Constants.ARG_EMAIL, currentUser.getEmail());
        info.put(Constants.MESSAGE, "Sends you a friend request.");
        info.put(Constants.ARG_UID, currentUser.getUid());
        info.put(Constants.SENDER_TOKEN, currentUser.getFirebaseToken());
        info.put(Constants.RECEIVER_TOKEN, onClickUser.getFirebaseToken());
        return info;
    }

    /**
     * The method is to notification message to receiver given all the info.
     */
    private void sendRequest(HashMap<String, String> allInfo) {
        String email = allInfo.get(Constants.ARG_EMAIL);
        String message = allInfo.get(Constants.MESSAGE);
        String uid = allInfo.get(Constants.ARG_UID);
        String senderToken = allInfo.get(Constants.SENDER_TOKEN);
        String receiverToken = allInfo.get(Constants.RECEIVER_TOKEN);

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
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    // For Testing.
    public ListView getListView() {
        return this.mListView;
    }
}
