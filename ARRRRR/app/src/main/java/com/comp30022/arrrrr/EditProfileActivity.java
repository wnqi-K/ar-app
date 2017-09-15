package com.comp30022.arrrrr;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;


import com.comp30022.arrrrr.models.Users;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;



public class EditProfileActivity extends AppCompatActivity{

    private static final String TAG = "AddToDatabase";

    //Submit button
    private Button btnSubmit;

    //Gender Ratio BTN
    AlertDialog alertDialog1;
    CharSequence[] values = {"Female","Male"};

    //Address Dialog
    AlertDialog alertDialog2;

    //Input value
    Boolean changeName = false;
    Boolean changePhoneNum = false;
    private EditText mName,mPhoneNum;
    private String mGender="";
    private String mAddress = "";
    private String userID;

    //add Firebase Database stuff
    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference myRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        //Value passed by UserProfile.class
        //String UserName  = getIntent().getStringExtra(UserProfile.EXTRA_NAME);

        mName = (EditText) findViewById(R.id.etName);
        mPhoneNum = (EditText) findViewById(R.id.etPhone);

        TextView name = (TextView)findViewById(R.id.clickable_edit_name);

        //declare the database reference object. This is what we use to access the database.
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();
        FirebaseUser user = mAuth.getCurrentUser();
        userID = user.getUid();

        // Display Data in TextView
        //name.setText("Name:  "+UserName);


        //Add value to database after click submit btn
        btnSubmit = (Button)findViewById(R.id.btnSubmit);
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = mName.getText().toString();
                String phoneNum = mPhoneNum.getText().toString();

                Log.d(TAG, "onClick: Attempting to submit to database: \n" +
                        "name: " + name + "\n" +
                        "phone number: " + phoneNum + "\n"
                );


                if(changeName==true && validateName(name)==true){
                    myRef.child("users").child(userID).child("username").setValue(name);
                    toastMessage("New Information has been saved.");
                    mName.setText("");
                }

                if(changePhoneNum==true && validatePhoneNumber(phoneNum)==true){
                    myRef.child("users").child(userID).child("phoneNum").setValue(phoneNum);
                    toastMessage("New Information has been saved.");
                    mPhoneNum.setText("");
                }

                if(!mGender.equals("")){
                    myRef.child("users").child(userID).child("gender").setValue(mGender);
                    toastMessage("New Information has been saved.");

                }

                if(!mAddress.equals("")){
                    myRef.child("users").child(userID).child("address").setValue(mAddress);
                    toastMessage("New Information has been saved.");
                }
            }
        });

    }


    public void TextViewClicked(View view){

        // Declare in and out animations and load them using AnimationUtils class
        Animation in = AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left);
        Animation out = AnimationUtils.loadAnimation(this, android.R.anim.slide_out_right);


        switch(view.getId()){
            case R.id.clickable_edit_name:
                ViewSwitcher switcher1 = (ViewSwitcher) findViewById(R.id.switcherName);
                switcher1.setInAnimation(in);
                switcher1.setOutAnimation(out);
                switcher1.showNext();
                changeName = true;
                break;

            case R.id.clickable_edit_number:
                ViewSwitcher switcher3 = (ViewSwitcher) findViewById(R.id.switcherNumber);
                switcher3.setInAnimation(in);
                switcher3.setOutAnimation(out);
                switcher3.showNext();
                changePhoneNum = true;
        }

    }

    /** Called when the user clicks the gender button */
    public void clickGender(View view){

        CreateAlertDialogWithGenderRadioButtonGroup() ;

    }

    /** Called when the user clicks the address button */
    public void clickAddress(View view){

        CreateAlertDialogWithAddress() ;
    }

    private void CreateAlertDialogWithAddress() {
        AlertDialog.Builder builder = new AlertDialog.Builder(EditProfileActivity.this);

        builder.setTitle("Address");

        // Set up the input
        final EditText input = new EditText(this);

        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mAddress = input.getText().toString();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();

    }

    public void CreateAlertDialogWithGenderRadioButtonGroup(){
        AlertDialog.Builder builder = new AlertDialog.Builder(EditProfileActivity.this);

        builder.setTitle("Gender");

        builder.setSingleChoiceItems(values, -1, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {

                switch(item)
                {
                    case 0:

                        Toast.makeText(EditProfileActivity.this, "Female Clicked", Toast.LENGTH_LONG).show();
                        mGender = "Female";
                        break;
                    case 1:

                        Toast.makeText(EditProfileActivity.this, "Male Clicked", Toast.LENGTH_LONG).show();
                        mGender = "Male";
                        break;
                }
                alertDialog1.dismiss();
            }
        });
        alertDialog1 = builder.create();
        alertDialog1.show();

    }

    //Validation for edit name
    public boolean validateName(String name){

        boolean valid = true;

        if( name.matches("") || name.length()>8 ){
            mName.setError("Enter a valid name ");
            valid = false;
        }

        return valid;
    }

    //Validation for edit phone number
    public boolean validatePhoneNumber(String phone){
        boolean valid = true;
        String regexStr1 = "^\\+[0-9]{10,13}$";
        String regexStr2 = "^[0-9]{10}$";

        if(phone.matches(regexStr1)==false && phone.matches(regexStr2)==false) {
            mPhoneNum.setError("Enter a valid telephone number");
            valid = false;
        }



        return valid;
    }


    public void back_UserProfile(View v){
        Intent intent = new Intent(this, UserProfile.class);
        startActivity(intent);
        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
    }

    private void toastMessage(String message){
        Toast.makeText(this,message,Toast.LENGTH_SHORT).show();
    }

}