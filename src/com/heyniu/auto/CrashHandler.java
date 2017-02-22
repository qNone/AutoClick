package com.heyniu.auto;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@SuppressWarnings("ucd")
class CrashHandler implements UncaughtExceptionHandler {

	private UncaughtExceptionHandler mDefaultHandler;
	private static CrashHandler instance;
	private Context mContext;
	private Solo.Config config;
	private Map<String, String> infos = new HashMap<>();
	private DateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault());
	private String cls;
	private String pkg;

	private CrashHandler() {
	}

	static CrashHandler getInstance() {
		if (instance == null) {
			synchronized (CrashHandler.class) {
				if (instance == null) {
					instance = new CrashHandler();
				}
			}
		}
		return instance;
	}

	void init(Context context, Solo.Config config) {
		this.mContext = context;
		this.config = config;
		this.mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
		Thread.setDefaultUncaughtExceptionHandler(this);
		SharedPreferencesHelper helper = new SharedPreferencesHelper(mContext, SharedPreferencesHelper.ARGUMENTS);
		cls = helper.getString(SharedPreferencesHelper.CLASS);
		pkg = helper.getString(SharedPreferencesHelper.PACKAGE);
	}

	@Override
	public void uncaughtException(Thread thread, Throwable ex) {
		if (!handleException(ex) && mDefaultHandler != null) {
			mDefaultHandler.uncaughtException(thread, ex);
		} else {
			rebootApplication();
			exitApplication();
		}
	}

	private void rebootApplication() {
		android.content.Intent intent = new android.content.Intent();
		intent.setAction("Auto.Monitor");
		intent.putExtra("package", pkg);
		intent.putExtra("class", cls);
		intent.putExtra("runner", config.runner);
		mContext.sendBroadcast(intent);
	}

	private void exitApplication() {
		android.os.Process.killProcess(android.os.Process.myPid());
		System.exit(0);
	}

	private boolean handleException(Throwable ex) {
		if (ex == null) {
			return false;
		}
		collectDeviceInfo(mContext);
		saveCrashInfo(ex);
		return true;
	}

	private void collectDeviceInfo(Context context) {
		try {
			PackageManager pm = context.getPackageManager();
			PackageInfo pi = pm.getPackageInfo(context.getPackageName(), PackageManager.GET_ACTIVITIES);
			if (pi != null) {
				String versionName = pi.versionName == null ? "null" : pi.versionName;
				String versionCode = pi.versionCode + "";
				infos.put("versionName", versionName);
				infos.put("versionCode", versionCode);
			}
		} catch (NameNotFoundException e) {
			Log.e(Solo.LOG_TAG, "an error occured when collect package info", e);
		}
		Field[] fields = Build.class.getDeclaredFields();
		for (Field field : fields) {
			try {
				field.setAccessible(true);
				infos.put(field.getName(), field.get(null).toString());
			} catch (Exception e) {
				Log.e(Solo.LOG_TAG, "an error occured when collect crash info", e);
			}
		}
	}

	private String saveCrashInfo(Throwable ex) {

		StringBuilder sb = new StringBuilder();
		for (Map.Entry<String, String> entry : infos.entrySet()) {
			String key = entry.getKey();
			String value = entry.getValue();
			sb.append(key).append("=").append(value).append("\n");
		}

		Writer writer = new StringWriter();
		PrintWriter printWriter = new PrintWriter(writer);
		ex.printStackTrace(printWriter);
		Throwable cause = ex.getCause();
		while (cause != null) {
			cause.printStackTrace(printWriter);
			cause = cause.getCause();
		}
		printWriter.close();
		String result = writer.toString();
		sb.append(System.getProperty("line.separator"));
		sb.append(result);
		try {
			String time = formatter.format(new Date());
			String fileName = "crash - " + time  + ".log";
			if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
				File dir = new File(Environment.getExternalStorageDirectory(),
						String.format(Solo.Config.PATH, pkg) + "/Crash");
				if (!dir.exists()) {
					dir.mkdirs();
				}
				FileOutputStream fos = new FileOutputStream(dir.getAbsolutePath() + "/" + fileName);
				fos.write(sb.toString().getBytes());
				fos.close();
			}
			return fileName;
		} catch (Exception e) {
			Log.e(Solo.LOG_TAG, "an error occured while writing file...", e);
		}
		return null;
	}

}