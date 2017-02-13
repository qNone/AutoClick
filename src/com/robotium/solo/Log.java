package com.robotium.solo;

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

    public static void v(String tag, String msg) {
        FileUtils.writeLog(TimeUtils.getDate() + " V/" + tag + ": " + msg);
        android.util.Log.v(tag, msg);
    }

    public static void w(String tag, String msg) {
        FileUtils.writeLog(TimeUtils.getDate() + " W/" + tag + ": " + msg);
        android.util.Log.w(tag, msg);
    }

}
