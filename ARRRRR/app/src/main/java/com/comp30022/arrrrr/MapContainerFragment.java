package com.comp30022.arrrrr;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.SharedPreferences;
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
import com.comp30022.arrrrr.utils.LocationPermissionHelper;
import com.comp30022.arrrrr.utils.LocationRequestManager;
import com.comp30022.arrrrr.utils.LocationSettingsHelper;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.text.DateFormat;
import java.util.Date;

/**
 * Fragment containing map interface.
 *
 * @author Dafu Ai
 */
public class MapContainerFragment extends Fragment implements
        OnMapReadyCallback,
        SelfPositionReceiver.SelfPositionUpdateListener,
        LocationSettingsHelper.OnLocationSettingsResultListener {

    private static final String TAG = MapContainerFragment.class.getSimpleName();

    /**
     * Constant used in the location settings dialog.
     */
    private static final int REQUEST_CHECK_SETTINGS = 0x1;

    // Keys for storing activity state in the Bundle.
    private final static String KEY_SELF_LOCATION = "self_location";

    /**
     * Default zoom level value of the map camera
     */
    private final static float DEFAULT_CAMERA_ZOOM_LEVEL = 15;

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
     * Marker on the map of the user's device.
     */
    private Marker mSelfMarker;

    /**
     * Receiver for self positioning service.
     */
    private SelfPositionReceiver mPositioningReceiver;

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
        saveCurrentMapView();
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

        // Sets up permission checker.
        mPermissionChecker = new LocationPermissionHelper(mContext);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.mGoogleMap = googleMap;
        Toast.makeText(mContext, "Map ready", Toast.LENGTH_SHORT).show();
        initializeMapUI();
        updateLocations();
        restoreCurrentMapView();
    }

    @Override
    public void onSelfLocationUpdate(Location location) {
        mCurrentLocation = location;
        Toast.makeText(mContext, DateFormat.getTimeInstance().format(
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
                    rae.startResolutionForResult(mContext, REQUEST_CHECK_SETTINGS);
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
     * Initialize map interface.
     */
    private void initializeMapUI() {
        mGoogleMap.setMinZoomPreference(5);
        mGoogleMap.setMaxZoomPreference(20);
        mGoogleMap.animateCamera(CameraUpdateFactory.zoomTo(DEFAULT_CAMERA_ZOOM_LEVEL));
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
                Toast.makeText(mContext, "Location updated from savedInstanceState", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void checkLocationSettings() {
        //LocationRequest request = LocationRequestManager.getRequest();
        LocationSettingsHelper helper = new LocationSettingsHelper(this, mContext);
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
            // Add a marker in Sydney, Australia,
            // and move the map's camera to the same location.
            Double latitude = mCurrentLocation.getLatitude();
            Double longitude = mCurrentLocation.getLongitude();
            LatLng currLatLng = new LatLng(latitude, longitude);

            if(mSelfMarker == null) {
                mSelfMarker = mGoogleMap.addMarker(new MarkerOptions()
                        .title("My position")
                        .position(currLatLng));
            } else {
                mSelfMarker.setPosition(currLatLng);
            }
            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(currLatLng));
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
                Toast.makeText(mContext, R.string.permission_denied_explanation_location, Toast.LENGTH_SHORT).show();

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

        Intent mPositioningServiceIntent = new Intent(mContext, PositioningService.class);
        mPositioningServiceIntent.putExtra(PositioningService.PARAM_IN_PERM_GRANTED, true);
        mContext.startService(mPositioningServiceIntent);
        Toast.makeText(mContext, "Starting positioning service...", Toast.LENGTH_SHORT).show();
    }

    /**
     * Register all receivers. Should be called in onResume().
     */
    public void registerReceivers() {
        registerPositioningReceiver();
    }

    public void registerPositioningReceiver() {
        IntentFilter filter = new IntentFilter(SelfPositionReceiver.ACTION_SELF_POSITION);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        mPositioningReceiver = new SelfPositionReceiver(this);
        mContext.registerReceiver(mPositioningReceiver, filter);
    }

    /**
     * Unregister all receives. Should be called in onPause().
     */
    public void unregisterReceivers() {
        mContext.unregisterReceiver(mPositioningReceiver);
    }

    /**
     * Save current view of the map into shared preferences.
     */
    public void restoreCurrentMapView() {
        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        double latitude = sharedPref.getFloat(getString(R.string.saved_camera_lat), (float) -37.8141);
        double longitude = sharedPref.getFloat(getString(R.string.saved_camera_long), (float) 144.9633);
        LatLng currLatLng = new LatLng(latitude, longitude);
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(currLatLng));
        mGoogleMap.animateCamera(CameraUpdateFactory.zoomTo(DEFAULT_CAMERA_ZOOM_LEVEL));

    }

    /**
     * Save current view of the map into shared preferences.
     */
    public void saveCurrentMapView() {
        if(mCurrentLocation != null) {
            SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putFloat(getString(R.string.saved_camera_lat), (float) mCurrentLocation.getLatitude());
            editor.putFloat(getString(R.string.saved_camera_long), (float) mCurrentLocation.getLongitude());
            editor.apply();
        }
    }

    public interface OnMapContainerFragmentInteractionListener {
        // TODO: Update argument type and name
        void onMapContainerFragmentInteraction(Uri uri);
    }
}
