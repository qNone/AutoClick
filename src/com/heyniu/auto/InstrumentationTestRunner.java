package com.heyniu.auto;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;

public class InstrumentationTestRunner extends android.test.InstrumentationTestRunner{


    @Override
    public void onCreate(Bundle arguments) {
        super.onCreate(arguments);
        saveBundle(arguments);
    }

    private void saveBundle(Bundle arguments) {
        SharedPreferencesHelper helper = new SharedPreferencesHelper(super.getTargetContext(), SharedPreferencesHelper.ARGUMENTS);

        android.util.Log.e(Solo.LOG_TAG, "---------------------------");

        String cls = arguments.getString(SharedPreferencesHelper.CLASS);
        android.util.Log.e(Solo.LOG_TAG, "Class: " + cls);
        helper.putString(SharedPreferencesHelper.CLASS, cls);

        String useNative = arguments.getString(SharedPreferencesHelper.USE_NATIVE);
        if (useNative != null && useNative.length() > 0) {
            android.util.Log.e(Solo.LOG_TAG, "UseNative: " + useNative);
            helper.putString(SharedPreferencesHelper.USE_NATIVE, useNative);
        } else helper.remove(SharedPreferencesHelper.USE_NATIVE);

        String newReptile = arguments.getString(SharedPreferencesHelper.NEW_REPTILE);
        if (newReptile != null && newReptile.length() > 0) {
            android.util.Log.e(Solo.LOG_TAG, "NewReptile: " + newReptile);
            helper.putString(SharedPreferencesHelper.NEW_REPTILE, newReptile);
        } else helper.remove(SharedPreferencesHelper.NEW_REPTILE);

        Context context = super.getContext();
        PackageInfo info = null;
        try {
            info = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_INSTRUMENTATION);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if (info == null) throw new RuntimeException("Android test runner not found.");
        String runner = info.instrumentation[0].name;
        android.util.Log.e(Solo.LOG_TAG, "Runner: " + runner);
        helper.putString(SharedPreferencesHelper.RUNNER, runner);

        android.util.Log.e(Solo.LOG_TAG, "---------------------------");
    }
}
