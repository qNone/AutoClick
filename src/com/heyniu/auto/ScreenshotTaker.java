package com.heyniu.auto;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;

import com.robotium.solo.Sleeper;
import com.robotium.solo.ViewFetcher;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class ScreenshotTaker extends com.robotium.solo.ScreenshotTaker{

    private HandlerThread screenShotSaverThread = null;
    private ScreenShotSaver screenShotSaver = null;
    private String pkg;

    /**
     * Constructs this object.
     *
     * @param config          the {@code Config} instance
     * @param instrumentation the {@code Instrumentation} instance.
     * @param activityUtils   the {@code ActivityUtils} instance
     * @param viewFetcher     the {@code ViewFetcher} instance
     * @param sleeper         the {@code Sleeper} instance
     */
    ScreenshotTaker(Solo.Config config, Instrumentation instrumentation, ActivityUtils activityUtils, ViewFetcher viewFetcher, Sleeper sleeper) {
        super(config, instrumentation, activityUtils, viewFetcher, sleeper);

        Context mContext = instrumentation.getTargetContext().getApplicationContext();
        SharedPreferencesHelper helper = new SharedPreferencesHelper(mContext, SharedPreferencesHelper.ARGUMENTS);
        pkg = helper.getString(SharedPreferencesHelper.PACKAGE);
    }

    void takeScreenshotForUiAutomation(String rectInfo, String activity){
        Bitmap b = watermarkForRect(getRect(rectInfo));
        if (b != null) {
            saveFile(activity + "/" +  TimeUtils.getDate(), b, 60);
            b.recycle();
        }
    }

    private Bitmap watermarkForRect(Rect rect) {
        Bitmap bitmap = instrumentation.getUiAutomation().takeScreenshot();
        if (bitmap != null) {
            int w = bitmap.getWidth();
            int h = bitmap.getHeight();
            Paint paint = new Paint();
            paint.setAlpha(150);
            paint.setAntiAlias(true);
            paint.setColor(Color.RED);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(DesignUtils.px2dip(instrumentation.getContext(), 25));
            Bitmap newb = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            Canvas cv = new Canvas(newb);
            cv.drawBitmap(bitmap, 0, 0, null);
            cv.drawRect(rect, paint);
            cv.save(Canvas.ALL_SAVE_FLAG);
            cv.restore();
            return newb;
        }
        return null;
    }

    private Rect getRect(String string) {
        ArrayList<Integer> arrayList = new ArrayList<>();
        Pattern p = Pattern.compile("\\d+");
        Matcher matcher = p.matcher(string);
        while (matcher.find()) {
            arrayList.add(Integer.parseInt(matcher.group()));
        }
        if (arrayList.size() == 4) return new Rect(arrayList.get(0), arrayList.get(1),arrayList.get(2),arrayList.get(3));
        return new Rect(0, 0, 0, 0);
    }

    /**
     * Saves a file.
     *
     * @param name the name of the file
     * @param b the bitmap to save
     * @param quality the compression rate. From 0 (compress for lowest size) to 100 (compress for maximum quality).
     *
     */
    private void saveFile(String name, Bitmap b, int quality){
        FileOutputStream fos;
        String fileName = getFileName(name);
        File directory;
        String path = String.format(Solo.Config.screenshotSavePath, pkg);
        if (fileName.contains("/")) {
            directory = new File(path, fileName.split("/")[0]);
            fileName = fileName.split("/")[1];
        } else {
            directory = new File(path);
        }

        directory.mkdirs();
        File fileToSave = new File(directory,fileName);
        try {
            fos = new FileOutputStream(fileToSave);
            if(config.screenshotFileType == Solo.Config.ScreenshotFileType.JPEG){
                if (!b.compress(Bitmap.CompressFormat.JPEG, quality, fos)){
                    Log.d(Solo.LOG_TAG, "Compress/Write failed");
                }
            }
            else{
                if (!b.compress(Bitmap.CompressFormat.PNG, quality, fos)){
                    Log.d(Solo.LOG_TAG, "Compress/Write failed");
                }
            }
            fos.flush();
            fos.close();
        } catch (Exception e) {
            Log.d(Solo.LOG_TAG, "Can't save the screenshot! Requires write permission (android.permission.WRITE_EXTERNAL_STORAGE) in AndroidManifest.xml of the application under test.");
            e.printStackTrace();
        }
    }

    /**
     *
     * Add a watermark to the image
     * @param bitmap image
     * @param watermark_x the watermark x coordinate
     * @param watermark_y the watermark y coordinate
     * @return watermark bitmap
     */
    private Bitmap watermark(Bitmap bitmap, float watermark_x, float watermark_y) {
        if (bitmap == null) {
            return null;
        }
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        if ((int)watermark_x > w) {
            watermark_x = w;
        }
        if ((int)watermark_y > h) {
            watermark_y = h;
        }
        Paint paint = new Paint();
        paint.setAlpha(150);
        paint.setAntiAlias(true);
        paint.setColor(Color.RED);
        Bitmap newb = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas cv = new Canvas(newb);
        cv.drawBitmap(bitmap, 0, 0, null);
        cv.drawCircle(watermark_x, watermark_y, DesignUtils.px2dip(instrumentation.getContext(), 120), paint);
        paint.setAlpha(100);
        cv.drawCircle(watermark_x, watermark_y, DesignUtils.px2dip(instrumentation.getContext(), 200), paint);
        cv.save(Canvas.ALL_SAVE_FLAG);
        cv.restore();
        return newb;
    }

    /**
     * This method initializes the aysnc screenshot saving logic
     */
    private void initScreenShotSaver() {
        if(screenShotSaverThread == null || screenShotSaver == null) {
            screenShotSaverThread = new HandlerThread("ScreenShotSaver");
            screenShotSaverThread.start();
            screenShotSaver = new ScreenShotSaver(screenShotSaverThread);
        }
    }

    /**
     * Takes a screenshot and saves it in the {@link Solo.Config} objects save path.
     * Requires write permission (android.permission.WRITE_EXTERNAL_STORAGE) in AndroidManifest.xml of the application under test.
     *
     * @param name the name to give the screenshot image
     * @param quality the compression rate. From 0 (compress for lowest size) to 100 (compress for maximum quality).
     * @param watermark_x x
     * @param watermark_y y
     */
    void takeScreenshot(final String name, final int quality, float watermark_x, float watermark_y) {
        View decorView = getScreenshotView();
        if(decorView == null)
            return;

        initScreenShotSaver();
        ScreenshotRunnable runnable = new ScreenshotRunnable(decorView, name, quality, watermark_x, watermark_y);

        synchronized (screenshotMutex) {
            Activity activity = activityUtils.getCurrentActivity(false);
            if(activity != null)
                activity.runOnUiThread(runnable);
            else
                instrumentation.runOnMainSync(runnable);

            try {
                screenshotMutex.wait(TIMEOUT_SCREENSHOT_MUTEX);
            } catch (InterruptedException ignored) {
            }
        }
    }

    private class ScreenshotRunnable extends com.robotium.solo.ScreenshotTaker.ScreenshotRunnable {

        private float watermark_x;
        private float watermark_y;

        ScreenshotRunnable(final View _view, final String _name, final int _quality, final float _watermark_x,
                                  final float _watermark_y) {
            super(_view, _name, _quality);

            this.watermark_x = _watermark_x;
            this.watermark_y = _watermark_y;
        }

        public void run() {
            if(view !=null){
                Bitmap  b;

                if(view instanceof WebView){
                    b = getBitmapOfWebView((WebView) view);
                }
                else{
                    b = getBitmapOfView(view);
                }
                if(b != null) {
                    screenShotSaver.saveBitmap(b, name, quality, watermark_x, watermark_y);
                    // Return here so that the screenshotMutex is not unlocked,
                    // since this is handled by save bitmap
                    return;
                }
                else
                    Log.d(Solo.LOG_TAG, "NULL BITMAP!!");
            }

            // Make sure the screenshotMutex is unlocked
            synchronized (screenshotMutex) {
                screenshotMutex.notify();
            }
        }
    }

    private class ScreenShotSaver extends com.robotium.solo.ScreenshotTaker.ScreenShotSaver {

        ScreenShotSaver(HandlerThread thread) {
            super(thread);
        }

        /**
         * This method posts a Bitmap with meta-data to the Handler queue.
         *
         * @param bitmap the bitmap to save
         * @param name the name of the file
         * @param quality the compression rate. From 0 (compress for lowest size) to 100 (compress for maximum quality).
         * @param watermark_x the watermark x coordinate
         * @param watermark_y the watermark y coordinate
         */
        void saveBitmap(Bitmap bitmap, String name, int quality, float watermark_x, float watermark_y) {
            Message message = this.obtainMessage();
            message.arg1 = quality;
            message.obj = bitmap;
            message.getData().putString("name", name);
            message.getData().putFloat("watermark_x", watermark_x);
            message.getData().putFloat("watermark_y", watermark_y);
            this.sendMessage(message);
        }

        /**
         * Here we process the Handler queue and save the bitmaps.
         *
         * @param message A Message containing the bitmap to save, and some metadata.
         */
        public void handleMessage(Message message) {
            synchronized (screenshotMutex) {
                String name = message.getData().getString("name");
                float watermark_x = message.getData().getFloat("watermark_x");
                float watermark_y = message.getData().getFloat("watermark_y");
                int quality = message.arg1;
                Bitmap b = (Bitmap)message.obj;
                if(b != null) {
                    if (watermark_x > 0f && watermark_y > 0f) {
                        Bitmap watermark_b = watermark(b, watermark_x, watermark_y);
                        if (watermark_b != null) {
                            saveFile(name, watermark_b, quality);
                            watermark_b.recycle();
                        }
                    } else {
                        saveFile(name, b, quality);
                    }
                    b.recycle();
                }
                else {
                    Log.d(Solo.LOG_TAG, "NULL BITMAP!!");
                }

                screenshotMutex.notify();
            }
        }
    }
}
