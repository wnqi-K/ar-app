package com.comp30022.arrrrr;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.RestrictTo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.comp30022.arrrrr.database.UserManagement;
import com.comp30022.arrrrr.receivers.AddressResultReceiver;
import com.comp30022.arrrrr.receivers.SimpleRequestResultReceiver;
import com.comp30022.arrrrr.receivers.SingleUserLocationReceiver;
import com.comp30022.arrrrr.services.FetchAddressIntentService;
import com.comp30022.arrrrr.services.LocationSharingService;
import com.comp30022.arrrrr.utils.BroadcastReceiverManager;
import com.comp30022.arrrrr.utils.Constants;
import com.comp30022.arrrrr.utils.GeoUtil;
import com.comp30022.arrrrr.utils.PreferencesAccess;
import com.comp30022.arrrrr.utils.ServiceManager;
import com.comp30022.arrrrr.utils.TimeUtil;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class SettingFragment extends Fragment implements
        SimpleRequestResultReceiver.SimpleRequestResultListener,
        SingleUserLocationReceiver.SingleUserLocationListener,
        AddressResultReceiver.AddressResultListener {

    public static String TAG = SettingFragment.class.getSimpleName();

    private OnSettingFragmentInteractionListener mListener;

    //add Firebase Database stuff
    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private DatabaseReference myRef;
    private String mUserID;

    private ImageButton mUserProfileButton;
    private ImageView mPhoto;
    private Button mButtonClearRecords;
    private Switch mSwitchLocationSharing;
    private Switch mSwitchNearbyNotification;
    private TextView mTextViewLastLocation;
    private Spinner mSpinnerFilterDistance;

    private BroadcastReceiverManager mBroadcastReceivers;
    private String mTimeBuffer;
    private HashMap<String, Double> mFilterDistances;
    /**
     * Indicates whether the selection event is fired by initialization
     */
    private boolean mSpinnerFilterDistanceFlag;

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
        mUserID = currentUser.getUid();

        mBroadcastReceivers = new BroadcastReceiverManager(getActivity());

        mFilterDistances = new HashMap<>();
        mFilterDistances.put(getString(R.string.item_distance_2km), 2d);
        mFilterDistances.put(getString(R.string.item_distance_10km), 10d);
        mFilterDistances.put(getString(R.string.item_distance_100km), 100d);
    }

    @Override
    public void onResume() {
        super.onResume();

        // Register receivers
        mBroadcastReceivers.add(SimpleRequestResultReceiver.register(getActivity(), this));
        mBroadcastReceivers.add(SingleUserLocationReceiver.register(getActivity(), this));
        mBroadcastReceivers.add(AddressResultReceiver.register(getActivity(), this));

        updateUIFromPreferences();
        updateLastLocationDisplay();
    }

    @Override
    public void onPause() {
        super.onPause();
        mBroadcastReceivers.unregisterAll();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_setting, container, false);

        TextView mStatusView = (TextView) view.findViewById(R.id.login_status_view);

        TextView accountPrivacy = (TextView) view.findViewById(R.id.accountPrivacy);

        mStatusView.setText(currentUser.getEmail());

        Button logoutButton = (Button) view.findViewById(R.id.logoutButton);
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
                DatabaseReference userReference = firebaseDatabase.getReference();
                FirebaseUser user = mAuth.getCurrentUser();

                userReference.child(Constants.ARG_USERS).child(user.getUid())
                        .child(Constants.ARG_STATUS).removeValue();

                mAuth.signOut();
                ServiceManager.stopLocationSharingService(getActivity());
                ServiceManager.stopPositioningService(getActivity());

                Intent intent = new Intent(getActivity(), MainActivity.class);
                startActivity(intent);
            }
        });

        //Go user profile
        mUserProfileButton = (ImageButton) view.findViewById(R.id.profileButton);
        mUserProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), UserProfileActivity.class);
                startActivity(intent);
            }
        });

        //Go user account privacy
        accountPrivacy.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {

                    // Do what you want
                    Intent intent = new Intent(getActivity(), AccountPrivacyActivity.class);
                    startActivity(intent);

                    return true;
                }
                return false;
            }
        });

        mButtonClearRecords = (Button) view.findViewById(R.id.button_clear_records);
        mButtonClearRecords.setOnClickListener(mOnClearRecordsClickListener);

        mSwitchLocationSharing = (Switch) view.findViewById(R.id.switch_location_sharing);
        mSwitchLocationSharing.setOnCheckedChangeListener(onLocSharingCheckedChangeListener);

        mSwitchNearbyNotification = (Switch) view.findViewById(R.id.switch_nearby_friend_notification);
        mSwitchNearbyNotification.setOnCheckedChangeListener(onNearbyNotifyCheckedChangeListener);

        mSpinnerFilterDistance = (Spinner) view.findViewById(R.id.spinner_filter_distance);
        mSpinnerFilterDistance.setOnItemSelectedListener(onFilterDistanceItemSelectedListener);
        mSpinnerFilterDistanceFlag = true;

        mTextViewLastLocation = (TextView) view.findViewById(R.id.text_view_last_location);

        //Set profile head portrait photo
        mPhoto = (ImageView) view.findViewById(R.id.profilePhoto);
        myRef.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    if (ds.child(mUserID).hasChild(Constants.ARG_IMAGE)) {

                        try {
                            // Use UserManagement to get profile image
                            //String url = ds.child(mUserID).child(Constants.ARG_IMAGE).getValue(String.class);
                            Bitmap imageBitmap = UserManagement.getInstance().getUserProfileImage(mUserID, getActivity());
                            mPhoto.setImageBitmap(imageBitmap);
                        } catch (Exception e) {
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
     * Handles when an option has been selected for the filter distance spinner.
     */
    private AdapterView.OnItemSelectedListener onFilterDistanceItemSelectedListener
            = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            String selected = adapterView.getItemAtPosition(i).toString();

            double radius;

            // Assign radius value from string option
            if (mFilterDistances.containsKey(selected)) {
                radius = mFilterDistances.get(selected);
            } else {
                radius = LocationSharingService.DEFAULT_GEO_QUERY_RADIUS;
            }

            // Save to preference
            SharedPreferences preferences = PreferencesAccess.getSettingsPreferences(getActivity());
            SharedPreferences.Editor editor = preferences.edit();
            editor.putLong(getString(R.string.PREF_KEY_FILTER_DISTANCE), (long) radius);
            editor.putInt(getString(R.string.PREF_KEY_FILTER_DISTANCE_INDEX), i);
            editor.apply();

            if (!mSpinnerFilterDistanceFlag) {
                Toast.makeText(getActivity(),
                        getString(R.string.text_change_filter_distance_success),
                        Toast.LENGTH_LONG).show();
            }
            mSpinnerFilterDistanceFlag = false;
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {
            mSpinnerFilterDistance.setSelection(0);
        }
    };

    /**
     * Display user's last location
     */
    private void updateLastLocationDisplay() {
        LocationSharingService.requestAddUserLocationListener(getActivity(), UserManagement
                .getInstance()
                .getCurrentUser()
                .getUid());
    }

    /**
     * Handles when we have received last user's location
     */
    @Override
    public void onReceivingSingleUserLocation(String uid, LatLng latLng, long time) {
        mTimeBuffer = TimeUtil.getFriendlyTime(time);
        FetchAddressIntentService.requestFetchAddress(getActivity(),
                GeoUtil.latLngToLocation(latLng));
    }

    /**
     * Handles when we have successfully fetched the address
     */
    @Override
    public void onAddressFetchSuccess(Address address, Location location) {
        String timeString = mTimeBuffer.equals("") ? "" : " (" + mTimeBuffer + ")";
        String text = getString(R.string.text_your_last_location_was_at)
                + address.getLocality() + " " + timeString;
        mTextViewLastLocation.setText(text);
    }

    /**
     * Handles when we have failed fetching the address
     */
    @Override
    public void onAddressFetchFailure(Location location) {
        String text = getString(R.string.text_your_last_location_was_at) +
                getString(R.string.text_unknown);
        mTextViewLastLocation.setText(text);
    }

    /**
     * OnCheckedChangeListener for location sharing switch
     */
    private CompoundButton.OnCheckedChangeListener onLocSharingCheckedChangeListener
            = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
            // Save to preferences
            compoundButton.setEnabled(false);

            SharedPreferences preferences = PreferencesAccess.getSettingsPreferences(getActivity());
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean(getString(R.string.PREF_KEY_ENABLE_LOCATION_SHARING), b);
            editor.apply();

            if (!b) {
                // Give user friendly help message.
                Toast.makeText(getActivity(),
                        getString(R.string.text_location_sharing_disabled),
                        Toast.LENGTH_LONG).show();
            }
            compoundButton.setEnabled(true);
        }
    };

    /**
     * OnCheckedChangeListener for nearby friends notification switch
     */
    private CompoundButton.OnCheckedChangeListener onNearbyNotifyCheckedChangeListener
            = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
            // Save to preferences
            compoundButton.setEnabled(false);

            SharedPreferences preferences = PreferencesAccess.getSettingsPreferences(getActivity());
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean(getString(R.string.PREF_KEY_ENABLE_NEARBY_NOTIFICATION), b);
            editor.apply();

            compoundButton.setEnabled(true);
        }
    };

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
                        R.string.text_remove_locatioin_records_success,
                        Toast.LENGTH_SHORT).show();

            } else {
                Toast.makeText(getActivity(),
                        R.string.text_remove_locatioin_records_fail,
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

    public interface OnSettingFragmentInteractionListener {
        void onSettingFragmentInteraction(Uri uri);
    }

    /**
     * Update settings UI from saved shared preferences
     */
    private void updateUIFromPreferences() {
        SharedPreferences preferences = PreferencesAccess.getSettingsPreferences(getActivity());

        boolean isLocationSharingEnabled = preferences.getBoolean(
                getString(R.string.PREF_KEY_ENABLE_LOCATION_SHARING), true);
        boolean isNearbyNotificationEnabled = preferences.getBoolean(
                getString(R.string.PREF_KEY_ENABLE_NEARBY_NOTIFICATION), true);

        long distance = preferences.getLong(getString(R.string.PREF_KEY_FILTER_DISTANCE),
                (long) (double) LocationSharingService.DEFAULT_GEO_QUERY_RADIUS);

        int index = preferences.getInt(getString(R.string.PREF_KEY_FILTER_DISTANCE_INDEX), 0);

        mSpinnerFilterDistance.setSelection(index);
        mSwitchLocationSharing.setChecked(isLocationSharingEnabled);
        mSwitchNearbyNotification.setChecked(isNearbyNotificationEnabled);
    }
}
