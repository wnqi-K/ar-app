package com.comp30022.arrrrr;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * The interface to accept or deny friend request after clicking on the notification.
 * Created by Wenqiang Kuang on 27/09/2017.
 */
public class AcceptRequestActivity extends AppCompatActivity {
    private CardView mCardView;
    private Button mAcceptButton;
    private Button mRejectButton;
    private TextView mUserName;
    private TextView mUserEmail;
    private TextView mUserGender;
    private TextView mUserAddress;
    private ImageView mUserAvatar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accept_request);

        // Get extra info from notification and display.
        Intent intent = getIntent();
        String senderName = intent.getExtras().getString("SenderName");
        String senderEmail = intent.getExtras().getString("SenderEmail");
        String senderGender = intent.getExtras().getString("SenderGender");
        String senderAddress = intent.getExtras().getString("SenderAddress");

        mCardView = (CardView) findViewById(R.id.requestUserInfo);
        mAcceptButton = (Button) findViewById(R.id.accept_button);
        mRejectButton = (Button) findViewById(R.id.reject_button);
        mUserAvatar = (ImageView) mCardView.findViewById(R.id.request_user_avatar);
        mUserName = (TextView)mCardView.findViewById(R.id.request_user_name);
        mUserEmail = (TextView)mCardView.findViewById(R.id.request_user_email);
        mUserGender = (TextView)mCardView.findViewById(R.id.request_user_gender);
        mUserAddress = (TextView)mCardView.findViewById(R.id.request_user_address);

        // Display info carried by the intent.
        mUserAvatar.setImageDrawable(getResources().getDrawable(R.drawable.avatar));
        mUserName.setText(senderName);
        mUserEmail.setText(senderEmail);
        mUserGender.setText(senderGender);
        mUserAddress.setText(senderAddress);

        // Update the database if accepting.
        mAcceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateDatabase();
            }
        });

        // Discard the notification if rejecting.
        mRejectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Pop up the msg and go back to the main view.
                Toast.makeText(getBaseContext(), "You've reject the request. ",
                        Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getBaseContext(), MainViewActivity.class);
                startActivity(intent);
            }
        });
    }

    /**
     * The method is to update the database, adding the request user to current user's friend list
     * and adding current user to request user's friend list.
     */
    private void updateDatabase() {
        //TODO
    }
}