package com.heyniu.auto;

import android.app.Instrumentation;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.UiAutomation;
import android.content.Context;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.os.SystemClock;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import static android.view.accessibility.AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED;
import static com.heyniu.auto.Solo.LOG_TAG;

class AcrossApplication {

    private Context context;
    private String pkg;
    private Instrumentation instrumentation;
    private Solo.Config config;
    private UiAutomation uiAutomation;

    private final static String QQ = "com.tencent.mobileqq";
    private final static String WECHAT = "com.tencent.mm";
    private final static String WEIBO = "com.sina.weibo";

    AcrossApplication(Context context, Instrumentation instrumentation, Solo.Config config){
        this.context = context;
        this.pkg = config.PACKAGE;
        this.instrumentation = instrumentation;
        this.config = config;
        uiAutomation = instrumentation.getUiAutomation();
    }

    /**
     * Work across application boundaries for permission.
     */
    void acrossForPermission(){
        Permission permission = new Permission(context, pkg, instrumentation);
        permission.requestPermissions();
    }

    /**
     * Work across application boundaries for camera.
     * @param viewId The fully qualified resource name of the view id to find. e.g: com.sec.android.app.camera:id/okay
     */
    void acrossForCamera(String viewId){
        Log.d(LOG_TAG, "acrossForCamera()");
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2) return;
        UiAutomation uiAutomation = instrumentation.getUiAutomation();
        uiAutomation.setOnAccessibilityEventListener(new UiAutomation.OnAccessibilityEventListener() {
            @Override
            public void onAccessibilityEvent(AccessibilityEvent event) {
                if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
                    if (viewId.contains(event.getPackageName())) {
                        if (event.getSource() != null) {
                            List<AccessibilityNodeInfo> infoList = event.getSource().findAccessibilityNodeInfosByViewId(viewId);
                            if (infoList == null || infoList.isEmpty()) return;
                            performClick(infoList.get(0));
                        }
                    }
                }
            }
        });
        sleep(config.sleepDuration * 4);
        shellCommand("input keyevent 27");
    }

    /**
     * Work across application boundaries for QQ login.
     */
    void acrossForQQLogin(String account, String password){
        boolean installed = isInstalled(QQ);
        if (!installed) {
            Log.w(LOG_TAG, "QQ is not installed.");
            return;
        }
        uiAutomation.setOnAccessibilityEventListener(new UiAutomation.OnAccessibilityEventListener() {
            @Override
            public void onAccessibilityEvent(AccessibilityEvent event) {
                Log.d(LOG_TAG, "Event: " + event.toString());
                if (event.getEventType() == TYPE_WINDOW_STATE_CHANGED && QQ.contains(event.getPackageName())){
                    handleQQLogin(event, account, password);
                    handleAuthorization(event);
                }
            }
        });
    }

    private void handleAuthorization(AccessibilityEvent event) {
        if (event.getClassName().toString().contains("com.tencent.open.agent.AuthorityActivity")) {
            Log.i(LOG_TAG, "QQ Login: " + event.toString());
            sleep(config.sleepDuration * 4);
            AccessibilityNodeInfo nodeInfo = uiAutomation.getRootInActiveWindow();
            IterationNode(nodeInfo, "android.widget.Button");
        }
    }

    /**
     * Loop through the view and click.
     * @param nodeInfo node
     * @param name class name, e.g: android.widget.Button
     */
    private void IterationNode(AccessibilityNodeInfo nodeInfo, String name) {
        for (int i = 0; i < nodeInfo.getChildCount(); i ++){
            AccessibilityNodeInfo node = nodeInfo.getChild(i);
            Log.i(LOG_TAG, "QQ Login nodeInfo.getChild(i): " + node.getClassName());
            if(name.contains(node.getClassName())) {
                performClick(node);
                break;
            } else IterationNode(node, name);
        }
    }

    private void handleQQLogin(AccessibilityEvent event, String account, String password) {
        if (event.getClassName().toString().contains("com.tencent.qqconnect.wtlogin.Login")) {
            AccessibilityNodeInfo nodeInfo = uiAutomation.getRootInActiveWindow();
            List<AccessibilityNodeInfo> infoList = nodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mobileqq:id/account");
            if (infoList == null || infoList.isEmpty()) return;
            // Click the account edit text and enter text.
            performClick(infoList.get(0));
            acrossForEnterText(account);
            infoList = nodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mobileqq:id/password");
            if (infoList == null || infoList.isEmpty()) return;
            // Click the password edit text and enter text.
            performClick(infoList.get(0));
            acrossForEnterText(password);
            sleep(config.sleepDuration * 5);
            IterationNode(nodeInfo, "android.widget.Button");
        }
    }

    /**
     * Enter text for the edit text.
     * @param text the text, Does not support Chinese.
     */
    void acrossForEnterText(String text){
        shellCommand("input text " + text);
    }

    /**
     * Work across application boundaries for click.
     * @param x the x coordinate
     * @param y the y coordinate
     */
    void acrossForClick(float x, float y){
        MotionEvent motionDown = MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), KeyEvent.ACTION_DOWN,
				x,  y, 0);
		motionDown.setSource(InputDevice.SOURCE_TOUCHSCREEN);
		uiAutomation.injectInputEvent(motionDown, true);
		MotionEvent motionUp = MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), KeyEvent.ACTION_UP,
				x, y, 0);
		motionUp.setSource(InputDevice.SOURCE_TOUCHSCREEN);
		uiAutomation.injectInputEvent(motionUp, true);
		motionUp.recycle();
		motionDown.recycle();
    }

    /**
     * Work across application boundaries for click.
     * @param nodeInfo {@link AccessibilityNodeInfo}
     */
    void acrossForClick(AccessibilityNodeInfo nodeInfo){
        performClick(nodeInfo);
    }

    /**
     * Work across application boundaries for execute shell command.
     * @param command command
     */
    void acrossForShellCommand(String command) {
        shellCommand(command);
    }

    /**
     * Work across application boundaries for notification.
     * Listen to the notification bar 10 seconds at a time.
     */
    void acrossForNotification(){
        UiAutomation uiAutomation = instrumentation.getUiAutomation();
        uiAutomation.setOnAccessibilityEventListener(new UiAutomation.OnAccessibilityEventListener() {
            @Override
            public void onAccessibilityEvent(AccessibilityEvent event) {
                if (event.getEventType() == AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED) {
                    if (config.PACKAGE.contains(event.getPackageName())) {
                        if (event.getParcelableData() != null && event.getParcelableData() instanceof Notification) {
                            Notification notification = (Notification) event.getParcelableData();
                            PendingIntent pendingIntent = notification.contentIntent;
                            try {
                                android.util.Log.d(LOG_TAG, "Notification: " + event.toString());
                                pendingIntent.send();
                            } catch (PendingIntent.CanceledException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        });
        sleep(1000 * 10);
    }

    /**
     * Performs click on the node.
     * @param nodeInfo {@link AccessibilityNodeInfo}
     */
    private void performClick(AccessibilityNodeInfo nodeInfo) {
        if(nodeInfo == null){
            Log.w(LOG_TAG, "performClick: nodeInfo is null.");
            return;
        }
        if(nodeInfo.isClickable()) nodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);
        else performClick(nodeInfo.getParent());
    }

    /**
     * Sleeps the current thread for <code>time</code> milliseconds.
     *
     * @param time the length of the sleep in milliseconds
     */

    private void sleep(int time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException ignored) {}
    }

    /**
     * The application is installed on the device.
     * @param pkg package
     * @return the application is installed on the device.
     */
    private boolean isInstalled(String pkg) {
        String s = shellCommand("pm list packages " + pkg);
        return s.contains(pkg);
    }

    private static StringBuilder getRuntime(String command){
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

    /**
     * Executes a shell command. This method returs a file descriptor that points
     * to the standard output stream. The command execution is similar to running
     * "adb shell <command>" from a host connected to the device.
     * @param command The command to execute.
     * @return result
     */
    private String shellCommand(String command){
        StringBuilder sb = new StringBuilder();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ParcelFileDescriptor parcelFileDescriptor = uiAutomation.executeShellCommand(command);
            InputStream is = new ParcelFileDescriptor.AutoCloseInputStream(parcelFileDescriptor);
            BufferedReader reader = null;
            try {
                reader = new BufferedReader(new InputStreamReader(is));
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
        } else return getRuntime(command).toString();
        return sb.toString();
    }
}
