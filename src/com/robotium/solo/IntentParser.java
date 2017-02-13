package com.robotium.solo;


import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class IntentParser {

    /**
     * Parse the activity parameters.
     * @param activity activity
     * @return HashMap
     */
    private static Map<String, Object> parseIntent(Activity activity){
        Map<String, Object> hashMap = new HashMap<String, Object>();
        if (activity == null)return hashMap;
        android.content.Intent intent = activity.getIntent();
        if (intent != null) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                Set<String> stringSet = bundle.keySet();
                for(String s: stringSet){
                    hashMap.put(s, bundle.get(s));
                }
            }
        }
        return hashMap;
    }

    /**
     * Record extras for activity.
     * Contains activity name and activity parameters.
     * @param context context
     * @param activity activity
     */
    public static void recordExtras(Context context, Activity activity){
        Intent intent = new Intent(context, activity.getClass());
        parseExtras(parseIntent(activity), intent);
    }

    /**
     * Parameters are written to the sdcard.
     * @param params hashMap params
     * @param intent intent
     */
    private static void parseExtras(Map<String, Object> params, Intent intent){
        if (params != null){
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                if (entry.getValue() instanceof Integer) {
                    intent.putExtra(entry.getKey(), ((Integer) entry.getValue()).intValue());
                }else
                if (entry.getValue() instanceof String) {
                    intent.putExtra(entry.getKey(), (String) entry.getValue());
                }else
                if (entry.getValue() instanceof Double) {
                    intent.putExtra(entry.getKey(), ((Double) entry.getValue()).doubleValue());
                }else
                if (entry.getValue() instanceof Float) {
                    intent.putExtra(entry.getKey(), ((Float) entry.getValue()).floatValue());
                }else
                if (entry.getValue() instanceof Long) {
                    intent.putExtra(entry.getKey(), ((Long) entry.getValue()).longValue());
                }else
                if (entry.getValue() instanceof Boolean) {
                    intent.putExtra(entry.getKey(), ((Boolean) entry.getValue()).booleanValue());
                }else
                if (entry.getValue() instanceof Byte) {
                    intent.putExtra(entry.getKey(), ((Byte) entry.getValue()).byteValue());
                }else
                if (entry.getValue() instanceof Short) {
                    intent.putExtra(entry.getKey(), ((Short) entry.getValue()).shortValue());
                }else
                if (entry.getValue() instanceof Serializable) {
                    intent.putExtra(entry.getKey(), ((Serializable) entry.getValue()));
                }else
                if (entry.getValue() instanceof Parcelable[]) {
                    intent.putExtra(entry.getKey(), ((Parcelable[]) entry.getValue()));
                }else
                if (entry.getValue() instanceof Parcelable) {
                    intent.putExtra(entry.getKey(), ((Parcelable) entry.getValue()));
                }else
                if (entry.getValue() instanceof CharSequence) {
                    intent.putExtra(entry.getKey(), ((CharSequence) entry.getValue()));
                }else
                if (entry.getValue() instanceof Bundle) {
                    intent.putExtra(entry.getKey(), ((Bundle) entry.getValue()));
                }else
                if (entry.getValue() instanceof CharSequence[]) {
                    intent.putExtra(entry.getKey(), ((CharSequence[]) entry.getValue()));
                }else
                if (entry.getValue() instanceof String[]) {
                    intent.putExtra(entry.getKey(), ((String[]) entry.getValue()));
                }else
                if (entry.getValue() instanceof double[]) {
                    intent.putExtra(entry.getKey(), ((double[]) entry.getValue()));
                }else
                if (entry.getValue() instanceof float[]) {
                    intent.putExtra(entry.getKey(), ((float[]) entry.getValue()));
                }else
                if (entry.getValue() instanceof long[]) {
                    intent.putExtra(entry.getKey(), ((long[]) entry.getValue()));
                }else
                if (entry.getValue() instanceof int[]) {
                    intent.putExtra(entry.getKey(), ((int[]) entry.getValue()));
                }else
                if (entry.getValue() instanceof char[]) {
                    intent.putExtra(entry.getKey(), ((char[]) entry.getValue()));
                }else
                if (entry.getValue() instanceof short[]) {
                    intent.putExtra(entry.getKey(), ((short[]) entry.getValue()));
                }else
                if (entry.getValue() instanceof byte[]) {
                    intent.putExtra(entry.getKey(), ((byte[]) entry.getValue()));
                }else
                if (entry.getValue() instanceof boolean[]) {
                    intent.putExtra(entry.getKey(), ((boolean[]) entry.getValue()));
                }
            }
        }
    }

    /**
     * Parse intent extra type.
     * @param text param
     * @return object
     */
    public static Object parseType(String text) {
        String[] strings = text.split("\\|");
        String tmp = strings[0].toLowerCase().trim();
        if (tmp.equals("int".toLowerCase())) {
            return Integer.parseInt(strings[1]);
        }else
        if (tmp.equals("string".toLowerCase())) {
            return strings.length == 1 ? "" : strings[1];
        }else
        if (tmp.equals("double".toLowerCase())) {
            return Double.parseDouble(strings[1]);
        }else
        if (tmp.equals("float".toLowerCase())) {
            return Float.parseFloat(strings[1]);
        }else
        if (tmp.equals("long".toLowerCase())) {
            return Long.parseLong(strings[1]);
        }else
        if (tmp.equals("boolean".toLowerCase())) {
            return Boolean.parseBoolean(strings[1]);
        }else
        if (tmp.equals("byte".toLowerCase())) {
            return Byte.parseByte(strings[1]);
        }else
        if (tmp.equals("short".toLowerCase())) {
            return Short.parseShort(strings[1]);
        }
        return FileUtils.readObject(strings[1]);
    }

    /**
     * Return the intent that started this activity.
     * @param context context
     * @param target target activity
     * @param params activity params
     * @return Return the intent that started this activity.
     */
    public static Intent getIntent(Context context, Class<?> target, Map<String, Object> params) {
        Intent intent = new Intent(context, target);
        parseExtras(params, intent);
        return intent;
    }
}
