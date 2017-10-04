package com.comp30022.arrrrr;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.RestrictTo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.comp30022.arrrrr.database.UserManagement;
import com.comp30022.arrrrr.receivers.SimpleRequestResultReceiver;
import com.comp30022.arrrrr.services.LocationSharingService;
import com.comp30022.arrrrr.utils.Constants;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;

public class SettingFragment extends Fragment
    implements SimpleRequestResultReceiver.SimpleRequestResultListener{

    public static String TAG = SettingFragment.class.getSimpleName();

    private OnSettingFragmentInteractionListener mListener;

    //add Firebase Database stuff
    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private DatabaseReference myRef;
    private  String userID;
    //private User uInfo = new User();

    private ImageButton userprofileButton;
    private ImageView mPhoto;
    private Button mButtonClearRecords;

    private SimpleRequestResultReceiver mSimpleRequestResultReceiver;

    public SettingFragment() {
        // Required empty public constructor
    }

    public static SettingFragment newInstance() {
        SettingFragment fragment = new SettingFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        //declare the database reference object.
        mAuth = FirebaseAuth.getInstance();

        currentUser = mAuth.getCurrentUser();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();
        userID = currentUser.getUid();
    }

    @Override
    public void onResume() {
        super.onResume();
        SimpleRequestResultReceiver.register(getActivity(), this);
    }

    @Override
    public void onPause() {
        super.onPause();
        try {
            getActivity().unregisterReceiver(mSimpleRequestResultReceiver);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_setting, container, false);

        TextView mStatusView = (TextView) view.findViewById(R.id.login_status_view);

        mStatusView.setText(currentUser.getEmail());

        Button logoutButton = (Button)view.findViewById(R.id.logoutButton);
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();

                Intent intent = new Intent(getActivity(), MainActivity.class);
                startActivity(intent);
                //getActivity().finish();
            }
        });

        //Go user profile
        userprofileButton = (ImageButton)view.findViewById(R.id.profileButton);
        userprofileButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = new Intent(getActivity(),UserProfileActivity.class);
                startActivity(intent);
            }
        });

        mButtonClearRecords = (Button)view.findViewById(R.id.button_clear_records);
        mButtonClearRecords.setOnClickListener(mOnClearRecordsClickListener);


        //Set profile head portrait photo
        mPhoto = (ImageView)view.findViewById(R.id.profilePhoto);
        myRef.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                for(DataSnapshot ds : dataSnapshot.getChildren()) {
                    if(ds.child(userID).hasChild(Constants.ARG_IMAGE)){

                        try{
                            // Use UserManagement to get profile image
                            //String url = ds.child(userID).child(Constants.ARG_IMAGE).getValue(String.class);
                            Bitmap imageBitmap = UserManagement.getInstance().getUserProfileImage(userID, getActivity());
                            mPhoto.setImageBitmap(imageBitmap);
                        }catch(Exception e){
                            e.printStackTrace();
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        return view;
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        // Hide the adding friends and ar entry options in setting fragment
        menu.findItem(R.id.adding_friends).setVisible(false);
        menu.findItem(R.id.quick_ar_entry).setVisible(false);
        super.onPrepareOptionsMenu(menu);
    }

    /**
     * OnClickListener for clearing records button
     */
    private View.OnClickListener mOnClearRecordsClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            mButtonClearRecords.setEnabled(false);
            mButtonClearRecords.setText(R.string.text_removing_location_records);
            LocationSharingService.requestClearLocationRecords(getActivity());
        }
    };

    @RestrictTo(RestrictTo.Scope.TESTS)
    public Button getButtonClearRecords() {
        return mButtonClearRecords;
    }

    @RestrictTo(RestrictTo.Scope.TESTS)
    public View.OnClickListener getOnClearRecordsClickListener() {
        return mOnClearRecordsClickListener;
    }

    /**
     * Handles when request result has been send back from the service.
     */
    @Override
    public void onReceivingSimpleRequestResult(String requestType, boolean success) {
        if (requestType.equals(LocationSharingService.REQUEST_CLEAR_LOCATION_RECORDS)) {
            if (success) {
                Toast.makeText(getActivity(),
                                "Your location records has been successfully removed.",
                                Toast.LENGTH_SHORT).show();

            } else {
                Toast.makeText(getActivity(),
                        "Your location records could not be removed at this moment due to network issues, "
                                + "please try again later.",
                        Toast.LENGTH_LONG).show();
            }
            mButtonClearRecords.setEnabled(true);
            mButtonClearRecords.setText(R.string.text_clear_my_location_records);
        }
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onSettingFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnSettingFragmentInteractionListener) {
            mListener = (OnSettingFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnSettingFragmentInteractionListener {
        // TODO: Update argument type and name
        void onSettingFragmentInteraction(Uri uri);
    }
}
