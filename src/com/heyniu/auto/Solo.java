package com.heyniu.auto;

import android.app.Activity;
import android.app.Application;
import android.app.Instrumentation;
import android.app.UiAutomation;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.os.SystemClock;
import android.support.v7.widget.RecyclerView;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.robotium.solo.Timeout;
import com.robotium.solo.WebElement;

import junit.framework.Assert;

import java.lang.reflect.Field;
import java.util.ArrayList;

public class Solo extends com.robotium.solo.Solo{

    private final Application.ActivityLifecycleCallbacks activityLifecycleCallbacks;
    private final Application application;
    private final AcrossApplication acrossApplication;
    private final UiAutomation uiAutomation;
    private final Context context;
    private final Handler handler;

    private final Config config;
    private final ScreenshotTaker screenshotTaker;
    private final ActivityUtils activityUtils;
    private final Scroller scroller;
    private final Clicker clicker;

    public final static String LOG_TAG = "Robotium";
    private final static String DAEMON = "com.heyniu.monitor";

    private int width;
    private int height;

    public Solo(Instrumentation instrumentation, Config config, Activity activity) {
        super(instrumentation, config, activity);

        this.config = config;
        this.application = (Application) instrumentation.getTargetContext().getApplicationContext();
        this.acrossApplication = new AcrossApplication(activity, instrumentation, config);
        this.uiAutomation = instrumentation.getUiAutomation();
        this.context = activity;
        this.activityUtils = new ActivityUtils(config, instrumentation, activity, sleeper);
        this.screenshotTaker = new ScreenshotTaker(config, instrumentation, activityUtils,
                viewFetcher, sleeper);
        this.scroller = new Scroller(config, instrumentation, viewFetcher, sleeper);
        this.clicker = new Clicker(config, activityUtils, viewFetcher,sender, instrumentation,
                sleeper, waiter, webUtils, dialogUtils, activity, screenshotTaker);
        this.activityLifecycleCallbacks = new Application.ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
                Log.d(LOG_TAG, "onActivityCreated: " + activity.getClass().getName());
                activityListener(activity);
            }

            @Override
            public void onActivityStarted(Activity activity) {
            }

            @Override
            public void onActivityResumed(Activity activity) {

            }

            @Override
            public void onActivityPaused(Activity activity) {

            }

            @Override
            public void onActivityStopped(Activity activity) {

            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
                Log.d(LOG_TAG, "onActivitySaveInstanceState: " + activity.getClass().getName());
                BundleSingleton.getInstance().put(activity.getClass().getName(), outState);
            }

            @Override
            public void onActivityDestroyed(Activity activity) {

            }
        };

        init(config, instrumentation, activity);

        this.handler = new Handler(this);
    }

    public static class Config extends com.robotium.solo.Solo.Config {

        /**
         * The screenshot save path. Default save path is /sdcard/AutoClick/package/Screenshots/.
         */
        public static final String screenshotSavePath = Environment.getExternalStorageDirectory() + "/AutoClick/%s/Screenshots/";

        /**
         * The Robotium save path. Default save path is /sdcard/AutoClick/package.
         */
        public static final String PATH = "/AutoClick/%s";

        /**
         * Iterated activities.
         */
        public static final String ACTIVITY = "Activities.txt";

        /**
         * Custom Intent record the activity parameters.
         */
        public static final String PARAMS = "ActivityParams.txt";

        /**
         * Starts the activity parameter json file.
         */
        public static final String JSON = "Params.json";

        /**
         * If true, every time you start a activity will be screenshot.
         */
        public boolean activityScreenShots = true;

        /**
         * If true, every steps you start a activity will be screenshot.
         */
        public boolean iterationScreenShots = true;

        /**
         * Iteration mode.
         * FAST >> Fast mode. Only start activity, quickly check the crash.
         * NORMAL >> Normal mode. Start activity and iteration it, check for crash caused by click.
         * REPTILE >> Reptile mode. Reptile new activity and click, if have new activity.
         * RECORD >> Record mode. Record the required parameters for every activity.
         */
        public enum Mode{
            FAST, NORMAL, REPTILE, RECORD
        }

        public Mode mode;

        /**
         * Application home page.
         */
        public String homeActivity;

        /**
         * Application login activity.
         */
        public String loginActivity;

        /**
         * Login activity log in account.
         */
        public String loginAccount;

        /**
         * Login activity log in password.
         */
        public String loginPassword;

        /**
         * Login activity log in button.
         */
        public String loginId;

        /**
         * The activity within the array will not be iterated.
         */
        public String[] ignoreActivities;

        /**
         * The view within the array will not be iterated.
         */
        public String[] ignoreViews;

        /**
         * If true, keep activities ScreenShots.
         */
        public boolean keepActivitiesScreenShots = true;

        /**
         * If true, Clean up the data at reptile mode.
         * Initialize the data.
         */
        public boolean newReptile = true;

        /**
         * If true, use the handleNative method iteration.
         */
        public boolean useNative = false;

        /**
         * Android test runner.
         */
        public String runner;

    }

    /**
     * Returns the Config used by Robotium.
     *
     * @return the Config used by Robotium
     */

    public Config getConfig(){
        if(config.commandLogging){
            Log.d(config.commandLoggingTag, "getConfig()");
        }

        return config;
    }

    /**
     * Returns the UiAutomation used by Robotium.
     *
     * @return the UiAutomation used by Robotium
     */

    UiAutomation getUiAutomation(){
        if(config.commandLogging){
            Log.d(config.commandLoggingTag, "getUiAutomation()");
        }

        return uiAutomation;
    }

    /**
     * Returns the ScreenshotTaker used by Robotium.
     *
     * @return the ScreenshotTaker used by Robotium
     */

    ScreenshotTaker getScreenshotTaker(){
        if(config.commandLogging){
            Log.d(config.commandLoggingTag, "getScreenshotTaker()");
        }

        return screenshotTaker;
    }

    /**
     * Returns the ActivityUtils used by Robotium.
     *
     * @return the ActivityUtils used by Robotium
     */

    ActivityUtils getActivityUtils(){
        if(config.commandLogging){
            Log.d(config.commandLoggingTag, "getActivityUtils()");
        }

        return activityUtils;
    }

    /**
     * Returns the Context used by Robotium.
     *
     * @return the Context used by Robotium
     */

    Context getContext(){
        if(config.commandLogging){
            Log.d(config.commandLoggingTag, "getContext()");
        }

        return context;
    }

    /**
     * The Activities that are alive are finished. Usually used in tearDown().
     */

    public void finishOpenedActivities(){
        if(config.commandLogging){
            Log.d(config.commandLoggingTag, "finishOpenedActivities()");
        }

        activityUtils.finishOpenedActivities();
        application.unregisterActivityLifecycleCallbacks(activityLifecycleCallbacks);
    }

    /**
     * Clicks the specified WebElement.
     *
     * @param webElement the WebElement to click
     */

    public void clickOnWebElement(WebElement webElement){
        if(config.commandLogging){
            Log.d(config.commandLoggingTag, "clickOnWebElement("+webElement+")");
        }

        if(webElement == null){
            Log.e(LOG_TAG, "WebElement is null and can therefore not be clicked!");
            return;
        }

        clicker.clickOnScreenForWeb(webElement.getLocationX(), webElement.getLocationY());
    }

    /**
     * Clicks the specified View.
     *
     * @param view the {@link View} to click
     */

    public void clickOnView(View view) {
        if(config.commandLogging){
            Log.d(config.commandLoggingTag, "clickOnView("+view+")");
        }

        view = waiter.waitForView(view, Timeout.getSmallTimeout());
        clicker.clickOnScreen(view);
    }

    /**
     * Clicks the specified list line and returns an ArrayList of the TextView objects that
     * the list line is displaying. Will use the first ListView it finds.
     *
     * @param line the line to click
     * @return an {@code ArrayList} of the {@link TextView} objects located in the list line
     */

    public ArrayList<TextView> clickInList(int line) {
        if(config.commandLogging){
            Log.d(config.commandLoggingTag, "clickInList("+line+")");
        }

        return clicker.clickInList(line);
    }

    /**
     *
     * @param name name the name to give the screenshot
     * @param quality the compression rate. From 0 (compress for lowest size) to 100 (compress for maximum quality)
     * @param watermark_x the watermark x coordinate
     * @param watermark_y the watermark y coordinate
     */
    public void takeScreenshot(String name, int quality, float watermark_x, float watermark_y){
        if(config.commandLogging){
            Log.d(config.commandLoggingTag, "takeScreenshot(\""+name+"\", "+quality+")");
        }

        screenshotTaker.takeScreenshot(name, quality, watermark_x, watermark_y);
    }

    /**
     * Takes a screenshot and saves it with the specified name in the {@link Config} objects save path (default set to: /sdcard/Robotium-Screenshots/).
     * Requires write permission (android.permission.WRITE_EXTERNAL_STORAGE) in AndroidManifest.xml of the application under test.
     *
     * @param name the name to give the screenshot
     */

    public void takeScreenshotForAuto(String name){
        if(config.commandLogging){
            Log.d(config.commandLoggingTag, "takeScreenshot(\""+name+"\")");
        }
        screenshotTaker.takeScreenshot(name, 100, 0, 0);
    }

    /**
     *
     * @param name name the name to give the screenshot
     */
    void takeScreenshotForAuto(String name, WebElement element){
        if(config.commandLogging){
            Log.d(config.commandLoggingTag, "takeScreenshot(\""+name+"\", "+element+")");
        }
        if (element == null)return;
        int xy[] = new int[2];
        element.getLocationOnScreen(xy);
        float watermark_x = element.getLocationX();
        float watermark_y = element.getLocationY();
        screenshotTaker.takeScreenshot(name + "/" +  TimeUtils.getDate(), 20, watermark_x, watermark_y);
    }

    /**
     * Scrolls a ScrollView.
     *
     * @param direction the direction to be scrolled
     * @return {@code true} if scrolling occurred, false if it did not
     */
    public boolean scrollView(final View view, int direction){
        return scroller.scrollView(view, direction);
    }

    /**
     * Scroll the specified RecyclerView to the specified line.
     *
     * @param recyclerView the {@link RecyclerView} to scroll
     * @param line the line to scroll to
     */

    public void scrollRecyclerViewToLine(RecyclerView recyclerView, int line){
        if(config.commandLogging){
            Log.d(config.commandLoggingTag, "scrollRecyclerViewToLine("+recyclerView+", "+line+")");
        }

        scroller.scrollRecyclerViewToLine(recyclerView, line);
    }

    public ArrayList<TextView> clickInRecyclerView(ViewGroup recyclerView, int itemIndex) {
        if(config.commandLogging){
            Log.d(config.commandLoggingTag, "clickInRecyclerView("+itemIndex+")");
        }

        return clicker.clickInRecyclerView(recyclerView, itemIndex);
    }

    public void scrollLeft() {
        sleep(config.sleepDuration);
        drag(width / 2, 0, height / 2, height / 2, 1);
        sleep(config.sleepDuration);
    }

    public void scrollRight() {
        sleep(config.sleepDuration);
        drag(width / 2, width, height / 2, height / 2, 1);
        sleep(config.sleepDuration);
    }

    /**
     * Waits for the current web page URL.
     * @param timeout the the amount of time in milliseconds to wait
     * @return the current web page URL
     */

    public String waitForWebUrl(int timeout){
        String url = "";
        long endTime = SystemClock.uptimeMillis() + (long)timeout;
        while (SystemClock.uptimeMillis() <= endTime) {
            sleeper.sleepMini();
            url = getWebUrl();
            if (!TextUtils.isEmpty(url)) {
                return url;
            }
        }
        return url;
    }

    /**
     * Pull down to refresh for ListView or RecyclerView
     * @param timeout timeout
     */
    public void pullDown(int timeout){
        Log.d(LOG_TAG, "Pull-down refresh.");
        sleep(config.sleepDuration);
        drag(width / 2, width / 2, height / 2, height, 20);
        sleep(timeout);
    }

    /**
     * Pull up to refresh for ListView or RecyclerView
     * @param timeout timeout
     */
    public void pullUp(int timeout){
        Log.d(LOG_TAG, "Pull-up loading.");
        sleep(config.sleepDuration);
        drag(width / 2, width / 2, height / 2, 0, 20);
        sleep(timeout);
    }

    /**
     * Types text in the specified EditText.
     *
     * @param editText the {@link EditText} to type text in
     */

    public void typeText(EditText editText) {
        if(config.commandLogging){
            Log.d(config.commandLoggingTag, "typeText("+editText+")");
        }
        int type = editText.getInputType();
        int length = getMaxLengthForEditText(editText);
        switch (type) {
            case InputType.TYPE_CLASS_NUMBER:
                typeText(editText, RandomUtils.getRandomNumber(length));
                break;
            case InputType.TYPE_CLASS_PHONE:
                typeText(editText, RandomUtils.getRandomPhone());
                break;
            case InputType.TYPE_CLASS_TEXT:
                typeText(editText, RandomUtils.getRandomText(length));
                break;
            case InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS:
                typeText(editText, RandomUtils.getRandomEmail(length));
                break;
            case InputType.TYPE_TEXT_VARIATION_URI:
                typeText(editText, RandomUtils.getRandomUrl(length));
                break;
            case InputType.TYPE_NUMBER_FLAG_SIGNED:
                typeText(editText, "-" + RandomUtils.getRandomNumber(length - 1));
                break;
            case InputType.TYPE_NUMBER_FLAG_DECIMAL:
                typeText(editText, "." + RandomUtils.getRandomNumber(length - 1));
                break;
            default:
                typeText(editText, RandomUtils.getRandomText(length));
                break;
        }
        sleep(config.sleepDuration / 10);
        hideSoftKeyboard();
    }

    /**
     * Returns the maximum length of the editText
     * @param editText editText
     * @return maximum length
     */
    private int getMaxLengthForEditText(EditText editText){
        int maxLength = 0;
        for (InputFilter inputFilter : editText.getFilters()) {
            if (inputFilter instanceof InputFilter.LengthFilter) {
                Class<InputFilter.LengthFilter> clazz = InputFilter.LengthFilter.class;
                try {
                    Field maxField = clazz.getDeclaredField("mMax");
                    maxField.setAccessible(true);
                    return (int) maxField.get(inputFilter);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return maxLength;
    }

    /**
     * Start the iteration.
     * This method is iterating the total switch, only call it.
     * @throws Exception
     */
    public void startIteration() throws Exception{
        checkResult();
        switch (config.mode) {
            case FAST:
                FastOrNormalMode();
                break;
            case NORMAL:
                FastOrNormalMode();
                break;
            case REPTILE:
                reptileMode();
                break;
            case RECORD:
                recordMode();
                break;
            default:
                recordMode();
                break;
        }
    }

    /**
     * Login operation.
     * @throws Exception
     */
    public void login() throws Exception{
        String[] strings = config.homeActivity.split("\\.");
        String homeActivity = strings[strings.length - 1];
        boolean isSuccess = waitForActivity(homeActivity, 5000);
        // 如果等不到主页则迭代当前页面（可能为授权页面弹框）
        if (!isSuccess) {
            handler.iterationNode(null, "", null);
            isSuccess = waitForActivity(homeActivity, 3000);
            // 处理完弹框还等待不到主页，则可能是进入了引导页面
            while (!isSuccess){
                scrollLeft();
                handler.iterationNode(null, "", null);
                isSuccess = waitForActivity(homeActivity, 2000);
            }
        }
        isSuccess = waitForActivity(homeActivity, 5000);
        // 可能存在类似新手引导的界面覆盖了主页，finish
        if (!isSuccess) handler.finish(getCurrentActivity().getComponentName().getClassName());
        handler.startActivity(context, null, config.loginActivity);
        handler.handleLogin();
    }

    /**
     * Fast mode or Normal mode.
     * @throws Exception
     */
    private void FastOrNormalMode() throws Exception{
        String[] strings = config.homeActivity.split("\\.");
        waitForActivity(strings[strings.length - 1]);
        sleep(config.sleepDuration * 6);
        if (!FileUtils.existsJson()) {
            JsonParser.createJson();
        }
        handler.handleParams();
    }

    /**
     * Reptile mode.
     * @throws Exception
     */
    private void reptileMode() throws Exception{
        String[] strings = config.homeActivity.split("\\.");
        waitForActivity(strings[strings.length - 1]);
        sleep(config.sleepDuration * 10);
        if (config.newReptile) {
            handler.iteration(config.homeActivity, null, true, false);
            loopReptile();
        } else {
            throwException();
            handler.handleParams();
            loopReptile();
        }
    }

    /**
     * Record mode.
     * Sleep 1 hour, during which you can operate the app, record the activity parameters.
     */
    private void recordMode() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) Assert.fail("The current api version less than 14.");
        sleep(3600 * 1000);
    }

    private void loopReptile() throws Exception{
        JsonParser.updateJson();
        while (FileUtils.existsJson()) {
            handler.handleParams();
            JsonParser.updateJson();
        }
    }

    private void throwException() {
        boolean result = FileUtils.existsJson();
        if (!result) throw new RuntimeException(config.homeActivity +
                " may be happen crash, please check the log.");
    }

    private void checkResult() {
        if (config.mode == Config.Mode.REPTILE) {
            boolean isExists = FileUtils.existsForActivity();
            if (!isExists) return;
            throwException();
            boolean isFinish = JsonParser.isFinish();
            Log.i(LOG_TAG, "Finish: " + isFinish);
            if (!isFinish) {
                JsonParser.updateParams();
            } else {
                quitMonitor(context);
            }
            sleep(config.sleepDuration);
        }
    }

    /**
     *  Quit the Monitor.
     */
    private void quitMonitor(Context context) {
        Log.i(LOG_TAG, "Iteration is complete.");
        android.content.Intent intent = new android.content.Intent();
        intent.setAction("Auto.Monitor.quit");
        context.sendBroadcast(intent);
    }

    private void init(Config config, Instrumentation instrumentation, Activity context) {
        Context mContext = instrumentation.getTargetContext().getApplicationContext();
        SharedPreferencesHelper helper = new SharedPreferencesHelper(mContext, SharedPreferencesHelper.ARGUMENTS);
        String pkg = helper.getString(SharedPreferencesHelper.PACKAGE);



        checkConfig(config);
        checkReptile(config);
        checkNative(config);
        checkRunner(config);

        Log.i(LOG_TAG, "setUp()");
        Log.i(LOG_TAG, "Iteration mode is " + config.mode.toString().toLowerCase());
        // If mode is reptile to clean up the data.
        if (config.mode == Config.Mode.REPTILE && config.newReptile) {
            clearData();
        }
        int[] wh = DesignUtils.getDisplayWH(context);
        width = wh[0];
        height = wh[1];
        Log.i(LOG_TAG, "The device width: " + width);
        Log.i(LOG_TAG, "The device height: " + height);

        config.sleepDuration = changeSleepStandard(config);

        CrashHandler.getInstance().init(instrumentation.getTargetContext().getApplicationContext(),
                config);

        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy
                .Builder()
                .detectAll()
                .penaltyLog()
                .build());
        StrictMode.setVmPolicy(new StrictMode.VmPolicy
                .Builder()
                .detectAll()
                .penaltyLog()
                .build());

        startMonitor(context);
        authorizationMonitor(instrumentation);
        application.registerActivityLifecycleCallbacks(activityLifecycleCallbacks);

        new Permission(context, pkg, instrumentation).requestPermissionsForShell();
    }

    private void checkRunner(Config config) {
        SharedPreferencesHelper helper = new SharedPreferencesHelper(instrumentation.getTargetContext(),
                SharedPreferencesHelper.ARGUMENTS);
        String runner = helper.getString(SharedPreferencesHelper.RUNNER);
        if (runner != null) config.runner = runner;
        Log.d(LOG_TAG, "Runner: " + config.runner);
    }

    private void checkNative(Config config) {
        SharedPreferencesHelper helper = new SharedPreferencesHelper(instrumentation.getTargetContext(),
                SharedPreferencesHelper.ARGUMENTS);
        String useNative = helper.getString(SharedPreferencesHelper.USE_NATIVE);
        if (useNative != null && useNative.contains("true")) config.useNative = true;
        Log.d(LOG_TAG, "Use Native: " + config.useNative);
    }

    private void checkReptile(Config config) {
        SharedPreferencesHelper helper = new SharedPreferencesHelper(instrumentation.getTargetContext(),
                SharedPreferencesHelper.ARGUMENTS);
        String newReptile = helper.getString(SharedPreferencesHelper.NEW_REPTILE);
        if (newReptile != null && newReptile.contains("false")) config.newReptile = false;
        if (config.mode == Config.Mode.REPTILE)
            Log.d(LOG_TAG, "New Reptile: " + config.newReptile);
    }

    /**
     * Initialize the data.
     */
    private void clearData() {
        Log.i(LOG_TAG, "Clear data.");
        FileUtils.deleteJson();
        FileUtils.deleteActivity();
        FileUtils.deleteParams();
    }

    private void authorizationMonitor(Instrumentation instrumentation) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return;
        UiAutomation uiAutomation = instrumentation.getUiAutomation();
        if (uiAutomation == null) return;
        acrossForShellCommand("pm grant " + DAEMON + " " + Permission.WRITE_EXTERNAL_STORAGE);
    }

    /**
     * Required parameter check.
     * @param config config
     */
    private void checkConfig(Config config) {
        if (config.mode == null) {
            throw new RuntimeException("The mode is not configured, please check!");
        }
        if (config.homeActivity == null || config.homeActivity.isEmpty()) {
            throw new RuntimeException("The homeActivity is not configured, please check!");
        }
        if (config.loginActivity == null || config.loginActivity.isEmpty()) {
            throw new RuntimeException("The loginActivity is not configured, please check!");
        }
        if (config.loginAccount == null || config.loginAccount.isEmpty()) {
            throw new RuntimeException("The loginAccount is not configured, please check!");
        }
        if (config.loginPassword == null || config.loginPassword.isEmpty()) {
            throw new RuntimeException("The loginPassword is not configured, please check!");
        }
        if (config.loginId == null || config.loginId.isEmpty()) {
            throw new RuntimeException("The loginId is not configured, please check!");
        }
    }

    /**
     * Start the Monitor.
     */
    private void startMonitor(Activity activity) {
        android.content.Intent intent = activity.getPackageManager().getLaunchIntentForPackage(DAEMON);
        if (intent != null) {
            activity.startActivity(intent);
            Log.i(LOG_TAG, "Start the Monitor: " + DAEMON);
        }
    }

    /**
     * Start listening activities.
     * @param activity activity
     */
    private void activityListener(Activity activity) {
        if (config.mode == Solo.Config.Mode.RECORD || config.mode == Solo.Config.Mode.REPTILE) {
            IntentParser.recordExtras(context, activity);
        }
    }

    private int changeSleepStandard(Config config){
        if (height <= 1280){
            return config.sleepDuration *= 2;
        }
        if (height <= 1920){
            return config.sleepDuration *= 1.5;
        }
        return config.sleepDuration;
    }

    /**
     * Work across application boundaries for permission.
     */
    public void acrossForPermission(){
        acrossApplication.acrossForPermission();
    }

    /**
     * Work across application boundaries for camera.
     * @param viewId The fully qualified resource name of the view id to find. e.g: com.sec.android.app.camera:id/okay
     */
    public void acrossForCamera(String viewId){
        acrossApplication.acrossForCamera(viewId);
    }

    /**
     * Work across application boundaries for notification.
     * Listen to the notification bar 10 seconds at a time.
     */
    public void acrossForNotification(){
        acrossApplication.acrossForNotification();
    }

    /**
     * Enter text for the edit text.
     * @param text the text, Does not support Chinese.
     */
    public void acrossForEnterText(String text){
        acrossApplication.acrossForEnterText(text);
    }

    /**
     * Work across application boundaries for click.
     * @param x the x coordinate
     * @param y the y coordinate
     */
    public void acrossForClick(float x, float y){
        acrossApplication.acrossForClick(x, y);
    }

    /**
     * Work across application boundaries for click.
     * @param nodeInfo {@link AccessibilityNodeInfo}
     */
    public void acrossForClick(AccessibilityNodeInfo nodeInfo){
        acrossApplication.acrossForClick(nodeInfo);
    }

    /**
     * Work across application boundaries for QQ login.
     */
    public void acrossForQQLogin(String account, String password){
        acrossApplication.acrossForQQLogin(account, password);
    }

    /**
     * Work across application boundaries for execute shell command.
     * @param command command
     */
    public void acrossForShellCommand(String command) {
        acrossApplication.acrossForShellCommand(command);
    }
}
