package com.comp30022.arrrrr.ar;

import android.location.Location;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by krzysztofjackowski on 24/09/15.
 */
public interface OnLocationChangedListener {
    void onLocationChanged(Location currentLocation);
}