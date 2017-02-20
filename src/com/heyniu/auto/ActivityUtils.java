package com.heyniu.auto;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.view.KeyEvent;

import com.robotium.solo.Sleeper;
import com.robotium.solo.Solo;

import java.util.ArrayList;
import java.util.List;

class ActivityUtils extends com.robotium.solo.ActivityUtils{

    /**
     * Constructs this object.
     *
     * @param config   the {@code Config} instance
     * @param inst     the {@code Instrumentation} instance.
     * @param activity the start {@code Activity}
     * @param sleeper  the {@code Sleeper} instance
     */
    ActivityUtils(Solo.Config config, Instrumentation inst, Activity activity, Sleeper sleeper) {
        super(config, inst, activity, sleeper);
    }

    /**
     * Returns to the given {@link Activity}.
     *
     * @param name the name of the {@code Activity} to return to, e.g. {@code "MyActivity"}
     */

    public void goBackToActivity(String name)
    {
        ArrayList<Activity> activitiesOpened = getAllOpenedActivities();
        boolean found = false;
        for (Activity anActivitiesOpened1 : activitiesOpened) {
            if (anActivitiesOpened1.getClass().getSimpleName().equals(name)) {
                found = true;
                break;
            }
        }
        if(found){
            while(!getCurrentActivity().getClass().getSimpleName().equals(name))
            {
                try{
                    inst.sendKeyDownUpSync(KeyEvent.KEYCODE_BACK);
                }catch(SecurityException ignored){}
            }
        }
        else{
            for (Activity anActivitiesOpened : activitiesOpened) {
                Log.d(com.heyniu.auto.Solo.LOG_TAG, "Activity priorly opened: " + anActivitiesOpened.getClass().getSimpleName());
            }
            Log.e(com.heyniu.auto.Solo.LOG_TAG, "No Activity named: '" + name + "' has been priorly opened");
        }
    }

    /**
     * Get target package activities.
     * @param context context
     * @return activities
     */
    ActivityInfo[] getAllActivities(Context context){
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo info = pm.getPackageInfo(context.getPackageName(), PackageManager.GET_ACTIVITIES);
            return info.activities;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Get target launcher Activity.
     * @return launcher Activity
     */
    public String getLauncherActivity(Context context){
        android.content.Intent intent = new android.content.Intent(Intent.ACTION_MAIN, null);
        intent.setPackage(context.getPackageName());
        List<ResolveInfo> resolveInfos = context.getPackageManager().queryIntentActivities(intent, 0);
        if (resolveInfos != null && resolveInfos.size() > 0) return resolveInfos.get(0).activityInfo.name;
        return null;
    }

}
