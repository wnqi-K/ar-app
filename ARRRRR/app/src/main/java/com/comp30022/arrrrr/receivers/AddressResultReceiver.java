package com.comp30022.arrrrr.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Address;
import android.location.Location;

import com.comp30022.arrrrr.services.FetchAddressIntentService;
import com.comp30022.arrrrr.utils.Constants;

/**
 * Receiver for address fetching result.
 *
 * @author Dafu Ai
 */

public class AddressResultReceiver extends BroadcastReceiver {

    public static final String ACTION_ADDRESS_RESULT = Constants.ACTION_PACKAGE + ".ADDRESS_RESULT";

    private AddressResultListener mListener;

    public AddressResultReceiver(AddressResultListener mListener) {
        this.mListener = mListener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Boolean success = intent.getBooleanExtra(FetchAddressIntentService.PARAM_OUT_SUCCESS, false);
        Location location = intent.getParcelableExtra(FetchAddressIntentService.PARAM_OUT_LOCATION_DATA);
        if (success) {
            Address address = intent.getParcelableExtra(FetchAddressIntentService.PARAM_OUT_ADDRESS);
            mListener.onAddressFetchSuccess(address, location);
        } else {
            mListener.onAddressFetchFailure(location);
        }
    }

    /**
     * Required interface to inject into {@link AddressResultReceiver}.
     */
    public interface AddressResultListener {
        /**
         * Handles when an address has been successfully fetched from a location.
         * @param address address fetched
         * @param location original location
         */
        void onAddressFetchSuccess(Address address, Location location);

        /**
         * Handles when there is a failure fetching address.
         * @param location original location
         */
        void onAddressFetchFailure(Location location);
    }

    /**
     * Provides receiver registration functionality.
     * @param context context which registration is attached
     * @param listener {@link AddressResultListener}
     * @return a AddressResultReceiver object which has been registered
     */
    public static AddressResultReceiver register(Context context,
                                                 AddressResultListener listener) {
        AddressResultReceiver receiver = new AddressResultReceiver(listener);
        IntentFilter filter = new IntentFilter(ACTION_ADDRESS_RESULT);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        context.registerReceiver(receiver, filter);
        return receiver;
    }
}
