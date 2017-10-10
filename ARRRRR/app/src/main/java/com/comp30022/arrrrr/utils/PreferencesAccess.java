package com.comp30022.arrrrr.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.comp30022.arrrrr.R;

/**
 * Provides shared entry points for getting {@link SharedPreferences}
 *
 * @author Dafu Ai
 * modified by zijie shen
 */

public class PreferencesAccess {
    /**
     * Name of the preference file
     */
    private static final String APP_PREFS = "application_preferences";
    public static final String PREF_SETTINGS = "PREF_SETTINGS";

    private static PreferencesAccess mInstance;
    private Context mContext;
    private SharedPreferences mSharedPreferences;
    private SharedPreferences.Editor mEditor;

    private PreferencesAccess(Context mContext) {
        this.mContext = mContext;
    }

    public static synchronized PreferencesAccess getInstance(Context context){
        if(mInstance == null){
            mInstance = new PreferencesAccess(context);
        }
        return mInstance;
    }

    /**
     * Save a string into shared preference
     *
     * @param key   The name of the preference to modify
     * @param value The new value for the preference
     */
    public void saveString(String key, String value) {
        mSharedPreferences = mContext.getSharedPreferences(APP_PREFS, Context.MODE_PRIVATE);
        mEditor = mSharedPreferences.edit();
        mEditor.putString(key, value);
        mEditor.commit();
    }

    /**
     * Retrieve a String value from the preferences.
     *
     * @param key The name of the preference to retrieve.
     * @return Returns the preference value if it exists, or null.
     * Throws ClassCastException if there is a preference with this name that is not a String.
     */
    public String getString(String key) {
        mSharedPreferences = mContext.getSharedPreferences(APP_PREFS, Context.MODE_PRIVATE);
        return mSharedPreferences.getString(key, null);
    }


    /**
     * Clears the shared preference file
     */
    public void clear() {
        mSharedPreferences = mContext.getSharedPreferences(APP_PREFS, Context.MODE_PRIVATE);
        mSharedPreferences.edit().clear().apply();
    }

    /**
     * Internal method to get shared preferences across this app
     */
    public static SharedPreferences getSettingsPreferences(Context context) {
        return context.getSharedPreferences(
                PREF_SETTINGS,
                Context.MODE_PRIVATE);
    }
}
