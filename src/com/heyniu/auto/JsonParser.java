package com.heyniu.auto;

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

class JsonParser {

    /**
     * Json 转 ArrayList<Params>
     * @param json json
     * @return .
     */
    static ArrayList<ParamsEntity> fromJson(String json) {
        Type jsonType = new TypeToken<ArrayList<ParamsEntity>>() {
        }.getType();
        Gson gson = new Gson();
        return gson.fromJson(json, jsonType);
    }

    /**
     * ArrayList<Params> 转 json
     * @param params params
     * @return json
     */
    private static String toJson(ArrayList<ParamsEntity> params) {
        Gson gson = new Gson();
        return gson.toJson(params);
    }

    /**
     * ArrayList<Params>去重
     * @param params params
     * @return .
     */
    private static ArrayList<ParamsEntity> removeDuplicate(ArrayList<ParamsEntity> params) {
        ArrayList<ParamsEntity> temp = new ArrayList<>();
        for (ParamsEntity p : params) {
            if (!temp.contains(p)) {
                temp.add(p);
            }
        }
        return temp;
    }

    /**
     * 通过检查Activities.txt的最后一个activity与Params.json最后一个activity对比可知是否完成遍历。
     * @return isFinish
     */
    static boolean isFinish() {
        Log.d(Solo.LOG_TAG, "isFinish()");
        String strings = FileUtils.readActivities();
        String[] activities = strings.split(System.getProperty("line.separator"));

        String json = FileUtils.readJson();
        ArrayList<ParamsEntity> arrayList = fromJson(json);
        if (arrayList == null || arrayList.size() == 0) return true;
        ParamsEntity params = arrayList.get(arrayList.size() - 1);
        String lastActivityForJson = params.getName();
        String lastActivityForTxt = activities[activities.length - 1];
        boolean isStop = lastActivityForTxt.contains("stopped") && lastActivityForTxt.contains(lastActivityForJson);
        boolean isStart = lastActivityForTxt.contains("starting") && lastActivityForTxt.contains(lastActivityForJson);
        return isStop || !isStart;
    }

    /**
     * 当自动遍历未完成退出时（一般是发生了崩溃）更新Params
     */
    static void updateParams() {
        Log.d(Solo.LOG_TAG, "updateParams()");
        Log.d(Solo.LOG_TAG, "The iteration is not complete.");
        if (!FileUtils.existsJson()) throw new RuntimeException("Params.json Not Found, please check the log.");
        String json = FileUtils.readJson();
        // 没迭代完成的也是迭代过，没迭代完成的一般是遇到了崩溃
        ArrayList<String> iterated = getActivitiesForStart();
        for(String s: iterated) {
            Log.d(Solo.LOG_TAG, "iterated: " + s);
        }
        ArrayList<ParamsEntity> arrayList = fromJson(json);
        arrayList = removeDuplicate(arrayList);

        ArrayList<ParamsEntity> nowParams = removeIteratedActivities(iterated, arrayList);
        // 迭代时产生的ActivityParams
        ArrayList<ParamsEntity> params = getParams();
        params = removeDuplicate(params);
        params = removeIteratedActivities(iterated, params);
        nowParams.addAll(params);

        for (ParamsEntity p: nowParams) {
            Log.d(Solo.LOG_TAG, "No iteration Params: " + p.getName());
        }
        String text = toJson(nowParams);
        if (text != null) {
            FileUtils.writeJson(text);
        }
    }

    /**
     * 为快速模式或正常（迭代）模式创建json
     */
    static void createJson() {
        Log.d(Solo.LOG_TAG, "createJson()");
        ArrayList<ParamsEntity> params = getParams();
        for (ParamsEntity p: params) {
            Log.d(Solo.LOG_TAG, "Create Params: " + p.getName());
        }
        String json = toJson(params);
        if (json != null) {
            FileUtils.writeJson(json);
        }
    }

    /**
     * 爬虫模式时更新json
     */
    static void updateJson() {
        Log.d(Solo.LOG_TAG, "updateJson()");
        FileUtils.deleteJson();
        ArrayList<String> strings = getActivitiesForStop();
        ArrayList<ParamsEntity> params = getParams();
        params = removeDuplicate(params);
        params = removeIteratedActivities(strings, params);
        Log.d(Solo.LOG_TAG, "Update Params: >> ");
        for (ParamsEntity p: params) {
            Log.d(Solo.LOG_TAG, "Update Params: " + p.getName());
        }
        String text = toJson(params);
        if (text != null && text.length() > 100) {
            FileUtils.writeJson(text);
        }
    }

    /**
     * 移除已经遍历的界面
     * @param params params
     * @param strings strings 已经遍历的界面
     * @return .
     */
    private static ArrayList<ParamsEntity> removeIteratedActivities(ArrayList<String> strings, ArrayList<ParamsEntity> params) {
        ArrayList<ParamsEntity> temp = new ArrayList<>();
        for (ParamsEntity p : params) {
            if (!strings.contains(p.getName()))temp.add(p);
        }
        return temp;
    }

    /**
     * 返回所有启动过的界面
     * @return 返回所有启动过的界面
     */
    private static ArrayList<String> getActivitiesForStart() {
        return getActivities("starting");
    }

    /**
     * 返回所有启动并遍历完成的界面
     * @return 返回所有启动并遍历完成的界面
     */
    private static ArrayList<String> getActivitiesForStop() {
        return getActivities("stopped");
    }

    /**
     * 从Activities.txt文件读取已经启动过的页面
     * @param key starting or stopped
     * @return .
     */
    private static ArrayList<String> getActivities(String key) {
        ArrayList<String> list = new ArrayList<>();
        String strings = FileUtils.readActivities();
        Pattern p;
        if (key.contains("stopped")) {
            p = Pattern.compile("stopped iteration: ([\\w|.]+)");
        } else p = Pattern.compile("starting iteration: ([\\w|.]+)");
        Matcher m = p.matcher(strings);
        while (m.find()) {
            list.add(m.group(1).trim());
        }
        return list;
    }

    /**
     * 从ActivityParams.txt读取数据生成Params
     * @return .
     */
    private static ArrayList<ParamsEntity> getParams() {
        String[] strings = FileUtils.readParams().split("\\n");
        ArrayList<ParamsEntity> list = new ArrayList<>();
        ArrayList<ParamEntity> paramArrayList  = new ArrayList<>();
        String name = "";
        boolean isEnd = false;
        for (String s: strings) {
            if (isEnd) {
                isEnd = false;
                paramArrayList = new ArrayList<>();
            }
            if (s.startsWith("Activity:")) {
                String[] tmp = s.split("Activity:");
                name = tmp[tmp.length - 1].trim();
            }
            if (s.startsWith("String")) {
                String[] tmp = s.split(" ", 4);
                ParamEntity param = new ParamEntity();
                param.setKey(tmp[0] + "|" + tmp[1]);
                if (tmp.length == 3) {
                    param.setValue(tmp[2] + "|" + "");
                } else param.setValue(tmp[2] + "|" + tmp[3].trim());
                paramArrayList.add(param);
            }
            if (s.startsWith("Tag")) {
                if (!name.equals("")) {
                    ParamsEntity params = new ParamsEntity();
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

    private static StringBuilder read(String path) {
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
