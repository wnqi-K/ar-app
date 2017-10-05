package com.comp30022.arrrrr.utils;

import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Location;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.comp30022.arrrrr.ArViewActivity;
import com.comp30022.arrrrr.ChatActivity;
import com.comp30022.arrrrr.R;
import com.comp30022.arrrrr.animations.LatLngInterpolator;
import com.comp30022.arrrrr.animations.MarkerAnimation;
import com.comp30022.arrrrr.database.UserManagement;
import com.comp30022.arrrrr.models.GeoLocationInfo;
import com.comp30022.arrrrr.receivers.AddressResultReceiver;
import com.comp30022.arrrrr.receivers.GeoQueryLocationsReceiver;
import com.comp30022.arrrrr.receivers.SelfPositionReceiver;
import com.comp30022.arrrrr.services.FetchAddressIntentService;
import com.comp30022.arrrrr.services.LocationSharingService;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashMap;

/**
 * Manages GooglesMaps UI.
 *
 * @author Dafu Ai
 */

public class MapUIManager implements
        GeoQueryLocationsReceiver.GeoQueryLocationsListener,
        SelfPositionReceiver.SelfLocationListener,
        GoogleMap.OnMarkerClickListener,
        AddressResultReceiver.AddressResultListener,
        GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnCameraMoveListener,
        GoogleMap.OnInfoWindowClickListener {

    private final String TAG = MapUIManager.class.getSimpleName();

    // Default parameters
    private final static float DEFAULT_CAMERA_ZOOM_LEVEL = 15;
    private final double CIRCLE_RADIUS = 80;
    private final int CIRCLE_STROKE_COLOR = Color.argb(150, 0, 191, 255);
    private final int CIRCLE_FILL_COLOR = Color.argb(40, 0, 191, 255);
    private final int CIRCLE_STROKE_WIDTH = 3;
    private final int PROFILE_ICON_WIDTH = 100;
    private final int PROFILE_ICON_HEIGHT = 100;
    private final double DEFAULT_INI_LAT = -37.8141;
    private final double DEFAULT_INT_LONG = 144.9633;


    private GoogleMap mGoogleMap;
    private Marker mSelfMarker;
    private HashMap<String, Marker> mFriendMarkers;
    private HashMap<String, LatLng> mUserGeoLocations;
    private HashMap<String, GeoLocationInfo> mUserGeoLocationInfos;
    private Circle mSelfCircle;
    private AppCompatActivity mContext;
    private Fragment mFragment;
    private HashMap<String, Bitmap> mFriendIcons;
    private String mBufferFriendUid;

    public MapUIManager(Fragment fragment, AppCompatActivity context, GoogleMap googleMap) {
        mContext = context;
        mGoogleMap = googleMap;
        mFragment = fragment;
        mFriendMarkers = new HashMap<>();
        mFriendIcons = new HashMap<>();
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        if (mFriendMarkers.containsValue(marker)) {
            String uid = (String) marker.getTag();
            marker.showInfoWindow();
        }
        return true;
    }

    /**
     * Show selection popup for user to perform action associated with the friend's marker.
     */
    @Override
    public void onInfoWindowClick(Marker marker) {
        CharSequence options[] = new CharSequence[]{"Navigate by AR", "Chat"};

        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        String uid = (String) marker.getTag();
        mBufferFriendUid = uid;
        builder.setTitle(uid);
        builder.setIcon(new BitmapDrawable(mContext.getResources(), mFriendIcons.get(uid)));
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                switch (i) {
                    case 0:
                        switchToAR(mBufferFriendUid);
                        break;
                    case 1:
                        switchToChat(mBufferFriendUid);
                        break;
                }
            }
        });
        builder.show();
    }

    /**
     * Customized info window for display when a friend's marker is clicked.
     */
    private class FriendInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {
        private final View mContents;

        public FriendInfoWindowAdapter() {
            this.mContents = mContext.getLayoutInflater().inflate(R.layout.friend_info_contents, null);
        }

        @Override
        public View getInfoWindow(Marker marker) {
            return null;
        }

        @Override
        public View getInfoContents(Marker marker) {
            render(marker, mContents);
            return mContents;
        }

        private void render(Marker marker, View view) {
            String uid = (String) marker.getTag();
            TextView textViewClickHint = (TextView) view.findViewById(R.id.text_view_click_hint);
            TextView textViewUserName = (TextView) view.findViewById(R.id.text_view_user_name);
            TextView textViewGeoInfo = (TextView) view.findViewById(R.id.text_view_location_info);
            // Get time string (in local timezone)
            String time = TimeUtil.getFriendlyTime(mUserGeoLocationInfos.get(uid).time);

            textViewClickHint.setText(R.string.friend_info_window_click_hint);
            textViewUserName.setText(uid);
            textViewGeoInfo.setText(time);
        }
    }

    @Override
    public void onAddressFetchSuccess(Address address, Location location) {
        TextView textViewAddress = (TextView) mContext.findViewById(R.id.text_view_address);
        textViewAddress.setText(address.getAddressLine(0));
        hideProgressBarLocating();
    }

    @Override
    public void onAddressFetchFailure(Location location) {
        TextView textViewAddress = (TextView) mContext.findViewById(R.id.text_view_address);
        textViewAddress.setText(R.string.text_fetching_address);
        hideProgressBarLocating();
    }

    /**
     * Handles a GeoQuery event result by updating markers accordingly.
     */
    @Override
    public void onGeoQueryEvent(String type,
                                String key,
                                HashMap<String, LatLng> geoLocations,
                                HashMap<String, GeoLocationInfo> geoLocationInfos) {
        if (type == null) {
            Log.v(TAG, "Error receiving intent content.");
            return;
        }
        if (type.equals(LocationSharingService.ON_KEY_ENTERED)) {
            updateLocalGeoData(geoLocations, geoLocationInfos);
            addFriendMarker(key);
        } else if (type.equals(LocationSharingService.ON_KEY_EXITED)) {
            // Remove marker
            mFriendMarkers.get(key).remove();
            mFriendMarkers.remove(key);
        } else if (type.equals(LocationSharingService.ON_KEY_MOVED)) {
            if (!geoLocations.containsKey(key)) {
                // A new friend!
                updateLocalGeoData(geoLocations, geoLocationInfos);
                addFriendMarker(key);
            } else {
                // Move marker
                LatLng position = geoLocations.get(key);
                moveMarkerSmoothly(key, position);
                updateLocalGeoData(geoLocations, geoLocationInfos);
            }
        } else if (type.equals(LocationSharingService.ON_REQUEST_CURR_DATA)) {
            updateLocalGeoData(geoLocations, geoLocationInfos);
            restoreFriendMarkers();
        }
    }

    /**
     * Initialize GoogleMaps UI:
     * - set up listeners for GoogleMap
     * - restore previous camera view
     * - add customized my location button
     */
    public void initializeMapUI() {
        mGoogleMap.setOnMarkerClickListener(this);
        mGoogleMap.setInfoWindowAdapter(new FriendInfoWindowAdapter());
        mGoogleMap.setOnInfoWindowClickListener(this);
        mGoogleMap.setOnCameraMoveListener(this);
        mGoogleMap.setOnMyLocationButtonClickListener(this);
        restorePrevMapView();
        requestToGetCurrData();

        // Relocate my location button
        View locationButton = ((View) mContext.findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
        RelativeLayout.LayoutParams rlp = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();
        // position on right bottom
        rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
        rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
        rlp.setMargins(0, 220, 180, 0);
    }

    /**
     * Handles when there is a location update.
     * Adjust marker's and circle's locations in accordance with the new location.
     */
    @Override
    public void onSelfLocationChanged(Location location) {
        requestFetchAddress(location);
        showProgressBarLocating();

        LatLng currLatLng = locationToLatLng(location);

        if (mSelfCircle == null) {
            mSelfCircle = mGoogleMap.addCircle(new CircleOptions()
                    .center(currLatLng)
                    .radius(CIRCLE_RADIUS)
                    .strokeColor(CIRCLE_STROKE_COLOR)
                    .fillColor(CIRCLE_FILL_COLOR)
                    .strokeWidth(CIRCLE_STROKE_WIDTH));
        } else {
            mSelfCircle.setCenter(currLatLng);
        }

        if (mSelfMarker == null) {
            mSelfMarker = mGoogleMap.addMarker(new MarkerOptions()
                    .title("My position")
                    .position(currLatLng)
                    .icon(MapUIManager.bitmapDescriptorFromVector(
                            mContext,
                            R.drawable.ic_radio_button_checked_dodgeblue_24dp,
                            1))
                    .anchor(0.5f, 0.5f));
        } else {
            mSelfMarker.setPosition(currLatLng);
        }

        animateCameraToPosition(currLatLng);
    }

    @Override
    public boolean onMyLocationButtonClick() {
        if (isMyPosInitialized()) {
            animateCameraToPosition(mSelfMarker.getPosition());
        }
        return true;
    }

    @Override
    public void onCameraMove() {
        if (isMyPosInitialized()) {
            if (mGoogleMap.getCameraPosition().target != mSelfMarker.getPosition() ||
                    mGoogleMap.getCameraPosition().zoom != DEFAULT_CAMERA_ZOOM_LEVEL) {
                //noinspection MissingPermission
                mGoogleMap.setMyLocationEnabled(true);
            } else {
                //noinspection MissingPermission
                mGoogleMap.setMyLocationEnabled(false);
            }
        }
    }

    /**
     * Animate camera to specified position and adjust the zoom level
     */
    private void animateCameraToPosition(LatLng position) {
        CameraUpdate cameraUpdate =
                CameraUpdateFactory.newLatLngZoom(position, DEFAULT_CAMERA_ZOOM_LEVEL);
        mGoogleMap.animateCamera(cameraUpdate);
    }

    /**
     * Update local data (from geo query results)
     */
    private void updateLocalGeoData(HashMap<String, LatLng> geoLocations,
                                    HashMap<String, GeoLocationInfo> geoLocationInfos) {
        mUserGeoLocations = geoLocations;
        mUserGeoLocationInfos = geoLocationInfos;
    }

    /**
     * Add a new marker associated with a friend.
     *
     * @param key friend's uid
     */
    private void addFriendMarker(String key) {
        // Create new marker
        LatLng position = mUserGeoLocations.get(key);
        Log.v(TAG, "Found a friend at ("
                + String.valueOf(position.latitude)
                + ", "
                + String.valueOf(position.longitude)
                + ").");

        Bitmap profileBitmap = UserManagement.getInstance().getUserProfileImage(key, mContext);
        Bitmap circleBitmap = BitmapUtil.getCircleCrop(profileBitmap);
        Bitmap profileIconBitmap = BitmapUtil.getResizedBitmap(circleBitmap, PROFILE_ICON_WIDTH, PROFILE_ICON_HEIGHT);
        BitmapDescriptor iconDescriptor = BitmapDescriptorFactory.fromBitmap(profileIconBitmap);

        Marker FriendMarker = mGoogleMap.addMarker(new MarkerOptions()
                .position(position)
                .icon(iconDescriptor)
        );
        FriendMarker.setTag(key);

        mFriendMarkers.put(key, FriendMarker);
        if (!mFriendIcons.containsKey(key)) {
            mFriendIcons.put(key, profileIconBitmap);
        }
    }

    /**
     * Move marker smoothly to a new position
     *
     * @param key user's uid
     */
    private void moveMarkerSmoothly(String key, LatLng position) {
        // Animate marker instead of simply changing its location
        LatLngInterpolator interpolator = new LatLngInterpolator.LinearFixed();
        MarkerAnimation.animateMarkerToICS(mFriendMarkers.get(key), position, interpolator);
    }


    /**
     * Switch to AR window, targeting the specified friend.
     *
     * @param uid Friend's uid
     */
    private void switchToAR(String uid) {
        LatLng location = mUserGeoLocations.get(uid);
        ArViewActivity.startActivity(mContext, uid, location);
    }

    /**
     * Switch to chat window, targeting the specified friend.
     *
     * @param uid Friend's uid
     */
    private void switchToChat(String uid) {
        ChatActivity.startActivity(mContext, uid);
    }

    /**
     * Determines whether the map has got a self position at least once.
     */
    private boolean isMyPosInitialized() {
        return mSelfMarker != null && mSelfMarker.getPosition() != null;
    }

    /**
     * Show progress bar for locating process.
     */
    private void showProgressBarLocating() {
        ProgressBar progressBar = (ProgressBar) mContext.findViewById(R.id.progress_bar_locating);
        progressBar.setVisibility(View.VISIBLE);
    }

    /**
     * Hide progress bar after locating process is complete.
     */
    private void hideProgressBarLocating() {
        ProgressBar progressBar = (ProgressBar) mContext.findViewById(R.id.progress_bar_locating);
        progressBar.setVisibility(View.GONE);
    }

    /**
     * Send an intent to {@link FetchAddressIntentService} to request fetching location to address
     */
    private void requestFetchAddress(Location location) {
        Intent intent = new Intent(mContext, FetchAddressIntentService.class);
        intent.putExtra(FetchAddressIntentService.PARAM_IN_LOCATION_DATA, location);
        mContext.startService(intent);
    }

    /**
     * Save current view of the map into shared preferences.
     */
    private void restorePrevMapView() {
        SharedPreferences sharedPref = PreferencesAccess.getSettingsPreferences(mFragment.getActivity());
        double latitude = sharedPref.getFloat(mFragment.getString(R.string.PREF_KEY_CAMERA_LAT), (float) DEFAULT_INI_LAT);
        double longitude = sharedPref.getFloat(mFragment.getString(R.string.PREF_KEY_CAMERA_LONG), (float) DEFAULT_INT_LONG);
        LatLng currLatLng = new LatLng(latitude, longitude);

        animateCameraToPosition(currLatLng);

        if (latitude != DEFAULT_INI_LAT) {
            Log.v(TAG, "Last map view has been restored.");
        }
    }

    /**
     * Send a request intent to location sharing service to get current geo data.
     */
    private void requestToGetCurrData() {
        Intent intent = new Intent(mContext, LocationSharingService.class);
        intent.putExtra(LocationSharingService.PARAM_IN_REQUEST_TYPE,
                        LocationSharingService.REQUEST_GET_CURR_GEODATA);
        mContext.startService(intent);
    }

    /**
     * Restore previous markers
     * ('previous' refers to last time the map is opened in current app lifecycle)
     */
    private void restoreFriendMarkers() {
        // Check whether we do have previous data first.
        if (mUserGeoLocations != null) {
            for (String key : mUserGeoLocations.keySet()) {
                addFriendMarker(key);
            }
            Log.v(TAG, "Previous (friend) markers have been restored.");
        }
    }

    /**
     * Save current view of the map into shared preferences.
     */
    public void saveCurrentMapView() {
        if (isMyPosInitialized()) {
            SharedPreferences sharedPref = PreferencesAccess.getSettingsPreferences(mFragment.getActivity());
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putFloat(mFragment.getString(R.string.PREF_KEY_CAMERA_LAT), (float) mSelfMarker.getPosition().latitude);
            editor.putFloat(mFragment.getString(R.string.PREF_KEY_CAMERA_LONG), (float) mSelfMarker.getPosition().longitude);
            editor.apply();
        }
    }

    /**
     * Convert a vector asset resource to a {@link BitmapDescriptor}
     *
     * @param enlarge enlarge from original size
     */
    public static BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorResId, int enlarge) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth() * enlarge, vectorDrawable.getIntrinsicHeight() * enlarge);
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth() * enlarge, vectorDrawable.getIntrinsicHeight() * enlarge, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    /**
     * Convert a {@link Location} to a {@link LatLng}
     */
    public static LatLng locationToLatLng(Location location) {
        Double latitude = location.getLatitude();
        Double longitude = location.getLongitude();
        return new LatLng(latitude, longitude);
    }
}
