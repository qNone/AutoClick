package com.heyniu.auto;

import android.annotation.SuppressLint;

import java.text.SimpleDateFormat;
import java.util.Date;

class TimeUtils {
	
	@SuppressLint("SimpleDateFormat")
	private static String formatTime(String pattern) {
		SimpleDateFormat format = new SimpleDateFormat(pattern);
        return format.format(new Date());
    }
	
	static String getDate(){
		return formatTime("yyyyMMddHHmmss");
	}

}
