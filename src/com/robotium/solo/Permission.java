package com.robotium.solo;

import android.app.Activity;
import android.app.Instrumentation;
import android.app.UiAutomation;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.ArrayList;
import java.util.List;

import static com.robotium.solo.Solo.LOG_TAG;

class Permission {

    /**
     * CALENDAR
     **/
    private static final String READ_CALENDAR = "android.permission.READ_CALENDAR";
    private static final String WRITE_CALENDAR = "android.permission.WRITE_CALENDAR";
    /**
     * CAMERA
     **/
    private static final String CAMERA = "android.permission.CAMERA";
    /**
     * CONTACTS
     **/
    private static final String READ_CONTACTS = "android.permission.READ_CONTACTS";
    private static final String WRITE_CONTACTS = "android.permission.WRITE_CONTACTS";
    private static final String GET_ACCOUNTS = "android.permission.GET_ACCOUNTS";
    /**
     * LOCATION
     **/
    private static final String ACCESS_FINE_LOCATION = "android.permission.ACCESS_FINE_LOCATION";
    private static final String ACCESS_COARSE_LOCATION = "android.permission.ACCESS_COARSE_LOCATION";
    /**
     * AUDIO
     **/
    private static final String RECORD_AUDIO = "android.permission.RECORD_AUDIO";
    /**
     * PHONE
     **/
    private static final String READ_PHONE_STATE = "android.permission.READ_PHONE_STATE";
    private static final String CALL_PHONE = "android.permission.CALL_PHONE";
    private static final String READ_CALL_LOG = "android.permission.READ_CALL_LOG";
    private static final String WRITE_CALL_LOG = "android.permission.WRITE_CALL_LOG";
    private static final String ADD_VOICEMAIL = "com.android.voicemail.permission.ADD_VOICEMAIL";
    private static final String USE_SIP = "android.permission.USE_SIP";
    private static final String PROCESS_OUTGOING_CALLS = "android.permission.PROCESS_OUTGOING_CALLS";
    /**
     * SENSORS
     **/
    private static final String BODY_SENSORS = "android.permission.BODY_SENSORS";
    /**
     * SMS
     **/
    private static final String SEND_SMS = "android.permission.SEND_SMS";
    private static final String RECEIVE_SMS = "android.permission.RECEIVE_SMS";
    private static final String READ_SMS = "android.permission.READ_SMS";
    private static final String RECEIVE_WAP_PUSH = "android.permission.RECEIVE_WAP_PUSH";
    private static final String RECEIVE_MMS = "android.permission.RECEIVE_MMS";
    private static final String READ_CELL_BROADCASTS = "android.permission.READ_CELL_BROADCASTS";
    /**
     * SD Card
     **/
    private static final String READ_EXTERNAL_STORAGE = "android.permission.READ_EXTERNAL_STORAGE";
    private static final String WRITE_EXTERNAL_STORAGE = "android.permission.WRITE_EXTERNAL_STORAGE";

    /**
     * Package Installer
     */
    private static final String PACKAGE_INSTALLER = "com.android.packageinstaller";
    private static final String PERMISSION_ALLOW_ID = "com.android.packageinstaller:id/permission_allow_button";
    private static final String PACKAGE_INSTALLER_XIAOMI = "com.lbe.security.miui";
    private static final String PERMISSION_ALLOW_ID_XIAOMI = "android:id/button1";

    private static final String[] PermissionGroup = new String[]{READ_CALENDAR, WRITE_CALENDAR, CAMERA, READ_CONTACTS,
            WRITE_CONTACTS, GET_ACCOUNTS, ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION, RECORD_AUDIO, READ_PHONE_STATE,
            CALL_PHONE, READ_CALL_LOG, WRITE_CALL_LOG, ADD_VOICEMAIL, USE_SIP, PROCESS_OUTGOING_CALLS, BODY_SENSORS,
            SEND_SMS, RECEIVE_SMS, READ_SMS, RECEIVE_WAP_PUSH, RECEIVE_MMS, READ_CELL_BROADCASTS, READ_EXTERNAL_STORAGE,
            WRITE_EXTERNAL_STORAGE};

    private Context context;
    private String pkg;
    private Instrumentation instrumentation;

    Permission(Context context, String pkg, Instrumentation instrumentation){
        this.context = context;
        this.pkg = pkg;
        this.instrumentation = instrumentation;
    }

    /**
     * Returns Manufacturer.
     */
    private String getManufacturer(){
        return Build.MANUFACTURER;
    }

    /**
     * Requests permissions to be granted to this application.
     */
    void requestPermissions(){
        if (Build.VERSION.SDK_INT >= 23) {
            String[] permissions = checkPermissions();
            if (permissions == null || permissions.length == 0) return;
            final String manufacturer = getManufacturer();
            ActivityCompat.requestPermissions((Activity) context, permissions, 10000);
            UiAutomation uiAutomation = instrumentation.getUiAutomation();
            uiAutomation.setOnAccessibilityEventListener(new UiAutomation.OnAccessibilityEventListener() {
                @Override
                public void onAccessibilityEvent(AccessibilityEvent event) {
                    android.util.Log.d(LOG_TAG, "UiAutomation: " + event.toString());
                    if (manufacturer.toLowerCase().contains("mi")) {
                        handlePermissions(event, PACKAGE_INSTALLER_XIAOMI, PERMISSION_ALLOW_ID_XIAOMI);
                    } else handlePermissions(event, PACKAGE_INSTALLER, PERMISSION_ALLOW_ID);
                }
            });
        }
    }

    private void handlePermissions(AccessibilityEvent event, String packageInstaller, String permissionAllowId) {
        if (packageInstaller.contains(event.getPackageName())) {
            if (event.getSource() != null) {
                List<AccessibilityNodeInfo> infoList = event.getSource().findAccessibilityNodeInfosByViewId(permissionAllowId);
                if (infoList == null || infoList.isEmpty()) return;
                performClick(infoList.get(0));
            }
        }
    }

    private void performClick(AccessibilityNodeInfo nodeInfo) {
        if(nodeInfo == null){
            Log.w(LOG_TAG, "performClick: nodeInfo is null.");
            return;
        }
        if(nodeInfo.isClickable()) nodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);
        else performClick(nodeInfo.getParent());
    }

    /**
     * Check Permissions, if permission denied add to requestPermissions.
     * @return requestPermissions
     */
    private String[] checkPermissions(){
        ArrayList<String> requestPermissions = new ArrayList<>();
        String[] permissions = filterPermissions();
        if (permissions == null) return null;
        for (String p: permissions) {
            int mPermission = ContextCompat.checkSelfPermission(context, p);
            if (mPermission != PackageManager.PERMISSION_GRANTED) requestPermissions.add(p);
        }
        return requestPermissions.toArray(new String[requestPermissions.size()]);
    }

    /**
     * Filter Permissions
     * @return Permissions
     */
    private String[] filterPermissions(){
        ArrayList<String> filter = new ArrayList<>();
        String[] allPermissions = getAllPermissions(context, pkg);
        if (allPermissions == null) return null;
        for (String permission: PermissionGroup) {
            for (String s: allPermissions) {
                if (permission.contains(s)) filter.add(permission);
            }
        }
        return filter.toArray(new String[filter.size()]);
    }

    /**
     * Get target package requestedPermissions.
     * @param context context
     * @param pkg target package
     * @return Permissions
     */
    private String[] getAllPermissions(Context context, String pkg){
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo info = pm.getPackageInfo(pkg, PackageManager.GET_PERMISSIONS);
            return info.requestedPermissions;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
}
