package com.heyniu.auto;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.content.ContextCompat;

public final class Log {

    public Log() {
        super();
    }

    public static void i(String tag, String msg) {
        FileUtils.writeLog(TimeUtils.getDate() + " I/" + tag + ": " + msg);
        android.util.Log.i(tag, msg);
    }

    public static void d(String tag, String msg) {
        FileUtils.writeLog(TimeUtils.getDate() + " D/" + tag + ": " + msg);
        android.util.Log.d(tag, msg);
    }

    public static void e(String tag, String msg) {
        FileUtils.writeLog(TimeUtils.getDate() + " E/" + tag + ": " + msg);
        android.util.Log.e(tag, msg);
    }

    public static void v(String tag, String msg, Context context) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            int mPermission = ContextCompat.checkSelfPermission(context, Permission.WRITE_EXTERNAL_STORAGE);
            if (mPermission == PackageManager.PERMISSION_GRANTED) {
                FileUtils.writeLog(TimeUtils.getDate() + " V/" + tag + ": " + msg);
                android.util.Log.v(tag, msg);
            }
        }
    }

    public static void w(String tag, String msg) {
        FileUtils.writeLog(TimeUtils.getDate() + " W/" + tag + ": " + msg);
        android.util.Log.w(tag, msg);
    }

}
