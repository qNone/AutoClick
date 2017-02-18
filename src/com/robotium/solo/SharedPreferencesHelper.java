package com.robotium.solo;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferencesHelper {

    private SharedPreferences sharedPreferences;
    public static final String ARGUMENTS = "arguments";
    public static final String USE_NATIVE = "useNative";
    public static final String NEW_REPTILE = "newReptile";
    public static final String CLASS = "class";
    public static final String RUNNER = "runner";


    public SharedPreferencesHelper(Context context, String name) {
        sharedPreferences = context.getSharedPreferences(name, 0);
    }

    public void putString (String key, String value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public void putInt (String key, int value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(key, value);
        editor.apply();
    }

    public void putBoolean (String key, boolean value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    public void putFloat (String key, float value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putFloat(key, value);
        editor.apply();
    }

    public void putLong (String key, long value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong(key, value);
        editor.apply();
    }

    public String getString (String key) {
        return sharedPreferences.getString(key, null);
    }

    public Integer getInt (String key) {
        return sharedPreferences.getInt(key, 0);
    }

    public boolean getBoolean (String key) {
        return sharedPreferences.getBoolean(key, false);
    }

    public float getFloat (String key) {
        return sharedPreferences.getFloat(key, 0f);
    }

    public long getLong (String key) {
        return sharedPreferences.getLong(key, 0L);
    }

    public void remove (String key) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(key);
        editor.apply();
    }
}
