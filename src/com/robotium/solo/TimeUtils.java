package com.robotium.solo;

import android.annotation.SuppressLint;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeUtils {
	
	@SuppressLint("SimpleDateFormat")
	public static String formatTime(String pattern) {
		SimpleDateFormat format = new SimpleDateFormat(pattern);
        return format.format(new Date());
    }
	
	public static String getDate(){
		return formatTime("yyyyMMddHHmmss");
	}

}
