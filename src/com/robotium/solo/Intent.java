package com.robotium.solo;


import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;

import java.io.Serializable;

public class Intent extends android.content.Intent implements Parcelable, Cloneable {

    private static final String TAG = "ActivityParams";

    public Intent() {
        super();
    }

    public Intent(Context packageContext, Class<?> cls) {
        super(packageContext, cls);
        Log.d(TAG, "Tag");
        Log.d(TAG, "Activity: " + cls.getName());
        FileUtils.writeActivityParams("Tag");
        FileUtils.writeActivityParams("Activity: " + cls.getName());
    }

    public android.content.Intent setClass(Context packageContext, Class<?> cls) {
        super.setClass(packageContext, cls);
        Log.d(TAG, "Tag");
        Log.d(TAG, "Activity: " + cls.getName());
        FileUtils.writeActivityParams("Tag");
        FileUtils.writeActivityParams("Activity: " + cls.getName());
        return this;
    }

    public android.content.Intent putExtra(String name, boolean value) {
        super.putExtra(name, value);
        Log.d(TAG, "String " + name + " boolean " + value);
        FileUtils.writeActivityParams("String " + name + " boolean " + value);
        return this;
    }

    public android.content.Intent putExtra(String name, boolean[] value) {
        super.putExtra(name, value);
        FileUtils.writeObject(value);
        Log.d(TAG, "String " + name + " boolean[] " + MD5Utils.getMD5(value.toString()));
        FileUtils.writeActivityParams("String " + name + " boolean[] " + MD5Utils.getMD5(value.toString()));
        return this;
    }

    public android.content.Intent putExtra(String name, byte[] value) {
        super.putExtra(name, value);
        FileUtils.writeObject(value);
        Log.d(TAG, "String " + name + " byte[] " + MD5Utils.getMD5(value.toString()));
        FileUtils.writeActivityParams("String " + name + " byte[] " + MD5Utils.getMD5(value.toString()));
        return this;
    }

    public android.content.Intent putExtra(String name, short[] value) {
        super.putExtra(name, value);
        FileUtils.writeObject(value);
        Log.d(TAG, "String " + name + " short[] " + MD5Utils.getMD5(value.toString()));
        FileUtils.writeActivityParams("String " + name + " short[] " + MD5Utils.getMD5(value.toString()));
        return this;
    }

    public android.content.Intent putExtra(String name, char[] value) {
        super.putExtra(name, value);
        FileUtils.writeObject(value);
        Log.d(TAG, "String " + name + " char[] " + MD5Utils.getMD5(value.toString()));
        FileUtils.writeActivityParams("String " + name + " char[] " + MD5Utils.getMD5(value.toString()));
        return this;
    }

    public android.content.Intent putExtra(String name, int[] value) {
        super.putExtra(name, value);
        FileUtils.writeObject(value);
        Log.d(TAG, "String " + name + " int[] " + MD5Utils.getMD5(value.toString()));
        FileUtils.writeActivityParams("String " + name + " int[] " + MD5Utils.getMD5(value.toString()));
        return this;
    }

    public android.content.Intent putExtra(String name, long[] value) {
        super.putExtra(name, value);
        FileUtils.writeObject(value);
        Log.d(TAG, "String " + name + " long[] " + MD5Utils.getMD5(value.toString()));
        FileUtils.writeActivityParams("String " + name + " long[] " + MD5Utils.getMD5(value.toString()));
        return this;
    }

    public android.content.Intent putExtra(String name, float[] value) {
        super.putExtra(name, value);
        FileUtils.writeObject(value);
        Log.d(TAG, "String " + name + " float[] " + MD5Utils.getMD5(value.toString()));
        FileUtils.writeActivityParams("String " + name + " float[] " + MD5Utils.getMD5(value.toString()));
        return this;
    }

    public android.content.Intent putExtra(String name, double[] value) {
        super.putExtra(name, value);
        FileUtils.writeObject(value);
        Log.d(TAG, "String " + name + " double[] " + MD5Utils.getMD5(value.toString()));
        FileUtils.writeActivityParams("String " + name + " double[] " + MD5Utils.getMD5(value.toString()));
        return this;
    }

    public android.content.Intent putExtra(String name, String[] value) {
        super.putExtra(name, value);
        FileUtils.writeObject(value);
        Log.d(TAG, "String " + name + " String[] " + MD5Utils.getMD5(value.toString()));
        FileUtils.writeActivityParams("String " + name + " String[] " + MD5Utils.getMD5(value.toString()));
        return this;
    }

    public android.content.Intent putExtra(String name, CharSequence[] value) {
        super.putExtra(name, value);
        FileUtils.writeObject(value);
        Log.d(TAG, "String " + name + " CharSequence[] " + MD5Utils.getMD5(value.toString()));
        FileUtils.writeActivityParams("String " + name + " CharSequence[] " + MD5Utils.getMD5(value.toString()));
        return this;
    }

    public android.content.Intent putExtra(String name, Serializable value) {
        super.putExtra(name, value);
        FileUtils.writeObject(value);
        Log.d(TAG, "String " + name + " Serializable " + MD5Utils.getMD5(value.toString()));
        FileUtils.writeActivityParams("String " + name + " Serializable " + MD5Utils.getMD5(value.toString()));
        return this;
    }

    public android.content.Intent putExtra(String name, Bundle value) {
        super.putExtra(name, value);
        FileUtils.writeObject(value);
        Log.d(TAG, "String " + name + " Bundle " + MD5Utils.getMD5(value.toString()));
        FileUtils.writeActivityParams("String " + name + " Bundle " + MD5Utils.getMD5(value.toString()));
        return this;
    }

//    public android.content.Intent putExtra(String name, IBinder value) {
//        super.putExtra(name, value);
//        FileUtils.writeObject("IBinder", value);
//        Log.d(TAG, "String " + name + " IBinder " + value.toString());
//        FileUtils.writeActivityParams("String " + name + " IBinder " + value.toString());
//        return this;
//    }

    public android.content.Intent putExtra(String name, byte value) {
        super.putExtra(name, value);
        Log.d(TAG, "String " + name + " byte " + value);
        FileUtils.writeActivityParams("String " + name + " byte " + value);
        return this;
    }

    public android.content.Intent putExtra(String name, char value) {
        super.putExtra(name, value);
        Log.d(TAG, "String " + name + " char " + value);
        FileUtils.writeActivityParams("String " + name + " char " + value);
        return this;
    }

    public android.content.Intent putExtra(String name, short value) {
        super.putExtra(name, value);
        Log.d(TAG, "String " + name + " short " + value);
        FileUtils.writeActivityParams("String " + name + " short " + value);
        return this;
    }

    public android.content.Intent putExtra(String name, int value) {
        super.putExtra(name, value);
        Log.d(TAG, "String " + name + " int " + value);
        FileUtils.writeActivityParams("String " + name + " int " + value);
        return this;
    }

    public android.content.Intent putExtra(String name, long value) {
        super.putExtra(name, value);
        Log.d(TAG, "String " + name + " long " + value);
        FileUtils.writeActivityParams("String " + name + " long " + value);
        return this;
    }

    public android.content.Intent putExtra(String name, float value) {
        super.putExtra(name, value);
        Log.d(TAG, "String " + name + " float " + value);
        FileUtils.writeActivityParams("String " + name + " float " + value);
        return this;
    }

    public android.content.Intent putExtra(String name, double value) {
        super.putExtra(name, value);
        Log.d(TAG, "String " + name + " double " + value);
        FileUtils.writeActivityParams("String " + name + " double " + value);
        return this;
    }

    public android.content.Intent putExtra(String name, String value) {
        super.putExtra(name, value);
        Log.d(TAG, "String " + name + " String " + value);
        FileUtils.writeActivityParams("String " + name + " String " + value);
        return this;
    }

    public android.content.Intent putExtra(String name, CharSequence value) {
        super.putExtra(name, value);
        FileUtils.writeObject(value);
        Log.d(TAG, "String " + name + " CharSequence " + MD5Utils.getMD5(value.toString()));
        FileUtils.writeActivityParams("String " + name + " CharSequence " + MD5Utils.getMD5(value.toString()));
        return this;
    }

    public android.content.Intent putExtra(String name, Parcelable value) {
        super.putExtra(name, value);
        FileUtils.writeObject(value);
        Log.d(TAG, "String " + name + " Parcelable " + MD5Utils.getMD5(value.toString()));
        FileUtils.writeActivityParams("String " + name + " Parcelable " + MD5Utils.getMD5(value.toString()));
        return this;
    }

    public android.content.Intent putExtra(String name, Parcelable[] value) {
        super.putExtra(name, value);
        FileUtils.writeObject(value);
        Log.d(TAG, "String " + name + " Parcelable[] " + MD5Utils.getMD5(value.toString()));
        FileUtils.writeActivityParams("String " + name + " Parcelable[] " + MD5Utils.getMD5(value.toString()));
        return this;
    }

}
