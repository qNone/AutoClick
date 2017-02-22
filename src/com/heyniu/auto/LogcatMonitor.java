package com.heyniu.auto;

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

class LogcatMonitor extends Thread implements Runnable{

    private DateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault());
    private Context mContext;

    LogcatMonitor(Context context) {
        this.mContext = context;
    }

    @Override
    public void run() {
        logcat();
    }

    void clear(){
        try {
            Runtime.getRuntime().exec(new String[]{"logcat", "-c"});
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void logcat() {
        String fileName = getFileName();
        List<String> command = new ArrayList<>();
        command.add("logcat");
        command.add("-v");
        command.add("time");
        command.add("-f");
        command.add(getLogPath() + "/" + fileName);
        try {
            Runtime.getRuntime().exec(command.toArray(new String[command.size()]));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getLogPath(){
        String pkg = getTargetPackage(mContext);
        File file = new File(Environment.getExternalStorageDirectory(),
                String.format(Solo.Config.PATH, pkg + "/Log"));
        if (!file.exists()) file.mkdirs();
        return file.getAbsolutePath();
    }

    private String getTargetPackage(Context context) {
        String pkg = context.getPackageName();
        return pkg.substring(0, pkg.lastIndexOf("."));
    }

    private String getFileName() {
        String time = formatter.format(new Date());
        return "Log-" + time  + ".log";
    }
}
