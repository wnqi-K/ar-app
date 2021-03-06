package com.comp30022.arrrrr.utils;

import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.comp30022.arrrrr.R;

/**
 * Helper class for checking location access permissions.
 * This class is currently considered for API level 17.
 * With API level 23 we could have more encapsulation with checking location permission
 * inside the class.
 *
 * @author Dafu Ai
 */
public class LocationPermissionHelper {
    /**
     * Code used in requesting runtime permissions.
     */
    public static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;
    private static final String TAG = LocationPermissionHelper.class.getSimpleName();
    private AppCompatActivity mContext;

    public LocationPermissionHelper(AppCompatActivity context) {
        this.mContext = context;
    }

    /**
     * Return the current state of the permissions needed.
     */
    public boolean checkPermissions() {
        int permissionState = ActivityCompat.checkSelfPermission(mContext,
                android.Manifest.permission.ACCESS_FINE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Request location access permission from user.
     */
    public void requestPermissions() {
        boolean shouldProvideRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(mContext,
                        android.Manifest.permission.ACCESS_FINE_LOCATION);

        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale) {
            Log.i(TAG, "Displaying permission rationale to provide additional context.");
            Toast.makeText(mContext, R.string.permission_rationale_location, Toast.LENGTH_SHORT).show();

        } else {
            Log.i(TAG, "Requesting permission");
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".
        }
        ActivityCompat.requestPermissions(mContext,
                new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                REQUEST_PERMISSIONS_REQUEST_CODE);
    }
}
