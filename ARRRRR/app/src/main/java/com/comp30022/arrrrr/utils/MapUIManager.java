package com.comp30022.arrrrr.utils;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.comp30022.arrrrr.R;
import com.comp30022.arrrrr.animations.LatLngInterpolator;
import com.comp30022.arrrrr.animations.MarkerAnimation;
import com.comp30022.arrrrr.models.GeoLocationInfo;
import com.comp30022.arrrrr.receivers.GeoQueryLocationsReceiver;
import com.comp30022.arrrrr.receivers.SelfPositionReceiver;
import com.comp30022.arrrrr.services.LocationSharingService;
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
        GoogleMap.OnMarkerClickListener {

    private final String TAG = MapUIManager.class.getSimpleName();

    // Default parameters
    private final static float DEFAULT_CAMERA_ZOOM_LEVEL = 15;
    private final double CIRCLE_RADIUS = 80;
    private final int CIRCLE_STROKE_COLOR = Color.argb(150, 0, 191, 255);
    private final int CIRCLE_FILL_COLOR = Color.argb(40, 0, 191, 255);
    private final int CIRCLE_STROKE_WIDTH = 3;
    private final int PROFILE_ICON_WIDTH = 100;
    private final int PROFILE_ICON_HEIGHT = 100;

    private GoogleMap mGoogleMap;
    private Marker mSelfMarker;
    private HashMap<String, Marker> mFriendMarkers;
    private HashMap<String, GeoLocationInfo> mUserGeoLocationInfos;
    private Circle mSelfCircle;
    private AppCompatActivity mContext;
    private Fragment mFragment;

    public MapUIManager(Fragment fragment, AppCompatActivity context, GoogleMap googleMap) {
        mContext = context;
        mGoogleMap = googleMap;
        mFragment = fragment;
        mFriendMarkers = new HashMap<>();

        mGoogleMap.setOnMarkerClickListener(this);
        mGoogleMap.setInfoWindowAdapter(new FriendInfoWindowAdapter());
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
            Button button = (Button) view.findViewById(R.id.button_enter_ar);
            TextView textViewUserName = (TextView) view.findViewById(R.id.text_view_user_name);
            TextView textViewGeoInfo = (TextView) view.findViewById(R.id.text_view_location_info);
            // Get time string (in local timezone)
            String time = TimeUtil.getFriendlyTime(mUserGeoLocationInfos.get(uid).time);

            button.setText(R.string.friend_info_window_button_enter_text);
            textViewUserName.setText(uid);
            textViewGeoInfo.setText(time);
        }
    }

    @Override
    public void onGeoQueryEvent(String type, String key, HashMap<String, LatLng> geoLocations, HashMap<String, GeoLocationInfo> geoLocationInfos) {
        if (type == null) {
            Log.v(TAG, "Error receiving intent content.");
            return;
        }
        if (type.equals(LocationSharingService.ON_KEY_ENTERED) ) {
            // Create new marker
            LatLng position = geoLocations.get(key);
            Log.v(TAG, "Found a friend at ("
                    + String.valueOf(position.latitude)
                    + ", "
                    + String.valueOf(position.longitude)
                    + ").");


            // TODO: use real profile photo when ready
            Bitmap profileBitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.portrait_photo);
            Bitmap circleBitmap = BitmapUtil.getCircleCrop(profileBitmap);
            Bitmap profileIconBitmap = BitmapUtil.getResizedBitmap(circleBitmap, PROFILE_ICON_WIDTH, PROFILE_ICON_HEIGHT);
            BitmapDescriptor iconDescriptor = BitmapDescriptorFactory.fromBitmap(profileIconBitmap);

            Marker FriendMarker = mGoogleMap.addMarker(new MarkerOptions()
                    .position(position)
                    .icon(iconDescriptor)
            );
            FriendMarker.setTag(key);

            mFriendMarkers.put(key, FriendMarker);
            this.mUserGeoLocationInfos = geoLocationInfos;
        } else if (type.equals(LocationSharingService.ON_KEY_EXITED) ) {
            // Remove marker
            mFriendMarkers.get(key).remove();
            mFriendMarkers.remove(key);
        } else if (type.equals(LocationSharingService.ON_KEY_MOVED)) {
            // Move marker
            LatLng position = geoLocations.get(key);
            // mFriendMarkers.get(key).setPosition(position);
            // Animate marker intead of simply changing its location
            LatLngInterpolator interpolator = new LatLngInterpolator.LinearFixed();
            MarkerAnimation.animateMarkerToICS(mFriendMarkers.get(key), position, interpolator);
            this.mUserGeoLocationInfos = geoLocationInfos;
        }
    }

    /**
     * Initialize GoogleMaps UI:
     * TODO: add comments
     */
    public void initializeMapUI() {
        mGoogleMap.setMinZoomPreference(5);
        mGoogleMap.setMaxZoomPreference(20);
        mGoogleMap.animateCamera(CameraUpdateFactory.zoomTo(DEFAULT_CAMERA_ZOOM_LEVEL));
        restoreCurrentMapView();
    }

    /**
     * Handles when there is a location update.
     * Adjust marker's and circle's locations in accordance with the new location.
     */
    @Override
    public void onSelfLocationChanged(Location location) {
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

        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(currLatLng));
    }

    /**
     * Save current view of the map into shared preferences.
     */
    public void restoreCurrentMapView() {
        SharedPreferences sharedPref = mFragment.getActivity().getPreferences(Context.MODE_PRIVATE);
        double latitude = sharedPref.getFloat(mFragment.getString(R.string.saved_camera_lat), (float) -37.8141);
        double longitude = sharedPref.getFloat(mFragment.getString(R.string.saved_camera_long), (float) 144.9633);
        LatLng currLatLng = new LatLng(latitude, longitude);
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(currLatLng));
        mGoogleMap.animateCamera(CameraUpdateFactory.zoomTo(DEFAULT_CAMERA_ZOOM_LEVEL));
    }

    /**
     * Save current view of the map into shared preferences.
     */
    public void saveCurrentMapView() {
        if(mSelfMarker != null && mSelfMarker.getPosition() != null) {
            SharedPreferences sharedPref = mFragment.getActivity().getPreferences(Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putFloat(mFragment.getString(R.string.saved_camera_lat), (float) mSelfMarker.getPosition().latitude);
            editor.putFloat(mFragment.getString(R.string.saved_camera_long), (float) mSelfMarker.getPosition().longitude);
            editor.apply();
        }
    }

    /**
     * Convert a vector asset resource to a {@link BitmapDescriptor}
     * @param enlarge enlarge from original size
     */
    public static BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorResId, int enlarge) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth()*enlarge, vectorDrawable.getIntrinsicHeight()*enlarge);
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth()*enlarge, vectorDrawable.getIntrinsicHeight()*enlarge, Bitmap.Config.ARGB_8888);
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

//    /**
//     * Credit to https://stackoverflow.com/questions/29222864/get-radius-of-visible-map-in-android
//     * @param visibleRegion
//     * @return Approximate visible radius of the visible region
//     */
//    private double calculateVisibleRadius(VisibleRegion visibleRegion) {
//        float[] distanceWidth = new float[1];
//        float[] distanceHeight = new float[1];
//
//        LatLng farRight = visibleRegion.farRight;
//        LatLng farLeft = visibleRegion.farLeft;
//        LatLng nearRight = visibleRegion.nearRight;
//        LatLng nearLeft = visibleRegion.nearLeft;
//
//        //calculate the distance width (left <-> right of map on screen)
//        Location.distanceBetween(
//                (farLeft.latitude + nearLeft.latitude) / 2,
//                farLeft.longitude,2
//                (farRight.latitude + nearRight.latitude) / 2,
//                farRight.longitude,
//                distanceWidth
//        );
//
//        //calculate the distance height (top <-> bottom of map on screen)
//        Location.distanceBetween(
//                farRight.latitude,
//                (farRight.longitude + farLeft.longitude) / 2,
//                nearRight.latitude,
//                (nearRight.longitude + nearLeft.longitude) / 2,
//                distanceHeight
//        );
//
//        //visible radius is (smaller distance) / 2:
//        return (distanceWidth[0] < distanceHeight[0]) ? distanceWidth[0] / 2 : distanceHeight[0] / 2;
//    }
}
