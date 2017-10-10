package com.comp30022.arrrrr.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.comp30022.arrrrr.R;

/**
 * Provides shared entry points for getting {@link SharedPreferences}
 *
 * @author Dafu Ai
 */

public class PreferencesAccess {
    public static final String PREF_SETTINGS = "PREF_SETTINGS";

    /**
     * Internal method to get shared preferences across this app
     */
    public static SharedPreferences getSettingsPreferences(Context context) {
        return context.getSharedPreferences(
                PREF_SETTINGS,
                Context.MODE_PRIVATE);
    }
}
