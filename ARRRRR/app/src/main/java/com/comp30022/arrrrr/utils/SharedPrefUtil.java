package com.comp30022.arrrrr.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * This class is used to store firebase token and
 * update it
 *
 * @author Zijie Shen
 * */
public class SharedPrefUtil {
    /**
     * Name of the preference file
     */
    private static final String APP_PREFS = "application_preferences";

    private Context mContext;
    private SharedPreferences mSharedPreferences;
    private SharedPreferences.Editor mEditor;

    public SharedPrefUtil(Context mContext) {
        this.mContext = mContext;
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
}
