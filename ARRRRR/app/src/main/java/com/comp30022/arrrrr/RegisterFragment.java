package com.comp30022.arrrrr;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.comp30022.arrrrr.models.User;

import com.comp30022.arrrrr.utils.Constants;
import com.comp30022.arrrrr.utils.LoginHelper;
import com.comp30022.arrrrr.utils.SharedPrefUtil;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class RegisterFragment extends Fragment implements View.OnClickListener{
    private static final String TAG = RegisterFragment.class.getSimpleName();


    private EditText mETxtEmail, mETxtPassword,mETxtPhonenum,
            mETxtUsername,mETxtAddress;
    private Button mBtnRegister;
    private RadioGroup mRadioGroup;
    private ProgressDialog mProgressDialog;
    private FirebaseAuth mAuth;
    private DatabaseReference mRef;
    FirebaseUser currentUser;


    public static RegisterFragment newInstance() {
        Bundle args = new Bundle();
        RegisterFragment fragment = new RegisterFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_register, container, false);
        mAuth = FirebaseAuth.getInstance();
        mRef = FirebaseDatabase.getInstance().getReference();
        bindViews(fragmentView);
        return fragmentView;
    }

    private void bindViews(View view) {
        mETxtEmail = (EditText) view.findViewById(R.id.edit_text_email_id);
        mETxtPassword = (EditText) view.findViewById(R.id.edit_text_password);
        mETxtUsername = (EditText) view.findViewById(R.id.edit_text_username);
        mETxtPhonenum = (EditText) view.findViewById(R.id.edit_text_phonenum);
        mETxtAddress = (EditText) view.findViewById(R.id.edit_text_address);
        mBtnRegister = (Button) view.findViewById(R.id.button_register);
        mRadioGroup = (RadioGroup) view.findViewById(R.id.radioGroup);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        init();
    }

    private void init() {
        mBtnRegister.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        int viewId = view.getId();

        switch (viewId) {
            case R.id.button_register:
                onRegister(view);
                break;
        }
    }

    private void onRegister(View view) {
        String emailId = mETxtEmail.getText().toString();
        String password = mETxtPassword.getText().toString();
        String username = mETxtUsername.getText().toString();
        String phoneNum = mETxtPhonenum.getText().toString();
        String address = mETxtAddress.getText().toString();
        String gender = null;

        if (mRadioGroup.getCheckedRadioButtonId() == R.id.radioButton_male){
            gender = Constants.ARG_MALE;
        }else if (mRadioGroup.getCheckedRadioButtonId() == R.id.radioButton_female){
            gender = Constants.ARG_FEMALE;
        }

        if(gender == null){
            Toast.makeText(getActivity(), "gender required", Toast.LENGTH_SHORT).show();
        } else if(!LoginHelper.validateForm2(mETxtEmail,
                mETxtPassword,
                mETxtUsername,
                mETxtPhonenum,
                mETxtAddress)){
            // if input is not valid
            Toast.makeText(getActivity(), "Error occurs, please check your input", Toast.LENGTH_SHORT).show();
        }
        else{
            performFirebaseRegistration(getActivity(), emailId, password,gender);
        }
    }


    /**
     * create a Firebase User using email and password
     * */
    private void performFirebaseRegistration(Activity activity,
                                             final String email,
                                             String password,
                                             final String gender) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(activity, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.e(TAG, "performFirebaseRegistration:onComplete:" + task.isSuccessful());
                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Toast.makeText(getActivity(), Constants.ARG_FAILURE, Toast.LENGTH_SHORT).show();
                            //Toast.makeText(getActivity(),task.getException().getMessage(), Toast.LENGTH_SHORT).show();

                        } else {
                           addUserToDatabase(getActivity(),task.getResult().getUser(),gender);
                        }
                    }
                });
    }

    /**
     * add user info to Firebase database
     * */
    private void addUserToDatabase(Activity activity,FirebaseUser firebaseUser,String gender){
        String uid = firebaseUser.getUid();
        User user = User.createUserWithoutAdmin(uid,
                firebaseUser.getEmail(),
                new SharedPrefUtil(activity).getString(Constants.ARG_FIREBASE_TOKEN),
                mETxtUsername.getText().toString(),
                mETxtPhonenum.getText().toString(),
                gender,
                mETxtAddress.getText().toString());
        DatabaseReference mRef = FirebaseDatabase.getInstance().getReference();
        mRef.child(Constants.ARG_USERS)
                .child(uid)
                .setValue(user)
                .addOnCompleteListener(activity, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (!task.isSuccessful()) {
                            Toast.makeText(getContext(),
                                    Constants.ARG_FAILURE, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getContext(),
                                    Constants.ARG_SUCCESS, Toast.LENGTH_SHORT).show();
                            EmailLoginActivity.startActivity(getActivity());

                        }
                    }
                });
    }

}
