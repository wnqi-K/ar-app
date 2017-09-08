package com.comp30022.arrrrr.utils;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.support.v4.content.ContextCompat;

import com.comp30022.arrrrr.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Manages GooglesMaps UI.
 *
 * @author Dafu Ai
 */

public class MapUIManager {

    // Default parameters
    private final static float DEFAULT_CAMERA_ZOOM_LEVEL = 15;
    private final double CIRCLE_RADIUS = 80;
    private final int CIRCLE_STROKE_COLOR = Color.argb(150, 0, 191, 255);
    private final int CIRCLE_FILL_COLOR = Color.argb(40, 0, 191, 255);
    private final int CIRCLE_STROKE_WIDTH = 3;

    private GoogleMap mGoogleMap;
    private Marker mSelfMarker;
    private Circle mSelfCircle;
    private Context mContext;
    private Fragment mFragment;

    public MapUIManager(Fragment fragment, Context context, GoogleMap googleMap) {
        mContext = context;
        mGoogleMap = googleMap;
        mFragment = fragment;
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
    public void onSelfLocationUpdate(Location location) {
        LatLng currLatLng = locationToLatLng(location);

        if(mSelfCircle == null) {
            mSelfCircle = mGoogleMap.addCircle(new CircleOptions()
                    .center(currLatLng)
                    .radius(CIRCLE_RADIUS)
                    .strokeColor(CIRCLE_STROKE_COLOR)
                    .fillColor(CIRCLE_FILL_COLOR)
                    .strokeWidth(CIRCLE_STROKE_WIDTH));
        } else {
            mSelfCircle.setCenter(currLatLng);
        }

        if(mSelfMarker == null) {
            mSelfMarker = mGoogleMap.addMarker(new MarkerOptions()
                    .title("My position")
                    .position(currLatLng)
                    .icon(MapUIManager.bitmapDescriptorFromVector(mContext, R.drawable.ic_radio_button_checked_dodgeblue_24dp))
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
    public void saveCurrentMapView(Location currentLocation) {
        if(currentLocation != null) {
            SharedPreferences sharedPref = mFragment.getActivity().getPreferences(Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putFloat(mFragment.getString(R.string.saved_camera_lat), (float) currentLocation.getLatitude());
            editor.putFloat(mFragment.getString(R.string.saved_camera_long), (float) currentLocation.getLongitude());
            editor.apply();
        }
    }

    /**
     * Convert a vector asset resource to a {@link BitmapDescriptor}
     */
    public static BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorResId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
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
