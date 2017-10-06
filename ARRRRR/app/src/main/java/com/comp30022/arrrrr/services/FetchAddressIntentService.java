package com.comp30022.arrrrr.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.comp30022.arrrrr.receivers.AddressResultReceiver;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Asynchronously handles an intent using a worker thread. Receives a ResultReceiver object and a
 * location through an intent. Tries to fetch the address for the location using a Geocoder, and
 * sends the result to the AddressResultReceiver.
 */

public class FetchAddressIntentService extends IntentService {
    private static final String TAG = FetchAddressIntentService.class.getSimpleName();

    public static final String PARAM_IN_LOCATION_DATA = "IN_LOCATION_DATA";
    public static final String PARAM_OUT_SUCCESS = "OUT_SUCCESS";
    public static final String PARAM_OUT_ADDRESS = "OUT_ADDRESS";
    public static final String PARAM_OUT_LOCATION_DATA = "OUT_LOCATION_DATA";

    public FetchAddressIntentService() {
        super(TAG);
    }

    public FetchAddressIntentService(String name) {
        super(name);
    }

    /**
     * Tries to get the location address using a Geocoder. If successful, sends an address to a
     * result receiver. If unsuccessful, reports the error message internally.
     */
    @Override
    protected void onHandleIntent(Intent intent) {

        // Get the location passed to this service through an extra.
        Location location = intent.getParcelableExtra(PARAM_IN_LOCATION_DATA);

        if (location == null) {
            Log.v(TAG, "Error fetching address: no location data provided.");
            return;
        }

        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        // Address found using the Geocoder.
        List<Address> addresses;

        try {
            // The results are a best guess and are not guaranteed to be accurate.
            addresses = geocoder.getFromLocation(
                    location.getLatitude(),
                    location.getLongitude(),
                    // In this sample, we get just a single address.
                    1);
        } catch (IOException ioException) {
            // Catch network or other I/O problems.
            Log.v(TAG, "Error fetching address: service not available.");
            sendResultBroadcast(false, null, location);
            return;
        } catch (IllegalArgumentException illegalArgumentException) {
            // Catch invalid latitude or longitude values.
            Log.v(TAG, "Error fetching address: invalid latitude or longitude values. " +
                    "Latitude = " + location.getLatitude() +
                    ", Longitude = " + location.getLongitude(), illegalArgumentException);
            sendResultBroadcast(false, null, location);
            return;
        }

        Log.v(TAG, "Fetching address: an address has been found.");
        Address address = addresses.get(0);
        sendResultBroadcast(true, address, location);
    }

    /**
     * Send an intent to {@link FetchAddressIntentService} to request fetching location to address
     */
    public static void requestFetchAddress(Context context, Location location) {
        Intent intent = new Intent(context, FetchAddressIntentService.class);
        intent.putExtra(FetchAddressIntentService.PARAM_IN_LOCATION_DATA, location);
        context.startService(intent);
    }

    /**
     * Sends the address result to the receiver.
     */
    private void sendResultBroadcast(Boolean success, @Nullable Address address, @NonNull Location location) {
        // Construct and send broadcast.
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(AddressResultReceiver.ACTION_ADDRESS_RESULT);

        broadcastIntent.putExtra(PARAM_OUT_SUCCESS, success);
        broadcastIntent.putExtra(PARAM_OUT_ADDRESS, address);
        broadcastIntent.putExtra(PARAM_OUT_LOCATION_DATA, location);

        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
        sendBroadcast(broadcastIntent);
    }
}
