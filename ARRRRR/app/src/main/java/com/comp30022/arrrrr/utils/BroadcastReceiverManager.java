package com.comp30022.arrrrr.utils;

import android.content.BroadcastReceiver;
import android.content.Context;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages a list of broadcast receivers.
 * Provides reliable method to unregister receivers.
 *
 * @author Dafu Ai
 */

public class BroadcastReceiverManager {

    /**
     * List of managing receivers
     */
    private List<BroadcastReceiver> mReceiverList;

    /**
     * Context for the receivers
     */
    private Context mContext;

    public BroadcastReceiverManager(Context context) {
        mReceiverList = new ArrayList<>();
        mContext = context;
    }

    /**
     * Add receiver to managing list
     */
    public void add(BroadcastReceiver receiver) {
        mReceiverList.add(receiver);
    }

    /**
     * Reliable way to unregister receivers
     */
    public void unregisterAll() {
        for (BroadcastReceiver receiver : mReceiverList) {
            try {
                mContext.unregisterReceiver(receiver);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
        }
    }
}
