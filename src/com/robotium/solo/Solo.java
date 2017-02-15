package com.robotium.solo;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.app.Instrumentation;
import android.app.Instrumentation.ActivityMonitor;
import android.app.UiAutomation;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.PointF;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTabHost;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.inputmethod.EditorInfo;
import android.webkit.WebView;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CheckedTextView;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SlidingDrawer;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.ToggleButton;

import junit.framework.Assert;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Main class for development of Robotium tests.
 * Robotium has full support for Views, WebViews, Activities, Dialogs, Menus and Context Menus.
 * <br>
 * Robotium can be used in conjunction with Android test classes like
 * ActivityInstrumentationTestCase2 and SingleLaunchActivityTestCase.
 *
 *
 *
 *
 * @author Renas Reda, renas.reda@robotium.com
 */
public class Solo {

	protected final Asserter asserter;
	protected final ViewFetcher viewFetcher;
	protected final Checker checker;
	protected final Clicker clicker;
	protected final Presser presser;
	protected final Searcher searcher;
	protected final ActivityUtils activityUtils;
	protected final DialogUtils dialogUtils;
	protected final TextEnterer textEnterer;
	protected final Rotator rotator;
	protected final Scroller scroller;
	protected final Sleeper sleeper;
	protected final Swiper swiper;
	protected final Tapper tapper;
	protected final Illustrator illustrator;
	protected final Waiter waiter;
	protected final Setter setter;
	protected final Getter getter;
	protected final WebUtils webUtils;
	protected final Sender sender;
	protected final ScreenshotTaker screenshotTaker;
	protected final Instrumentation instrumentation;
	protected final Context context;
	protected Activity currentActivity;
	protected int sleepStandard = 1;
	protected final Zoomer zoomer;
	protected final SystemUtils systemUtils;
	protected String webUrl = null;
	private final Config config;
	private final Application.ActivityLifecycleCallbacks activityLifecycleCallbacks;
	private final Application application;
	private final AcrossApplication acrossApplication;
	private final UiAutomation uiAutomation;
	private ArrayList<String> clickeds = new ArrayList<>();
	public final static int LANDSCAPE = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;   // 0
	public final static int PORTRAIT = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;     // 1
	public final static int RIGHT = KeyEvent.KEYCODE_DPAD_RIGHT;
	public final static int LEFT = KeyEvent.KEYCODE_DPAD_LEFT;
	public final static int UP = KeyEvent.KEYCODE_DPAD_UP;
	public final static int DOWN = KeyEvent.KEYCODE_DPAD_DOWN;
	public final static int ENTER = KeyEvent.KEYCODE_ENTER;
	public final static int MENU = KeyEvent.KEYCODE_MENU;
	public final static int DELETE = KeyEvent.KEYCODE_DEL;
	public final static int HOME = KeyEvent.KEYCODE_HOME;
	public final static int CLOSED = 0;
	public final static int OPENED = 1;
	public final static String LOG_TAG = "Robotium";

    private int width;
	private int height;

	/**
	 * Constructor that takes the Instrumentation object and the start Activity.
	 *
	 * @param instrumentation the {@link Instrumentation} instance
	 * @param activity the start {@link Activity} or {@code null}
	 * if no Activity is specified
	 */

	public Solo(Instrumentation instrumentation, Activity activity) {
		this(new Config(), instrumentation, activity);
		
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "Solo("+instrumentation+", "+activity+")");
		}	
	}

	/**
	 * Constructor that takes the Instrumentation and Config objects.
	 *
	 * @param instrumentation the {@link Instrumentation} instance
	 * @param config the {@link Config} instance
	 */

	public Solo(Instrumentation instrumentation, Config config) {
		this(config, instrumentation, null);
		
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "Solo("+instrumentation+", "+config+")");
		}
	}

	/**
	 * Constructor that takes the Instrumentation, Config and Activity objects.
	 *
	 * @param instrumentation the {@link Instrumentation} instance
	 * @param config the {@link Config} instance
	 * @param activity the start {@link Activity} or {@code null}
	 * if no Activity is specified
	 */

	public Solo(Instrumentation instrumentation, Config config, Activity activity) {
		this(config, instrumentation, activity);

		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "Solo("+instrumentation+", "+config+", "+activity+")");
		}
	}

	/**
	 * Private constructor.
	 *
	 * @param config the {@link Config} instance. If {@code null} one will be created.
	 * @param instrumentation the {@link Instrumentation} instance
	 * @param activity the start {@link Activity} or {@code null}
	 * if no Activity is specified
	 */

	private Solo(Config config, Instrumentation instrumentation, Activity activity) {
		checkConfig(config);
		new Permission(activity, config.PACKAGE, instrumentation).requestPermissionsForShell();

		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "Solo("+config+", "+instrumentation+", "+activity+")");
		}
		
		this.config = (config == null) ? new Config(): config;
		this.instrumentation = instrumentation;
        this.context = activity;
		this.sleeper = new Sleeper(config.sleepDuration, config.sleepMiniDuration);
		this.sender = new Sender(instrumentation, sleeper);
		this.activityUtils = new ActivityUtils(config, instrumentation, activity, sleeper);
		this.viewFetcher = new ViewFetcher(instrumentation, sleeper);
		this.screenshotTaker = new ScreenshotTaker(config, instrumentation, activityUtils, viewFetcher, sleeper);
		this.dialogUtils = new DialogUtils(instrumentation, activityUtils, viewFetcher, sleeper);
		this.webUtils = new WebUtils(config, instrumentation,viewFetcher, sleeper);
		this.scroller = new Scroller(config, instrumentation, viewFetcher, sleeper);
		this.searcher = new Searcher(viewFetcher, webUtils, scroller, sleeper);
		this.waiter = new Waiter(instrumentation, activityUtils, viewFetcher, searcher,scroller, sleeper);
		this.getter = new Getter(instrumentation, activityUtils, waiter);
		this.clicker = new Clicker(config, activityUtils, viewFetcher,sender, instrumentation, sleeper, waiter, webUtils, dialogUtils, activity);
		this.setter = new Setter(activityUtils, getter, clicker, waiter);
		this.asserter = new Asserter(activityUtils, waiter);
		this.checker = new Checker(viewFetcher, waiter);
		this.zoomer = new Zoomer(instrumentation);
		this.swiper = new Swiper(instrumentation);
		this.tapper =  new Tapper(instrumentation);
		this.illustrator = new Illustrator(instrumentation);
		this.rotator = new Rotator(instrumentation);
		this.presser = new Presser(viewFetcher, clicker, instrumentation, sleeper, waiter, dialogUtils);
		this.textEnterer = new TextEnterer(instrumentation, clicker, dialogUtils);
		this.systemUtils = new SystemUtils(instrumentation);
		this.application = (Application) instrumentation.getTargetContext().getApplicationContext();
		this.uiAutomation = instrumentation.getUiAutomation();
		this.acrossApplication = new AcrossApplication(activity, instrumentation, config);
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

			}

			@Override
			public void onActivityDestroyed(Activity activity) {

			}
		};

		CrashHandler.getInstance().init(activity.getApplicationContext());
		initialize();
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
		if (config.PACKAGE == null || config.PACKAGE.isEmpty() || !config.PACKAGE.contains(".")) {
			throw new RuntimeException("The target package is not configured, please check!");
		}
	}

	/**
	 * Config class used to set the scroll behaviour, default timeouts, screenshot filetype and screenshot save path.
	 * <br> <br>
	 * Example of usage:
	 * <pre>
	 *  public void setUp() throws Exception {
	 *	Config config = new Config();
	 *	config.screenshotFileType = ScreenshotFileType.PNG;
	 *	config.screenshotSavePath = Environment.getExternalStorageDirectory() + "/Robotium/";
	 *	config.shouldScroll = false;
	 *	solo = new Solo(getInstrumentation(), config);
	 *	getActivity();
	 * }
	 * </pre>
	 *
	 * @author Renas Reda, renas.reda@robotium.com
	 */

	public static class Config {

		/**
		 * The timeout length of the get, is, set, assert, enter and click methods. Default length is 10 000 milliseconds.
		 */
		public int timeout_small = 10000;

		/**
		 * The timeout length of the waitFor methods. Default length is 20 000 milliseconds.
		 */
		public int timeout_large = 20000;

		/**
		 * The screenshot save path. Default save path is /sdcard/AutoClick-Screenshots/.
		 */
		public static final String screenshotSavePath = Environment.getExternalStorageDirectory() + "/AutoClick/Screenshots/";

		/**
		 * The Robotium save path. Default save path is /sdcard/AutoClick/.
		 */
		public static final String PATH = "/AutoClick";

		/**
		 * Iterated activities.
		 */
		public static final String ACTIVITY = "Activities.txt";

		/**
		 * Test results.
		 */
		public static final String RESULT = "Result.txt";

		/**
		 * Custom Intent record the activity parameters.
		 */
		public static final String PARAMS = "ActivityParams.txt";

		/**
		 * Starts the activity parameter json file.
		 */
		public static final String JSON = "Params.json";

		/**
		 * The log is save in this file.
		 */
		public static final String LOG = "Log.log";


		/**
		 * The screenshot file type, JPEG or PNG. Use ScreenshotFileType.JPEG or ScreenshotFileType.PNG. Default file type is JPEG.
		 */
		public ScreenshotFileType screenshotFileType = ScreenshotFileType.JPEG;

		/**
		 * Set to true if the get, is, set, enter, type and click methods should scroll. Default value is true.
		 */
		public boolean shouldScroll = true;

		/**
		 * Set to true if JavaScript should be used to click WebElements. Default value is false.
		 */
		public boolean useJavaScriptToClickWebElements = true;

		/**
		 * The screenshot file type, JPEG or PNG.
		 *
		 * @author Renas Reda, renas.reda@robotium.com
		 *
		 */
		public enum ScreenshotFileType {
			JPEG, PNG
		}

		/**
		 *  Set to true if Activity tracking should be enabled. Default value is true.
		 */

		public boolean trackActivities = true;
		
		/**
		 * Set the web frame to be used by Robotium. Default value is document.  
		 */
		
		public String webFrame = "document";
		
		/**
		 *  Set to true if logging should be enabled. Default value is false.
		 */
		
		public boolean commandLogging = true;
		
		/**
		 * The command logging tag. Default value is "Robotium".
		 */
		
		public String commandLoggingTag = "Robotium";

		/**
		 * The sleep duration for the Sleeper sleep method. Default length is 500 milliseconds.
		 */
		public int sleepDuration = 500;

		/**
		 * The sleep duration for the Sleeper sleepMini method. Default length is 300 milliseconds.
		 */
		public int sleepMiniDuration = 300;

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
		 * Target application, the test program will auto iteration it.
		 */
		public String PACKAGE;

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

	}

	/**
	 * Constructor that takes the instrumentation object.
	 *
	 * @param instrumentation the {@link Instrumentation} instance
	 */

	public Solo(Instrumentation instrumentation) {
		this(new Config(), instrumentation, null);
		
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "Solo("+instrumentation+")");
		}
	}

	/**
	 * Start listening activities.
	 * @param activity activity
	 */
	private void activityListener(Activity activity) {
		if (config.mode == Config.Mode.RECORD || config.mode == Config.Mode.REPTILE) {
			IntentParser.recordExtras(context, activity);
		}
	}

	/**
	 * Returns the ActivityMonitor used by Robotium.
	 *
	 * @return the ActivityMonitor used by Robotium
	 */

	public ActivityMonitor getActivityMonitor(){
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "getActivityMonitor()");
		}
		
		return activityUtils.getActivityMonitor();
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
	 * Returns an ArrayList of all the View objects located in the focused
	 * Activity or Dialog.
	 *
	 * @return an {@code ArrayList} of the {@link View} objects located in the focused window
	 */

	public ArrayList<View> getViews() {
		try {
			if(config.commandLogging){
				Log.d(config.commandLoggingTag, "getViews()");
			}
			
			return viewFetcher.getViews(null, false);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Returns an ArrayList of the View objects contained in the parent View.
	 *
	 * @param parent the parent view from which to return the views
	 * @return an {@code ArrayList} of the {@link View} objects contained in the specified {@code View}
	 */

	public ArrayList<View> getViews(View parent) {
		try {
			if(config.commandLogging){
				Log.d(config.commandLoggingTag, "getViews()");
			}
			
			return viewFetcher.getViews(parent, false);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Returns the absolute top parent View of the specified View.
	 *
	 * @param view the {@link View} whose top parent is requested
	 * @return the top parent {@link View}
	 */

	public View getTopParent(View view) {
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "getTopParent("+view+")");
		}
		
		View topParent = viewFetcher.getTopParent(view);
		return topParent;
	}

	/**
	 * Waits for the specified text to appear. Default timeout is 20 seconds.
	 *
	 * @param text the text to wait for, specified as a regular expression
	 * @return {@code true} if text is displayed and {@code false} if it is not displayed before the timeout
	 */

	public boolean waitForText(String text) {
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "waitForText(\""+text+"\")");
		}
		
		return (waiter.waitForText(text) != null);
	}

	/**
	 * Waits for the specified text to appear.
	 *
	 * @param text the text to wait for, specified as a regular expression
	 * @param minimumNumberOfMatches the minimum number of matches that are expected to be found. {@code 0} means any number of matches
	 * @param timeout the the amount of time in milliseconds to wait
	 * @return {@code true} if text is displayed and {@code false} if it is not displayed before the timeout
	 */

	public boolean waitForText(String text, int minimumNumberOfMatches, long timeout) {
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "waitForText(\""+text+"\", "+minimumNumberOfMatches+", "+timeout+")");
		}
		
		return (waiter.waitForText(text, minimumNumberOfMatches, timeout) != null);
	}

	/**
	 * Waits for the specified text to appear.
	 *
	 * @param text the text to wait for, specified as a regular expression
	 * @param minimumNumberOfMatches the minimum number of matches that are expected to be found. {@code 0} means any number of matches
	 * @param timeout the the amount of time in milliseconds to wait
	 * @param scroll {@code true} if scrolling should be performed
	 * @return {@code true} if text is displayed and {@code false} if it is not displayed before the timeout
	 */

	public boolean waitForText(String text, int minimumNumberOfMatches, long timeout, boolean scroll) {
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "waitForText(\""+text+"\", "+minimumNumberOfMatches+", "+timeout+", "+scroll+")");
		}
		
		return (waiter.waitForText(text, minimumNumberOfMatches, timeout, scroll) != null);
	}

	/**
	 * Waits for the specified text to appear.
	 *
	 * @param text the text to wait for, specified as a regular expression
	 * @param minimumNumberOfMatches the minimum number of matches that are expected to be found. {@code 0} means any number of matches
	 * @param timeout the the amount of time in milliseconds to wait
	 * @param scroll {@code true} if scrolling should be performed
	 * @param onlyVisible {@code true} if only visible text views should be waited for
	 * @return {@code true} if text is displayed and {@code false} if it is not displayed before the timeout
	 */

	public boolean waitForText(String text, int minimumNumberOfMatches, long timeout, boolean scroll, boolean onlyVisible) {
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "waitForText(\""+text+"\", "+minimumNumberOfMatches+", "+timeout+", "+scroll+", "+onlyVisible+")");
		}
		
		return (waiter.waitForText(text, minimumNumberOfMatches, timeout, scroll, onlyVisible, true) != null);
	}

	/**
	 * Waits for a View matching the specified resource id. Default timeout is 20 seconds.
	 *
	 * @param id the R.id of the {@link View} to wait for
	 * @return {@code true} if the {@link View} is displayed and {@code false} if it is not displayed before the timeout
	 */

	public boolean waitForView(int id){
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "waitForView("+id+")");
		}
		
		return waitForView(id, 0, Timeout.getLargeTimeout(), true);
	}

	/**
	 * Waits for a View matching the specified resource id.
	 *
	 * @param id the R.id of the {@link View} to wait for
	 * @param minimumNumberOfMatches the minimum number of matches that are expected to be found. {@code 0} means any number of matches
	 * @param timeout the amount of time in milliseconds to wait
	 * @return {@code true} if the {@link View} is displayed and {@code false} if it is not displayed before the timeout
	 */

	public boolean waitForView(int id, int minimumNumberOfMatches, int timeout){
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "waitForView("+id+", "+minimumNumberOfMatches+", "+timeout+")");
		}
		
		return waitForView(id, minimumNumberOfMatches, timeout, true);
	}

	/**
	 * Waits for a View matching the specified resource id.
	 *
	 * @param id the R.id of the {@link View} to wait for
	 * @param minimumNumberOfMatches the minimum number of matches that are expected to be found. {@code 0} means any number of matches
	 * @param timeout the amount of time in milliseconds to wait
	 * @param scroll {@code true} if scrolling should be performed
	 * @return {@code true} if the {@link View} is displayed and {@code false} if it is not displayed before the timeout
	 */

	public boolean waitForView(int id, int minimumNumberOfMatches, int timeout, boolean scroll){
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "waitForView("+id+", "+minimumNumberOfMatches+", "+timeout+", "+scroll+")");
		}
		
		int index = minimumNumberOfMatches-1;

		if(index < 1)
			index = 0;

		return (waiter.waitForView(id, index, timeout, scroll) != null);
	}

	/**
	 * Waits for a View matching the specified tag. Default timeout is 20 seconds.
	 *
	 * @param tag the {@link View#getTag() tag} of the {@link View} to wait for
	 * @return {@code true} if the {@link View} is displayed and {@code false} if it is not displayed before the timeout
	 */

	public boolean waitForView(Object tag){
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "waitForView("+tag+")");
		}
		
		return waitForView(tag, 0, Timeout.getLargeTimeout(), true);
	}

	/**
	 * Waits for a View matching the specified tag.
	 *
	 * @param tag the {@link View#getTag() tag} of the {@link View} to wait for
	 * @param minimumNumberOfMatches the minimum number of matches that are expected to be found. {@code 0} means any number of matches
	 * @param timeout the amount of time in milliseconds to wait
	 * @return {@code true} if the {@link View} is displayed and {@code false} if it is not displayed before the timeout
	 */

	public boolean waitForView(Object tag, int minimumNumberOfMatches, int timeout){
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "waitForView("+tag+", "+minimumNumberOfMatches+", "+timeout+")");
		}
		
		return waitForView(tag, minimumNumberOfMatches, timeout, true);
	}

	/**
	 * Waits for a View matching the specified tag
	 *
	 * @param tag the {@link View#getTag() tag} of the {@link View} to wait for
	 * @param minimumNumberOfMatches the minimum number of matches that are expected to be found. {@code 0} means any number of matches
	 * @param timeout the amount of time in milliseconds to wait
	 * @param scroll {@code true} if scrolling should be performed
	 * @return {@code true} if the {@link View} is displayed and {@code false} if it is not displayed before the timeout
	 */

	public boolean waitForView(Object tag, int minimumNumberOfMatches, int timeout, boolean scroll){
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "waitForView("+tag+", "+minimumNumberOfMatches+", "+timeout+", "+scroll+")");
		}
		
		int index = minimumNumberOfMatches-1;

		if(index < 1) {
			index = 0;
		}

		return (waiter.waitForView(tag, index, timeout, scroll) != null);
	}

	/**
	 * Waits for a View matching the specified class. Default timeout is 20 seconds.
	 *
	 * @param viewClass the {@link View} class to wait for
	 * @return {@code true} if the {@link View} is displayed and {@code false} if it is not displayed before the timeout
	 */

	public <T extends View> boolean waitForView(final Class<T> viewClass){
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "waitForView("+viewClass+")");
		}
		
		return waiter.waitForView(viewClass, 0, Timeout.getLargeTimeout(), true);
	}

	/**
	 * Waits for the specified View. Default timeout is 20 seconds.
	 *
	 * @param view the {@link View} object to wait for
	 * @return {@code true} if the {@link View} is displayed and {@code false} if it is not displayed before the timeout
	 */

	public <T extends View> boolean waitForView(View view){
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "waitForView("+view+")");
		}
		
		return waiter.waitForView(view);
	}

	/**
	 * Waits for the specified View.
	 *
	 * @param view the {@link View} object to wait for
	 * @param timeout the amount of time in milliseconds to wait
	 * @param scroll {@code true} if scrolling should be performed
	 * @return {@code true} if the {@link View} is displayed and {@code false} if it is not displayed before the timeout
	 */

	public <T extends View> boolean waitForView(View view, int timeout, boolean scroll){
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "waitForView("+view+", "+timeout+", "+scroll+")");
		}
		
		boolean checkIsShown = false;

		if(!scroll){
			checkIsShown = true;
		}

		View viewToWaitFor = waiter.waitForView(view, timeout, scroll, checkIsShown);

		if(viewToWaitFor != null)
			return true;

		return false;
	}

	/**
	 * Waits for a View matching the specified class.
	 *
	 * @param viewClass the {@link View} class to wait for
	 * @param minimumNumberOfMatches the minimum number of matches that are expected to be found. {@code 0} means any number of matches
	 * @param timeout the amount of time in milliseconds to wait
	 * @return {@code true} if the {@link View} is displayed and {@code false} if it is not displayed before the timeout
	 */

	public <T extends View> boolean waitForView(final Class<T> viewClass, final int minimumNumberOfMatches, final int timeout){
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "waitForView("+viewClass+", "+minimumNumberOfMatches+", "+timeout+")");
		}
		
		int index = minimumNumberOfMatches-1;

		if(index < 1)
			index = 0;

		return waiter.waitForView(viewClass, index, timeout, true);
	}

	/**
	 * Waits for a View matching the specified class.
	 *
	 * @param viewClass the {@link View} class to wait for
	 * @param minimumNumberOfMatches the minimum number of matches that are expected to be found. {@code 0} means any number of matches
	 * @param timeout the amount of time in milliseconds to wait
	 * @param scroll {@code true} if scrolling should be performed
	 * @return {@code true} if the {@link View} is displayed and {@code false} if it is not displayed before the timeout
	 */

	public <T extends View> boolean waitForView(final Class<T> viewClass, final int minimumNumberOfMatches, final int timeout,final boolean scroll){
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "waitForView("+viewClass+", "+minimumNumberOfMatches+", "+timeout+", "+scroll+")");
		}
		
		int index = minimumNumberOfMatches-1;

		if(index < 1)
			index = 0;

		return waiter.waitForView(viewClass, index, timeout, scroll);
	}

	/**
	 * Waits for a WebElement matching the specified By object. Default timeout is 20 seconds.
	 *
	 * @param by the By object. Examples are: {@code By.id("id")} and {@code By.name("name")}
	 * @return {@code true} if the {@link WebElement} is displayed and {@code false} if it is not displayed before the timeout
	 */

	public boolean waitForWebElement(By by){
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "waitForWebElement("+by+")");
		}
		
		return (waiter.waitForWebElement(by, 0, Timeout.getLargeTimeout(), true) != null);
	}

	/**
	 * Waits for a WebElement matching the specified By object.
	 *
	 * @param by the By object. Examples are: {@code By.id("id")} and {@code By.name("name")}
	 * @param timeout the the amount of time in milliseconds to wait
	 * @param scroll {@code true} if scrolling should be performed
	 * @return {@code true} if the {@link WebElement} is displayed and {@code false} if it is not displayed before the timeout
	 */

	public boolean waitForWebElement(By by, int timeout, boolean scroll){
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "waitForWebElement("+by+", "+timeout+", "+scroll+")");
		}
		
		return (waiter.waitForWebElement(by, 0, timeout, scroll) != null);
	}

	/**
	 * Waits for a WebElement matching the specified By object.
	 *
	 * @param by the By object. Examples are: {@code By.id("id")} and {@code By.name("name")}
	 * @param minimumNumberOfMatches the minimum number of matches that are expected to be found. {@code 0} means any number of matches
	 * @param timeout the the amount of time in milliseconds to wait
	 * @param scroll {@code true} if scrolling should be performed
	 * @return {@code true} if the {@link WebElement} is displayed and {@code false} if it is not displayed before the timeout
	 */

	public boolean waitForWebElement(By by, int minimumNumberOfMatches, int timeout, boolean scroll){
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "waitForWebElement("+by+", "+minimumNumberOfMatches+","+timeout+", "+scroll+")");
		}
		
		return (waiter.waitForWebElement(by, minimumNumberOfMatches, timeout, scroll) != null);
	}

	/**
	 * Waits for a condition to be satisfied.
	 *
	 * @param condition the condition to wait for
	 * @param timeout the amount of time in milliseconds to wait
	 * @return {@code true} if condition is satisfied and {@code false} if it is not satisfied before the timeout
	 */

	public boolean waitForCondition(Condition condition, final int timeout){
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "waitForCondition("+condition+","+timeout+")");
		}
		
		return waiter.waitForCondition(condition, timeout);
	}

	/**
	 * Searches for a text in the EditText objects currently displayed and returns true if found. Will automatically scroll when needed.
	 *
	 * @param text the text to search for
	 * @return {@code true} if an {@link EditText} displaying the specified text is found or {@code false} if it is not found
	 */

	public boolean searchEditText(String text) {
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "searchEditText(\""+text+"\")");
		}
		
		return searcher.searchWithTimeoutFor(EditText.class, text, 1, true, false);
	}


	/**
	 * Searches for a Button displaying the specified text and returns {@code true} if at least one Button
	 * is found. Will automatically scroll when needed.
	 *
	 * @param text the text to search for. The parameter will be interpreted as a regular expression
	 * @return {@code true} if a {@link Button} displaying the specified text is found and {@code false} if it is not found
	 */

	public boolean searchButton(String text) {
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "searchButton(\""+text+"\")");
		}
		
		return searcher.searchWithTimeoutFor(Button.class, text, 0, true, false);
	}

	/**
	 * Searches for a Button displaying the specified text and returns {@code true} if at least one Button
	 * is found. Will automatically scroll when needed.
	 *
	 * @param text the text to search for. The parameter will be interpreted as a regular expression
	 * @param onlyVisible {@code true} if only {@link Button} visible on the screen should be searched
	 * @return {@code true} if a {@link Button} displaying the specified text is found and {@code false} if it is not found
	 */

	public boolean searchButton(String text, boolean onlyVisible) {
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "searchButton(\""+text+"\", "+onlyVisible+")");
		}
		
		return searcher.searchWithTimeoutFor(Button.class, text, 0, true, onlyVisible);
	}

	/**
	 * Searches for a ToggleButton displaying the specified text and returns {@code true} if at least one ToggleButton
	 * is found. Will automatically scroll when needed.
	 *
	 * @param text the text to search for. The parameter will be interpreted as a regular expression
	 * @return {@code true} if a {@link ToggleButton} displaying the specified text is found and {@code false} if it is not found
	 */

	public boolean searchToggleButton(String text) {
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "searchToggleButton(\""+text+"\")");
		}
		
		return searcher.searchWithTimeoutFor(ToggleButton.class, text, 0, true, false);
	}

	/**
	 * Searches for a Button displaying the specified text and returns {@code true} if the
	 * searched Button is found a specified number of times. Will automatically scroll when needed.
	 *
	 * @param text the text to search for. The parameter will be interpreted as a regular expression
	 * @param minimumNumberOfMatches the minimum number of matches expected to be found. {@code 0} matches means that one or more
	 * matches are expected to be found
	 * @return {@code true} if a {@link Button} displaying the specified text is found a specified number of times and {@code false}
	 * if it is not found
	 */

	public boolean searchButton(String text, int minimumNumberOfMatches) {
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "searchButton(\""+text+"\", "+minimumNumberOfMatches+")");
		}
		
		return searcher.searchWithTimeoutFor(Button.class, text, minimumNumberOfMatches, true, false);
	}

	/**
	 * Searches for a Button displaying the specified text and returns {@code true} if the
	 * searched Button is found a specified number of times. Will automatically scroll when needed.
	 *
	 * @param text the text to search for. The parameter will be interpreted as a regular expression
	 * @param minimumNumberOfMatches the minimum number of matches expected to be found. {@code 0} matches means that one or more
	 * matches are expected to be found
	 * @param onlyVisible {@code true} if only {@link Button} visible on the screen should be searched
	 * @return {@code true} if a {@link Button} displaying the specified text is found a specified number of times and {@code false}
	 * if it is not found
	 */

	public boolean searchButton(String text, int minimumNumberOfMatches, boolean onlyVisible) {
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "searchButton(\""+text+"\", "+minimumNumberOfMatches+", "+onlyVisible+")");
		}
		
		return searcher.searchWithTimeoutFor(Button.class, text, minimumNumberOfMatches, true, onlyVisible);
	}

	/**
	 * Searches for a ToggleButton displaying the specified text and returns {@code true} if the
	 * searched ToggleButton is found a specified number of times. Will automatically scroll when needed.
	 *
	 * @param text the text to search for. The parameter will be interpreted as a regular expression
	 * @param minimumNumberOfMatches the minimum number of matches expected to be found. {@code 0} matches means that one or more
	 * matches are expected to be found
	 * @return {@code true} if a {@link ToggleButton} displaying the specified text is found a specified number of times and {@code false}
	 * if it is not found
	 */

	public boolean searchToggleButton(String text, int minimumNumberOfMatches) {
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "searchToggleButton(\""+text+"\", "+minimumNumberOfMatches+")");
		}
		
		return searcher.searchWithTimeoutFor(ToggleButton.class, text, minimumNumberOfMatches, true, false);
	}

	/**
	 * Searches for the specified text and returns {@code true} if at least one item
	 * is found displaying the expected text. Will automatically scroll when needed.
	 *
	 * @param text the text to search for. The parameter will be interpreted as a regular expression
	 * @return {@code true} if the search string is found and {@code false} if it is not found
	 */

	public boolean searchText(String text) {
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "searchText(\""+text+"\")");
		}
		
		return searcher.searchWithTimeoutFor(TextView.class, text, 0, true, false);
	}

	/**
	 * Searches for the specified text and returns {@code true} if at least one item
	 * is found displaying the expected text. Will automatically scroll when needed.
	 *
	 * @param text the text to search for. The parameter will be interpreted as a regular expression
	 * @param onlyVisible {@code true} if only texts visible on the screen should be searched
	 * @return {@code true} if the search string is found and {@code false} if it is not found
	 */

	public boolean searchText(String text, boolean onlyVisible) {
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "searchText(\""+text+"\", "+onlyVisible+")");
		}
		
		return searcher.searchWithTimeoutFor(TextView.class, text, 0, true, onlyVisible);
	}

	/**
	 * Searches for the specified text and returns {@code true} if the searched text is found a specified
	 * number of times. Will automatically scroll when needed.
	 *
	 * @param text the text to search for. The parameter will be interpreted as a regular expression
	 * @param minimumNumberOfMatches the minimum number of matches expected to be found. {@code 0} matches means that one or more
	 * matches are expected to be found
	 * @return {@code true} if text is found a specified number of times and {@code false} if the text
	 * is not found
	 */

	public boolean searchText(String text, int minimumNumberOfMatches) {
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "searchText(\""+text+"\", "+minimumNumberOfMatches+")");
		}
		
		return searcher.searchWithTimeoutFor(TextView.class, text, minimumNumberOfMatches, true, false);
	}

	/**
	 * Searches for the specified text and returns {@code true} if the searched text is found a specified
	 * number of times.
	 *
	 * @param text the text to search for. The parameter will be interpreted as a regular expression.
	 * @param minimumNumberOfMatches the minimum number of matches expected to be found. {@code 0} matches means that one or more
	 * matches are expected to be found
	 * @param scroll {@code true} if scrolling should be performed
	 * @return {@code true} if text is found a specified number of times and {@code false} if the text
	 * is not found
	 */

	public boolean searchText(String text, int minimumNumberOfMatches, boolean scroll) {
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "searchText(\""+text+"\", "+minimumNumberOfMatches+", "+scroll+")");
		}
		
		return searcher.searchWithTimeoutFor(TextView.class, text, minimumNumberOfMatches, scroll, false);
	}

	/**
	 * Searches for the specified text and returns {@code true} if the searched text is found a specified
	 * number of times.
	 *
	 * @param text the text to search for. The parameter will be interpreted as a regular expression.
	 * @param minimumNumberOfMatches the minimum number of matches expected to be found. {@code 0} matches means that one or more
	 * matches are expected to be found
	 * @param scroll {@code true} if scrolling should be performed
	 * @param onlyVisible {@code true} if only texts visible on the screen should be searched
	 * @return {@code true} if text is found a specified number of times and {@code false} if the text
	 * is not found
	 */

	public boolean searchText(String text, int minimumNumberOfMatches, boolean scroll, boolean onlyVisible) {
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "searchText(\""+text+"\", "+minimumNumberOfMatches+", "+scroll+", "+onlyVisible+")");
		}
		
		return searcher.searchWithTimeoutFor(TextView.class, text, minimumNumberOfMatches, scroll, onlyVisible);
	}

	/**
	 * Sets the Orientation (Landscape/Portrait) for the current Activity.
	 *
	 * @param orientation the orientation to set. <code></code>{@link #LANDSCAPE} for landscape or
	 * <code></code>{@link #PORTRAIT} for portrait.
	 */

	public void setActivityOrientation(int orientation)
	{
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "setActivityOrientation("+orientation+")");
		}
		
		activityUtils.setActivityOrientation(orientation);
	}

	/**
	 * Returns the current Activity.
	 *
	 * @return the current Activity
	 */

	public Activity getCurrentActivity() {
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "getCurrentActivity()");
		}
		
		return activityUtils.getCurrentActivity(false);
	}

	/**
	 * Asserts that the Activity matching the specified name is active.
	 *
	 * @param message the message to display if the assert fails
	 * @param name the name of the {@link Activity} that is expected to be active. Example is: {@code "MyActivity"}
	 */

	public void assertCurrentActivity(String message, String name)
	{
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "assertCurrentActivity(\""+message+"\", \""+name+"\")");
		}
		
		asserter.assertCurrentActivity(message, name);
	}

	/**
	 * Asserts that the Activity matching the specified class is active.
	 *
	 * @param message the message to display if the assert fails
	 * @param activityClass the class of the Activity that is expected to be active. Example is: {@code MyActivity.class}
	 */

	@SuppressWarnings("unchecked")
	public void assertCurrentActivity(String message, @SuppressWarnings("rawtypes") Class activityClass)
	{
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "assertCurrentActivity("+message+", "+activityClass+")");
		}
		
		asserter.assertCurrentActivity(message, activityClass);

	}

	/**
	 * Asserts that the Activity matching the specified name is active, with the possibility to
	 * verify that the expected Activity is a new instance of the Activity.
	 *
	 * @param message the message to display if the assert fails
	 * @param name the name of the Activity that is expected to be active. Example is: {@code "MyActivity"}
	 * @param isNewInstance {@code true} if the expected {@link Activity} is a new instance of the {@link Activity}
	 */

	public void assertCurrentActivity(String message, String name, boolean isNewInstance)
	{
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "assertCurrentActivity("+message+", "+name+", "+isNewInstance+")");
		}
		
		asserter.assertCurrentActivity(message, name, isNewInstance);
	}

	/**
	 * Asserts that the Activity matching the specified class is active, with the possibility to
	 * verify that the expected Activity is a new instance of the Activity.
	 *
	 * @param message the message to display if the assert fails
	 * @param activityClass the class of the Activity that is expected to be active. Example is: {@code MyActivity.class}
	 * @param isNewInstance {@code true} if the expected {@link Activity} is a new instance of the {@link Activity}
	 */

	@SuppressWarnings("unchecked")
	public void assertCurrentActivity(String message, @SuppressWarnings("rawtypes") Class activityClass,
			boolean isNewInstance) {
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "assertCurrentActivity(\""+message+"\", "+activityClass+", "+isNewInstance+")");
		}
		
		asserter.assertCurrentActivity(message, activityClass, isNewInstance);
	}

	/**
	 * Asserts that the available memory is not considered low by the system.
	 */

	public void assertMemoryNotLow()
	{
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "assertMemoryNotLow()");
		}
		
		asserter.assertMemoryNotLow();
	}

	/**
	 * Waits for a Dialog to open. Default timeout is 20 seconds.
	 *
	 * @return {@code true} if the {@link android.app.Dialog} is opened before the timeout and {@code false} if it is not opened
	 */

	public boolean waitForDialogToOpen() {
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "waitForDialogToOpen()");
		}
		
		return dialogUtils.waitForDialogToOpen(Timeout.getLargeTimeout(), true);
	}

	/**
	 * Waits for a Dialog to close. Default timeout is 20 seconds.
	 *
	 * @return {@code true} if the {@link android.app.Dialog} is closed before the timeout and {@code false} if it is not closed
	 */

	public boolean waitForDialogToClose() {
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "waitForDialogToClose()");
		}
		
		return dialogUtils.waitForDialogToClose(Timeout.getLargeTimeout());
	}

	/**
	 * Waits for a Dialog to open.
	 *
	 * @param timeout the amount of time in milliseconds to wait
	 * @return {@code true} if the {@link android.app.Dialog} is opened before the timeout and {@code false} if it is not opened
	 */

	public boolean waitForDialogToOpen(long timeout) {
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "waitForDialogToOpen("+timeout+")");
		}
		
		return dialogUtils.waitForDialogToOpen(timeout, true);
	}

	/**
	 * Waits for a Dialog to close.
	 *
	 * @param timeout the amount of time in milliseconds to wait
	 * @return {@code true} if the {@link android.app.Dialog} is closed before the timeout and {@code false} if it is not closed
	 */

	public boolean waitForDialogToClose(long timeout) {
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "waitForDialogToClose("+timeout+")");
		}
		
		return dialogUtils.waitForDialogToClose(timeout);
	}


	/**
	 * Simulates pressing the hardware back key.
	 */

	public void goBack()
	{
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "goBack()");
		}
		
		hideSoftKeyboard();
		sender.goBack();
	}

	/**
	 * Clicks the specified coordinates.
	 *
	 * @param x the x coordinate
	 * @param y the y coordinate
	 */

	public void clickOnScreen(float x, float y) {
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "clickOnScreen("+x+", "+y+")");
		}
		
		sleeper.sleep();
		clicker.clickOnScreen(x, y, null);
	}

	/**
	 * Clicks the specified coordinates rapidly a specified number of times. Requires API level >= 14.
	 *
	 * @param x the x coordinate
	 * @param y the y coordinate
	 * @param numberOfClicks the number of clicks to perform
	 */

	public void clickOnScreen(float x, float y, int numberOfClicks) {
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "clickOnScreen("+x+", "+y+", "+numberOfClicks+")");
		}
		
		if (Build.VERSION.SDK_INT < 14){
			throw new RuntimeException("clickOnScreen(float x, float y, int numberOfClicks) requires API level >= 14");

		}
		tapper.generateTapGesture(numberOfClicks, new PointF(x, y));
	}

	/**
	 * Long clicks the specified coordinates.
	 *
	 * @param x the x coordinate
	 * @param y the y coordinate
	 */

	public void clickLongOnScreen(float x, float y) {
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "clickLongOnScreen("+x+", "+y+")");
		}

		clicker.clickLongOnScreen(x, y, 0, null);
	}

	/**
	 * Long clicks the specified coordinates for a specified amount of time.
	 *
	 * @param x the x coordinate
	 * @param y the y coordinate
	 * @param time the amount of time to long click
	 */

	public void clickLongOnScreen(float x, float y, int time) {
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "clickLongOnScreen("+x+", "+y+", "+time+")");
		}

		clicker.clickLongOnScreen(x, y, time, null);
	}


	/**
	 * Clicks a Button displaying the specified text. Will automatically scroll when needed.
	 *
	 * @param text the text displayed by the {@link Button}. The parameter will be interpreted as a regular expression
	 */

	public void clickOnButton(String text) {
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "clickOnButton(\""+text+"\")");
		}

		clicker.clickOn(Button.class, text);

	}

	/**
	 * Clicks an ImageButton matching the specified index.
	 *
	 * @param index the index of the {@link ImageButton} to click. 0 if only one is available
	 */

	public void clickOnImageButton(int index) {
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "clickOnImageButton("+index+")");
		}

		clicker.clickOn(ImageButton.class, index);
	}

	/**
	 * Clicks a ToggleButton displaying the specified text.
	 *
	 * @param text the text displayed by the {@link ToggleButton}. The parameter will be interpreted as a regular expression
	 */

	public void clickOnToggleButton(String text) {
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "clickOnToggleButton(\""+text+"\")");
		}

		clicker.clickOn(ToggleButton.class, text);
	}

	/**
	 * Clicks a MenuItem displaying the specified text.
	 *
	 * @param text the text displayed by the MenuItem. The parameter will be interpreted as a regular expression
	 */

	public void clickOnMenuItem(String text)
	{
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "clickOnMenuItem(\""+text+"\")");
		}

		clicker.clickOnMenuItem(text);
	}

	/**
	 * Clicks a MenuItem displaying the specified text.
	 *
	 * @param text the text displayed by the MenuItem. The parameter will be interpreted as a regular expression
	 * @param subMenu {@code true} if the menu item could be located in a sub menu
	 */

	public void clickOnMenuItem(String text, boolean subMenu)
	{
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "clickOnMenuItem(\""+text+"\", "+subMenu+")");
		}

		clicker.clickOnMenuItem(text, subMenu);
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

	public void parseType(){

	}

	/**
	 * Clicks a WebElement matching the specified By object.
	 *
	 * @param by the By object. Examples are: {@code By.id("id")} and {@code By.name("name")}
	 */

	public void clickOnWebElement(By by){
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "clickOnWebElement("+by+")");
		}

		clickOnWebElement(by, 0, true);
	}

	/**
	 * Clicks a WebElement matching the specified By object.
	 *
	 * @param by the By object. Examples are: {@code By.id("id")} and {@code By.name("name")}
	 * @param match if multiple objects match, this determines which one to click
	 */

	public void clickOnWebElement(By by, int match){
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "clickOnWebElement("+by+", "+match+")");
		}

		clickOnWebElement(by, match, true);
	}

	/**
	 * Clicks a WebElement matching the specified By object.
	 *
	 * @param by the By object. Examples are: {@code By.id("id")} and {@code By.name("name")}
	 * @param match if multiple objects match, this determines which one to click
	 * @param scroll {@code true} if scrolling should be performed
	 */

	public void clickOnWebElement(By by, int match, boolean scroll){
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "clickOnWebElement("+by+", "+match+", "+scroll+")");
		}

		clicker.clickOnWebElement(by, match, scroll, config.useJavaScriptToClickWebElements);
	}

	/**
	 * Presses a MenuItem matching the specified index. Index {@code 0} is the first item in the
	 * first row, Index {@code 3} is the first item in the second row and
	 * index {@code 6} is the first item in the third row.
	 *
	 * @param index the index of the {@link android.view.MenuItem} to press
	 */

	public void pressMenuItem(int index) {
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "pressMenuItem("+index+")");
		}

		presser.pressMenuItem(index);
	}

	/**
	 * Presses a MenuItem matching the specified index. Supports three rows with a specified amount
	 * of items. If itemsPerRow equals 5 then index 0 is the first item in the first row,
	 * index 5 is the first item in the second row and index 10 is the first item in the third row.
	 *
	 * @param index the index of the {@link android.view.MenuItem} to press
	 * @param itemsPerRow the amount of menu items there are per row
	 */

	public void pressMenuItem(int index, int itemsPerRow) {
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "pressMenuItem("+index+", "+itemsPerRow+")");
		}

		presser.pressMenuItem(index, itemsPerRow);
	}

	/**
	 * Presses the soft keyboard next button.
	 */

	/**
	 * Presses the soft keyboard next button.
	 */

	public void pressSoftKeyboardNextButton(){
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "pressSoftKeyboardNextButton()");
		}

		presser.pressSoftKeyboard(EditorInfo.IME_ACTION_NEXT);
	}

	/**
	 * Presses the soft keyboard search button.
	 */

	public void pressSoftKeyboardSearchButton(){
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "pressSoftKeyboardSearchButton()");
		}

		presser.pressSoftKeyboard(EditorInfo.IME_ACTION_SEARCH);
	}

	/**
	 * Presses the soft keyboard go button.
	 */

	public void pressSoftKeyboardGoButton(){
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "pressSoftKeyboardGoButton()");
		}

		presser.pressSoftKeyboard(EditorInfo.IME_ACTION_GO);
	}

	/**
	 * Presses the soft keyboard done button.
	 */

	public void pressSoftKeyboardDoneButton(){
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "pressSoftKeyboardDoneButton()");
		}

		presser.pressSoftKeyboard(EditorInfo.IME_ACTION_DONE);
	}

	/**
	 * Presses a Spinner (drop-down menu) item.
	 *
	 * @param spinnerIndex the index of the {@link Spinner} menu to use
	 * @param itemIndex the index of the {@link Spinner} item to press relative to the currently selected item.
	 * A Negative number moves up on the {@link Spinner}, positive moves down
	 */

	public void pressSpinnerItem(int spinnerIndex, int itemIndex)
	{
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "pressSpinnerItem("+spinnerIndex+", "+itemIndex+")");
		}

		presser.pressSpinnerItem(spinnerIndex, itemIndex);
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
	 * Clicks the specified View.
	 *
	 * @param view the {@link View} to click
	 * @param immediately {@code true} if View should be clicked without any wait
	 */

	public void clickOnView(View view, boolean immediately){
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "clickOnView("+view+", "+immediately+")");
		}

		if(immediately)
			clicker.clickOnScreen(view);
		else{
			view = waiter.waitForView(view, Timeout.getSmallTimeout());
			clicker.clickOnScreen(view);
		}
	}

	/**
	 * Long clicks the specified View.
	 *
	 * @param view the {@link View} to long click
	 */

	public void clickLongOnView(View view) {
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "clickLongOnView("+view+")");
		}

		view = waiter.waitForView(view, Timeout.getSmallTimeout());
		clicker.clickOnScreen(view, true, 0);

	}

	/**
	 * Long clicks the specified View for a specified amount of time.
	 *
	 * @param view the {@link View} to long click
	 * @param time the amount of time to long click
	 */

	public void clickLongOnView(View view, int time) {
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "clickLongOnView("+view+", "+time+")");
		}

		clicker.clickOnScreen(view, true, time);

	}

	/**
	 * Clicks a View or WebElement displaying the specified
	 * text. Will automatically scroll when needed.
	 *
	 * @param text the text to click. The parameter will be interpreted as a regular expression
	 */

	public void clickOnText(String text) {
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "clickOnText(\""+text+"\")");
		}

		clicker.clickOnText(text, false, 1, true, 0);
	}

	/**
	 * Clicks a View or WebElement displaying the specified text. Will automatically scroll when needed.
	 *
	 * @param text the text to click. The parameter will be interpreted as a regular expression
	 * @param match if multiple objects match the text, this determines which one to click
	 */

	public void clickOnText(String text, int match) {
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "clickOnText(\""+text+"\", "+match+")");
		}

		clicker.clickOnText(text, false, match, true, 0);
	}

	/**
	 * Clicks a View or WebElement displaying the specified text.
	 *
	 * @param text the text to click. The parameter will be interpreted as a regular expression
	 * @param match if multiple objects match the text, this determines which one to click
	 * @param scroll {@code true} if scrolling should be performed
	 */

	public void clickOnText(String text, int match, boolean scroll) {
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "clickOnText(\""+text+"\", "+match+", "+scroll+")");
		}

		clicker.clickOnText(text, false, match, scroll, 0);
	}

	/**
	 * Long clicks a View or WebElement displaying the specified text. Will automatically scroll when needed.
	 *
	 * @param text the text to click. The parameter will be interpreted as a regular expression
	 */

	public void clickLongOnText(String text)
	{
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "clickLongOnText(\""+text+"\")");
		}

		clicker.clickOnText(text, true, 1, true, 0);
	}

	/**
	 * Long clicks a View or WebElement displaying the specified text. Will automatically scroll when needed.
	 *
	 * @param text the text to click. The parameter will be interpreted as a regular expression
	 * @param match if multiple objects match the text, this determines which one to click
	 */

	public void clickLongOnText(String text, int match)
	{
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "clickLongOnText(\""+text+"\", "+match+")");
		}

		clicker.clickOnText(text, true, match, true, 0);
	}

	/**
	 * Long clicks a View or WebElement displaying the specified text.
	 *
	 * @param text the text to click. The parameter will be interpreted as a regular expression
	 * @param match if multiple objects match the text, this determines which one to click
	 * @param scroll {@code true} if scrolling should be performed
	 */

	public void clickLongOnText(String text, int match, boolean scroll)
	{
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "clickLongOnText(\""+text+"\", "+match+", "+scroll+")");
		}

		clicker.clickOnText(text, true, match, scroll, 0);
	}

	/**
	 * Long clicks a View or WebElement displaying the specified text.
	 *
	 * @param text the text to click. The parameter will be interpreted as a regular expression
	 * @param match if multiple objects match the text, this determines which one to click
	 * @param time the amount of time to long click
	 */

	public void clickLongOnText(String text, int match, int time)
	{
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "clickLongOnText(\""+text+"\", "+match+", "+time+")");
		}

		clicker.clickOnText(text, true, match, true, time);
	}

	/**
	 * Long clicks a View displaying the specified text and then selects
	 * an item from the context menu that appears. Will automatically scroll when needed.
	 *
	 * @param text the text to click. The parameter will be interpreted as a regular expression
	 * @param index the index of the menu item to press. {@code 0} if only one is available
	 */

	public void clickLongOnTextAndPress(String text, int index) {
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "clickLongOnTextAndPress(\""+text+"\", "+index+")");
		}

		clicker.clickLongOnTextAndPress(text, index);
	}

	/**
	 * Clicks a Button matching the specified index.
	 *
	 * @param index the index of the {@link Button} to click. {@code 0} if only one is available
	 */

	public void clickOnButton(int index) {
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "clickOnButton("+index+")");
		}

		clicker.clickOn(Button.class, index);
	}

	/**
	 * Clicks a RadioButton matching the specified index.
	 *
	 * @param index the index of the {@link RadioButton} to click. {@code 0} if only one is available
	 */

	public void clickOnRadioButton(int index) {
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "clickOnRadioButton("+index+")");
		}

		clicker.clickOn(RadioButton.class, index);
	}

	/**
	 * Clicks a CheckBox matching the specified index.
	 *
	 * @param index the index of the {@link CheckBox} to click. {@code 0} if only one is available
	 */

	public void clickOnCheckBox(int index) {
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "clickOnCheckBox("+index+")");
		}

		clicker.clickOn(CheckBox.class, index);
	}

	/**
	 * Clicks an EditText matching the specified index.
	 *
	 * @param index the index of the {@link EditText} to click. {@code 0} if only one is available
	 */

	public void clickOnEditText(int index) {
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "clickOnEditText("+index+")");
		}

		clicker.clickOn(EditText.class, index);
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
	 * Clicks the specified list line in the ListView matching the specified index and
	 * returns an ArrayList of the TextView objects that the list line is displaying.
	 *
	 * @param line the line to click
	 * @param index the index of the list. {@code 0} if only one is available
	 * @return an {@code ArrayList} of the {@link TextView} objects located in the list line
	 */

	public ArrayList<TextView> clickInList(int line, int index) {
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "clickInList("+line+", "+index+")");
		}

		return clicker.clickInList(line, index, 0, false, 0);
	}

	/**
	 * Clicks a View with a specified resource id on the specified line in the list matching the specified index
	 *
	 * @param line the line where the View exists
	 * @param index the index of the List. {@code 0} if only one is available
	 * @param id the resource id of the View to click
	 */

	public void clickInList(int line, int index, int id) {
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "clickInList("+line+", "+index+", " + id +")");
		}

		clicker.clickInList(line, index, id, false, 0);
	}

	/**
	 * Long clicks the specified list line and returns an ArrayList of the TextView objects that
	 * the list line is displaying. Will use the first ListView it finds.
	 *
	 * @param line the line to click
	 * @return an {@code ArrayList} of the {@link TextView} objects located in the list line
	 */

	public ArrayList<TextView> clickLongInList(int line){
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "clickLongInList("+line+")");
		}

		return clicker.clickInList(line, 0, 0, true, 0);
	}

	/**
	 * Long clicks the specified list line in the ListView matching the specified index and
	 * returns an ArrayList of the TextView objects that the list line is displaying.
	 *
	 * @param line the line to click
	 * @param index the index of the list. {@code 0} if only one is available
	 * @return an {@code ArrayList} of the {@link TextView} objects located in the list line
	 */

	public ArrayList<TextView> clickLongInList(int line, int index){
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "clickLongInList("+line+", "+index+")");
		}

		return clicker.clickInList(line, index, 0, true, 0);
	}

	/**
	 * Long clicks the specified list line in the ListView matching the specified index and
	 * returns an ArrayList of the TextView objects that the list line is displaying.
	 *
	 * @param line the line to click
	 * @param index the index of the list. {@code 0} if only one is available
	 * @param time the amount of time to long click
	 * @return an {@code ArrayList} of the {@link TextView} objects located in the list line
	 */

	public ArrayList<TextView> clickLongInList(int line, int index, int time){
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "clickLongInList("+line+", "+index+", "+time+")");
		}

		return clicker.clickInList(line, index, 0, true, time);
	}


	/**
	 * Clicks the specified item index and returns an ArrayList of the TextView objects that
	 * the item index is displaying. Will use the first RecyclerView it finds.
	 *
	 * @param itemIndex the item index to click
	 * @return an {@code ArrayList} of the {@link TextView} objects located in the item index
	 */

	public ArrayList<TextView> clickInRecyclerView(int itemIndex) {
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "clickInRecyclerView("+itemIndex+")");
		}

		return clicker.clickInRecyclerView(itemIndex);
	}

	public ArrayList<TextView> clickInRecyclerView(ViewGroup recyclerView, int itemIndex) {
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "clickInRecyclerView("+itemIndex+")");
		}

		return clicker.clickInRecyclerView(recyclerView, itemIndex);
	}

	/**
	 * Clicks the specified item index in the RecyclerView matching the specified RecyclerView index and
	 * returns an ArrayList of the TextView objects that the list line is displaying.
	 *
	 * @param itemIndex the line to click
	 * @param recyclerViewIndex the index of the RecyclerView. {@code 0} if only one is available
	 * @return an {@code ArrayList} of the {@link TextView} objects located in the item index
	 */

	public ArrayList<TextView> clickInRecyclerView(int itemIndex, int recyclerViewIndex) {
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "clickInRecyclerView("+itemIndex+", "+recyclerViewIndex+")");
		}

		return clicker.clickInRecyclerView(itemIndex, recyclerViewIndex, 0, false, 0);
	}

	/**
	 * Clicks a View with a specified resource id on the specified item index in the RecyclerView matching the specified RecyclerView index
	 *
	 * @param itemIndex the line where the View exists
	 * @param recyclerViewIndex the index of the RecyclerView. {@code 0} if only one is available
	 * @param id the resource id of the View to click
	 */

	public void clickInRecyclerView(int itemIndex, int recyclerViewIndex, int id) {
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "clickInRecyclerView("+itemIndex+", "+recyclerViewIndex+", " + id +")");
		}

		clicker.clickInRecyclerView(itemIndex, recyclerViewIndex, id, false, 0);
	}

	/**
	 * Long clicks the specified item index and returns an ArrayList of the TextView objects that
	 * the item index is displaying. Will use the first RecyclerView it finds.
	 *
	 * @param itemIndex the item index to click
	 * @return an {@code ArrayList} of the {@link TextView} objects located in the list line
	 */

	public ArrayList<TextView> clickLongInRecycleView(int itemIndex){
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "clickLongInRecycleView("+itemIndex+")");
		}

		return clicker.clickInRecyclerView(itemIndex, 0, 0, true, 0);
	}

	/**
	 * Long clicks the specified item index in the RecyclerView matching the specified RecyclerView index and
	 * returns an ArrayList of the TextView objects that the item index is displaying.
	 *
	 * @param itemIndex the item index to click
	 * @param recyclerViewIndex the index of the RecyclerView. {@code 0} if only one is available
	 * @return an {@code ArrayList} of the {@link TextView} objects located in the list line
	 */

	public ArrayList<TextView> clickLongInRecycleView(int itemIndex, int recyclerViewIndex){
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "clickLongInRecycleView("+itemIndex+", "+recyclerViewIndex+")");
		}

		return clicker.clickInRecyclerView(itemIndex, recyclerViewIndex, 0, true, 0);
	}

	/**
	 * Long clicks the specified item index in the RecyclerView matching the specified RecyclerView index and
	 * returns an ArrayList of the TextView objects that the item index is displaying.
	 *
	 * @param itemIndex the item index to click
	 * @param recyclerViewIndex the index of the RecyclerView. {@code 0} if only one is available
	 * @param time the amount of time to long click
	 * @return an {@code ArrayList} of the {@link TextView} objects located in the list line
	 */

	public ArrayList<TextView> clickLongInRecycleView(int itemIndex, int recyclerViewIndex, int time){
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "clickLongInRecycleView("+itemIndex+", "+recyclerViewIndex+", "+time+")");
		}

		return clicker.clickInRecyclerView(itemIndex, recyclerViewIndex, 0, true, time);
	}

	/**
	 * Clicks an ActionBarItem matching the specified resource id.
	 *
	 * @param id the R.id of the ActionBar item to click
	 */

	public void clickOnActionBarItem(int id){
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "clickOnActionBarItem("+id+")");
		}

		clicker.clickOnActionBarItem(id);
	}

	/**
	 * Clicks an ActionBar Home/Up button.
	 */

	public void clickOnActionBarHomeButton() {
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "clickOnActionBarHomeButton()");
		}

		instrumentation.runOnMainSync(new Runnable() {
			@Override
			public void run() {
				clicker.clickOnActionBarHomeButton();
			}
		});
	}

	/**
	* An Builder pattern that will allow the client to build a new illustration
	*/

	public Illustration.Builder createIllustrationBuilder(){
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "createIllustrationBuilder()");
		}

		return new Illustration.Builder();
	}

    /**
	 * Simulate drawing an illustration on the screen.
	 * <br> <br>
	 * Example of usage:
	 * <pre>
	 *
	 *  	Illustration.Builder builder = createIllustrationBuilder();
	 *	builder.addPoint(200, 20000, MotionEvent.TOOL_TYPE_FINGER);
	 *	builder.addPoint(1000, 1500, MotionEvent.TOOL_TYPE_FINGER);
	 *	Illustration illustration = builder.build();
	 *	illustrate(illustration);
	 * </pre>
	 * @param illustration Built-up illustration containing toolType, pressure, and coordinate information.
	 */

	public void illustrate(Illustration illustration){
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "illustrate("+illustration+")");
		}

		illustrator.illustrate(illustration);
	}

	/**
	 * Simulate touching the specified location and dragging it to a new location.
	 *
	 *
	 * @param fromX X coordinate of the initial touch, in screen coordinates
	 * @param toX X coordinate of the drag destination, in screen coordinates
	 * @param fromY Y coordinate of the initial touch, in screen coordinates
	 * @param toY Y coordinate of the drag destination, in screen coordinates
	 * @param stepCount how many move steps to include in the drag. Less steps results in a faster drag
	 */

	public void drag(float fromX, float toX, float fromY, float toY,
			int stepCount) {
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "drag("+fromX+", "+toX+", "+fromY+", "+toY+")");
		}

		dialogUtils.hideSoftKeyboard(null, false, true);
		scroller.drag(fromX, toX, fromY, toY, stepCount);
	}

	/**
	 * Scrolls down the screen.
	 *
	 * @return {@code true} if more scrolling can be performed and {@code false} if it is at the end of
	 * the screen
	 */

	@SuppressWarnings("unchecked")
	public boolean scrollDown() {
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "scrollDown()");
		}

		View recyclerView = viewFetcher.getRecyclerView(true, 0);

		if(recyclerView != null){
			waiter.waitForViews(true, AbsListView.class, ScrollView.class, WebView.class, recyclerView.getClass());
		}
		else {
			waiter.waitForViews(true, AbsListView.class, ScrollView.class, WebView.class);
		}
		return scroller.scroll(Scroller.DOWN);
	}

	/**
	 * Scrolls to the bottom of the screen.
	 */

	@SuppressWarnings("unchecked")
	public void scrollToBottom() {
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "scrollToBottom()");
		}

		View recyclerView = viewFetcher.getRecyclerView(true, 0);
		if(recyclerView != null){
			waiter.waitForViews(true, AbsListView.class, ScrollView.class, WebView.class, recyclerView.getClass());
		}
		else {
			waiter.waitForViews(true, AbsListView.class, ScrollView.class, WebView.class);
		}
		scroller.scroll(Scroller.DOWN, true);
	}


	/**
	 * Scrolls up the screen.
	 *
	 * @return {@code true} if more scrolling can be performed and {@code false} if it is at the top of
	 * the screen
	 */

	@SuppressWarnings("unchecked")
	public boolean scrollUp(){
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "scrollUp()");
		}

		View recyclerView = viewFetcher.getRecyclerView(true, 0);
		if(recyclerView != null){
			waiter.waitForViews(true, AbsListView.class, ScrollView.class, WebView.class, recyclerView.getClass());
		}
		else {
			waiter.waitForViews(true, AbsListView.class, ScrollView.class, WebView.class);
		}
		return scroller.scroll(Scroller.UP);
	}

	/**
	 * Scrolls to the top of the screen.
	 */

	@SuppressWarnings("unchecked")
	public void scrollToTop() {
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "scrollToTop()");
		}

		View recyclerView = viewFetcher.getRecyclerView(true, 0);
		if(recyclerView != null){
			waiter.waitForViews(true, AbsListView.class, ScrollView.class, WebView.class, recyclerView.getClass());
		}
		else {
			waiter.waitForViews(true, AbsListView.class, ScrollView.class, WebView.class);
		}
		scroller.scroll(Scroller.UP, true);
	}

	/**
	 * Scrolls down the specified AbsListView.
	 *
	 * @param list the {@link AbsListView} to scroll
	 * @return {@code true} if more scrolling can be performed
	 */

	public boolean scrollDownList(AbsListView list) {
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "scrollDownList("+list+")");
		}

		return scroller.scrollList(list, Scroller.DOWN, false);
	}

	/**
	 * Scrolls to the bottom of the specified AbsListView.
	 *
	 * @param list the {@link AbsListView} to scroll
	 * @return {@code true} if more scrolling can be performed
	 */

	public boolean scrollListToBottom(AbsListView list) {
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "scrollListToBottom("+list+")");
		}

		return scroller.scrollList(list, Scroller.DOWN, true);
	}

	/**
	 * Scrolls up the specified AbsListView.
	 *
	 * @param list the {@link AbsListView} to scroll
	 * @return {@code true} if more scrolling can be performed
	 */

	public boolean scrollUpList(AbsListView list) {
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "scrollUpList("+list+")");
		}

		return scroller.scrollList(list, Scroller.UP, false);
	}

	/**
	 * Scrolls to the top of the specified AbsListView.
	 *
	 * @param list the {@link AbsListView} to scroll
	 * @return {@code true} if more scrolling can be performed
	 */

	public boolean scrollListToTop(AbsListView list) {
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "scrollListToTop("+list+")");
		}

		return scroller.scrollList(list, Scroller.UP, true);
	}

	/**
	 * Scrolls down a ListView matching the specified index.
	 *
	 * @param index the index of the {@link ListView} to scroll. {@code 0} if only one list is available
	 * @return {@code true} if more scrolling can be performed
	 */

	public boolean scrollDownList(int index) {
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "scrollDownList("+index+")");
		}

		return scroller.scrollList(waiter.waitForAndGetView(index, ListView.class), Scroller.DOWN, false);
	}

	/**
	 * Scrolls a ListView matching the specified index to the bottom.
	 *
	 * @param index the index of the {@link ListView} to scroll. {@code 0} if only one list is available
	 * @return {@code true} if more scrolling can be performed
	 */

	public boolean scrollListToBottom(int index) {
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "scrollListToBottom("+index+")");
		}

		return scroller.scrollList(waiter.waitForAndGetView(index, ListView.class), Scroller.DOWN, true);
	}

	/**
	 * Scrolls up a ListView matching the specified index.
	 *
	 * @param index the index of the {@link ListView} to scroll. {@code 0} if only one list is available
	 * @return {@code true} if more scrolling can be performed
	 */

	public boolean scrollUpList(int index) {
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "scrollUpList("+index+")");
		}

		return scroller.scrollList(waiter.waitForAndGetView(index, ListView.class), Scroller.UP, false);
	}

	/**
	 * Scrolls a ListView matching the specified index to the top.
	 *
	 * @param index the index of the {@link ListView} to scroll. {@code 0} if only one list is available
	 * @return {@code true} if more scrolling can be performed
	 */

	public boolean scrollListToTop(int index) {
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "scrollListToTop("+index+")");
		}

		return scroller.scrollList(waiter.waitForAndGetView(index, ListView.class), Scroller.UP, true);
	}

	/**
	 * Scroll the specified AbsListView to the specified line.
	 *
	 * @param absListView the {@link AbsListView} to scroll
	 * @param line the line to scroll to
	 */

	public void scrollListToLine(AbsListView absListView, int line){
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "scrollListToLine("+absListView+", "+line+")");
		}

		scroller.scrollListToLine(absListView, line);
	}

	/**
	 * Scroll a AbsListView matching the specified index to the specified line.
	 *
	 * @param index the index of the {@link AbsListView} to scroll
	 * @param line the line to scroll to
	 */

	public void scrollListToLine(int index, int line){
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "scrollListToLine("+index+", "+line+")");
		}

		scroller.scrollListToLine(waiter.waitForAndGetView(index, AbsListView.class), line);
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
	 * Scrolls down a RecyclerView matching the specified index.
	 *
	 * @param index the index of the RecyclerView to scroll. {@code 0} if only one RecyclerView is available
	 * @return {@code true} if more scrolling can be performed
	 */

	public boolean scrollDownRecyclerView(int index) {
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "scrollDownRecyclerView("+index+")");
		}

		if(!config.shouldScroll) {
			return true;
		}

		View recyclerView = viewFetcher.getRecyclerView(index, Timeout.getSmallTimeout());

		return scroller.scrollView(recyclerView, Scroller.DOWN);

	}

	/**
	 * Scrolls a RecyclerView matching the specified index to the bottom.
	 *
	 * @param index the index of the RecyclerView to scroll. {@code 0} if only one list is available
	 * @return {@code true} if more scrolling can be performed
	 */

	public boolean scrollRecyclerViewToBottom(int index) {
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "scrollRecyclerViewToBottom("+index+")");
		}

		if(!config.shouldScroll) {
			return true;
		}

		View recyclerView = viewFetcher.getRecyclerView(index, Timeout.getSmallTimeout());

		scroller.scrollViewAllTheWay(recyclerView, Scroller.DOWN);

		return false;
	}

	/**
	 * Scrolls up a RecyclerView matching the specified index.
	 *
	 * @param index the index of the RecyclerView to scroll. {@code 0} if only one list is available
	 * @return {@code true} if more scrolling can be performed
	 */

	public boolean scrollUpRecyclerView(int index) {
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "scrollUpRecyclerView("+index+")");
		}

		if(!config.shouldScroll) {
			return true;
		}

		View recyclerView = viewFetcher.getRecyclerView(index, Timeout.getSmallTimeout());

		return scroller.scrollView(recyclerView, Scroller.UP);
	}

	/**
	 * Scrolls a RecyclerView matching the specified index to the top.
	 *
	 * @param index the index of the RecyclerView to scroll. {@code 0} if only one list is available
	 * @return {@code true} if more scrolling can be performed
	 */

	public boolean scrollRecyclerViewToTop(int index) {
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "scrollRecyclerViewToTop("+index+")");
		}

		if(!config.shouldScroll) {
			return false;
		}

		View recyclerView = viewFetcher.getRecyclerView(index, Timeout.getSmallTimeout());

		scroller.scrollViewAllTheWay(recyclerView, Scroller.UP);

		return false;
	}

	/**
	 * Scrolls horizontally.
	 *
	 * @param side the side to scroll; {@link #RIGHT} or {@link #LEFT}
	 * @param scrollPosition the position to scroll to, from 0 to 1 where 1 is all the way. Example is: 0.60
	 * @param stepCount how many move steps to include in the scroll. Less steps results in a faster scroll
	 */

	public void scrollToSide(int side, float scrollPosition, int stepCount) {
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "scrollToSide("+side+", "+scrollPosition+", "+stepCount+")");
		}

		switch (side){
		case RIGHT: scroller.scrollToSide(Scroller.Side.RIGHT, scrollPosition, stepCount); break;
		case LEFT:  scroller.scrollToSide(Scroller.Side.LEFT, scrollPosition, stepCount);  break;
		}
	}

	/**
	 * Scrolls horizontally.
	 *
	 * @param side the side to scroll; {@link #RIGHT} or {@link #LEFT}
	 * @param scrollPosition the position to scroll to, from 0 to 1 where 1 is all the way. Example is: 0.60
	 */

	public void scrollToSide(int side, float scrollPosition) {
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "scrollToSide("+scrollPosition+")");
		}

		scrollToSide(side, scrollPosition, 20);
	}

	/**
	 * Scrolls horizontally.
	 *
	 * @param side the side to scroll; {@link #RIGHT} or {@link #LEFT}
	 */

	public void scrollToSide(int side) {
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "scrollToSide("+side+")");
		}

		scrollToSide(side, 0.75F);
	}

	/**
	 * Scrolls a View horizontally.
	 *
	 * @param view the View to scroll
	 * @param side the side to scroll; {@link #RIGHT} or {@link #LEFT}
	 * @param scrollPosition the position to scroll to, from 0 to 1 where 1 is all the way. Example is: 0.60
	 * @param stepCount how many move steps to include in the scroll. Less steps results in a faster scroll
	 */

	public void scrollViewToSide(View view, int side, float scrollPosition, int stepCount) {
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "scrollViewToSide("+view+", "+side+", "+scrollPosition+", "+stepCount+")");
		}

		waitForView(view);
		sleeper.sleep();
		switch (side){
		case RIGHT: scroller.scrollViewToSide(view, Scroller.Side.RIGHT, scrollPosition, stepCount); break;
		case LEFT:  scroller.scrollViewToSide(view, Scroller.Side.LEFT, scrollPosition, stepCount);  break;
		}
	}

	/**
	 * Scrolls a View horizontally.
	 *
	 * @param view the View to scroll
	 * @param side the side to scroll; {@link #RIGHT} or {@link #LEFT}
	 * @param scrollPosition the position to scroll to, from 0 to 1 where 1 is all the way. Example is: 0.60
	 */

	public void scrollViewToSide(View view, int side, float scrollPosition) {
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "scrollViewToSide("+view+", "+side+", "+scrollPosition+")");
		}

		scrollViewToSide(view, side, scrollPosition, 20);
	}

	/**
	 * Scrolls a View horizontally.
	 *
	 * @param view the View to scroll
	 * @param side the side to scroll; {@link #RIGHT} or {@link #LEFT}
	 */

	public void scrollViewToSide(View view, int side) {
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "scrollViewToSide("+view+", "+side+")");
		}

		scrollViewToSide(view, side, 0.70F);
	}

	/**
	 * Zooms in or out if startPoint1 and startPoint2 are larger or smaller then endPoint1 and endPoint2. Requires API level >= 14.
	 *
	 * @param startPoint1 First "finger" down on the screen
	 * @param startPoint2 Second "finger" down on the screen
	 * @param endPoint1 Corresponding ending point of startPoint1
	 * @param endPoint2 Corresponding ending point of startPoint2
	 */

	public void pinchToZoom(PointF startPoint1, PointF startPoint2, PointF endPoint1, PointF endPoint2)
	{
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "pinchToZoom("+startPoint1+", "+startPoint2+", "+endPoint1+", "+endPoint2+")");
		}

		if (Build.VERSION.SDK_INT < 14){
			throw new RuntimeException("pinchToZoom() requires API level >= 14");
		}
		zoomer.generateZoomGesture(startPoint1, startPoint2, endPoint1, endPoint2);
	}

	/**
	 * Swipes with two fingers in a linear path determined by starting and ending points. Requires API level >= 14.
	 *
	 * @param startPoint1 First "finger" down on the screen
	 * @param startPoint2 Second "finger" down on the screen
	 * @param endPoint1 Corresponding ending point of startPoint1
	 * @param endPoint2 Corresponding ending point of startPoint2
	 */

	public void swipe(PointF startPoint1, PointF startPoint2, PointF endPoint1, PointF endPoint2)
	{
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "swipe("+startPoint1+", "+startPoint2+", "+endPoint1+", "+endPoint2+")");
		}

		if (Build.VERSION.SDK_INT < 14){
			throw new RuntimeException("swipe() requires API level >= 14");
		}
		swiper.generateSwipeGesture(startPoint1, startPoint2, endPoint1,
				endPoint2);
	}

	/**
	 * Draws two semi-circles at the specified centers. Both circles are larger than rotateSmall(). Requires API level >= 14.
	 *
	 * @param center1 Center of semi-circle drawn from [0, Pi]
	 * @param center2 Center of semi-circle drawn from [Pi, 3*Pi]
	 */

	public void rotateLarge(PointF center1, PointF center2)
	{
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "rotateLarge("+center1+", "+center2+")");
		}

		if (Build.VERSION.SDK_INT < 14){
			throw new RuntimeException("rotateLarge(PointF center1, PointF center2) requires API level >= 14");
		}
		rotator.generateRotateGesture(Rotator.LARGE, center1, center2);
	}

	/**
	 * Draws two semi-circles at the specified centers. Both circles are smaller than rotateLarge(). Requires API level >= 14.
	 *
	 * @param center1 Center of semi-circle drawn from [0, Pi]
	 * @param center2 Center of semi-circle drawn from [Pi, 3*Pi]
	 */

	public void rotateSmall(PointF center1, PointF center2)
	{
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "rotateSmall("+center1+", "+center2+")");
		}

		if (Build.VERSION.SDK_INT < 14){
			throw new RuntimeException("rotateSmall(PointF center1, PointF center2) requires API level >= 14");
		}
		rotator.generateRotateGesture(Rotator.SMALL, center1, center2);
	}

	/**
	 * Sets if mobile data should be turned on or off. Requires android.permission.CHANGE_NETWORK_STATE in the AndroidManifest.xml of the application under test.
	 * NOTE: Setting it to false can kill the adb connection.
	 *
	 * @param turnedOn true if mobile data is to be turned on and false if not
	 */

	public void setMobileData(Boolean turnedOn){
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "setMobileData("+turnedOn+")");
		}

		systemUtils.setMobileData(turnedOn);
	}

	/**
	 * Sets if wifi data should be turned on or off. Requires android.permission.CHANGE_WIFI_STATE in the AndroidManifest.xml of the application under test.
	 *
	 *
	 * @param turnedOn true if mobile wifi is to be turned on and false if not
	 */

	public void setWiFiData(Boolean turnedOn){
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "setWiFiData("+turnedOn+")");
		}

		systemUtils.setWiFiData(turnedOn);
	}


	/**
	 * Sets the date in a DatePicker matching the specified index.
	 *
	 * @param index the index of the {@link DatePicker}. {@code 0} if only one is available
	 * @param year the year e.g. 2011
	 * @param monthOfYear the month which starts from zero e.g. 0 for January
	 * @param dayOfMonth the day e.g. 10
	 */

	public void setDatePicker(int index, int year, int monthOfYear, int dayOfMonth) {
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "setDatePicker("+index+", "+year+", "+monthOfYear+", "+dayOfMonth+")");
		}

		setDatePicker(waiter.waitForAndGetView(index, DatePicker.class), year, monthOfYear, dayOfMonth);
	}

	/**
	 * Sets the date in the specified DatePicker.
	 *
	 * @param datePicker the {@link DatePicker} object
	 * @param year the year e.g. 2011
	 * @param monthOfYear the month which starts from zero e.g. 03 for April
	 * @param dayOfMonth the day e.g. 10
	 */

	public void setDatePicker(DatePicker datePicker, int year, int monthOfYear, int dayOfMonth) {
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "setDatePicker("+datePicker+", "+year+", "+monthOfYear+", "+dayOfMonth+")");
		}

		datePicker = (DatePicker) waiter.waitForView(datePicker, Timeout.getSmallTimeout());
		setter.setDatePicker(datePicker, year, monthOfYear, dayOfMonth);
	}

	/**
	 * Sets the time in a TimePicker matching the specified index.
	 *
	 * @param index the index of the {@link TimePicker}. {@code 0} if only one is available
	 * @param hour the hour e.g. 15
	 * @param minute the minute e.g. 30
	 */

	public void setTimePicker(int index, int hour, int minute) {
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "setTimePicker("+index+", "+hour+", "+minute+")");
		}

		setTimePicker(waiter.waitForAndGetView(index, TimePicker.class), hour, minute);
	}

	/**
	 * Sets the time in the specified TimePicker.
	 *
	 * @param timePicker the {@link TimePicker} object
	 * @param hour the hour e.g. 15
	 * @param minute the minute e.g. 30
	 */

	public void setTimePicker(TimePicker timePicker, int hour, int minute) {
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "setTimePicker("+timePicker+", "+hour+", "+minute+")");
		}

		timePicker = (TimePicker) waiter.waitForView(timePicker, Timeout.getSmallTimeout());
		setter.setTimePicker(timePicker, hour, minute);
	}

	/**
	 * Sets the progress of a ProgressBar matching the specified index. Examples of ProgressBars are: {@link android.widget.SeekBar} and {@link android.widget.RatingBar}.
	 *
	 * @param index the index of the {@link ProgressBar}
	 * @param progress the progress to set the {@link ProgressBar}
	 */

	public void setProgressBar(int index, int progress){
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "setProgressBar("+index+", "+progress+")");
		}

		setProgressBar(waiter.waitForAndGetView(index, ProgressBar.class), progress);
	}

	/**
	 * Sets the progress of the specified ProgressBar. Examples of ProgressBars are: {@link android.widget.SeekBar} and {@link android.widget.RatingBar}.
	 *
	 * @param progressBar the {@link ProgressBar}
	 * @param progress the progress to set the {@link ProgressBar}
	 */

	public void setProgressBar(ProgressBar progressBar, int progress){
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "setProgressBar("+progressBar+", "+progress+")");
		}

		progressBar = (ProgressBar) waiter.waitForView(progressBar, Timeout.getSmallTimeout());
		setter.setProgressBar(progressBar, progress);
	}

	/**
	 * Sets the status of the NavigationDrawer. Examples of status are: {@code CLOSED} and {@code OPENED}.
	 *
	 * @param status the status that the NavigationDrawer should be set to
	 */

	public void setNavigationDrawer(final int status){
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "setNavigationDrawer("+status+")");
		}

		setter.setNavigationDrawer(status);
	}

	/**
	 * Sets the status of a SlidingDrawer matching the specified index. Examples of status are: {@code CLOSED} and {@code OPENED}.
	 *
	 * @param index the index of the {@link SlidingDrawer}
	 * @param status the status to set the {@link SlidingDrawer}
	 */

	public void setSlidingDrawer(int index, int status){
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "setSlidingDrawer("+index+", "+status+")");
		}

		setSlidingDrawer(waiter.waitForAndGetView(index, SlidingDrawer.class), status);
	}

	/**
	 * Sets the status of the specified SlidingDrawer. Examples of status are: {@code CLOSED} and {@code OPENED}.
	 *
	 * @param slidingDrawer the {@link SlidingDrawer}
	 * @param status the status to set the {@link SlidingDrawer}
	 */

	@SuppressWarnings("deprecation")
	public void setSlidingDrawer(SlidingDrawer slidingDrawer, int status){
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "setSlidingDrawer("+slidingDrawer+", "+status+")");
		}

		slidingDrawer = (SlidingDrawer) waiter.waitForView(slidingDrawer, Timeout.getSmallTimeout());
		setter.setSlidingDrawer(slidingDrawer, status);
	}

	/**
	 * Enters text in an EditText matching the specified index.
	 *
	 * @param index the index of the {@link EditText}. {@code 0} if only one is available
	 * @param text the text to enter in the {@link EditText} field
	 */

	public void enterText(int index, String text) {
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "enterText("+index+", \""+text+"\")");
		}

		textEnterer.setEditText(waiter.waitForAndGetView(index, EditText.class), text);
	}

	/**
	 * Enters text in the specified EditText.
	 *
	 * @param editText the {@link EditText} to enter text in
	 * @param text the text to enter in the {@link EditText} field
	 */

	public void enterText(EditText editText, String text) {
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "enterText("+editText+", \""+text+"\")");
		}

		editText = (EditText) waiter.waitForView(editText, Timeout.getSmallTimeout());
		textEnterer.setEditText(editText, text);
	}

	/**
	 * Enters text in a WebElement matching the specified By object.
	 *
	 * @param by the By object. Examples are: {@code By.id("id")} and {@code By.name("name")}
	 * @param text the text to enter in the {@link WebElement} field
	 */

	public void enterTextInWebElement(By by, String text){
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "enterTextInWebElement("+by+", \""+text+"\")");
		}

		if(waiter.waitForWebElement(by, 0, Timeout.getSmallTimeout(), false) == null) {
			Log.e(LOG_TAG, "WebElement with " + webUtils.splitNameByUpperCase(by.getClass().getSimpleName()) + ": '" + by.getValue() + "' is not found!");
		}
		webUtils.enterTextIntoWebElement(by, text);
	}

	/**
	 * Types text in an EditText matching the specified index.
	 *
	 * @param index the index of the {@link EditText}. {@code 0} if only one is available
	 * @param text the text to type in the {@link EditText} field
	 */

	public void typeText(int index, String text) {
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "typeText("+index+", \""+text+"\")");
		}

		textEnterer.typeText(waiter.waitForAndGetView(index, EditText.class), text);
	}

	/**
	 * Types text in the specified EditText.
	 *
	 * @param editText the {@link EditText} to type text in
	 * @param text the text to type in the {@link EditText} field
	 */

	public void typeText(EditText editText, String text) {
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "typeText("+editText+", \""+text+"\")");
		}

		editText = (EditText) waiter.waitForView(editText, Timeout.getSmallTimeout());
		textEnterer.typeText(editText, text);
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
	 * Types text in a WebElement matching the specified By object.
	 *
	 * @param by the By object. Examples are: {@code By.id("id")} and {@code By.name("name")}
	 * @param text the text to enter in the {@link WebElement} field
	 */

	public void typeTextInWebElement(By by, String text){
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "typeTextInWebElement("+by+", \""+text+"\")");
		}

		typeTextInWebElement(by, text, 0);
	}

	/**
	 * Types text in a WebElement matching the specified By object.
	 *
	 * @param by the By object. Examples are: {@code By.id("id")} and {@code By.name("name")}
	 * @param text the text to enter in the {@link WebElement} field
	 * @param match if multiple objects match, this determines which one will be typed in
	 */

	public void typeTextInWebElement(By by, String text, int match){
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "typeTextInWebElement("+by+", \""+text+"\", "+match+")");
		}

		clicker.clickOnWebElement(by, match, true, false);
		dialogUtils.hideSoftKeyboard(null, true, true);
		instrumentation.sendStringSync(text);
	}

	/**
	 * Types text in the specified WebElement.
	 *
	 * @param webElement the WebElement to type text in
	 * @param text the text to enter in the {@link WebElement} field
	 */

	public void typeTextInWebElement(WebElement webElement, String text){
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "typeTextInWebElement("+webElement+", \""+text+"\")");
		}

		clickOnWebElement(webElement);
		dialogUtils.hideSoftKeyboard(null, true, true);
		instrumentation.sendStringSync(text);
	}

	/**
	 * Clears the value of an EditText.
	 *
	 * @param index the index of the {@link EditText} to clear. 0 if only one is available
	 */

	public void clearEditText(int index) {
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "clearEditText("+index+")");
		}

		textEnterer.setEditText(waiter.waitForAndGetView(index, EditText.class), "");
	}

	/**
	 * Clears the value of an EditText.
	 *
	 * @param editText the {@link EditText} to clear
	 */

	public void clearEditText(EditText editText) {
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "clearEditText("+editText+")");
		}

		editText = (EditText) waiter.waitForView(editText, Timeout.getSmallTimeout());
		textEnterer.setEditText(editText, "");
	}

	/**
	 * Clears text in a WebElement matching the specified By object.
	 *
	 * @param by the By object. Examples are: {@code By.id("id")} and {@code By.name("name")}
	 */

	public void clearTextInWebElement(By by){
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "clearTextInWebElement("+by+")");
		}

		webUtils.enterTextIntoWebElement(by, "");
	}

	/**
	 * Clicks an ImageView matching the specified index.
	 *
	 * @param index the index of the {@link ImageView} to click. {@code 0} if only one is available
	 */

	public void clickOnImage(int index) {
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "clickOnImage("+index+")");
		}

		clicker.clickOn(ImageView.class, index);
	}

	/**
	 * Returns an EditText matching the specified index.
	 *
	 * @param index the index of the {@link EditText}. {@code 0} if only one is available
	 * @return an {@link EditText} matching the specified index
	 */

	public EditText getEditText(int index) {
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "getEditText("+index+")");
		}

		return getter.getView(EditText.class, index);
	}

	/**
	 * Returns a Button matching the specified index.
	 *
	 * @param index the index of the {@link Button}. {@code 0} if only one is available
	 * @return a {@link Button} matching the specified index
	 */

	public Button getButton(int index) {
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "getButton("+index+")");
		}

		return getter.getView(Button.class, index);
	}

	/**
	 * Returns a TextView matching the specified index.
	 *
	 * @param index the index of the {@link TextView}. {@code 0} if only one is available
	 * @return a {@link TextView} matching the specified index
	 */

	public TextView getText(int index) {
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "getText("+index+")");
		}

		return getter.getView(TextView.class, index);
	}

	/**
	 * Returns an ImageView matching the specified index.
	 *
	 * @param index the index of the {@link ImageView}. {@code 0} if only one is available
	 * @return an {@link ImageView} matching the specified index
	 */

	public ImageView getImage(int index) {
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "getImage("+index+")");
		}

		return getter.getView(ImageView.class, index);
	}

	/**
	 * Returns an ImageButton matching the specified index.
	 *
	 * @param index the index of the {@link ImageButton}. {@code 0} if only one is available
	 * @return the {@link ImageButton} matching the specified index
	 */

	public ImageButton getImageButton(int index) {
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "getImageButton("+index+")");
		}

		return getter.getView(ImageButton.class, index);
	}

	/**
	 * Returns a TextView displaying the specified text.
	 *
	 * @param text the text that is displayed, specified as a regular expression
	 * @return the {@link TextView} displaying the specified text
	 */

	public TextView getText(String text)
	{
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "getText(\""+text+"\")");
		}

		return getter.getView(TextView.class, text, false);
	}

	/**
	 * Returns a TextView displaying the specified text.
	 *
	 * @param text the text that is displayed, specified as a regular expression
	 * @param onlyVisible {@code true} if only visible texts on the screen should be returned
	 * @return the {@link TextView} displaying the specified text
	 */

	public TextView getText(String text, boolean onlyVisible)
	{
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "getText(\""+text+"\", "+onlyVisible+")");
		}

		return getter.getView(TextView.class, text, onlyVisible);
	}

	/**
	 * Returns a Button displaying the specified text.
	 *
	 * @param text the text that is displayed, specified as a regular expression
	 * @return the {@link Button} displaying the specified text
	 */

	public Button getButton(String text)
	{
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "getButton(\""+text+"\")");
		}

		return getter.getView(Button.class, text, false);
	}

	/**
	 * Returns a Button displaying the specified text.
	 *
	 * @param text the text that is displayed, specified as a regular expression
	 * @param onlyVisible {@code true} if only visible buttons on the screen should be returned
	 * @return the {@link Button} displaying the specified text
	 */

	public Button getButton(String text, boolean onlyVisible)
	{
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "getButton(\""+text+"\", "+onlyVisible+")");
		}

		return getter.getView(Button.class, text, onlyVisible);
	}

	/**
	 * Returns an EditText displaying the specified text.
	 *
	 * @param text the text that is displayed, specified as a regular expression
	 * @return the {@link EditText} displaying the specified text
	 */

	public EditText getEditText(String text)
	{
		if(config.commandLogging){
		Log.d(config.commandLoggingTag, "getEditText(\""+text+"\")");
	}

		return getter.getView(EditText.class, text, false);
	}

	/**
	 * Returns an EditText displaying the specified text.
	 *
	 * @param text the text that is displayed, specified as a regular expression
	 * @param onlyVisible {@code true} if only visible EditTexts on the screen should be returned
	 * @return the {@link EditText} displaying the specified text
	 */

	public EditText getEditText(String text, boolean onlyVisible)
	{
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "getEditText(\""+text+"\", "+onlyVisible+")");
		}

		return getter.getView(EditText.class, text, onlyVisible);
	}

	/**
	 * Returns a View matching the specified resource id.
	 *
	 * @param id the R.id of the {@link View} to return
	 * @return a {@link View} matching the specified id
	 */

	public View getView(int id){
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "getView("+id+")");
		}

		return getView(id, 0);
	}

	/**
	 * Returns a View matching the specified resource id and index.
	 *
	 * @param id the R.id of the {@link View} to return
	 * @param index the index of the {@link View}. {@code 0} if only one is available
	 * @return a {@link View} matching the specified id and index
	 */

	public View getView(int id, int index){
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "getView("+id+", "+index+")");
		}

		View viewToReturn = getter.getView(id, index);
		if(viewToReturn == null) {
			String resourceName = "";
			try {
				resourceName = instrumentation.getTargetContext().getResources().getResourceEntryName(id);
			} catch (Exception e) {
				Log.d(config.commandLoggingTag, "unable to get resource entry name for ("+id+")");
			}

			int match = index + 1;
			if(match > 1){
				Log.e(LOG_TAG, match + " Views with id: '" + id + "', resource name: '" + resourceName + "' are not found!");
			}
			else {
				Log.e(LOG_TAG, "View with id: '" + id + "', resource name: '" + resourceName + "' is not found!");
			}
		}
		return viewToReturn;
	}

	/**
	 * Returns a View matching the specified tag.
	 *
	 * @param tag the tag of the {@link View} to return
	 * @return a {@link View} matching the specified id
	 */

	public View getView(Object tag){
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "getView("+tag+")");
		}

		return getView(tag, 0);
	}

	/**
	 * Returns a View matching the specified tag and index.
	 *
	 * @param tag the tag of the {@link View} to return
	 * @param index the index of the {@link View}. {@code 0} if only one is available
	 * @return a {@link View} matching the specified id and index
	 */

	public View getView(Object tag, int index){
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "getView("+tag+", "+index+")");
		}

		View viewToReturn = getter.getView(tag, index);

		if(viewToReturn == null) {
			int match = index + 1;
			if(match > 1){
				Log.e(LOG_TAG, match + " Views with id: '" + tag + "' are not found!");
			}
			else {
				Log.e(LOG_TAG, "View with id: '" + tag + "' is not found!");
			}
		}
		return viewToReturn;
	}

	/**
	 * Returns a View matching the specified resource id.
	 *
	 * @param id the id of the {@link View} to return
	 * @return a {@link View} matching the specified id
	 */

	public View getView(String id){
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "getView(\""+id+"\")");
		}

		return getView(id, 0);
	}

	private void clearClickeds() {
		clicker.clickeds.clear();
	}

	private void putClickeds(int id, int[] xy) {
		clicker.clickeds.put(id, xy);
	}



	/**
	 * Returns a View matching the specified resource id and index.
	 *
	 * @param id the id of the {@link View} to return
	 * @param index the index of the {@link View}. {@code 0} if only one is available
	 * @return a {@link View} matching the specified id and index
	 */

	public View getView(String id, int index){
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "getView(\""+id+"\", "+index+")");
		}

		View viewToReturn = getter.getView(id, index);

		if(viewToReturn == null) {
			int match = index + 1;
			if(match > 1){
				Log.e(LOG_TAG, match + " Views with id: '" + id + "' are not found!");
			}
			else {
				Log.e(LOG_TAG, "View with id: '" + id + "' is not found!");
			}
		}
		return viewToReturn;
	}

	/**
	 * Returns a View matching the specified class and index.
	 *
	 * @param viewClass the class of the requested view
	 * @param index the index of the {@link View}. {@code 0} if only one is available
	 * @return a {@link View} matching the specified class and index
	 */

	public <T extends View> T getView(Class<T> viewClass, int index){
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "getView("+viewClass+", "+index+")");
		}

		return waiter.waitForAndGetView(index, viewClass);
	}

	/**
	 * Returns a WebElement matching the specified By object and index.
	 *
	 * @param by the By object. Examples are: {@code By.id("id")} and {@code By.name("name")}
	 * @param index the index of the {@link WebElement}. {@code 0} if only one is available
	 * @return a {@link WebElement} matching the specified index
	 */

	public WebElement getWebElement(By by, int index){
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "getWebElement("+by+", "+index+")");
		}

		int match = index + 1;
		WebElement webElement = waiter.waitForWebElement(by, match, Timeout.getSmallTimeout(), true);

		if(webElement == null) {
			if(match > 1){
				Log.e(LOG_TAG, match + " WebElements with " + webUtils.splitNameByUpperCase(by.getClass().getSimpleName()) + ": '" + by.getValue() + "' are not found!");
			}
			else {
				Log.e(LOG_TAG, "WebElement with " + webUtils.splitNameByUpperCase(by.getClass().getSimpleName()) + ": '" + by.getValue() + "' is not found!");
			}
		}
		return webElement;
	}

	/**
	 * Returns the current web page URL.
	 *
	 * @return the current web page URL
	 */

	public String getWebUrl() {
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "getWebUrl()");
		}

		final WebView webView = waiter.waitForAndGetView(0, WebView.class);

		if(webView == null) {
			Log.w(config.commandLoggingTag, "WebView is not found!");
			return "";
		}

		instrumentation.runOnMainSync(new Runnable() {
			public void run() {
				webUrl = webView.getUrl();
			}
		});
		return webUrl;
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
	 * Return a list of the tasks that are currently running.
	 * @param context context
	 * @return ComponentName Return only the top one.
	 */
	public ComponentName getRunningTask(Context context) {
		ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		return am.getRunningTasks(1).get(0).topActivity;
	}

	/**
	 * HandleEditText, e.g: type text or type number.
	 * @param view
	 */
    public void handleEditText(View view){
        if (view instanceof EditText){
            EditText editText = (EditText)view;
            typeText(editText);
        }
    }

	/**
	 * HandleWeb, click everyone WebElement.
	 * @param activity current activity
	 * @param params start activity params
	 * @throws ClassNotFoundException
	 */

    public void handleWeb(String activity, Map<String, Object> params) throws ClassNotFoundException{
        String url = waitForWebUrl(config.sleepDuration * 20);
        Log.i(LOG_TAG, url);
        if (!TextUtils.isEmpty(url)) {
            ArrayList<WebElement> webElements = getCurrentWebElements();
            for (WebElement element: webElements) {
                if (element.getTagName().toUpperCase().contains("INPUT")) {
                    typeTextInWebElement(By.tagName(element.getTagName()), RandomUtils.getRandomText(10));
                    sleep(config.sleepDuration * 2);
                } else {
					takeScreenshot(activity, element);
					clickOnWebElement(element);
					sleep(config.sleepDuration * 2);
				}
                String tmp = waitForWebUrl(config.sleepDuration * 20);
                if (!tmp.equals(url)) {
					goBack();
                    if (!getCurrentActivity().toString().contains(activity)) {
                        startActivity(context, params, activity);
                        sleep(config.sleepDuration);
						if (TextUtils.isEmpty(waitForWebUrl(config.sleepDuration * 10))) break;
                    }
                }
            }
        } else
            Log.w(LOG_TAG, activity + " WebUrl is null, " + "timeout.");
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
	 * Returns an ArrayList of the Views currently displayed in the focused Activity or Dialog.
	 *
	 * @return an {@code ArrayList} of the {@link View} objects currently displayed in the
	 * focused window
	 */

	public ArrayList<View> getCurrentViews() {
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "getCurrentViews()");
		}

		return viewFetcher.getViews(null, true);
	}

	/**
	 * Returns an ArrayList of Views matching the specified class located in the focused Activity or Dialog.
	 *
	 * @param classToFilterBy return all instances of this class. Examples are: {@code Button.class} or {@code ListView.class}
	 * @return an {@code ArrayList} of {@code View}s matching the specified {@code Class} located in the current {@code Activity}
	 */

	public <T extends View> ArrayList<T> getCurrentViews(Class<T> classToFilterBy) {
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "getCurrentViews("+classToFilterBy+")");
		}

		return viewFetcher.getCurrentViews(classToFilterBy, true);
	}

	/**
	 * Returns an ArrayList of Views matching the specified class located in the focused Activity or Dialog.
	 *
	 * @param classToFilterBy return all instances of this class. Examples are: {@code Button.class} or {@code ListView.class}
	 * @param includeSubclasses include instances of the subclasses in the {@code ArrayList} that will be returned
	 * @return an {@code ArrayList} of {@code View}s matching the specified {@code Class} located in the current {@code Activity}
	 */

	public <T extends View> ArrayList<T> getCurrentViews(Class<T> classToFilterBy, boolean includeSubclasses) {
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "getCurrentViews("+classToFilterBy+", "+includeSubclasses+")");
		}

		return viewFetcher.getCurrentViews(classToFilterBy, includeSubclasses);
	}

	/**
	 * Returns an ArrayList of Views matching the specified class located under the specified parent.
	 *
	 * @param classToFilterBy return all instances of this class. Examples are: {@code Button.class} or {@code ListView.class}
	 * @param parent the parent {@code View} for where to start the traversal
	 * @return an {@code ArrayList} of {@code View}s matching the specified {@code Class} located under the specified {@code parent}
	 */

	public <T extends View> ArrayList<T> getCurrentViews(Class<T> classToFilterBy, View parent) {
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "getCurrentViews("+classToFilterBy+", "+parent+")");
		}

		return viewFetcher.getCurrentViews(classToFilterBy, true, parent);
	}

	/**
	 * Returns an ArrayList of Views matching the specified class located under the specified parent.
	 *
	 * @param classToFilterBy return all instances of this class. Examples are: {@code Button.class} or {@code ListView.class}
	 * @param includeSubclasses include instances of subclasses in the {@code ArrayList} that will be returned
	 * @param parent the parent {@code View} for where to start the traversal
	 * @return an {@code ArrayList} of {@code View}s matching the specified {@code Class} located under the specified {@code parent}
	 */
	public <T extends View> ArrayList<T> getCurrentViews(Class<T> classToFilterBy, boolean includeSubclasses, View parent) {
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "getCurrentViews("+classToFilterBy+", "+includeSubclasses+", "+parent+")");
		}

		return viewFetcher.getCurrentViews(classToFilterBy, includeSubclasses, parent);
	}

	/**
	 * Returns an ArrayList of all the WebElements displayed in the active WebView.
	 *
	 * @return an {@code ArrayList} of all the {@link WebElement} objects currently displayed in the active WebView
	 */

	public ArrayList<WebElement> getWebElements(){
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "getWebElements()");
		}

		return webUtils.getWebElements(false);
	}

	/**
	 * Returns an ArrayList of all the WebElements displayed in the active WebView matching the specified By object.
	 *
	 * @param by the By object. Examples are: {@code By.id("id")} and {@code By.name("name")}
	 * @return an {@code ArrayList} of all the {@link WebElement} objects displayed in the active WebView
	 */

	public ArrayList<WebElement> getWebElements(By by){
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "getWebElements("+by+")");
		}

		return webUtils.getWebElements(by, false);
	}

	/**
	 * Returns an ArrayList of the currently displayed WebElements in the active WebView.
	 *
	 * @return an {@code ArrayList} of the {@link WebElement} objects displayed in the active WebView
	 */

	public ArrayList<WebElement> getCurrentWebElements(){
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "getCurrentWebElements()");
		}

		return webUtils.getWebElements(true);
	}

	/**
	 * Returns an ArrayList of the currently displayed WebElements in the active WebView matching the specified By object.
	 *
	 * @param by the By object. Examples are: {@code By.id("id")} and {@code By.name("name")}
	 * @return an {@code ArrayList} of the {@link WebElement} objects currently displayed in the active WebView
	 */

	public ArrayList<WebElement> getCurrentWebElements(By by){
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "getCurrentWebElements("+by+")");
		}

		return webUtils.getWebElements(by, true);
	}

	/**
	 * Checks if a RadioButton matching the specified index is checked.
	 *
	 * @param index of the {@link RadioButton} to check. {@code 0} if only one is available
	 * @return {@code true} if {@link RadioButton} is checked and {@code false} if it is not checked
	 */

	public boolean isRadioButtonChecked(int index)
	{
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "isRadioButtonChecked("+index+")");
		}

		return checker.isButtonChecked(RadioButton.class, index);
	}

	/**
	 * Checks if a RadioButton displaying the specified text is checked.
	 *
	 * @param text the text that the {@link RadioButton} displays, specified as a regular expression
	 * @return {@code true} if a {@link RadioButton} matching the specified text is checked and {@code false} if it is not checked
	 */

	public boolean isRadioButtonChecked(String text)
	{
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "isRadioButtonChecked(\""+text+"\")");
		}

		return checker.isButtonChecked(RadioButton.class, text);
	}

	/**
	 * Checks if a CheckBox matching the specified index is checked.
	 *
	 * @param index of the {@link CheckBox} to check. {@code 0} if only one is available
	 * @return {@code true} if {@link CheckBox} is checked and {@code false} if it is not checked
	 */

	public boolean isCheckBoxChecked(int index)
	{
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "isCheckBoxChecked("+index+")");
		}

		return checker.isButtonChecked(CheckBox.class, index);
	}

	/**
	 * Checks if a ToggleButton displaying the specified text is checked.
	 *
	 * @param text the text that the {@link ToggleButton} displays, specified as a regular expression
	 * @return {@code true} if a {@link ToggleButton} matching the specified text is checked and {@code false} if it is not checked
	 */

	public boolean isToggleButtonChecked(String text)
	{
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "isToggleButtonChecked(\""+text+"\")");
		}

		return checker.isButtonChecked(ToggleButton.class, text);
	}

	/**
	 * Checks if a ToggleButton matching the specified index is checked.
	 *
	 * @param index of the {@link ToggleButton} to check. {@code 0} if only one is available
	 * @return {@code true} if {@link ToggleButton} is checked and {@code false} if it is not checked
	 */

	public boolean isToggleButtonChecked(int index)
	{
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "isToggleButtonChecked("+index+")");
		}

		return checker.isButtonChecked(ToggleButton.class, index);
	}

	/**
	 * Checks if a CheckBox displaying the specified text is checked.
	 *
	 * @param text the text that the {@link CheckBox} displays, specified as a regular expression
	 * @return {@code true} if a {@link CheckBox} displaying the specified text is checked and {@code false} if it is not checked
	 */

	public boolean isCheckBoxChecked(String text)
	{
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "isCheckBoxChecked(\""+text+"\")");
		}

		return checker.isButtonChecked(CheckBox.class, text);
	}

	/**
	 * Checks if the specified text is checked.
	 *
	 * @param text the text that the {@link CheckedTextView} or {@link CompoundButton} objects display, specified as a regular expression
	 * @return {@code true} if the specified text is checked and {@code false} if it is not checked
	 */

	@SuppressWarnings("unchecked")
	public boolean isTextChecked(String text){
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "isTextChecked(\""+text+"\")");
		}

		waiter.waitForViews(false, CheckedTextView.class, CompoundButton.class);

		if(viewFetcher.getCurrentViews(CheckedTextView.class, true).size() > 0 && checker.isCheckedTextChecked(text))
			return true;

		if(viewFetcher.getCurrentViews(CompoundButton.class, true).size() > 0 && checker.isButtonChecked(CompoundButton.class, text))
			return true;

		return false;
	}

	/**
	 * Checks if the specified text is selected in any Spinner located in the current screen.
	 *
	 * @param text the text that is expected to be selected, specified as a regular expression
	 * @return {@code true} if the specified text is selected in any {@link Spinner} and false if it is not
	 */

	public boolean isSpinnerTextSelected(String text)
	{
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "isSpinnerTextSelected(\""+text+"\")");
		}

		return checker.isSpinnerTextSelected(text);
	}

	/**
	 * Checks if the specified text is selected in a Spinner matching the specified index.
	 *
	 * @param index the index of the spinner to check. {@code 0} if only one spinner is available
	 * @param text the text that is expected to be selected, specified as a regular expression
	 * @return {@code true} if the specified text is selected in the specified {@link Spinner} and false if it is not
	 */

	public boolean isSpinnerTextSelected(int index, String text)
	{
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "isSpinnerTextSelected("+index+",\""+text+"\")");
		}

		return checker.isSpinnerTextSelected(index, text);
	}

	/**
	 * Hides the soft keyboard.
	 */

	public void hideSoftKeyboard() {
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "hideSoftKeyboard()");
		}

		dialogUtils.hideSoftKeyboard(null, true, false);
	}

	/**
	 * Unlocks the lock screen.
	 */

	public void unlockScreen(){
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "unlockScreen()");
		}

		final Activity activity = activityUtils.getCurrentActivity(false);
		instrumentation.runOnMainSync(new Runnable() {
			@Override
			public void run() {
				if(activity != null){
					activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
				}
			}
		});
	}

	/**
	 * Sends a key: Right, Left, Up, Down, Enter, Menu or Delete.
	 *
	 * @param key the key to be sent. Use {@code }{@link #RIGHT}, {@link #LEFT}, {@link #UP}, {@link #DOWN},
	 * {@link #ENTER}, {@link #MENU}, {@link #DELETE}
	 */

	public void sendKey(int key)
	{
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "sendKey("+key+")");
		}

		sender.sendKeyCode(key);
	}

	/**
	 * Returns to an Activity matching the specified name.
	 *
	 * @param name the name of the {@link Activity} to return to. Example is: {@code "MyActivity"}
	 */

	public void goBackToActivity(String name) {
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "goBackToActivity(\""+name+"\")");
		}

		activityUtils.goBackToActivity(name);
	}

	private void goBackToActivitySync(String name) {
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				goBackToActivity(name);
			}
		});
		thread.start();
		sleep(config.sleepDuration);
		thread.interrupt();
	}

	/**
	 * Finish the activity.
	 * @param activity
	 */
	public void finish(String activity) {
		if (config.homeActivity.contains(activity))return;
		ArrayList<Activity> activitiesOpened = activityUtils.getAllOpenedActivities();
		for (Activity activity2 : activitiesOpened) {
			if (activity.contains(activity2.getClass().getSimpleName())) {
				if(config.commandLogging){
					Log.d(config.commandLoggingTag, "finish("+activity2.getClass().getSimpleName()+")");
				}
				//Main thread finish activity.
				Handler handler = new Handler(Looper.getMainLooper());
				handler.post(new Runnable() {
					@Override
					public void run() {
						activity2.finish();
					}
				});
			}
		}
		clearClickeds();
	}

	/**
	 * Waits for an Activity matching the specified name. Default timeout is 20 seconds.
	 *
	 * @param name the name of the {@code Activity} to wait for. Example is: {@code "MyActivity"}
	 * @return {@code true} if {@code Activity} appears before the timeout and {@code false} if it does not
	 */

	public boolean waitForActivity(String name){
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "waitForActivity(\""+name+"\")");
		}

		return waiter.waitForActivity(name, Timeout.getLargeTimeout());
	}

	/**
	 * Waits for an Activity matching the specified name.
	 *
	 * @param name the name of the {@link Activity} to wait for. Example is: {@code "MyActivity"}
	 * @param timeout the amount of time in milliseconds to wait
	 * @return {@code true} if {@link Activity} appears before the timeout and {@code false} if it does not
	 */

	public boolean waitForActivity(String name, int timeout)
	{
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "waitForActivity(\""+name+"\", "+timeout+")");
		}

		return waiter.waitForActivity(name, timeout);
	}

	/**
	 * Waits for an Activity matching the specified class. Default timeout is 20 seconds.
	 *
	 * @param activityClass the class of the {@code Activity} to wait for. Example is: {@code MyActivity.class}
	 * @return {@code true} if {@code Activity} appears before the timeout and {@code false} if it does not
	 */

	public boolean waitForActivity(Class<? extends Activity> activityClass){
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "waitForActivity("+activityClass+")");
		}

		return waiter.waitForActivity(activityClass, Timeout.getLargeTimeout());
	}

	/**
	 * Waits for an Activity matching the specified class.
	 *
	 * @param activityClass the class of the {@code Activity} to wait for. Example is: {@code MyActivity.class}
	 * @param timeout the amount of time in milliseconds to wait
	 * @return {@code true} if {@link Activity} appears before the timeout and {@code false} if it does not
	 */

	public boolean waitForActivity(Class<? extends Activity> activityClass, int timeout)
	{
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "waitForActivity("+activityClass+", "+timeout+")");
		}

		return waiter.waitForActivity(activityClass, timeout);
	}


	/**
	 * Wait for the activity stack to be empty.
	 *
	 * @param timeout the amount of time in milliseconds to wait
	 * @return {@code true} if activity stack is empty before the timeout and {@code false} if it is not
	 */

	public boolean waitForEmptyActivityStack(int timeout)
	{
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "waitForEmptyActivityStack("+timeout+")");
		}

		return waiter.waitForCondition(
				new Condition(){
					@Override
					public boolean isSatisfied() {
						return activityUtils.isActivityStackEmpty();
					}
				}, timeout);
	}

	/**
	 * Waits for a Fragment matching the specified tag. Default timeout is 20 seconds.
	 *
	 * @param tag the name of the tag
	 * @return {@code true} if fragment appears and {@code false} if it does not appear before the timeout
	 */

	public boolean waitForFragmentByTag(String tag){
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "waitForFragmentByTag(\""+tag+"\")");
		}

		return waiter.waitForFragment(tag, 0, Timeout.getLargeTimeout());
	}

	/**
	 * Waits for a Fragment matching the specified tag.
	 *
	 * @param tag the name of the tag
	 * @param timeout the amount of time in milliseconds to wait
	 * @return {@code true} if fragment appears and {@code false} if it does not appear before the timeout
	 */

	public boolean waitForFragmentByTag(String tag, int timeout){
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "waitForFragmentByTag(\""+tag+"\", "+timeout+")");
		}

		return waiter.waitForFragment(tag, 0, timeout);
	}

	/**
	 * Waits for a Fragment matching the specified resource id. Default timeout is 20 seconds.
	 *
	 * @param id the R.id of the fragment
	 * @return {@code true} if fragment appears and {@code false} if it does not appear before the timeout
	 */

	public boolean waitForFragmentById(int id){
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "waitForFragmentById("+id+")");
		}

		return waiter.waitForFragment(null, id, Timeout.getLargeTimeout());
	}

	/**
	 * Waits for a Fragment matching the specified resource id.
	 *
	 * @param id the R.id of the fragment
	 * @param timeout the amount of time in milliseconds to wait
	 * @return {@code true} if fragment appears and {@code false} if it does not appear before the timeout
	 */

	public boolean waitForFragmentById(int id, int timeout){
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "waitForFragmentById("+id+", "+timeout+")");
		}

		return waiter.waitForFragment(null, id, timeout);
	}

	/**
	 * Waits for the specified log message to appear. Default timeout is 20 seconds.
	 * Requires read logs permission (android.permission.READ_LOGS) in AndroidManifest.xml of the application under test.
	 *
	 * @param logMessage the log message to wait for
	 * @return {@code true} if log message appears and {@code false} if it does not appear before the timeout
	 *
	 * @see #clearLog()
	 */

	public boolean waitForLogMessage(String logMessage){
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "waitForLogMessage(\""+logMessage+"\")");
		}

		return waiter.waitForLogMessage(logMessage, Timeout.getLargeTimeout());
	}

	/**
	 * Waits for the specified log message to appear.
	 * Requires read logs permission (android.permission.READ_LOGS) in AndroidManifest.xml of the application under test.
	 *
	 * @param logMessage the log message to wait for
	 * @param timeout the amount of time in milliseconds to wait
	 * @return {@code true} if log message appears and {@code false} if it does not appear before the timeout
	 *
	 * @see #clearLog()
	 */

	public boolean waitForLogMessage(String logMessage, int timeout){
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "waitForLogMessage(\""+logMessage+"\", "+timeout+")");
		}

		return waiter.waitForLogMessage(logMessage, timeout);
	}

	/**
	 * Clears the log.
	 */

	public void clearLog(){
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "clearLog()");
		}

		waiter.clearLog();
	}

	/**
	 * Returns a localized String matching the specified resource id.
	 *
	 * @param id the R.id of the String
	 * @return the localized String
	 */

	public String getString(int id)
	{
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "getString("+id+")");
		}

		return getter.getString(id);
	}

	/**
	 * Returns a localized String matching the specified resource id.
	 *
	 * @param id the id of the String
	 * @return the localized String
	 */

	public String getString(String id)
	{
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "getString(\""+id+"\")");
		}

		return getter.getString(id);
	}

	/**
	 * Robotium will sleep for the specified time.
	 *
	 * @param time the time in milliseconds that Robotium should sleep
	 */

	public void sleep(int time)
	{
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "sleep("+time+")");
		}

		sleeper.sleep(time);
	}

	/**
	 *
	 * Finalizes the Solo object and removes the ActivityMonitor.
	 *
	 * @see #finishOpenedActivities() finishOpenedActivities() to close the activities that have been active
	 */

	public void finalize() throws Throwable {
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "finalize()");
		}

		activityUtils.finalize();
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
	 * Takes a screenshot and saves it in the {@link Config} objects save path (default set to: /sdcard/Robotium-Screenshots/).
	 * Requires write permission (android.permission.WRITE_EXTERNAL_STORAGE) in AndroidManifest.xml of the application under test.
	 */

	public void takeScreenshot(){
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "takeScreenshot()");
		}

		takeScreenshot(null);
	}

	/**
	 * Takes a screenshot and saves it with the specified name in the {@link Config} objects save path (default set to: /sdcard/Robotium-Screenshots/).
	 * Requires write permission (android.permission.WRITE_EXTERNAL_STORAGE) in AndroidManifest.xml of the application under test.
	 *
	 * @param name the name to give the screenshot
	 */

	public void takeScreenshot(String name){
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "takeScreenshot(\""+name+"\")");
		}

		takeScreenshot(name, 100);
	}

	/**
	 * Takes a screenshot and saves the image with the specified name in the {@link Config} objects save path (default set to: /sdcard/Robotium-Screenshots/).
	 * Requires write permission (android.permission.WRITE_EXTERNAL_STORAGE) in AndroidManifest.xml of the application under test.
	 *
	 * @param name the name to give the screenshot
	 * @param quality the compression rate. From 0 (compress for lowest size) to 100 (compress for maximum quality)
	 */

	public void takeScreenshot(String name, int quality){
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "takeScreenshot(\""+name+"\", "+quality+")");
		}

		screenshotTaker.takeScreenshot(name, quality);
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
	 *
	 * @param name name the name to give the screenshot
	 */
	public void takeScreenshot(String name, WebElement element){
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
	 * Takes a screenshot sequence and saves the images with the specified name prefix in the {@link Config} objects save path (default set to: /sdcard/Robotium-Screenshots/).
	 *
	 * The name prefix is appended with "_" + sequence_number for each image in the sequence,
	 * where numbering starts at 0.
	 *
	 * Requires write permission (android.permission.WRITE_EXTERNAL_STORAGE) in AndroidManifest.xml of the application under test.
	 *
	 * At present multiple simultaneous screenshot sequences are not supported.
	 * This method will throw an exception if stopScreenshotSequence() has not been
	 * called to finish any prior sequences.
	 * Calling this method is equivalend to calling startScreenshotSequence(name, 80, 400, 100);
	 *
	 * @param name the name prefix to give the screenshot
	 */

	public void startScreenshotSequence(String name) {
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "startScreenshotSequence(\""+name+"\")");
		}

		startScreenshotSequence(name,
				80, // quality
				400, // 400 ms frame delay
				100); // max frames
	}

	/**
	 * Takes a screenshot sequence and saves the images with the specified name prefix in the {@link Config} objects save path (default set to: /sdcard/Robotium-Screenshots/).
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
	 */

	public void startScreenshotSequence(String name, int quality, int frameDelay, int maxFrames) {
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "startScreenshotSequence(\""+name+"\", "+quality+", "+frameDelay+", "+maxFrames+")");
		}

		screenshotTaker.startScreenshotSequence(name, quality, frameDelay, maxFrames);
	}

	/**
	 * Causes a screenshot sequence to end.
	 *
	 * If this method is not called to end a sequence and a prior sequence is still in
	 * progress, startScreenshotSequence() will throw an exception.
	 */

	public void stopScreenshotSequence() {
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "stopScreenshotSequence()");
		}

		screenshotTaker.stopScreenshotSequence();
	}

    /**
     * Start the Monitor application
     */
    public void startApplication(ComponentName cn) {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.putExtra("package", cn.getPackageName());
        intent.setComponent(cn);
        Log.i(LOG_TAG, "start monitor application: " + cn.getPackageName() + "/" + cn.getClassName());
        context.startActivity(intent);
    }

    /**
     * Start the Monitor service
     */
    public void startService(ComponentName cn) {
        Intent intent = new Intent();
        intent.setComponent(cn);
        intent.putExtra("package", cn.getPackageName());
        Log.i(LOG_TAG, "start monitor service: " + cn.getPackageName() + "/" + cn.getClassName());
        context.startService(intent);
    }

    /**
     * Start target activity
	 * Returns if retry >= 3, abandon start the target activity.
     */
    private boolean startActivity(Context context, Map<String, Object> params, String activity) throws ClassNotFoundException {
        Class<?> target;
		target = Class.forName(activity);
        context.startActivity(IntentParser.getIntent(context, target, params));
		sleep(config.sleepDuration);
		hideSoftKeyboard();
		Log.i(LOG_TAG, "start activity: " + activity);
		// In some cases, the target activity will be overwritten and will need to be retried.
		int retry = 0;
		while (retry < 3){
			String[] strings = activity.split("\\.");
			boolean result = waitForActivity(strings[strings.length - 1], 2000);
			if (!result){
				finish(activity);
				context.startActivity(IntentParser.getIntent(context, target, params));
				sleep(config.sleepDuration);
				hideSoftKeyboard();
				Log.i(LOG_TAG, "restart activity: " + activity);
				retry++;
			}else break;
		}

		currentActivity = getCurrentActivity();
		if (currentActivity == null) {
			throw new NullPointerException("currentActivity is null.");
		}

		// Abandon start the target activity, failure to start the target activity may be due to protocol jumps.
		if (retry >= 3) Log.e(LOG_TAG, "Abandon start " + activity + " , failure to start the target activity may be due to protocol jumps.");
		return retry >= 3;
	}

	/**
	 * Reads the json file, generates the parameters required for the iteration, and ignores part of the activity.
	 * @throws Exception
	 */
	private void handleParams() throws Exception {
		String json = FileUtils.readJson();
		Log.i(LOG_TAG, "json: " + json);
		ArrayList<Params> arrayList = filterActivities(JsonParser.fromJson(json), activityUtils.getAllActivities(context));
		Log.d(LOG_TAG, "Params size: " + arrayList.size());
		for (Params params : arrayList) {
			boolean isIgnore = false;
			Log.d(LOG_TAG, "Activity: " + params.name);
			if(config.mode == Config.Mode.REPTILE && params.name.contains(config.homeActivity))continue;
			for (String name: config.ignoreActivities) {
				if (params.name.contains(name)) {
					isIgnore = true;
					break;
				}
			}
			Log.d(LOG_TAG, "Ignore: " + isIgnore);
			if (!isIgnore) {
				Map<String, Object> hashMap = new HashMap<String, Object>();
				for (Param param : params.params) {
					Log.d(LOG_TAG, param.key + " " + param.value);
					hashMap.put(param.key.split("\\|")[1], IntentParser.parseType(param.value));
				}
				iteration(params.name, hashMap.isEmpty() ? null : hashMap,
						config.mode != Config.Mode.FAST && params.iteration, params.web);
			}
		}
	}

	/**
	 * If the activity not in target application, remove it.
	 */
	private ArrayList<Params> filterActivities(ArrayList<Params> arrayList, ActivityInfo[] activities) {
		ArrayList<Params> filterActivities = new ArrayList<>();
		for (Params params : arrayList) {
			for (ActivityInfo activity: activities){
				if (activity.name.contains(params.name)) filterActivities.add(params);
			}
		}
		return filterActivities;
	}

	/**
	 * Handle Login.
	 * By reading the configuration file to complete the operation.
	 */
	private void handleLogin(){
		String[] strings = config.loginActivity.split("\\.");
		waitForActivity(strings[strings.length - 1]);
		sleep(config.sleepDuration);
		typeText(0, config.loginAccount);
		typeText(1, config.loginPassword);
		clickOnView(getView(config.loginId));
		strings = config.homeActivity.split("\\.");
		boolean result = waitForActivity(strings[strings.length - 1]);
		if (!result) {
			takeScreenshot();
			try {
				startActivity(context, null, config.loginActivity);
				handleLogin();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
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
			iterationNode(null, "", null);
			isSuccess = waitForActivity(homeActivity, 3000);
			// 处理完弹框还等待不到主页，则可能是进入了引导页面
			while (!isSuccess){
				scrollLeft();
				iterationNode(null, "", null);
				isSuccess = waitForActivity(homeActivity, 2000);
			}
		}
		isSuccess = waitForActivity(homeActivity, 5000);
		// 可能存在类似新手引导的界面覆盖了主页，finish
		if (!isSuccess) finish(getCurrentActivity().getComponentName().getClassName());
        startActivity(context, null, config.loginActivity);
		handleLogin();
	}

	/**
	 * Fast mode or Normal mode.
	 * @throws Exception
	 */
	private void FastOrNormalMode() throws Exception{
		String[] strings = config.homeActivity.split("\\.");
		waitForActivity(strings[strings.length - 1]);
		sleep(config.sleepDuration * 6);
		handleParams();
	}

	/**
	 * Reptile mode.
	 * @throws Exception
	 */
	private void reptileMode() throws Exception{
		String[] strings = config.homeActivity.split("\\.");
		waitForActivity(strings[strings.length - 1]);
		sleep(config.sleepDuration * 10);
		iteration(config.homeActivity, null, true, false);
		JsonParser.updateJson();
		while (FileUtils.existsJson()) {
			handleParams();
			JsonParser.updateJson();
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

	private void iterationNode(AccessibilityNodeInfo nodeInfo, String activity, Map<String, Object> params) {
		try{
			if (nodeInfo == null) nodeInfo = uiAutomation.getRootInActiveWindow();
			if (nodeInfo != null) {
				for (int i = 0; i < nodeInfo.getChildCount(); i ++){
					AccessibilityNodeInfo node = nodeInfo.getChild(i);
					if (node == null) node = uiAutomation.getRootInActiveWindow();
					performClick(node, activity, params);
				}
			}
		}catch (NullPointerException ignored){}catch (StackOverflowError e){
			e.printStackTrace();
		}
	}

	/**
	 * If the handleJump method fails to process the jump, use the adb shell command to start the activity.
	 * @param node AccessibilityNodeInfo
	 */
	private void shellCommandStartActivity(AccessibilityNodeInfo node) {
		if (node != null && !config.PACKAGE.contains(node.getPackageName().toString())){
			String launcherActivity = activityUtils.getLauncherActivity(context);
			if (launcherActivity != null) {
				String command = "am start -n " + config.PACKAGE + "/" + launcherActivity;
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
					Log.i(LOG_TAG, "adb shell " + command);
					uiAutomation.executeShellCommand(command);
				}
				sleep(config.sleepDuration * 6);
			}
		}
	}

	private boolean viewInIgnoreViews(String string){
		if (string == null) return false;
		for (String s: config.ignoreViews) {
			if (s.contains(string)) return true;
		}
		return false;
	}

	private void performClick(AccessibilityNodeInfo node, String activity, Map<String, Object> params) {
		if (node != null && node.isClickable()) {
			String string = getBoundsInScreen(node);
			boolean clicked = clickeds.contains(string);
			boolean status = viewInIgnoreViews(node.getViewIdResourceName());
			if (!clicked && !status) {
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && node.canOpenPopup()) iterationNode(null, activity, params);
				if (node.getClassName().toString().contains("android.widget.EditText")) {
					String[] id = node.getViewIdResourceName().split("/");
					handleEditText(getView(id[id.length - 1]));
					sleep(config.sleepMiniDuration);
				}
				if (config.iterationScreenShots) {
					screenshotTaker.takeScreenshotForUiAutomation(string, activity);
				}
				Log.i(LOG_TAG, node.toString());
				node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
				clickeds.add(string);
				sleep(config.sleepDuration);
				try {
					handleJump(currentActivity, activity, params);
					shellCommandStartActivity(node);
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

	/**
	 * Start the iteration.
	 * This method is iterating the total switch, only call it.
	 * @throws Exception
	 */
	public void startIteration() throws Exception{
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
	 * Iteration method.
	 * @param activity the start activity
	 * @param params start activity params
	 * @param iteration if true, click everyone view.
	 * @param isWeb if true, the activity is webView.
	 * @throws Exception
	 */
	public void iteration(String activity, Map<String, Object> params, boolean iteration, boolean isWeb) throws Exception {
		boolean result = startActivity(context, params, activity);
		// Abandon start the target activity, failure to start the target activity may be due to protocol jumps.
		if (result) return;
		sleep(config.sleepDuration * 4);
		Log.i(LOG_TAG, "current activity: " + currentActivity);
		FileUtils.writeActivity(TimeUtils.getDate() + " starting iteration: " + activity);
		if (config.activityScreenShots) {
			String[] s = activity.split("\\.");
			takeScreenshot("Activities/" + s[s.length - 1]);
		}
		if (iteration) {
			if (isWeb) {
				handleWeb(activity, params);
			} else {
				if (Build.VERSION.SDK_INT >= 18) {
					if (activity.contains(config.homeActivity)) {
						handleFragmentTabHost(activity, params, true);
					} else iterationNode(null, activity, params);
				}else handleNative(activity, params);
			}
		}
		finish(activity);
		clickeds.clear();
		FileUtils.writeActivity(TimeUtils.getDate() + " stopped iteration: " + activity);
		if (!config.keepActivitiesScreenShots) FileUtils.deleteScreenShots(activity);
	}

	/**
	 * Handle native view
	 * @param activity
	 * @param params
	 * @throws Exception
	 */
	private void handleNative(String activity, Map<String, Object> params) throws Exception{
		handleFragmentTabHost(activity, params, false);
		handleViewPager(activity, params);
		handleRecyclerView(activity, params);
		handleListView(activity, params);
		handleGridView(activity, params);
		handleScrollView(activity, params);
		handleOtherView(activity, params);
	}

	/**
	 * Loop handle ScrollView, if child is LinearLayout.
	 * @param view
	 * @param activity
	 * @param params
	 * @throws Exception
	 */
	private void loopScrollView(View view, String activity, Map<String, Object> params) throws Exception{
		ViewGroup viewGroup = (ViewGroup) view;
		for (int j = 0; j < viewGroup.getChildCount(); j ++) {
			View view1 = viewGroup.getChildAt(j);
			Log.d(Solo.LOG_TAG, "ScrollView child child: " + view1);
			if (view1 != null) {
				if (view1 instanceof LinearLayout) loopScrollView(view1, activity, params);
				else {
					scrollView(view1, DOWN);
					sleep(config.sleepDuration);
					handleView(view1, activity, params, waitForDialogToOpen(50));
					handleJump(currentActivity, activity, params);
				}
			}
		}
	}

	/**
	 * Handle ScrollView
	 * @param activity
	 * @param params
	 * @throws Exception
	 */
	private void handleScrollView(String activity, Map<String, Object> params) throws Exception{
		ArrayList<ScrollView> scrollViews = getCurrentViews(ScrollView.class);
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
						scrollView(view, DOWN);
						sleep(config.sleepDuration);
						handleView(view, activity, params, waitForDialogToOpen(50));
						handleJump(currentActivity, activity, params);
					}
				}
			}
		}
	}

	/**
	 * Handle GridView
	 * @param activity
	 * @param params
	 * @throws Exception
	 */
	private void handleGridView(String activity, Map<String, Object> params) throws Exception{
		ArrayList<GridView> gridViews = getCurrentViews(GridView.class);
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
	 * @param activity
	 * @param params
	 * @param index
	 * @throws Exception
	 */
	private void reHandleGridView(String activity, Map<String, Object> params, int index) throws Exception{
		ArrayList<GridView> gridViews = getCurrentViews(GridView.class);
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
	 * @param activity
	 * @param params
	 * @throws Exception
	 */
	private void handleOtherView(String activity, Map<String, Object> params) throws Exception{
		ArrayList<View> views = getCurrentViews();
		for (View view: views) {
			if (view.isClickable()) {
				handleEditText(view);
				handleJump(currentActivity, activity, params);
				handleView(view, activity, params, waitForDialogToOpen(50));
			}
		}
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
	 * Handle Jump
	 * If the current activity is not the target page, press the back button,
	 * if not returned after the target activity is to restart the target activity
	 * @param activity activity name
	 * @param params The start parameter of the activity
	 * @return if restart activity return true
	 * @throws Exception
	 */
	private boolean handleJump(Activity context, String activity, Map<String, Object> params) throws Exception{
		ComponentName cn = getRunningTask(context);
		String pkg = cn.getPackageName();
		String act = cn.getClassName();
		Log.i(Solo.LOG_TAG, "current ComponentName: " + pkg + "/" + act);
		// If the iteration comes to the login activity, login.
		if (!activity.contains(config.loginActivity) && act.contains(config.loginActivity)){
			handleLogin();
			sleep(config.sleepDuration * 4);
			return false;
		}
		if (!act.contains(activity)) {
			Log.i(Solo.LOG_TAG, "package act: " + act);
			Log.i(Solo.LOG_TAG, "package target: " + config.PACKAGE);
			goBack();
			sleep(config.sleepDuration);
			cn = getRunningTask(context);
			act = cn.getClassName();
			if (!act.contains(activity)) {
				String[] names = activity.split("\\.");
				goBackToActivitySync(names[names.length - 1]);
				cn = getRunningTask(context);
				act = cn.getClassName();
				if (!act.contains(activity)) {
					finish(activity);
					Log.i(Solo.LOG_TAG, "ReStart Activity from getRunningTask: " + activity);
					startActivity(context, params, activity);
					sleep(config.sleepDuration * 4);
					return true;
				}
			}
			return false;
		}
		boolean isShown = isShown(context);
		Log.i(Solo.LOG_TAG, "current Activity " + context + " isShown: " + isShown);
		if (!isShown) {
			goBack();
			sleep(config.sleepDuration / 5);
			isShown = isShown(context);
			Log.i(Solo.LOG_TAG, "current Activity " + context + " isShown: " + isShown);
			if (!isShown) {
				String[] names = activity.split("\\.");
				goBackToActivitySync(names[names.length - 1]);
				isShown = isShown(context);
				if (!isShown) {
					finish(activity);
					Log.i(Solo.LOG_TAG, "ReStart Activity from isShown: " + activity);
					startActivity(context, params, activity);
					sleep(config.sleepDuration * 4);
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Handle ViewPager
	 * @param activity
	 * @param params
	 * @throws Exception
	 */
	private void handleViewPager(String activity, Map<String, Object> params) throws Exception{
		ArrayList<ViewPager> viewPagers = getCurrentViews(ViewPager.class);
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
				final int tab = i;
				if (viewPager.getAdapter() instanceof PagerAdapter) {
					viewPager.post(new Runnable() {
						@Override
						public void run() {
							viewPager.setCurrentItem(tab);
						}
					});
					Log.d(Solo.LOG_TAG, "ViewPager setCurrentItem(" + i + ")");
					sleep(config.sleepDuration * 2);
					handleListView(activity, params);
					handleOtherView(activity, params);
				} else
				if (viewPager.getAdapter() instanceof FragmentPagerAdapter || viewPager.getAdapter() instanceof FragmentStatePagerAdapter) {
					handleRecyclerView(activity, params);
				}
			}

		}
	}

	/**
	 * Handle fragmentTabHost
	 * @param activity
	 * @param params
	 */
	private void handleFragmentTabHost(String activity, Map<String, Object> params, boolean isUiAutomation) throws Exception{
		ArrayList<FragmentTabHost> fragmentTabHosts = getCurrentViews(FragmentTabHost.class);
		if (fragmentTabHosts.size() > 0) {
			FragmentTabHost fragmentTabHost = fragmentTabHosts.get(0);
			if (fragmentTabHost == null) {
				return;
			}
			Log.d(Solo.LOG_TAG, "Find fragmentTabHost: " + fragmentTabHost.toString());
			Field fieldFragment = FragmentTabHost.class.getDeclaredField("mTabs");
			fieldFragment.setAccessible(true);
			int size = ((ArrayList) fieldFragment.get(fragmentTabHost)).size();
			Log.d(Solo.LOG_TAG, "FragmentTabHost size: " + size);
			for (int i = 0; i < size; i ++) {
				final int tab = i;
				try {
					fragmentTabHost.post(new Runnable() {
						@Override
						public void run() {
							fragmentTabHost.setCurrentTab(tab);
						}
					});
				} catch (IllegalStateException ignored){}
				Log.d(Solo.LOG_TAG, "FragmentTabHost setCurrentTab(" + i + ")");
				sleep(config.sleepDuration * 4);
				if (isUiAutomation) iterationNode(null, activity, params);
				else {
					handleListView(activity, params);
					handleRecyclerView(activity, params);
					handleGridView(activity, params);
					handleViewPager(activity, params);
					handleOtherView(activity, params);
				}
			}
		} else iterationNode(null, activity, params);
	}

	/**
	 * Handle ListView
	 *Simulate pull-down refresh and pull-up loading.
	 */
	private void handleListView(String activity, Map<String, Object> params) throws Exception{
		ArrayList<ListView> listViews = getCurrentViews(ListView.class);
		if (listViews.size() > 0) {
			ListView listView = listViews.get(0);
			if (listView == null) return;
			Log.d(Solo.LOG_TAG, "Find listView: " + listView.toString());
			final int size = listView.getCount() - 1;
			Log.d(Solo.LOG_TAG, "handleListView ListView size: " + size);
			int lastPosition = listView.getLastVisiblePosition();
			if (size > 0) {
				// Pull-down refresh
				pullDown(config.sleepDuration * 4);
			}
			if (size >= lastPosition) {
				// Switch to the last item for listView.
				Log.d(Solo.LOG_TAG, "Switch to the last item for listView.");
				scrollListToLine(listView, size - 1);
				// Pull-up loading
				pullUp(config.sleepDuration * 4);
			}
			//ListView recovery
			Log.d(Solo.LOG_TAG, "ListView recovery.");
			scrollListToLine(listView, 0);
			sleep(config.sleepDuration);

			loopListView(activity, params);
		}

	}

	/**
	 * Handle ListView Item
	 * @param i the ListView Item
	 * @param activity
	 * @param params
	 * @return
	 * @throws Exception
	 */
	private boolean handleListViewItem(int i, String activity, Map<String, Object> params) throws Exception{
		clickInList(i);
		sleep(config.sleepDuration);
		boolean isRestart = handleJump(currentActivity, activity, params);
		if (isRestart) {
			reClickOnListView(activity, params, i);
			return true;
		}
		return false;
	}

	/**
	 * Handle RecyclerView Item
	 * @param i the RecyclerView Item
	 * @param activity
	 * @param params
	 * @return
	 * @throws Exception
	 */
	private boolean handleRecyclerViewItem(RecyclerView recyclerView, int i, String activity, Map<String, Object> params)
			throws Exception{
		Log.d(Solo.LOG_TAG, "RecyclerView item: " + recyclerView.getChildAt(i));
		clickInRecyclerView(recyclerView, i);
		sleep(config.sleepDuration);
		boolean isReStart = handleJump(currentActivity, activity, params);
		if (isReStart) {
			reClickOnRecyclerView(activity, params, i);
			return true;
		}
		return false;
	}

	/**
	 * Loop Handle ListView
	 * @param activity
	 * @param params
	 * @throws Exception
	 */
	private void loopListView(String activity, Map<String, Object> params) throws Exception{
		ArrayList<ListView> listViews = getCurrentViews(ListView.class);
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
						scrollListToLine(listView, lastPosition);
						sleep(config.sleepDuration);
						loopListView(activity, params);
						break;
					} else {
						// Not enough pages (screen).
						scrollDown();
						sleep(config.sleepDuration);
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
	 * @param activity
	 * @param params
	 * @param index
	 * @throws Exception
	 */
	private void reClickOnListView(String activity, Map<String, Object> params, int index) throws Exception{
		ArrayList<ListView> listViews = getCurrentViews(ListView.class);
		if (listViews.size() > 0){
			ListView listView = listViews.get(0);
			int size = listView.getCount() - 1;
			Log.d(Solo.LOG_TAG, "reClickOnListView ListView size:" + size);
			int lastPosition = listView.getLastVisiblePosition();
			for (int i = 0; i <= lastPosition; i++) {
				if (i > index){
					handleJump(currentActivity, activity, params);
					clickInList(i);
					sleep(config.sleepDuration);
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
						sleep(config.sleepDuration);
						lastPosition = listView.getLastVisiblePosition();
					}
				}
			}
		}
	}

	private void loopRecyclerView(String activity, Map<String, Object> params) throws Exception{
		ArrayList<RecyclerView> recyclerViews = getCurrentViews(RecyclerView.class);
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
			scrollToSide(RIGHT, 0.95f, 20);
		} else {
			Log.d(LOG_TAG, "RecyclerView scrollDown.");
			scrollDown();
		}
		sleep(config.sleepDuration);
	}

	/**
	 * Handle RecyclerView
	 * if view instanceof RecyclerView, simulate pull-down refresh and pull-up loading and
	 * get RecyclerView item count, iteration it.
	 * @param activity activity name
	 * @param params The start parameter of the activity
	 * @throws Exception
	 */
	private void handleRecyclerView(String activity, Map<String, Object> params) throws Exception{
		ArrayList<RecyclerView> recyclerViews = getCurrentViews(RecyclerView.class);
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
				pullDown(config.sleepDuration * 4);
			}
			RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
			if (layoutManager == null) {
				return;
			}
			if (layoutManager instanceof LinearLayoutManager) {
				lastPosition = ((LinearLayoutManager) layoutManager).findLastVisibleItemPosition();
				if (size > lastPosition) {
					// Switch to the last item for recyclerView
					scrollRecyclerViewToLine(recyclerView, size - 1);
					// Pull-up loading
					pullUp(config.sleepDuration * 4);
				}
				//RecyclerView recovery
				scrollRecyclerViewToLine(recyclerView, 0);
				sleep(config.sleepDuration);

				loopRecyclerView(activity, params);
			}
		}
	}

	/**
	 * When the recyclerView jumps, retry click on recyclerView.
	 * @param index jump index
	 */
	private void reClickOnRecyclerView(String activity, Map<String, Object> params, int index) throws Exception{
		ArrayList<RecyclerView> recyclerViews = getCurrentViews(RecyclerView.class);
		if (recyclerViews.size() > 0){
			RecyclerView recyclerView = recyclerViews.get(0);
			if (recyclerView == null || recyclerView.getAdapter() == null) {
				Log.d(Solo.LOG_TAG, "recyclerView or recyclerView.getAdapter() is null, return.");
				return;
			}
			for (int j = 0; j < recyclerView.getChildCount(); j++) {
				if (j > index){
					clickInRecyclerView(recyclerView, j);
					sleep(config.sleepDuration);
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
	 * @param view
	 */
	private void clickView(View view) {
		clickOnView(view);
		sleep(config.sleepDuration);
	}

	/**
	 * Handle Dialog
	 * If dialog is opened, get views from dialog, iteration it.
	 * @param activity activity name
	 */
	private void handleDialog(String activity, Map<String, Object> params) throws Exception{
		if (waitForDialogToOpen(50)) {
			Log.d(Solo.LOG_TAG, "dialog is open");
			ArrayList<View> views = getCurrentViews();
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
			if (waitForDialogToOpen(config.sleepDuration / 10)) {
				goBack();
				sleep(config.sleepDuration);
			}
		}
	}

	private int changeSleepStandard(){
		if (height <= 1280){
			return config.sleepDuration *= 3;
		}
		if (height <= 1920){
			return config.sleepDuration *= 2;
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
	 * Initialize timeout using 'adb shell setprop' or use setLargeTimeout() and setSmallTimeout(). Will fall back to the default values set by {@link Config}.
	 */

	private void initialize(){
		if(config.commandLogging){
			Log.d(config.commandLoggingTag, "initialize()");
		}

		Log.i(LOG_TAG, "setUp()");
		Log.i(LOG_TAG, "Iteration mode is " + config.mode.toString().toLowerCase());
		if (config.mode == Config.Mode.REPTILE) {
			Log.i(LOG_TAG, "Clear data.");
			FileUtils.deleteJson();
			FileUtils.deleteActivity();
			FileUtils.deleteParams();
			FileUtils.deleteLog();
		}
		
		Timeout.setLargeTimeout(initializeTimeout("solo_large_timeout", config.timeout_large));
		Timeout.setSmallTimeout(initializeTimeout("solo_small_timeout", config.timeout_small));

        int [] wh = DesignUtils.getDisplayWH(context);
        width = wh[0];
        height = wh[1];
        Log.i(LOG_TAG, "The device width: " + width);
        Log.i(LOG_TAG, "The device height: " + height);

		config.sleepDuration = changeSleepStandard();
		application.registerActivityLifecycleCallbacks(activityLifecycleCallbacks);
	}

	/**
	 * Parse a timeout value set using adb shell.
	 *
	 * There are two options to set the timeout. Set it using adb shell (requires root access):
	 * <br><br>
	 * 'adb shell setprop solo_large_timeout milliseconds'
	 * <br>
	 * 'adb shell setprop solo_small_timeout milliseconds'
	 * <br>
	 * Example: adb shell setprop solo_small_timeout 10000
	 * <br><br>
	 * Set the values directly using setLargeTimeout() and setSmallTimeout
	 *
	 * @param property name of the property to read the timeout from
	 * @param defaultValue default value for the timeout
	 * @return timeout in milliseconds
	 */

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static int initializeTimeout(String property, int defaultValue) {

		try {
			Class clazz = Class.forName("android.os.SystemProperties");
			Method method = clazz.getDeclaredMethod("get", String.class);
			String value = (String) method.invoke(null, property);
			return Integer.parseInt(value);
		} catch (Exception e) {
			return defaultValue;
		}
	}

}
