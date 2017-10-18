package com.comp30022.arrrrr;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.comp30022.arrrrr.database.UserManagement;
import com.comp30022.arrrrr.models.User;
import com.comp30022.arrrrr.utils.Constants;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

/**
 * The interface to accept or deny friend request after clicking on the notification.
 * Created by Wenqiang Kuang on 27/09/2017.
 */
public class AcceptRequestActivity extends AppCompatActivity {
    public static final String ALREADY_FRIEND_MESSAGE = "You are Already friends.";
    public static final String REJECT_MESSAGE = "You've reject the request. ";
    // For Testing.
    public static String TAG = "AcceptRequestActivity";

    private CardView mCardView;
    private Button mAcceptButton;
    private Button mRejectButton;
    private TextView mUserName;
    private TextView mUserEmail;
    private TextView mUserGender;
    private TextView mUserAddress;
    private ImageView mUserAvatar;
    private FirebaseAuth mAuth;
    private FirebaseDatabase mFirebaseDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accept_request);

        // Get extra info from notification and display.
        Intent intent = getIntent();
        final String senderUid = intent.getExtras().getString(Constants.SENDER_UID);
        String senderName = intent.getExtras().getString(Constants.SENDER_NAME);
        String senderEmail = intent.getExtras().getString(Constants.SENDER_EMAIL);
        String senderGender = intent.getExtras().getString(Constants.SENDER_GENDER);
        String senderAddress = intent.getExtras().getString(Constants.SENDER_ADDRESS);

        mCardView = (CardView) findViewById(R.id.requestUserInfo);
        mAcceptButton = (Button) findViewById(R.id.accept_button);
        mRejectButton = (Button) findViewById(R.id.reject_button);
        mUserAvatar = (ImageView) mCardView.findViewById(R.id.request_user_avatar);
        mUserName = (TextView) mCardView.findViewById(R.id.request_user_name);
        mUserEmail = (TextView) mCardView.findViewById(R.id.request_user_email);
        mUserGender = (TextView) mCardView.findViewById(R.id.request_user_gender);
        mUserAddress = (TextView) mCardView.findViewById(R.id.request_user_address);

        // Display info carried by the intent.
        Bitmap avatar;
        try {
            avatar = UserManagement.getInstance().getUserProfileImage(senderUid, getBaseContext());
        } catch (Exception e) {
            avatar = BitmapFactory.decodeResource(getResources(), R.drawable.portrait_photo);
        }
        mUserAvatar.setImageBitmap(avatar);
        mUserName.setText(Constants.NAME_PREFIX + senderName);
        mUserEmail.setText(Constants.EMAIL_PREFIX + senderEmail);
        mUserGender.setText(Constants.GENDER_PREFIX + senderGender);
        mUserAddress.setText(Constants.ADDRESS_PREFIX + senderAddress);

        // Update the database if accepting.
        mAcceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Check if is friends first.
                if (isAlreadyFriend(senderUid)) {
                    Toast.makeText(getBaseContext(), ALREADY_FRIEND_MESSAGE,
                            Toast.LENGTH_LONG).show();
                } else {
                    updateDatabase(senderUid);
                }
                goBackToMainView();
            }
        });

        // Discard the notification if rejecting.
        mRejectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Pop up the msg and go back to the main view.
                Toast.makeText(getBaseContext(), REJECT_MESSAGE,
                        Toast.LENGTH_SHORT).show();
                goBackToMainView();
            }
        });
    }

    /**
     * Check if the request is sent by friends.
     */
    private boolean isAlreadyFriend(String senderUid) {
        Boolean isFriend = false;
        ArrayList<User> friends = (ArrayList<User>) UserManagement.getInstance().getFriendList();
        for (User user : friends) {
            if (user.getUid() == senderUid) {
                isFriend = true;
                break;
            }
        }
        return isFriend;
    }

    /**
     * Go back to the mainViewActivity, after choosing 'accept' or 'reject'.
     */
    private void goBackToMainView() {
        Intent intent = new Intent(getBaseContext(), MainViewActivity.class);
        startActivity(intent);
        finish();
    }

    /**
     * The method is to update the database, adding the request user to current user's friend list
     * and adding current user to request user's friend list.
     */
    private void updateDatabase(String senderUid) {
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference userReference = mFirebaseDatabase.getReference();
        FirebaseUser user = mAuth.getCurrentUser();
        String currentUserUid = user.getUid();
        User currentUser = UserManagement.getInstance().getUserByUID(currentUserUid);
        User sender = UserManagement.getInstance().getUserByUID(senderUid);
        String senderEmail = sender.getEmail();
        String currentUserEmail = currentUser.getEmail();

        // Establish the friendship mutually. Update the friends database.
        userReference.child(Constants.ARG_FRIENDS).child(currentUserUid).child(senderUid).setValue(senderEmail);
        userReference.child(Constants.ARG_FRIENDS).child(senderUid).child(currentUserUid).setValue(currentUserEmail);
    }
}