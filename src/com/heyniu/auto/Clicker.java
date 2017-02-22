package com.heyniu.auto;

import android.app.Activity;
import android.app.Instrumentation;
import android.os.SystemClock;
import android.view.InputDevice;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.robotium.solo.DialogUtils;
import com.robotium.solo.RobotiumUtils;
import com.robotium.solo.Sender;
import com.robotium.solo.Sleeper;
import com.robotium.solo.Timeout;
import com.robotium.solo.ViewFetcher;
import com.robotium.solo.Waiter;
import com.robotium.solo.WebUtils;

import java.util.ArrayList;

class Clicker extends com.robotium.solo.Clicker{

    private final Activity context;
    private final Solo.Config config;
    private final ScreenshotTaker screenshotTaker;

    private ClickedSingleton mClickedSingleton = ClickedSingleton.getInstance();

    /**
     * Constructs this object.
     *
     * @param activityUtils the {@code ActivityUtils} instance
     * @param viewFetcher   the {@code ViewFetcher} instance
     * @param sender        the {@code Sender} instance
     * @param inst          the {@code android.app.Instrumentation} instance
     * @param sleeper       the {@code Sleeper} instance
     * @param waiter        the {@code Waiter} instance
     * @param webUtils      the {@code WebUtils} instance
     * @param dialogUtils   the {@code DialogUtils} instance
     */
    Clicker(Solo.Config config, ActivityUtils activityUtils, ViewFetcher viewFetcher,
            Sender sender, Instrumentation inst, Sleeper sleeper, Waiter waiter, WebUtils webUtils,
            DialogUtils dialogUtils, Activity context, ScreenshotTaker screenshotTaker) {
        super(activityUtils, viewFetcher, sender, inst, sleeper, waiter, webUtils, dialogUtils);

        this.context = context;
        this.config = config;
        this.screenshotTaker = screenshotTaker;
    }

    /**
     * Clicks on a given coordinate on the screen.
     *
     * @param x the x coordinate
     * @param y the y coordinate
     */

    public void clickOnScreen(float x, float y, View view) {
        if (view == null){
            Log.w(Solo.LOG_TAG, "view is null");
            return;
        }
        if (!view.isShown()){
            Log.w(Solo.LOG_TAG, view + " not shown");
            return;
        }
        int xy1[] = new int[2];
        view.getLocationOnScreen(xy1);
        // If activity is a login activity, has been clicked to continue to click.
        if (!view.getContext().toString().contains(config.loginActivity)) {
            // Remove view that have been clicked.
            if (mClickedSingleton.containsKeyForNative(view.getId())) {
                Log.w(Solo.LOG_TAG, view + " is clicked, ignore it.");
                return;
            }
        }
        takeScreenshot(view);
        int xy[] = new int[2];
        view.getLocationOnScreen(xy);
        if (view.getId() != -1) mClickedSingleton.putForNative(view.getId(), xy);
        // If true, double click on View.
        click(x, y);
    }

    /**
     * Clicks on a given coordinate on the screen.
     *
     * @param x the x coordinate
     * @param y the y coordinate
     */
    private void click(float x, float y){
        long downTime = SystemClock.uptimeMillis();
        long eventTime = SystemClock.uptimeMillis();
        MotionEvent event = MotionEvent.obtain(downTime, eventTime,
                MotionEvent.ACTION_DOWN, x, y, 0);
        MotionEvent event2 = MotionEvent.obtain(downTime, eventTime,
                MotionEvent.ACTION_UP, x, y, 0);
        event.setSource(InputDevice.SOURCE_TOUCHSCREEN);
        event2.setSource(InputDevice.SOURCE_TOUCHSCREEN);
        try{
            inst.sendPointerSync(event);
            inst.sendPointerSync(event2);
        } catch(SecurityException e){
            Log.e(Solo.LOG_TAG, "Click at ("+x+", "+y+") can not be completed! ("+(e != null ? e.getClass().getName()+": "+e.getMessage() : "null")+")");
        } finally {
            event.recycle();
            event2.recycle();
        }

    }

    /**
     * Clicks on a given {@link View}.
     *
     * @param view the view that should be clicked
     */

    public void clickOnScreen(View view) {
        clickOnScreen(view, false, 0);
    }

    /**
     * Private method used to click on a given view.
     *
     * @param view the view that should be clicked
     * @param longClick true if the click should be a long click
     * @param time the amount of time to long click
     */

    public void clickOnScreen(View view, boolean longClick, int time) {
        if(view == null){
            Log.e(Solo.LOG_TAG, "The view is null and can therefore not be clicked!");
            return;
        }

        float[] xyToClick = getClickCoordinates(view);
        float x = xyToClick[0];
        float y = xyToClick[1];

        if(x == 0 || y == 0){
            sleeper.sleepMini();
            try {
                view = viewFetcher.getIdenticalView(view);
            } catch (Exception ignored){}

            if(view != null){
                xyToClick = getClickCoordinates(view);
                x = xyToClick[0];
                y = xyToClick[1];
            }
        }

        if (longClick)
            clickLongOnScreen(x, y, time, view);
        else
            clickOnScreen(x, y, view);
    }

    /**
     * Clicks on a given coordinate on the screen from web view.
     *
     * @param x the x coordinate
     * @param y the y coordinate
     */
    void clickOnScreenForWeb(float x, float y) {
        click(x, y);
    }

    ArrayList<TextView> clickInRecyclerView(ViewGroup recyclerView, int line) {
        return clickInRecyclerView(recyclerView, line, 0, 0, false, 0);
    }

    private ArrayList<TextView> clickInRecyclerView(ViewGroup recyclerView, int itemIndex, int recyclerViewIndex, int id, boolean longClick, int time) {
        View viewOnLine;
        final long endTime = SystemClock.uptimeMillis() + Timeout.getSmallTimeout();

        if(itemIndex < 0)
            itemIndex = 0;

        ArrayList<View> views = new ArrayList<>();

        if(recyclerView == null){
            Log.e(Solo.LOG_TAG, "RecyclerView is not found!");
            return RobotiumUtils.filterViews(TextView.class, views);
        }
        else{
            failIfIndexHigherThenChildCount(recyclerView, itemIndex, endTime);
            viewOnLine = getViewOnRecyclerItemIndex(recyclerView, recyclerViewIndex, itemIndex);
        }

        if(viewOnLine != null){
            views = viewFetcher.getViews(viewOnLine, true);
            views = RobotiumUtils.removeInvisibleViews(views);

            if(id == 0){
                clickOnScreen(viewOnLine, longClick, time);
            }
            else{
                clickOnScreen(getView(id, views));
            }
        }
        return RobotiumUtils.filterViews(TextView.class, views);
    }

    /**
     * Screenshot with click coordinates
     * @param view view
     */
    private void takeScreenshot(View view){
        if (!config.iterationScreenShots){
            return;
        }
        if (view == null){
            Log.w(Solo.LOG_TAG, "view is null");
            return;
        }
        if (!view.isShown()){
            Log.w(Solo.LOG_TAG, view + " not shown");
            return;
        }
        int xy[] = new int[2];
        view.getLocationOnScreen(xy);
        float watermark_x = xy[0] + view.getWidth() / 2.0f;
        float watermark_y = xy[1] + view.getHeight() / 2.0f;
        // The watermark is displayed in half.
        int [] wh = DesignUtils.getDisplayWH(context);
        int height = wh[1];
        String name = view.getContext().getClass().getName();
        if (watermark_y > height) watermark_y = height - DesignUtils.px2dip(context, 100);
        Log.d(Solo.LOG_TAG, "takeScreenshot(\""+name+"\", "+view+")");
        screenshotTaker.takeScreenshot(name + "/" +  TimeUtils.getDate(), 20, watermark_x, watermark_y);
        sleeper.sleep(100);
    }
}
