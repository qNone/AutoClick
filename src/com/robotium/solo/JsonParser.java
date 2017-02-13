package com.robotium.solo;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JsonParser {

    public static ArrayList<Params> fromJson(String json) {
        Type jsonType = new TypeToken<ArrayList<Params>>() {
        }.getType();
        Gson gson = new Gson();
        return gson.fromJson(json, jsonType);
    }

    public static String toJson(ArrayList<Params> params) {
        Gson gson = new Gson();
        return gson.toJson(params);
    }

    private static ArrayList<Params> removeDuplicate(ArrayList<Params> params) {
        ArrayList<Params> temp = new ArrayList<Params>();
        for (Params p : params) {
            if (!temp.contains(p)) {
                temp.add(p);
            }
        }
        return temp;
    }

    public static void updateJson() {
        Log.d(Solo.LOG_TAG, "updateJson()");
        FileUtils.deleteJson();
        ArrayList<String> strings = getActivities();
        ArrayList<Params> params = getParams();
        params = removeDuplicate(params);
        params = removeIteratedActivities(strings, params);
        Log.d(Solo.LOG_TAG, "Update Params: >> ");
        for (Params p: params) {
            Log.d(Solo.LOG_TAG, "Update Params: " + p.name);
        }
        String text = toJson(params);
        if (text != null && text.length() > 100) {
            FileUtils.writeJson(text);
        }
    }

    private static ArrayList<Params> removeIteratedActivities(ArrayList<String> strings, ArrayList<Params> params) {
        ArrayList<Params> temp = new ArrayList<>();
        for (Params p : params) {
            if (!strings.contains(p.name))temp.add(p);
        }
        return temp;
    }

    private static ArrayList<String> getActivities() {
        ArrayList<String> list = new ArrayList<>();
        String strings = FileUtils.readActivities();
        Pattern p = Pattern.compile("stopped iteration: ([\\w|.]+)");
        Matcher m = p.matcher(strings);
        while (m.find()) {
            list.add(m.group(1).trim());
        }
        return list;
    }

    private static ArrayList<Params> getParams() {
        String[] strings = FileUtils.readParams().split("\\n");
        ArrayList<Params> list = new ArrayList<Params>();
        ArrayList<Param> paramArrayList  = new ArrayList<Param>();
        String name = "";
        boolean isEnd = false;
        for (String s: strings) {
            if (isEnd) {
                isEnd = false;
                paramArrayList = new ArrayList<Param>();
            }
            if (s.startsWith("Activity:")) {
                String[] tmp = s.split("Activity:");
                name = tmp[tmp.length - 1].trim();
            }
            if (s.startsWith("String")) {
                String[] tmp = s.split(" ", 4);
                Param param = new Param();
                param.setKey(tmp[0] + "|" + tmp[1]);
                if (tmp.length == 3) {
                    param.setValue(tmp[2] + "|" + "");
                } else param.setValue(tmp[2] + "|" + tmp[3].trim());
                paramArrayList.add(param);
            }
            if (s.startsWith("Tag")) {
                if (!name.equals("")) {
                    Params params = new Params();
                    params.setName(name);
                    params.setWeb(false);
                    params.setParams(paramArrayList);
                    params.setIteration(true);
                    list.add(params);
                    isEnd = true;
                }
            }
        }
        FileUtils.deleteParams();
        return list;
    }

    public static StringBuilder read(String path) {
        File file = new File(path);
        StringBuilder sb = new StringBuilder();
        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line).append(System.getProperty("line.separator"));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb;
    }

}
