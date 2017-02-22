package com.heyniu.auto;

import android.os.Environment;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;

class FileUtils {

	static void writeActivity(String activity){
		writer(activity, Solo.Config.ACTIVITY, true);
	}

	static void writeObject(Object obj){
		String pkg = PackageSingleton.getInstance().getPkg();
		File file = new File(Environment.getExternalStorageDirectory(),
				String.format(Solo.Config.PATH, pkg) + "/Object");
		if (!file.exists()) {
			file.mkdirs();
		}
		try {
			FileOutputStream fos = new FileOutputStream(file + "/" + MD5Utils.getMD5(obj.toString()));
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(obj);
			oos.flush();
			oos.close();
			fos.close();
		} catch (IOException e) {
			Log.w(Solo.LOG_TAG, "Object Not Serializable: " + obj);
			e.printStackTrace();
		}
	}

	/**
	 * The object is read from the specified directory on the SD card
	 * @param name filename
	 * @return Object
	 */
	static Object readObject(String name){
		String pkg = PackageSingleton.getInstance().getPkg();
		File file = new File(Environment.getExternalStorageDirectory(),
				String.format(Solo.Config.PATH, pkg) + "/Object");
		if (!file.exists()) {
			file.mkdirs();
		}
		Object object = null;
		File target = new File(file + "/" + name);
        Log.d(Solo.LOG_TAG, "Object path: " + target.getAbsolutePath());
		if (!target.exists()) {
			throw new RuntimeException("Object file not found >> " + target.getAbsolutePath());
		}
		try {
			FileInputStream fis = new FileInputStream(target);
			ObjectInputStream ois = new ObjectInputStream(fis);
			try {
				object = ois.readObject();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
			ois.close();
			fis.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return object;
	}

	static String readJson(){
		StringBuilder sb = reader(Solo.Config.JSON);
		return sb == null ? "" : sb.toString();
	}

	static String readParams(){
		StringBuilder sb = reader(Solo.Config.PARAMS);
		return sb == null ? "" : sb.toString();
	}

	static String readActivities(){
		StringBuilder sb = reader(Solo.Config.ACTIVITY);
		return sb == null ? "" : sb.toString();
	}


	static boolean existsJson() {
		String pkg = PackageSingleton.getInstance().getPkg();
		File file = new File(Environment.getExternalStorageDirectory(),
				String.format(Solo.Config.PATH, pkg) + "/" + Solo.Config.JSON);
		return file.exists();
	}

	/**
	 * Delete activity screenshots, if crash, will not be implemented to this method.
	 * @param name activity full name e.g: com.xx.xxActivity
	 */
	static void deleteScreenShots(String name){
		String pkg = PackageSingleton.getInstance().getPkg();
		File file = new File(String.format(Solo.Config.screenshotSavePath, pkg), name);
		Log.d(Solo.LOG_TAG, "deleteScreenShots: " + file.getAbsolutePath());
		try {
			Runtime.getRuntime().exec("rm -rf " + file.getAbsolutePath());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	static void deleteJson() {
		String pkg = PackageSingleton.getInstance().getPkg();
		File file = new File(Environment.getExternalStorageDirectory(),
				String.format(Solo.Config.PATH, pkg) + "/" + Solo.Config.JSON);
		if (file.exists()) {
			boolean b = file.delete();
			Log.d(Solo.LOG_TAG, "Delete result: " + b + " " + file.getAbsolutePath());
		}
	}

	static void deleteParams() {
		String pkg = PackageSingleton.getInstance().getPkg();
		File file = new File(Environment.getExternalStorageDirectory(),
				String.format(Solo.Config.PATH, pkg) + "/" + Solo.Config.PARAMS);
		if (file.exists()) {
			boolean b = file.delete();
			Log.d(Solo.LOG_TAG, "Delete result: " + b + " " + file.getAbsolutePath());
		}
	}

	static boolean existsForActivity() {
		String pkg = PackageSingleton.getInstance().getPkg();
		return new File(Environment.getExternalStorageDirectory(),
				String.format(Solo.Config.PATH, pkg) + "/" + Solo.Config.ACTIVITY).exists();
	}

	static void deleteActivity() {
		String pkg = PackageSingleton.getInstance().getPkg();
		File file = new File(Environment.getExternalStorageDirectory(),
				String.format(Solo.Config.PATH, pkg) + "/" + Solo.Config.ACTIVITY);
		if (file.exists()) {
			boolean b= file.delete();
			Log.d(Solo.LOG_TAG, "Delete result: " + b + " " + file.getAbsolutePath());
		}
	}

	static void writeActivityParams(String string){
		writer(string, Solo.Config.PARAMS, true);
	}

	static void writeJson(String json){
		writer(json, Solo.Config.JSON, false);
	}

	private static void writer(String string, String fileName, boolean append){
		String pkg = PackageSingleton.getInstance().getPkg();
		File exportDir = new File(Environment.getExternalStorageDirectory(),
				String.format(Solo.Config.PATH, pkg));
		if (!exportDir.exists()) {
			boolean b = exportDir.mkdirs();
			Log.d(Solo.LOG_TAG, "Mkdirs result: " + b + " " + exportDir.getAbsolutePath());
		}
		BufferedWriter out = null;
		try {
			out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(exportDir + "/" + fileName, append)));
			out.write(string + System.getProperty("line.separator"));
			out.newLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				if (out != null) {
					out.close();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private static StringBuilder reader(String fileName){
		StringBuilder sb = new StringBuilder();
		String pkg = PackageSingleton.getInstance().getPkg();
		File exportDir = new File(Environment.getExternalStorageDirectory(), String.format(Solo.Config.PATH, pkg));
		if (!exportDir.exists()) {
			Log.w(Solo.LOG_TAG, exportDir.getAbsolutePath() + " Not Found.");
			return null;
		}
		File target = new File(exportDir + "/" + fileName);
		if (!target.exists()) {
			Log.w(Solo.LOG_TAG, target.getAbsolutePath() + " Not Found.");
			return null;
		}
		Log.d(Solo.LOG_TAG, "Read file: " + target.getAbsolutePath());
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(target)));
			String line;
			while ((line = reader.readLine()) != null) {
				sb.append(line).append(System.getProperty("line.separator"));
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				if (reader != null) {
					reader.close();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return sb;
	}

	static StringBuilder getRuntime(String[] command){
		StringBuilder sb = new StringBuilder();
		Process p = null;
		try {
			p = Runtime.getRuntime().exec(command);
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (p != null){
			BufferedInputStream in = new BufferedInputStream(p.getInputStream());
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String line;
			try {
				while ((line = br.readLine()) != null) {
					sb.append(line).append(System.getProperty("line.separator"));
				}
				in.close();
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return sb;
	}
}
