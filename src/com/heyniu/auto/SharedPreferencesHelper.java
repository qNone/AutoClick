package com.heyniu.auto;

import android.content.Context;
import android.content.SharedPreferences;

class SharedPreferencesHelper {

    private SharedPreferences sharedPreferences;
    static final String ARGUMENTS = "arguments";
    static final String USE_NATIVE = "useNative";
    static final String NEW_REPTILE = "newReptile";
    static final String CLASS = "class";
    static final String RUNNER = "runner";


    SharedPreferencesHelper(Context context, String name) {
        sharedPreferences = context.getSharedPreferences(name, 0);
    }

    void putString (String key, String value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    void putInt (String key, int value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(key, value);
        editor.apply();
    }

    void putBoolean (String key, boolean value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    void putFloat (String key, float value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putFloat(key, value);
        editor.apply();
    }

    void putLong (String key, long value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong(key, value);
        editor.apply();
    }

    String getString (String key) {
        return sharedPreferences.getString(key, null);
    }

    Integer getInt (String key) {
        return sharedPreferences.getInt(key, 0);
    }

    boolean getBoolean (String key) {
        return sharedPreferences.getBoolean(key, false);
    }

    float getFloat (String key) {
        return sharedPreferences.getFloat(key, 0f);
    }

    long getLong (String key) {
        return sharedPreferences.getLong(key, 0L);
    }

    void remove (String key) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(key);
        editor.apply();
    }
}
