package com.comp30022.arrrrr;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.comp30022.arrrrr.receivers.SelfPositionReceiver;
import com.comp30022.arrrrr.receivers.GeoQueryLocationsReceiver;
import com.comp30022.arrrrr.utils.LocationPermissionHelper;
import com.comp30022.arrrrr.utils.LocationSettingsHelper;
import com.comp30022.arrrrr.utils.MapUIManager;
import com.comp30022.arrrrr.utils.ServiceManager;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;

import java.text.DateFormat;
import java.util.Date;

/**
 * Fragment containing map interface.
 *
 * @author Dafu Ai
 */
public class MapContainerFragment extends Fragment implements
        OnMapReadyCallback,
        SelfPositionReceiver.SelfLocationListener,
        LocationSettingsHelper.OnLocationSettingsResultListener {

    private static final String TAG = MapContainerFragment.class.getSimpleName();

    /**
     * Constant used in the location settings dialog.
     */
    private static final int REQUEST_CHECK_SETTINGS = 0x1;

    // Keys for storing activity state in the Bundle.
    private final static String KEY_SELF_LOCATION = "self_location";

    /**
     * Tracks the status of the location updates request. Value changes when the user presses the
     * Start Updates and Stop Updates buttons.
     */
    private Boolean mRequestingLocationUpdates;

    /**
     * Represents a geographical location.
     */
    private Location mCurrentLocation;

    /**
     * The main map using Google Map.
     */
    private GoogleMap mGoogleMap;

    /**
     * Receiver for self positioning service.
     */
    private SelfPositionReceiver mPositioningReceiver;

    /**
     * Receiver for location information from server
     */
    private GeoQueryLocationsReceiver mServerLocationsReceiver;

    /**
     * Context that this fragment is running under.
     */
    private AppCompatActivity mContext;

    /**
     * Listener for communication between this fragment and its parent context.
     */
    private OnMapContainerFragmentInteractionListener mListener;

    /**
     * Used for checking location permission.
     */
    private LocationPermissionHelper mPermissionChecker;

    /**
     * UI manager for the google maps
     */
    private MapUIManager mMapUIManager;

    public MapContainerFragment() {
        // Required empty public constructor
    }

    public static MapContainerFragment newInstance() {
        MapContainerFragment fragment = new MapContainerFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCurrentLocation = null;
        mRequestingLocationUpdates = false;

        //Update values using data stored in the Bundle.
        updateValuesFromBundle(savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map_container, null, false);
        // Get the SupportMapFragment and request notification
        // when the map is ready to be used.
        MapFragment mapFragment = (MapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map_container_map);
        mapFragment.getMapAsync(this);
        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (mCurrentLocation != null) {
            outState.putParcelable(KEY_SELF_LOCATION, mCurrentLocation);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onResume() {
        super.onResume();
        registerReceivers();

        ServiceManager.startLocationSharingService(getActivity());

        // Sets up permission checker.
        mPermissionChecker = new LocationPermissionHelper((AppCompatActivity)getActivity());
        // 1. Check location permissions.
        Boolean permissionGranted = mPermissionChecker.checkPermissions();

        if (!mRequestingLocationUpdates && permissionGranted) {
            // 2. Check location settings.
            // 3. Will start positioning service after checking is passed.
            checkLocationSettings();
        } else if (!permissionGranted) {
            mPermissionChecker.requestPermissions();
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        unregisterReceivers();
        mMapUIManager.saveCurrentMapView(mCurrentLocation);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        mContext = (AppCompatActivity) context;
        if (mContext instanceof OnMapContainerFragmentInteractionListener) {
            mListener = (OnMapContainerFragmentInteractionListener) mContext;
        } else {
            throw new RuntimeException(mContext.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.mGoogleMap = googleMap;
        Toast.makeText(getActivity(), "Map ready", Toast.LENGTH_SHORT).show();

        mMapUIManager = new MapUIManager(this, getActivity(), mGoogleMap);
        mMapUIManager.initializeMapUI();
    }

    @Override
    public void onSelfLocationChanged(Location location) {
        mCurrentLocation = location;
        Toast.makeText(getActivity(), DateFormat.getTimeInstance().format(
                new Date(mCurrentLocation.getTime())),
                Toast.LENGTH_SHORT)
                .show();
        updateLocations();
    }

    @Override
    public void onLocationSettingsResultSuccess(LocationSettingsResponse locationSettingsResponse) {
        Log.i(TAG, "All location settings are satisfied.");
        startPositioningService();
    }

    @Override
    public void onLocationSettingsResultFailure(@NonNull Exception e) {
        int statusCode = ((ApiException) e).getStatusCode();
        switch (statusCode) {
            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                Log.i(TAG, "Location settings are not satisfied. Attempting to upgrade " +
                        "location settings ");
                try {
                    // Show the dialog by calling startResolutionForResult(), and check the
                    // result in onActivityResult().
                    ResolvableApiException rae = (ResolvableApiException) e;
                    rae.startResolutionForResult(getActivity(), REQUEST_CHECK_SETTINGS);
                } catch (IntentSender.SendIntentException sie) {
                    Log.i(TAG, "PendingIntent unable to execute request.");
                }
                break;
            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                // TODO: Notify user:
                // Location settings are inadequate, and cannot be fixed here.
                // Fix in Settings.
                mRequestingLocationUpdates = false;
                break;
        }

    }

    /**
     * Updates fields based on data stored in the bundle.
     * @param savedInstanceState The activity state saved in the Bundle.
     */
    private void updateValuesFromBundle(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            // Update the value of mCurrentLocation from the Bundle
            if (savedInstanceState.keySet().contains(KEY_SELF_LOCATION)) {
                mCurrentLocation = savedInstanceState.getParcelable(KEY_SELF_LOCATION);
                Toast.makeText(getActivity(), "Location updated from savedInstanceState", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void checkLocationSettings() {
        //LocationRequest request = LocationRequestManager.getRequest();
        LocationSettingsHelper helper = new LocationSettingsHelper(this, getActivity());
        helper.checkLocationSettings();
    }

    /**
     * Handles when the activity has received a result from the intent.
     * This callback is intentionally implemented in this fragment for cohesion
     */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            // Check for the integer request code originally supplied to startResolutionForResult().
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        Log.i(TAG, "User agreed to make required location settings changes.");
                        // Nothing to do. startLocationupdates() gets called in onResume again.
                        break;
                    case Activity.RESULT_CANCELED:
                        Log.i(TAG, "User chose not to make required location settings changes.");
                        mRequestingLocationUpdates = false;
                        updateLocations();
                        break;
                }
                break;
        }
    }

    /**
     * Sets the value of the UI fields for the location latitude, longitude and last update time.
     */
    private void updateLocations() {
        if (mCurrentLocation != null) {
            mMapUIManager.onSelfLocationUpdate(mCurrentLocation);
        }
    }

    /**
     * Callback received when a permissions request has been completed.
     * This callback is intentionally implemented in this fragment for cohesion
     */
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        Log.i(TAG, "onRequestPermissionResult");
        if (requestCode == LocationPermissionHelper.REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length <= 0) {
                // If user interaction was interrupted, the permission request is cancelled and you
                // receive empty arrays.
                Log.i(TAG, "User interaction was cancelled.");
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (mRequestingLocationUpdates) {
                    Log.i(TAG, "Permission granted, updates requested, starting location updates");
                    checkLocationSettings();
                }
            } else {
                // Permission denied.
                Toast.makeText(getActivity(), R.string.permission_denied_explanation_location, Toast.LENGTH_SHORT).show();

                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null);
                intent.setData(uri);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        }
    }

    /**
     * Start positioning service ONLY AFTER :
     * 1. location permission has been granted
     * 2. location settings are okay.
     */
    private void startPositioningService() {
        mRequestingLocationUpdates = true;
        ServiceManager.startPositioningService(getActivity());
    }

    /**
     * Register all receivers. Should be called in onResume().
     */
    public void registerReceivers() {
        registerPositioningReceiver();
        registerServerLocationsReceiver();

    }

    public void registerPositioningReceiver() {
        IntentFilter filter = new IntentFilter(SelfPositionReceiver.ACTION_SELF_POSITION);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        mPositioningReceiver = new SelfPositionReceiver(this);
        getActivity().registerReceiver(mPositioningReceiver, filter);
    }

    public void registerServerLocationsReceiver() {
        IntentFilter filter = new IntentFilter(GeoQueryLocationsReceiver.ACTION_GEOQUERY_LOCATIONS);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        mServerLocationsReceiver = new GeoQueryLocationsReceiver();
        getActivity().registerReceiver(mServerLocationsReceiver, filter);
    }

    /**
     * Unregister all receives. Should be called in onPause().
     */
    public void unregisterReceivers() {
        getActivity().unregisterReceiver(mPositioningReceiver);
    }


    public interface OnMapContainerFragmentInteractionListener {
        // TODO: Update argument type and name
        void onMapContainerFragmentInteraction(Uri uri);
    }
}
