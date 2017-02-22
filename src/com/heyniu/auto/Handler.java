package com.heyniu.auto;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.UiAutomation;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Looper;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTabHost;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;

import com.robotium.solo.By;
import com.robotium.solo.WebElement;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.heyniu.auto.Solo.LOG_TAG;

class Handler {

    private final Solo solo;
    private final Solo.Config config;
    private final UiAutomation uiAutomation;
    private final Context context;
    private final ScreenshotTaker screenshotTaker;
    private final ActivityUtils activityUtils;

    private ClickedSingleton mClickedSingleton = ClickedSingleton.getInstance();
    private Activity currentActivity;

     Handler(Solo solo){
        this.solo = solo;
        this.config = solo.getConfig();
        this.uiAutomation = solo.getUiAutomation();
         this.context = solo.getContext();
        this.screenshotTaker = solo.getScreenshotTaker();
         this.activityUtils = solo.getActivityUtils();
    }

    /**
     * Handle native view
     */
    private void handleNative(String activity, Map<String, Object> params) throws Exception{
        handleFragmentTabHost(activity, params, false);
        handleViewPager(activity, params, false);
        handleRecyclerView(activity, params);
        handleListView(activity, params);
        handleGridView(activity, params);
        handleScrollView(activity, params);
        handleOtherView(activity, params);
    }

    /**
     * Handle fragmentTabHost
     * @param activity activity
     * @param params params
     */
    private void handleFragmentTabHost(String activity, Map<String, Object> params, boolean isUiAutomation) throws Exception{
        ArrayList<FragmentTabHost> fragmentTabHosts = solo.getCurrentViews(FragmentTabHost.class);
        if (fragmentTabHosts.size() > 0) {
            FragmentTabHost fragmentTabHost = fragmentTabHosts.get(0);
            if (fragmentTabHost == null) {
                return;
            }
            Log.d(LOG_TAG, "Find fragmentTabHost: " + fragmentTabHost.toString());
            Field fieldFragment = FragmentTabHost.class.getDeclaredField("mTabs");
            fieldFragment.setAccessible(true);
            int size = ((ArrayList) fieldFragment.get(fragmentTabHost)).size();
            Log.d(LOG_TAG, "FragmentTabHost size: " + size);
            for (int i = 0; i < size; i ++) {
                if (fragmentTabHost == null) return;

                final int tab = i;
                try {
                    fragmentTabHost.post(new Runnable() {
                        @Override
                        public void run() {
                            fragmentTabHost.setCurrentTab(tab);
                        }
                    });
                } catch (IllegalStateException ignored){}
                Log.d(LOG_TAG, "FragmentTabHost setCurrentTab(" + i + ")");
                solo.sleep(config.sleepDuration * 4);
                if (isUiAutomation) {
                    handleViewPager(activity, params, true);
                }
                else {
                    handleListView(activity, params);
                    handleRecyclerView(activity, params);
                    handleGridView(activity, params);
                    handleViewPager(activity, params, false);
                    handleScrollView(activity, params);
                    handleOtherView(activity, params);
                }
            }
        } else iterationNode(null, activity, params);
    }

    /**
     * Handle ViewPager
     */
    private void handleViewPager(String activity, Map<String, Object> params, boolean isUiAutomation) throws Exception{
        ArrayList<ViewPager> viewPagers = solo.getCurrentViews(ViewPager.class);
        if (viewPagers.size() > 0) {
            Log.d(Solo.LOG_TAG, "Find viewPager: " + viewPagers.get(0).toString());
            ViewPager viewPager = viewPagers.get(0);
            if (viewPager == null || viewPager.getAdapter() == null) {
                Log.d(Solo.LOG_TAG, "viewPager or viewPager.getAdapter() is null, return.");
                return;
            }
            int size = viewPager.getAdapter().getCount();
            Log.d(Solo.LOG_TAG, "ViewPager size: " + size);
            for (int i = 0; i < size; i ++) {
                if (viewPager == null) return;

                final int tab = i;
                if (viewPager.getAdapter() instanceof PagerAdapter) {
                    viewPager.post(new Runnable() {
                        @Override
                        public void run() {
                            viewPager.setCurrentItem(tab);
                        }
                    });
                    Log.d(Solo.LOG_TAG, "ViewPager setCurrentItem(" + i + ")");
                    solo.sleep(config.sleepDuration * 4);

                    if (isUiAutomation) iterationNode(null, activity, params);
                    else {
                        handleListView(activity, params);
                        handleRecyclerView(activity, params);
                        handleGridView(activity, params);
                        handleScrollView(activity, params);
                        handleOtherView(activity, params);
                    }

                } else
                if (viewPager.getAdapter() instanceof FragmentPagerAdapter || viewPager.getAdapter() instanceof FragmentStatePagerAdapter) {
                    handleRecyclerView(activity, params);
                }
            }

        } else iterationNode(null, activity, params);
    }

    /**
     * Handle ScrollView
     */
    private void handleScrollView(String activity, Map<String, Object> params) throws Exception{
        ArrayList<ScrollView> scrollViews = solo.getCurrentViews(ScrollView.class);
        if (scrollViews.size() > 0) {
            if (scrollViews.get(0) == null) return;
            ScrollView scrollView = scrollViews.get(0);
            Log.d(Solo.LOG_TAG, "Find scrollView: " + scrollView.toString());
            Log.d(Solo.LOG_TAG, "ScrollView ChildCount: " + scrollView.getChildCount());
            for (int i = 0; i < scrollView.getChildCount(); i ++){
                View view = scrollView.getChildAt(i);
                Log.d(Solo.LOG_TAG, "ScrollView child: " + view);
                if (view != null) {
                    if (!(view instanceof RelativeLayout) && view instanceof ViewGroup)
                        loopScrollView(view, activity, params);
                    else {
                        solo.scrollView(view, Solo.DOWN);
                        solo.sleep(config.sleepDuration);
                        handleView(view, activity, params, solo.waitForDialogToOpen(50));
                        handleJump(currentActivity, activity, params);
                    }
                }
            }
        }
    }

    /**
     * Loop handle ScrollView, if child is LinearLayout.
     */
    private void loopScrollView(View view, String activity, Map<String, Object> params) throws Exception{
        ViewGroup viewGroup = (ViewGroup) view;
        for (int j = 0; j < viewGroup.getChildCount(); j ++) {
            View view1 = viewGroup.getChildAt(j);
            Log.d(Solo.LOG_TAG, "ScrollView child child: " + view1);
            if (view1 != null) {
                if (view1 instanceof LinearLayout) loopScrollView(view1, activity, params);
                else {
                    solo.scrollView(view1, Solo.DOWN);
                    solo.sleep(config.sleepDuration);
                    handleView(view1, activity, params, solo.waitForDialogToOpen(50));
                    handleJump(currentActivity, activity, params);
                }
            }
        }
    }

    /**
     * Handle View >> if listener is not null click it.
     * @param view view
     * @param activity activity name
     */
    private void handleView(View view, String activity, Map<String, Object> params, boolean isDialog)
            throws Exception{
        if (view == null)return;
        if (!view.isShown())return;
        if (isDialog) {
            handleDialog(activity, params);
            return;
        }
        if (view.isClickable()) clickView(view);
    }

    /**
     * Click the target view.
     */
    private void clickView(View view) {
        solo.clickOnView(view);
        solo.sleep(config.sleepDuration);
    }

    /**
     * Handle Dialog
     * If dialog is opened, get views from dialog, iteration it.
     * @param activity activity name
     */
    private void handleDialog(String activity, Map<String, Object> params) throws Exception{
        if (solo.waitForDialogToOpen(50)) {
            Log.d(Solo.LOG_TAG, "dialog is open");
            ArrayList<View> views = solo.getCurrentViews();
            Random r = new Random();
            int index = 0;

            while (index < 20 ){
                View view = views.get(r.nextInt(views.size() - 1));
                if (view.isClickable()) {
                    clickView(view);
                    handleJump(currentActivity, activity, params);
                    break;
                }
                index ++;
            }
            if (solo.waitForDialogToOpen(config.sleepDuration / 10)) {
                solo.goBack();
                solo.sleep(config.sleepDuration);
            }
        }
    }

    /**
     * Handle GridView
     */
    private void handleGridView(String activity, Map<String, Object> params) throws Exception{
        ArrayList<GridView> gridViews = solo.getCurrentViews(GridView.class);
        if (gridViews.size() > 0) {
            if (gridViews.get(0) == null) return;
            GridView gridView = gridViews.get(0);
            Log.d(Solo.LOG_TAG, "Find gridView: " + gridView.toString());
            for (int i = 0; i < gridView.getChildCount(); i ++) {
                Log.d(Solo.LOG_TAG, "GridView child: " + gridView.getChildAt(i));
                handleView(gridView.getChildAt(i), activity, params, false);
                boolean isRestart = handleJump(currentActivity, activity, params);
                if (isRestart) {
                    reHandleGridView(activity, params, i);
                    break;
                }
            }
        }
    }

    /**
     * When the GridView jumps, retry click on GridView.
     */
    private void reHandleGridView(String activity, Map<String, Object> params, int index) throws Exception{
        ArrayList<GridView> gridViews = solo.getCurrentViews(GridView.class);
        if (gridViews.size() > 0) {
            if (gridViews.get(0) == null) return;
            GridView gridView = gridViews.get(0);
            for (int i = 0; i < gridView.getChildCount(); i ++) {
                if (i > index) {
                    Log.d(Solo.LOG_TAG, "GridView child: " + gridView.getChildAt(i));
                    handleView(gridView.getChildAt(i), activity, params, false);
                    boolean isRestart = handleJump(currentActivity, activity, params);
                    if (isRestart) {
                        reHandleGridView(activity, params, i);
                        break;
                    }
                }
            }
        }
    }

    /**
     * Handle Other View e.g: TextView, ImageView, Button, EditText ...
     */
    private void handleOtherView(String activity, Map<String, Object> params) throws Exception{
        ArrayList<View> views = solo.getCurrentViews();
        for (View view: views) {
            if (view.isClickable()) {
                handleEditText(view);
                handleJump(currentActivity, activity, params);
                handleView(view, activity, params, solo.waitForDialogToOpen(50));
            }
        }
    }

    /**
     * Handle ListView
     *Simulate pull-down refresh and pull-up loading.
     */
    private void handleListView(String activity, Map<String, Object> params) throws Exception{
        ArrayList<ListView> listViews = solo.getCurrentViews(ListView.class);
        if (listViews.size() > 0) {
            ListView listView = listViews.get(0);
            if (listView == null) return;
            Log.d(Solo.LOG_TAG, "Find listView: " + listView.toString());
            final int size = listView.getCount() - 1;
            Log.d(Solo.LOG_TAG, "handleListView ListView size: " + size);
            int lastPosition = listView.getLastVisiblePosition();
            if (size > 0) {
                // Pull-down refresh
                solo.pullDown(config.sleepDuration * 4);
            }
            if (size >= lastPosition) {
                // Switch to the last item for listView.
                Log.d(Solo.LOG_TAG, "Switch to the last item for listView.");
                solo.scrollListToLine(listView, size - 1);
                // Pull-up loading
                solo.pullUp(config.sleepDuration * 4);
            }
            //ListView recovery
            Log.d(Solo.LOG_TAG, "ListView recovery.");
            solo.scrollListToLine(listView, 0);
            solo.sleep(config.sleepDuration);

            loopListView(activity, params);
        }

    }

    /**
     * Loop Handle ListView
     */
    private void loopListView(String activity, Map<String, Object> params) throws Exception{
        ArrayList<ListView> listViews = solo.getCurrentViews(ListView.class);
        if (listViews.size() > 0) {
            ListView listView = listViews.get(0);
            if (listView == null) {
                return;
            }
            int size = listView.getCount() - 1;
            int lastPosition = listView.getLastVisiblePosition();
            int firstPosition = listView.getFirstVisiblePosition();
            int visibleItem = lastPosition - firstPosition;
            Log.d(LOG_TAG, "ListView size " + size);
            Log.d(LOG_TAG, "ListView firstPosition " + firstPosition);
            Log.d(LOG_TAG, "ListView lastPosition " + lastPosition);
            Log.d(LOG_TAG, "ListView visibleItem " + visibleItem);
            for (int i = 0; i <= visibleItem; i ++) {
                boolean isRestart = handleListViewItem(i, activity, params);
                if (isRestart) break;
                if (i == visibleItem && size > lastPosition) {
                    // More than one page (screen).
                    if (size - lastPosition >= visibleItem) {
                        Log.d(LOG_TAG, "ListView setSelection to " + lastPosition);
                        solo.scrollListToLine(listView, lastPosition);
                        solo.sleep(config.sleepDuration);
                        loopListView(activity, params);
                        break;
                    } else {
                        // Not enough pages (screen).
                        solo.scrollDown();
                        solo.sleep(config.sleepDuration);
                        lastPosition = listView.getLastVisiblePosition();
                        int newFirstPosition = listView.getFirstVisiblePosition();
                        visibleItem = lastPosition - newFirstPosition;
                        for (int j = firstPosition; j <= newFirstPosition; j ++) {
                            // Click from the bottom up.
                            isRestart = handleListViewItem(visibleItem, activity, params);
                            if (isRestart) break;
                            visibleItem --;
                        }
                    }
                }
            }
        }
    }

    /**
     * When the ListView jumps, retry click on ListView.
     */
    private void reClickOnListView(String activity, Map<String, Object> params, int index) throws Exception{
        ArrayList<ListView> listViews = solo.getCurrentViews(ListView.class);
        if (listViews.size() > 0){
            ListView listView = listViews.get(0);
            int size = listView.getCount() - 1;
            Log.d(Solo.LOG_TAG, "reClickOnListView ListView size:" + size);
            int lastPosition = listView.getLastVisiblePosition();
            for (int i = 0; i <= lastPosition; i++) {
                if (i > index){
                    handleJump(currentActivity, activity, params);
                    solo.clickInList(i);
                    solo.sleep(config.sleepDuration);
                    boolean isRestart = handleJump(currentActivity, activity, params);
                    if (isRestart) {
                        reClickOnListView(activity, params, i);
                    }
                    if (i == lastPosition) {
                        final int temp = i;
                        Log.d(LOG_TAG, "ListView setSelection to " + temp);
                        listView.post(new Runnable() {
                            @Override
                            public void run() {
                                listView.setSelection(temp);
                            }
                        });
                        solo.sleep(config.sleepDuration);
                        lastPosition = listView.getLastVisiblePosition();
                    }
                }
            }
        }
    }

    /**
     * Handle ListView Item
     */
    private boolean handleListViewItem(int i, String activity, Map<String, Object> params) throws Exception{
        solo.clickInList(i);
        solo.sleep(config.sleepDuration);
        boolean isRestart = handleJump(currentActivity, activity, params);
        if (isRestart) {
            reClickOnListView(activity, params, i);
            return true;
        }
        return false;
    }

    /**
     * Handle RecyclerView
     * if view instanceof RecyclerView, simulate pull-down refresh and pull-up loading and
     * get RecyclerView item count, iteration it.
     */
    private void handleRecyclerView(String activity, Map<String, Object> params) throws Exception{
        ArrayList<RecyclerView> recyclerViews = solo.getCurrentViews(RecyclerView.class);
        if (recyclerViews.size() > 0) {
            RecyclerView recyclerView = recyclerViews.get(0);
            if (recyclerView == null || recyclerView.getAdapter() == null) {
                Log.d(Solo.LOG_TAG, "recyclerView or recyclerView.getAdapter() is null, return.");
                return;
            }
            Log.d(Solo.LOG_TAG, "Find recyclerView: " + recyclerView.toString());
            int size = recyclerView.getAdapter().getItemCount();
            int lastPosition;
            if(size > 0){
                // Pull-down refresh
                solo.pullDown(config.sleepDuration * 4);
            }
            RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
            if (layoutManager == null) {
                return;
            }
            if (layoutManager instanceof LinearLayoutManager) {
                lastPosition = ((LinearLayoutManager) layoutManager).findLastVisibleItemPosition();
                if (size > lastPosition) {
                    // Switch to the last item for recyclerView
                    solo.scrollRecyclerViewToLine(recyclerView, size - 1);
                    // Pull-up loading
                    solo.pullUp(config.sleepDuration * 4);
                }
                //RecyclerView recovery
                solo.scrollRecyclerViewToLine(recyclerView, 0);
                solo.sleep(config.sleepDuration);

                loopRecyclerView(activity, params);
            }
        }
    }

    private void loopRecyclerView(String activity, Map<String, Object> params) throws Exception{
        ArrayList<RecyclerView> recyclerViews = solo.getCurrentViews(RecyclerView.class);
        if (recyclerViews.size() > 0) {
            RecyclerView recyclerView = recyclerViews.get(0);
            if (recyclerView == null || recyclerView.getAdapter() == null) {
                Log.d(Solo.LOG_TAG, "recyclerView or recyclerView.getAdapter() is null, return.");
                return;
            }
            int size = recyclerView.getAdapter().getItemCount();
            Log.d(Solo.LOG_TAG, "RecyclerView size: " + size);
            int firstPosition;
            int lastPosition;
            int visibleItem;
            RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
            if (layoutManager == null) {
                return;
            }
            if (layoutManager instanceof LinearLayoutManager) {
                LinearLayoutManager manager = (LinearLayoutManager) layoutManager;
                firstPosition = manager.findFirstVisibleItemPosition();
                // May be Covered.
                lastPosition = manager.findLastVisibleItemPosition() - 1;
                visibleItem = lastPosition - firstPosition;
                Log.d(LOG_TAG, "RecyclerView firstPosition: " + firstPosition);
                Log.d(LOG_TAG, "RecyclerView lastPosition: " + lastPosition);
                for (int i = 1; i <= visibleItem; i++) {
                    boolean isRestart = handleRecyclerViewItem(recyclerView, i, activity, params);
                    if (isRestart) break;
                    if (i == visibleItem && size > lastPosition) {
                        // More than one page (screen).
                        if (size - (lastPosition + 1) > visibleItem) {
                            scrollLoopRecyclerView(manager);
                            loopRecyclerView(activity, params);
                            break;
                        } else {
                            // Not enough pages (screen).
                            scrollLoopRecyclerView(manager);
                            lastPosition = manager.findLastVisibleItemPosition();
                            int newFirstPosition = manager.findFirstVisibleItemPosition();
                            visibleItem = lastPosition - newFirstPosition;
                            for (int j = firstPosition; j <= newFirstPosition; j ++) {
                                // Click from the bottom up.
                                isRestart = handleRecyclerViewItem(recyclerView, visibleItem, activity, params);
                                if (isRestart) break;
                                visibleItem --;
                            }
                        }

                    }
                }
            }
        }
    }

    /**
     * Scrolls horizontally or vertically for RecyclerView.
     * @param manager the RecyclerView LinearLayoutManager
     */
    private void scrollLoopRecyclerView(LinearLayoutManager manager) {
        if (manager.getOrientation() == LinearLayoutManager.HORIZONTAL) {
            Log.d(LOG_TAG, "RecyclerView scrollRight.");
            solo.scrollToSide(Solo.RIGHT, 0.95f, 20);
        } else {
            Log.d(LOG_TAG, "RecyclerView scrollDown.");
            solo.scrollDown();
        }
        solo.sleep(config.sleepDuration);
    }

    /**
     * Handle RecyclerView Item
     */
    private boolean handleRecyclerViewItem(RecyclerView recyclerView, int i, String activity, Map<String, Object> params)
            throws Exception{
        Log.d(Solo.LOG_TAG, "RecyclerView item: " + recyclerView.getChildAt(i));
        solo.clickInRecyclerView(recyclerView, i);
        solo.sleep(config.sleepDuration);
        boolean isReStart = handleJump(currentActivity, activity, params);
        if (isReStart) {
            reClickOnRecyclerView(activity, params, i);
            return true;
        }
        return false;
    }

    /**
     * When the recyclerView jumps, retry click on recyclerView.
     * @param index jump index
     */
    private void reClickOnRecyclerView(String activity, Map<String, Object> params, int index) throws Exception{
        ArrayList<RecyclerView> recyclerViews = solo.getCurrentViews(RecyclerView.class);
        if (recyclerViews.size() > 0){
            RecyclerView recyclerView = recyclerViews.get(0);
            if (recyclerView == null || recyclerView.getAdapter() == null) {
                Log.d(Solo.LOG_TAG, "recyclerView or recyclerView.getAdapter() is null, return.");
                return;
            }
            for (int j = 0; j < recyclerView.getChildCount(); j++) {
                if (j > index){
                    solo.clickInRecyclerView(recyclerView, j);
                    solo.sleep(config.sleepDuration);
                    boolean isReStart = handleJump(currentActivity, activity, params);
                    if (isReStart) {
                        reClickOnRecyclerView(activity, params, j);
                        break;
                    }
                }
            }
        }
    }

    /**
     * Handle Login.
     * By reading the configuration file to complete the operation.
     */
    void handleLogin(){
        String[] strings = config.loginActivity.split("\\.");
        solo.waitForActivity(strings[strings.length - 1]);
        solo.sleep(config.sleepDuration);
        solo.clearEditText(0);
        solo.sleep(100);
        solo.typeText(0, config.loginAccount);
        solo.sleep(100);
        solo.clearEditText(1);
        solo.sleep(100);
        solo.typeText(1, config.loginPassword);
        solo.clickOnView(solo.getView(config.loginId));
        strings = config.homeActivity.split("\\.");
        boolean result = solo.waitForActivity(strings[strings.length - 1]);
        if (!result) {
            solo.takeScreenshotForAuto(null);
            try {
                startActivity(context, null, config.loginActivity);
                handleLogin();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Handle Jump
     * If the current activity is not the target page, press the back button,
     * if not returned after the target activity is to restart the target activity
     */
    private boolean handleJump(Activity context, String activity, Map<String, Object> params) throws Exception{
        handleAcrossApplicationJump();

        ComponentName cn = getRunningTask(context);
        String pkg = cn.getPackageName();
        String act = cn.getClassName();
        Log.i(Solo.LOG_TAG, "current ComponentName: " + pkg + "/" + act);
        // If the iteration comes to the login activity, login.
        if (!activity.contains(config.loginActivity) && act.contains(config.loginActivity)){
            handleLogin();
            solo.sleep(config.sleepDuration * 4);
            return false;
        }
//        if (!act.contains(activity)) {
//            Log.i(Solo.LOG_TAG, "package act: " + act);
//            Log.i(Solo.LOG_TAG, "package target: " + config.PACKAGE);
//            solo.goBack();
//            solo.sleep(config.sleepDuration);
//            cn = getRunningTask(context);
//            act = cn.getClassName();
//            if (!act.contains(activity)) {
//                String[] names = activity.split("\\.");
//                goBackToActivitySync(names[names.length - 1]);
//                cn = getRunningTask(context);
//                act = cn.getClassName();
//                if (!act.contains(activity)) {
//                    Log.i(Solo.LOG_TAG, "ReStart Activity from getRunningTask: " + activity);
//                    boolean isAbandon = startActivity(context, params, activity);
//                    if (isAbandon) startTargetApplication();
//                    solo.sleep(config.sleepDuration * 4);
//                    return true;
//                }
//            }
//            return false;
//        }
        boolean isShown = isShown(context);
        Log.i(Solo.LOG_TAG, "current Activity " + context + " isShown: " + isShown);
        if (!isShown) {
            solo.goBack();
            solo.sleep(config.sleepDuration / 5);
            isShown = isShown(context);
            Log.i(Solo.LOG_TAG, "current Activity " + context + " isShown: " + isShown);
            if (!isShown) {
                String[] names = activity.split("\\.");
                goBackToActivitySync(names[names.length - 1]);
                isShown = isShown(context);
                if (!isShown) {
                    Log.i(Solo.LOG_TAG, "ReStart Activity from isShown: " + activity);
                    boolean isAbandon = startActivity(context, params, activity);
                    if (isAbandon) startTargetApplication();
                    solo.sleep(config.sleepDuration * 4);
                    return true;
                }
            }
        }
        return false;
    }

    private void handleAcrossApplicationJump() {
        boolean adbConnect = true;
        AccessibilityNodeInfo node = null;
        try {
            node = uiAutomation.getRootInActiveWindow();
        } catch (Exception ignored) {
            adbConnect = false;
        }
        if (adbConnect && node != null) {
            String pkg = node.getPackageName().toString();
            if (!pkg.contains(config.PACKAGE)) {
                solo.acrossForShellCommand("input keyevent " + KeyEvent.KEYCODE_BACK);
                solo.sleep(config.sleepDuration * 2);
            }
        }
    }

    private void startTargetApplication() {
        Log.i(LOG_TAG, "startTargetApplication()");
        android.content.Intent intent = context.getPackageManager().getLaunchIntentForPackage(config.PACKAGE);
        if (intent != null) context.startActivity(intent);
    }

    void iterationNode(AccessibilityNodeInfo nodeInfo, String activity, Map<String, Object> params) {
        try{
            if (nodeInfo == null) nodeInfo = uiAutomation.getRootInActiveWindow();
            if (nodeInfo != null) {
                for (int i = 0; i < nodeInfo.getChildCount(); i ++){
                    AccessibilityNodeInfo node = nodeInfo.getChild(i);
                    if (node == null) node = uiAutomation.getRootInActiveWindow();
                    performClick(node, activity, params);
                }
            }
        }catch (NullPointerException | StackOverflowError e){
            e.printStackTrace();
        }
    }

    private void performClick(AccessibilityNodeInfo node, String activity, Map<String, Object> params) {
        if (node != null && node.isClickable()) {
            String string = getBoundsInScreen(node);
            boolean clicked = mClickedSingleton.containsForNode(string);
            boolean status = viewInIgnoreViews(node.getViewIdResourceName());
            if (!clicked && !status) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && node.canOpenPopup()) iterationNode(null, activity, params);
                if (node.getClassName().toString().contains("android.widget.EditText")) {
                    String[] id = node.getViewIdResourceName().split("/");
                    handleEditText(solo.getView(id[id.length - 1]));
                    solo.sleep(config.sleepMiniDuration);
                }
                if (config.iterationScreenShots) {
                    screenshotTaker.takeScreenshotForUiAutomation(string, activity);
                }
                Log.w(LOG_TAG, node.toString());
                node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                mClickedSingleton.addForNode(string);
                solo.sleep(config.sleepDuration);
                try {
                    handleJump(currentActivity, activity, params);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (node.getChildCount() > 0) iterationNode(node, activity, params);
        } else iterationNode(node, activity, params);
    }

    private String getBoundsInScreen(AccessibilityNodeInfo node) {
        if (node != null) {
            Pattern pattern = Pattern.compile("boundsInScreen:.+?;");
            Matcher matcher = pattern.matcher(node.toString());
            if (matcher.find()) return matcher.group(0);
        }
        return "";
    }

    private boolean viewInIgnoreViews(String string){
        if (string == null) return false;
        for (String s: config.ignoreViews) {
            if (s.contains(string)) return true;
        }
        return false;
    }

    /**
     * Returns the visibility of this view and all of its ancestors
     *
     */
    private boolean isShown(Activity context){
        if (context == null) return false;
        Window window = context.getWindow();
        if (window != null) {
            View decorView = window.getDecorView();
            if (decorView != null) {
                return decorView.isShown();
            }
        }
        return false;
    }

    /**
     * Return a list of the tasks that are currently running.
     * @param context context
     * @return ComponentName Return only the top one.
     */
    private ComponentName getRunningTask(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        return am.getRunningTasks(1).get(0).topActivity;
    }

    private void goBackToActivitySync(String name) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                solo.goBackToActivity(name);
            }
        });
        thread.start();
        solo.sleep(config.sleepDuration);
        thread.interrupt();
    }

    /**
     * Finish the activity.
     * @param activity activity
     */
    void finish(String activity) {
        if (config.homeActivity.contains(activity))return;
        ArrayList<Activity> activitiesOpened = activityUtils.getAllOpenedActivities();
        for (Activity activity2 : activitiesOpened) {
            if (activity.contains(activity2.getClass().getSimpleName())) {
                if(config.commandLogging){
                    Log.d(config.commandLoggingTag, "finish("+activity2.getClass().getSimpleName()+")");
                }
                //Main thread finish activity.
                android.os.Handler handler = new android.os.Handler(Looper.getMainLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        activity2.finish();
                    }
                });
            }
        }
    }

    private Activity getActivity(String activity) {
        ArrayList<Activity> activitiesOpened = activityUtils.getAllOpenedActivities();
        for (Activity activity2 : activitiesOpened) {
            if (activity.contains(activity2.getClass().getSimpleName())) {
                return activity2;
            }
        }
        return null;
    }

    /**
     * Reads the json file, generates the parameters required for the iteration, and ignores part of the activity.
     */
    void handleParams() throws Exception {
        String json = FileUtils.readJson();
        Log.i(LOG_TAG, "json: " + json);
        ArrayList<ParamsEntity> arrayList = filterActivities(JsonParser.fromJson(json), activityUtils.getAllActivities(context));
        Log.d(LOG_TAG, "Params size: " + arrayList.size());
        for (ParamsEntity params : arrayList) {
            boolean isIgnore = false;
            Log.d(LOG_TAG, "Activity: " + params.getName());
            if(config.mode == Solo.Config.Mode.REPTILE && params.getName().contains(config.homeActivity))continue;
            for (String name: config.ignoreActivities) {
                if (params.getName().contains(name)) {
                    isIgnore = true;
                    break;
                }
            }
            Log.d(LOG_TAG, "Ignore: " + isIgnore);
            if (!isIgnore) {
                Map<String, Object> hashMap = new HashMap<>();
                for (ParamEntity param : params.getParams()) {
                    Log.d(LOG_TAG, param.getKey() + " " + param.getValue());
                    hashMap.put(param.getKey().split("\\|")[1], IntentParser.parseType(param.getValue()));
                }
                iteration(params.getName(), hashMap.isEmpty() ? null : hashMap,
                        config.mode != Solo.Config.Mode.FAST && params.isIteration(), params.isWeb());
            }
        }
    }

    /**
     * Iteration method.
     * @param activity the start activity
     * @param params start activity params
     * @param iteration if true, click everyone view.
     * @param isWeb if true, the activity is webView.
     */
    void iteration(String activity, Map<String, Object> params, boolean iteration, boolean isWeb) throws Exception {
        boolean isAbandon = false;
        // If the current activity is not target, start the target.
        Activity current = solo.getCurrentActivity();
        if (!current.toString().contains(activity)) {
            isAbandon = startActivity(context, params, activity);
        } else currentActivity = current;

        // Abandon start the target activity, failure to start the target activity may be due to protocol jumps.
        if (isAbandon) return;
        solo.sleep(config.sleepDuration * 4);
        Log.i(LOG_TAG, "current activity: " + currentActivity);
        FileUtils.writeActivity(TimeUtils.getDate() + " starting iteration: " + activity);
        if (config.activityScreenShots) {
            String[] s = activity.split("\\.");
            solo.takeScreenshotForAuto("Activities/" + s[s.length - 1]);
        }
        if (iteration) {
            if (isWeb) handleWeb(activity, params);
            if (config.useNative) handleNative(activity, params);
            else {
                if (Build.VERSION.SDK_INT >= 18) {
                    if (activity.contains(config.homeActivity)) handleFragmentTabHost(activity, params, true);
                    else handleViewPager(activity, params, true);
                }else handleNative(activity, params);
            }
        }
        finish(activity);
        mClickedSingleton.clearForNative();
        mClickedSingleton.clearForNode();
        BundleSingleton.getInstance().clear();
        FileUtils.writeActivity(TimeUtils.getDate() + " stopped iteration: " + activity);
        if (!config.keepActivitiesScreenShots) FileUtils.deleteScreenShots(activity);
    }

    /**
     * HandleWeb, click everyone WebElement.
     * @param activity current activity
     * @param params start activity params
     */

    private void handleWeb(String activity, Map<String, Object> params) throws ClassNotFoundException{
        String url = solo.waitForWebUrl(config.sleepDuration * 20);
        Log.i(LOG_TAG, url);
        if (!TextUtils.isEmpty(url)) {
            ArrayList<WebElement> webElements = solo.getCurrentWebElements();
            for (WebElement element: webElements) {
                if (element.getTagName().toUpperCase().contains("INPUT")) {
                    solo.typeTextInWebElement(By.tagName(element.getTagName()), RandomUtils.getRandomText(10));
                    solo.sleep(config.sleepDuration * 2);
                } else {
                    solo.takeScreenshotForAuto(activity, element);
                    solo.clickOnWebElement(element);
                    solo.sleep(config.sleepDuration * 2);
                }
                String tmp = solo.waitForWebUrl(config.sleepDuration * 20);
                if (!tmp.equals(url)) {
                    solo.goBack();
                    if (!solo.getCurrentActivity().toString().contains(activity)) {
                        startActivity(context, params, activity);
                        solo.sleep(config.sleepDuration);
                        if (TextUtils.isEmpty(solo.waitForWebUrl(config.sleepDuration * 10))) break;
                    }
                }
            }
        } else
            Log.w(LOG_TAG, activity + " WebUrl is null, " + "timeout.");
    }

    /**
     * If the activity not in target application, remove it.
     */
    private ArrayList<ParamsEntity> filterActivities(ArrayList<ParamsEntity> arrayList, ActivityInfo[] activities) {
        ArrayList<ParamsEntity> filterActivities = new ArrayList<>();
        for (ParamsEntity params : arrayList) {
            for (ActivityInfo activity: activities){
                if (activity.name.contains(params.getName())) filterActivities.add(params);
            }
        }
        return filterActivities;
    }


    /**
     * Start target activity
     * Returns if retry >= 3, abandon start the target activity.
     */
    boolean startActivity(Context context, Map<String, Object> params, String activity) throws ClassNotFoundException {
        Class<?> target;
        target = Class.forName(activity);
        startActivityForBundle(context, target, params);
        solo.sleep(config.sleepDuration);
        solo.hideSoftKeyboard();
        Log.i(LOG_TAG, "start activity: " + activity);
        // In some cases, the target activity will be overwritten and will need to be retried.
        int retry = 0;
        while (retry < 3){
            boolean isShown = isShown(getActivity(activity));
            if (!isShown){
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    try {
                        solo.acrossForShellCommand("input keyevent " + KeyEvent.KEYCODE_BACK);
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }
                    solo.sleep(config.sleepDuration);
                }
                solo.sleep(config.sleepDuration);
                startActivityForBundle(context, target, params);
                solo.hideSoftKeyboard();
                solo.sleep(config.sleepDuration);
                Log.i(LOG_TAG, "restart activity: " + activity);
                retry++;
            }else break;
        }

        currentActivity = solo.getCurrentActivity();
        if (currentActivity == null) {
            throw new NullPointerException("currentActivity is null.");
        }

        // Abandon start the target activity, failure to start the target activity may be due to protocol jumps.
        if (retry >= 3) Log.e(LOG_TAG, "Abandon start " + activity + " , failure to start the target activity may be due to protocol jumps.");
        return retry >= 3;
    }

    private void startActivityForBundle(Context context, Class<?> target, Map<String, Object> params) {
        finish(target.getName());
        // finish is asynchronous.
        solo.sleep(500);
        BundleSingleton instance = BundleSingleton.getInstance();
        if (instance.containsKey(target.getName())) {
            Log.i(LOG_TAG, "start Activity for Bundle: " + target);
            context.startActivity(IntentParser.getIntent(context, target, params),
                    instance.getBundle(target.getName()));
        } else context.startActivity(IntentParser.getIntent(context, target, params));
    }

    /**
     * HandleEditText, e.g: type text or type number.
     * @param view view
     */
    private void handleEditText(View view){
        if (view instanceof EditText){
            EditText editText = (EditText)view;
            solo.typeText(editText);
        }
    }

}
