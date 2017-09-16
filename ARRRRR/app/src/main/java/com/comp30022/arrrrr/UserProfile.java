package com.comp30022.arrrrr;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import com.comp30022.arrrrr.models.User;
import com.comp30022.arrrrr.utils.Constants;

public class UserProfile extends AppCompatActivity {

    public final static String EXTRA_NAME = "com.comp30022.EXTRA_NAME_MESSAGE";

    //add Firebase Database stuff
    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private DatabaseReference myRef;
    private  String userID;
    User uInfo = new User();


    /**-------------------------------------**/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_user_profile);

        //declare the database reference object.
        mAuth = FirebaseAuth.getInstance();

        currentUser = mAuth.getCurrentUser();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();
        userID = currentUser.getUid();

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                showData(dataSnapshot);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void showData(DataSnapshot dataSnapshot) {

        TextView UserId = (TextView)findViewById(R.id.name);
        TextView UserEmail = (TextView)findViewById(R.id.email);
        TextView UserPhone = (TextView)findViewById(R.id.number);
        TextView UserGender = (TextView)findViewById(R.id.gender);
        TextView UserAddress = (TextView)findViewById(R.id.address);


        for(DataSnapshot ds : dataSnapshot.getChildren()) {
            if (ds.child(userID).hasChild(Constants.ARG_USER_NAME)) {
                uInfo.setUsername(ds.child(userID).getValue(User.class).getUsername()); //set the name
                UserId.setText("Name:  "+ uInfo.getUsername());
            }else {
                UserId.setText("Name: please complete your profile by clicking Edit");
            }

            if (ds.child(userID).hasChild(Constants.ARG_PHONE_NUM)) {
                uInfo.setPhoneNum(ds.child(userID).getValue(User.class).getPhoneNum()); //set the phone_num
                UserPhone.setText("Phone Number: "+uInfo.getPhoneNum());
            }else
                UserPhone.setText("Phone Number:");

            if (ds.child(userID).hasChild(Constants.ARG_GENDER)) {
                uInfo.setGender(ds.child(userID).getValue(User.class).getGender()); //set the gender
                UserGender.setText("Gender: "+uInfo.getGender());
            }else
                UserGender.setText("Gender:");

            if (ds.child(userID).hasChild(Constants.ARG_EMAIL)) {
                UserEmail.setText("Email: "+currentUser.getEmail());
            }else
                UserEmail.setText("Email:");

            if (ds.child(userID).hasChild(Constants.ARG_ADDRESS)) {
                uInfo.setAddress(ds.child(userID).getValue(User.class).getAddress()); //set the gender
                UserAddress.setText("Address: "+uInfo.getAddress());
            }else
                UserAddress.setText("Address:");

        }
    }

    public void EditProfile(View v){
        Intent intent = new Intent(this, EditProfileActivity.class);
        startActivity(intent);
        intent.putExtra(EXTRA_NAME, uInfo.getUsername());
        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
    }

    public void Back(View v){
        Intent intent = new Intent(this, MainViewActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
    }
}
