package com.comp30022.arrrrr.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.comp30022.arrrrr.services.LocationSharingService;
import com.comp30022.arrrrr.utils.Constants;

/**
 * Receiver for simple request result from the services
 *
 * @author Dafu Ai
 */

public class SimpleRequestResultReceiver extends BroadcastReceiver {

    public static final String ACTION_SIMPLE_REQUEST_RESULT =
            Constants.ACTION_PACKAGE + ".ACTION_SIMPLE_REQUEST_RESULT";

    public SimpleRequestResultListener mListener;

    public SimpleRequestResultReceiver(SimpleRequestResultListener listener) {
        this.mListener = listener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String requestType = intent.getStringExtra(LocationSharingService.PARAM_OUT_REQUEST_TYPE);
        boolean success = intent.getBooleanExtra(LocationSharingService.PARAM_OUT_REQUEST_SUCCESS, false);

        // Notify listener.
        mListener.onReceivingSimpleRequestResult(requestType, success);
    }

    /**
     * Required interface in order to use this receiver.
     */
    public interface SimpleRequestResultListener {
        void onReceivingSimpleRequestResult(String requestType, boolean success);
    }

    /**
     * Provides receiver registration functionality.
     * @param context context which registration is attached
     * @param listener {@link SimpleRequestResultListener}
     * @return a SimpleRequestResultReceiver object which has been registered
     */
    public static SimpleRequestResultReceiver register(Context context,
                                                      SimpleRequestResultListener listener) {
        SimpleRequestResultReceiver receiver = new SimpleRequestResultReceiver(listener);
        IntentFilter filter = new IntentFilter(ACTION_SIMPLE_REQUEST_RESULT);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        context.registerReceiver(receiver, filter);
        return receiver;
    }
}
