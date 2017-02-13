package com.robotium.solo;

import android.app.Activity;
import android.app.Instrumentation;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Picture;
import android.graphics.Rect;
import android.opengl.GLSurfaceView;
import android.opengl.GLSurfaceView.Renderer;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.SystemClock;
import android.view.View;
import android.webkit.WebView;

import com.robotium.solo.Solo.Config;
import com.robotium.solo.Solo.Config.ScreenshotFileType;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Contains screenshot methods like: takeScreenshot(final View, final String name), startScreenshotSequence(final String name, final int quality, final int frameDelay, final int maxFrames), 
 * stopScreenshotSequence().
 * 
 * 
 * @author Renas Reda, renas.reda@robotium.com
 * 
 */

class ScreenshotTaker {

	private static final long TIMEOUT_SCREENSHOT_MUTEX = TimeUnit.SECONDS.toMillis(2);
	private final Object screenshotMutex = new Object();
	private final Config config;
	private static Instrumentation instrumentation;
	private final ActivityUtils activityUtils;
	private final String LOG_TAG = "Robotium";
	private ScreenshotSequenceThread screenshotSequenceThread = null;
	private HandlerThread screenShotSaverThread = null;
	private ScreenShotSaver screenShotSaver = null;
	private final ViewFetcher viewFetcher;
	private final Sleeper sleeper;


	/**
	 * Constructs this object.
	 * 
	 * @param config the {@code Config} instance
	 * @param instrumentation the {@code Instrumentation} instance.
	 * @param activityUtils the {@code ActivityUtils} instance
	 * @param viewFetcher the {@code ViewFetcher} instance
	 * @param sleeper the {@code Sleeper} instance
	 * 
	 */
	ScreenshotTaker(Config config, Instrumentation instrumentation, ActivityUtils activityUtils, ViewFetcher viewFetcher, Sleeper sleeper) {
		this.config = config;
		ScreenshotTaker.instrumentation = instrumentation;
		this.activityUtils = activityUtils;
		this.viewFetcher = viewFetcher;
		this.sleeper = sleeper;
	}

	/**
	 * Takes a screenshot and saves it in the {@link Config} objects save path.  
	 * Requires write permission (android.permission.WRITE_EXTERNAL_STORAGE) in AndroidManifest.xml of the application under test.
	 * 
	 * @param name the name to give the screenshot image
	 * @param quality the compression rate. From 0 (compress for lowest size) to 100 (compress for maximum quality).
	 */
	public void takeScreenshot(final String name, final int quality) {
		View decorView = getScreenshotView();
		if(decorView == null) 
			return;

		initScreenShotSaver();
		ScreenshotRunnable runnable = new ScreenshotRunnable(decorView, name, quality, -1, -1);

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

	/**
	 * Takes a screenshot and saves it in the {@link Config} objects save path.
	 * Requires write permission (android.permission.WRITE_EXTERNAL_STORAGE) in AndroidManifest.xml of the application under test.
	 *
	 * @param name the name to give the screenshot image
	 * @param quality the compression rate. From 0 (compress for lowest size) to 100 (compress for maximum quality).
	 * @param watermark_x x
	 * @param watermark_y y
	 */
	public void takeScreenshot(final String name, final int quality, float watermark_x, float watermark_y) {
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

	/**
	 * Takes a screenshot sequence and saves the images with the name prefix in the {@link Config} objects save path.  
	 *
	 * The name prefix is appended with "_" + sequence_number for each image in the sequence,
	 * where numbering starts at 0.  
	 *
	 * Requires write permission (android.permission.WRITE_EXTERNAL_STORAGE) in the 
	 * AndroidManifest.xml of the application under test.
	 *
	 * Taking a screenshot will take on the order of 40-100 milliseconds of time on the 
	 * main UI thread.  Therefore it is possible to mess up the timing of tests if
	 * the frameDelay value is set too small.
	 *
	 * At present multiple simultaneous screenshot sequences are not supported.  
	 * This method will throw an exception if stopScreenshotSequence() has not been
	 * called to finish any prior sequences.
	 *
	 * @param name the name prefix to give the screenshot
	 * @param quality the compression rate. From 0 (compress for lowest size) to 100 (compress for maximum quality)
	 * @param frameDelay the time in milliseconds to wait between each frame
	 * @param maxFrames the maximum number of frames that will comprise this sequence
	 *
	 */
	public void startScreenshotSequence(final String name, final int quality, final int frameDelay, final int maxFrames) {
		initScreenShotSaver();

		if(screenshotSequenceThread != null) {
			throw new RuntimeException("only one screenshot sequence is supported at a time");
		}

		screenshotSequenceThread = new ScreenshotSequenceThread(name, quality, frameDelay, maxFrames);

		screenshotSequenceThread.start();
	}

	/**
	 * Causes a screenshot sequence to end.
	 * 
	 * If this method is not called to end a sequence and a prior sequence is still in 
	 * progress, startScreenshotSequence() will throw an exception.
	 */
	public void stopScreenshotSequence() {
		if(screenshotSequenceThread != null) {
			screenshotSequenceThread.interrupt();
			screenshotSequenceThread = null;
		}
	}

	/**
	 * Gets the proper view to use for a screenshot.  
	 */
	private View getScreenshotView() {
		View decorView = viewFetcher.getRecentDecorView(viewFetcher.getWindowDecorViews());
		final long endTime = SystemClock.uptimeMillis() + Timeout.getSmallTimeout();

		while (decorView == null) {	

			final boolean timedOut = SystemClock.uptimeMillis() > endTime;

			if (timedOut){
				return null;
			}
			sleeper.sleepMini();
			decorView = viewFetcher.getRecentDecorView(viewFetcher.getWindowDecorViews());
		}
		wrapAllGLViews(decorView);

		return decorView;
	}

	/**
	 * Extract and wrap the all OpenGL ES Renderer.
	 */
	private void wrapAllGLViews(View decorView) {
		ArrayList<GLSurfaceView> currentViews = viewFetcher.getCurrentViews(GLSurfaceView.class, true, decorView);
		final CountDownLatch latch = new CountDownLatch(currentViews.size());

		for (GLSurfaceView glView : currentViews) {
			Object renderContainer = new Reflect(glView).field("mGLThread")
					.type(GLSurfaceView.class).out(Object.class);

			Renderer renderer = new Reflect(renderContainer).field("mRenderer").out(Renderer.class);

			if (renderer == null) {
				renderer = new Reflect(glView).field("mRenderer").out(Renderer.class);
				renderContainer = glView;
			}  
			if (renderer == null) {
				latch.countDown();
				continue;
			}
			if (renderer instanceof GLRenderWrapper) {
				GLRenderWrapper wrapper = (GLRenderWrapper) renderer;
				wrapper.setTakeScreenshot();
				wrapper.setLatch(latch);
			} else {
				GLRenderWrapper wrapper = new GLRenderWrapper(glView, renderer, latch);
				new Reflect(renderContainer).field("mRenderer").in(wrapper);
			}
		}

		try {
			latch.await();
		} catch (InterruptedException ex) {
			ex.printStackTrace();
		}
	}


	/**
	 * Returns a bitmap of a given WebView.
	 *  
	 * @param webView the webView to save a bitmap from
	 * @return a bitmap of the given web view
	 * 
	 */

	private Bitmap getBitmapOfWebView(final WebView webView){
		Picture picture = webView.capturePicture();
		Bitmap b = Bitmap.createBitmap( picture.getWidth(), picture.getHeight(), Bitmap.Config.ARGB_8888);
		Canvas c = new Canvas(b);
		picture.draw(c);
		return b;
	}

	/**
	 * Returns a bitmap of a given View.
	 * 
	 * @param view the view to save a bitmap from
	 * @return a bitmap of the given view
	 * 
	 */

	private Bitmap getBitmapOfView(final View view){
		view.destroyDrawingCache();
		view.buildDrawingCache(false);
		Bitmap orig = view.getDrawingCache();
		Bitmap.Config config = null;

		if(orig == null) {
			return null;
		}

		config = orig.getConfig();

		if(config == null) {
			config = Bitmap.Config.ARGB_8888;
		}
		Bitmap b = orig.copy(config, false);
		orig.recycle();
		view.destroyDrawingCache();
		return b; 
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

	public void takeScreenshotForUiAutomation(String rectInfo, String activity){
		Bitmap b = watermarkForRect(getRect(rectInfo));
		if (b != null) {
			saveFile(activity + "/" +  TimeUtils.getDate(), b, 60);
			b.recycle();
		}
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
		FileOutputStream fos = null;
		String fileName = getFileName(name);
		File directory = null;
		if (fileName.contains("/")) {
			directory = new File(config.screenshotSavePath, fileName.split("/")[0]);
			fileName = fileName.split("/")[1];
		} else {
			directory = new File(config.screenshotSavePath);
		}

		directory.mkdirs();
		File fileToSave = new File(directory,fileName);
		try {
			fos = new FileOutputStream(fileToSave);
			if(config.screenshotFileType == ScreenshotFileType.JPEG){
				if (!b.compress(Bitmap.CompressFormat.JPEG, quality, fos)){
					Log.d(LOG_TAG, "Compress/Write failed");
				}
			}
			else{
				if (!b.compress(Bitmap.CompressFormat.PNG, quality, fos)){
					Log.d(LOG_TAG, "Compress/Write failed");
				}
			}
			fos.flush();
			fos.close();
		} catch (Exception e) {
			Log.d(LOG_TAG, "Can't save the screenshot! Requires write permission (android.permission.WRITE_EXTERNAL_STORAGE) in AndroidManifest.xml of the application under test.");
			e.printStackTrace();
		}
	}

	/**
	 *
	 * Add a watermark to the image
	 * @param bitmap image
	 * @param watermark_x the watermark x coordinate
	 * @param watermark_y the watermark y coordinate
	 * @return
	 */
	public static Bitmap watermark(Bitmap bitmap, float watermark_x, float watermark_y) {
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
	 * Returns a proper filename depending on if name is given or not.
	 * 
	 * @param name the given name
	 * @return a proper filename depedning on if a name is given or not
	 * 
	 */

	private String getFileName(final String name){
		SimpleDateFormat sdf = new SimpleDateFormat("ddMMyy-hhmmss");
		String fileName = null;
		if(name == null){
			if(config.screenshotFileType == ScreenshotFileType.JPEG){
				fileName = sdf.format(new Date()) + ".jpg";
			}
			else{
				fileName = sdf.format(new Date()) + ".png";
			}
		}
		else {
			if(config.screenshotFileType == ScreenshotFileType.JPEG){
				fileName = name + ".jpg";
			}
			else {
				fileName = name + ".png";	
			}
		}
		return fileName;
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
	 * This is the thread which causes a screenshot sequence to happen
	 * in parallel with testing.
	 */
	private class ScreenshotSequenceThread extends Thread {
		private int seqno = 0;

		private String name;
		private int quality;
		private int frameDelay;
		private int maxFrames;

		private boolean keepRunning = true;

		public ScreenshotSequenceThread(String _name, int _quality, int _frameDelay, int _maxFrames) {
			name = _name;
			quality = _quality; 
			frameDelay = _frameDelay;
			maxFrames = _maxFrames;
		}

		public void run() {
			while(seqno < maxFrames) {
				if(!keepRunning || Thread.interrupted()) break;
				doScreenshot();
				seqno++;
				try {
					Thread.sleep(frameDelay);
				} catch (InterruptedException e) {
				}
			}
			screenshotSequenceThread = null;
		}

		public void doScreenshot() {
			View v = getScreenshotView();
			if(v == null) keepRunning = false;
			String final_name = name+"_"+seqno;
			ScreenshotRunnable r = new ScreenshotRunnable(v, final_name, quality, -1, -1);
			Log.d(LOG_TAG, "taking screenshot "+final_name);
			Activity activity = activityUtils.getCurrentActivity(false);
			if(activity != null){
				activity.runOnUiThread(r);
			}
			else {
				instrumentation.runOnMainSync(r);
			}
		}

		public void interrupt() {
			keepRunning = false;
			super.interrupt();
		}
	}

	/**
	 * Here we have a Runnable which is responsible for taking the actual screenshot,
	 * and then posting the bitmap to a Handler which will save it.
	 *
	 * This Runnable is run on the UI thread.
	 */
	private class ScreenshotRunnable implements Runnable {

		private View view;
		private String name;
		private int quality;
		private float watermark_x;
		private float watermark_y;

		public ScreenshotRunnable(final View _view, final String _name, final int _quality, final float _watermark_x,
								  final float _watermark_y) {
			view = _view;
			name = _name;
			quality = _quality;
			watermark_x = _watermark_x;
			watermark_y = _watermark_y;
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
					b = null;
					// Return here so that the screenshotMutex is not unlocked,
					// since this is handled by save bitmap
					return;
				}
				else
					Log.d(LOG_TAG, "NULL BITMAP!!");
			}

			// Make sure the screenshotMutex is unlocked
			synchronized (screenshotMutex) {
				screenshotMutex.notify();
			}
		}
	}

	/**
	 * This class is a Handler which deals with saving the screenshots on a separate thread.
	 *
	 * The screenshot logic by necessity has to run on the ui thread.  However, in practice
	 * it seems that saving a screenshot (with quality 100) takes approx twice as long
	 * as taking it in the first place. 
	 *
	 * Saving the screenshots in a separate thread like this will thus make the screenshot
	 * process approx 3x faster as far as the main thread is concerned.
	 *
	 */
	private class ScreenShotSaver extends Handler {
		public ScreenShotSaver(HandlerThread thread) {
			super(thread.getLooper());
		}

		/**
		 * This method posts a Bitmap with meta-data to the Handler queue.
		 *
		 * @param bitmap the bitmap to save
		 * @param name the name of the file
		 * @param quality the compression rate. From 0 (compress for lowest size) to 100 (compress for maximum quality).
		 */
		public void saveBitmap(Bitmap bitmap, String name, int quality) {
			Message message = this.obtainMessage();
			message.arg1 = quality;
			message.obj = bitmap;
			message.getData().putString("name", name);
			this.sendMessage(message);
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
		public void saveBitmap(Bitmap bitmap, String name, int quality, float watermark_x, float watermark_y) {
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
					if (watermark_x >= 0 && watermark_y >= 0) {
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
					Log.d(LOG_TAG, "NULL BITMAP!!");
				}

				screenshotMutex.notify();
			}
		}

	}
}
