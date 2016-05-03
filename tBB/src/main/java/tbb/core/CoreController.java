package tbb.core;

import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import tbb.core.ioManager.Monitor;
import tbb.core.logger.AssistivePlay;
import tbb.core.logger.IOTreeLogger;
import tbb.core.logger.KeystrokeLogger;
import tbb.core.logger.MessageLogger;
import tbb.core.service.TBBService;
import tbb.database.TbbDatabaseHelper;
import tbb.interfaces.AccessibilityEventReceiver;
import tbb.interfaces.IOEventReceiver;
import tbb.interfaces.NotificationReceiver;
import tbb.touch.PackageSession;
import tbb.touch.TouchRecognizer;

public class CoreController {

	// debugging tag
	private final static String SUBTAG = "CoreController: ";

    public static final String ACTION_INIT = "BB.ACTION.CORECONTROLLER.INIT";
    public static final String ACTION_STOP = "BB.ACTION.CORECONTROLLER.STOP";


    // singleton instance
    private static CoreController mSharedInstance = null;

	// List of receivers 
	private ArrayList<IOEventReceiver> mIOEventReceivers;
	private ArrayList<NotificationReceiver> mNotificationReceivers;
	private ArrayList<AccessibilityEventReceiver> mAccessibilityEventReceivers;
	private ArrayList<KeystrokeLogger> mKeystrokeEventReceiver;

    // module where to forward messages
    private Monitor mMonitor;

	// touch recognizer
	private TouchRecognizer mTouchRecognizer = null;


    // message logger
    private static MessageLogger mMessageLogger = null;


	// context, a.k.a tbb service
	private TBBService mTBBService = null;

	//database
	private TbbDatabaseHelper tbbDBHelper;

	// IO Variables
	public static final int SET_BLOCK = 0;
	public static final int MONITOR_DEV = 1;
	public static final int CREATE_VIRTUAL_TOUCH = 2;
	public static final int SETUP_TOUCH = 3;
	public static final int SET_TOUCH_RAW = 4;
	public static final int FOWARD_TO_VIRTUAL = 5;
	// Mapped screen resolution
	public double M_WIDTH;
	public double M_HEIGHT;

	public boolean permission=true;

	public boolean landscape;

    private int mUserID;
	private String user;

    protected CoreController() {}

    public synchronized static CoreController sharedInstance() {
        if(mSharedInstance == null) mSharedInstance = new CoreController();
        return mSharedInstance;
    }

    public TBBService getTBBService(){
        return mTBBService;
    }

	/**
	 * Initialise CoreController
	 */
	public void initialize(Monitor monitor, TBBService tbbService) {
        Log.v(TBBService.TAG, SUBTAG + "initialize");
		mMonitor = monitor;
		mTBBService = tbbService;


		// initialise receivers
        //initializeReceivers();

		// get screen resolution
		configureScreen();

        // announce service start
		//startService();
	}

    private void initializeReceivers(String username){
		Log.d("debug","CC initializing receivers");
		user = username;
        // Notification receivers
        // TODO are we using notifications?
        mNotificationReceivers = new ArrayList<>();

        // Event Receivers
        mAccessibilityEventReceivers = new ArrayList<>();
		AssistivePlay aPlay = new AssistivePlay(mTBBService.getApplicationContext());
		registerAccessibilityEventReceiver(aPlay);
        //registerAccessibilityEventReceiver(ioTreeLogger);

        boolean dbActive = false;
        if(tbbDBHelper != null){
            dbActive = true;
        }

        // IO receivers
        mIOEventReceivers = new ArrayList<>();
		IOTreeLogger ioTreeLogger = new IOTreeLogger("IO", "Tree", username, 250, 50,"Interaction",dbActive);
		ioTreeLogger.start(mTBBService.getApplicationContext());
        registerIOEventReceiver(ioTreeLogger);



        // Logger receivers
        mKeystrokeEventReceiver = new ArrayList<>();
        KeystrokeLogger ks = new KeystrokeLogger("Keystrokes", 150,username,dbActive);
        ks.start(mTBBService.getApplicationContext());
        registerKeystrokeEventReceiver(ks);


        //why shared instance ?
        mMessageLogger = MessageLogger.sharedInstance(username,dbActive);
        mMessageLogger.start(mTBBService.getApplicationContext());
    }

	public void unregisterReceivers(){

		// Notification receivers
		//mNotificationReceivers = null;

		// Event Receivers
		for(AccessibilityEventReceiver receiver : mAccessibilityEventReceivers){
			unregisterEvent(receiver);

		}
		//mAccessibilityEventReceivers = null;


		// IO receivers
		for(IOEventReceiver receiver : mIOEventReceivers){

			unregisterIOReceiver(receiver);
		}
		//mIOEventReceivers = null;

		// Logger receivers
		for(KeystrokeLogger receiver : mKeystrokeEventReceiver){
			receiver.stop();
			unregisterKeystrokeEventReceiver(receiver);
		}
		// mKeystrokeEventReceiver = null;

		mMessageLogger.stop();

	}

	public void resumeReceivers(){

		// Event Receivers
		AssistivePlay aPlay = new AssistivePlay(mTBBService.getApplicationContext());
		registerAccessibilityEventReceiver(aPlay);

		boolean dbActive = false;
		if(tbbDBHelper != null){
			dbActive = true;
		}

		// IO receivers
		IOTreeLogger ioTreeLogger = new IOTreeLogger("IO", "Tree", user, 250, 50,"Interaction",dbActive);
		ioTreeLogger.start(mTBBService.getApplicationContext());
		registerIOEventReceiver(ioTreeLogger);



		// Logger receivers
		KeystrokeLogger ks = new KeystrokeLogger("Keystrokes", 150,user,dbActive);
		ks.start(mTBBService.getApplicationContext());
		registerKeystrokeEventReceiver(ks);

		mMessageLogger.start(mTBBService.getApplicationContext());
	}

	public static MessageLogger getmMessageLogger(){
		return mMessageLogger;
	}
    private void configureScreen(){
        WindowManager wm = (WindowManager) mTBBService.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);
        Point size = new Point();
        display.getSize(size);
        M_WIDTH = size.x;
        M_HEIGHT = size.y;


    }
	/***********************************
	 * IO Commands and messages
	 * 
	 ************************************* */

	/**
	 * Register logger receiver (receives keystrokes info)
	 */
	public boolean registerKeystrokeEventReceiver(KeystrokeLogger receiver) {
        return mKeystrokeEventReceiver != null ? mKeystrokeEventReceiver.add(receiver) : false;
	}


    public boolean unregisterKeystrokeEventReceiver(KeystrokeLogger receiver) {
        return mKeystrokeEventReceiver != null ? mKeystrokeEventReceiver.remove(receiver) : false;
    }

    /**
     * Keystrokes propagated to mKeystrokeEventReceiver
     */
    public void updateKeystrokeEventReceivers(String keystroke, long timestamp, String text) {

        if(mKeystrokeEventReceiver == null) return;
        for(KeystrokeLogger receiver: mKeystrokeEventReceiver){
            receiver.onKeystroke(keystroke, timestamp, text);
        }
	}

	/**
	 * Register IO events receiver
	 */
	public boolean registerIOEventReceiver(IOEventReceiver ioReceiver) {
        return mIOEventReceivers != null ? mIOEventReceivers.add(ioReceiver) : false;
	}

	/**
	 * Unregister IOReceiver
	 */
	public boolean unregisterIOReceiver(IOEventReceiver ioReceiver) {
        return mIOEventReceivers != null ? mIOEventReceivers.remove(ioReceiver) : false;
	}

	/**
	 * IO event propagated to IOReceivers
	 */
	public void updateIOReceivers(int device, int type, int code, int value, int timestamp, long sysTime) {

        if(mIOEventReceivers == null) return;
        for(IOEventReceiver receiver: mIOEventReceivers){
            receiver.onUpdateIOEvent(device, type, code, value, timestamp, sysTime);
        }
	}

	public void sendTouchIOReceivers(int type) {

        if(mIOEventReceivers == null) return;
        for(IOEventReceiver receiver: mIOEventReceivers){
            receiver.onTouchReceived(type);
        }

	}

	/**
	 * Forwards the message to the appropriate component
	 * 
	 * @param command
	 *            - SET_BLOCK/MONITOR_DEV/CREATE_VIRTUAL_TOUCH/SETUP_TOUCH
	 * @param index
	 *            - device index for SET_BLOCK/MONITOR_DEV/SETUP_TOUCH
	 * @param state
	 *            - state SET_BLOCK/MONITOR_DEV
	 */
	public void commandIO(final int command, final int index,
			final boolean state) {

		Thread b = new Thread(new Runnable() {
			public void run() {

				// Separates and forwards messages to the appropriate module
				switch (command) {
				case SET_BLOCK:
					mMonitor.setBlock(index, state);
					break;
				case MONITOR_DEV:
					mMonitor.monitorDevice(index, state);
					break;
				case CREATE_VIRTUAL_TOUCH:
					mMonitor.createVirtualTouchDrive(index);
					break;
				case SETUP_TOUCH:
					mTBBService.storeTouchIndex(index);
					mMonitor.setupTouch(index);
					break;

				}
			}
		});
		b.start();
	}

	/**
	 * Inject event into touch virtual drive
	 * 
	 * @requires virtual touch driver created
	 * @param t
	 *            type
	 * @param c
	 *            code
	 * @param v
	 *            value
	 */
	public void injectToVirtual(int t, int c, int v) {
		Log.d("debug","INJECT TO VIRTUAL: T:"+t+" C:"+c+" V:"+v);
        mMonitor.injectToVirtual(t, c, v);
	}

	public void injectToTouch(int t, int c, int v) {
		Log.d("debug","INJECT TO touch: T:"+t+" C:"+c+" V:"+v);
        mMonitor.injectToTouch(t, c, v);
	}

	/**
	 * Inject event into the device on the position index
	 * 
	 * @param index
	 * @param type
	 * @param code
	 * @param value
	 */
	public void inject(int index, int type, int code, int value) {
		mMonitor.inject(index, type, code, value);
	}
	public void showToast(final String s) {

				Toast.makeText(mTBBService.getApplicationContext(), s,
						Toast.LENGTH_LONG).show();


	}
	public int monitorTouch(boolean state) {
        return mMonitor.monitorTouch(state);
	}
	public int getTouchDevice(){
		return mMonitor.getTouchIndex();
	}

	/**
	 * Get list of internal devices (touchscree, keypad, etc)
	 * 
	 * @return
	 */
	public String[] getDevices() {
        return mMonitor.getDevices();
	}

	/*************************************************
	 * Navigation and content Commands and messages
	 * 
	 ************************************************** 
	 **/

	/**
	 * Register events
	 *
	 */
	public boolean registerAccessibilityEventReceiver(AccessibilityEventReceiver eventReceiver) {

		return mAccessibilityEventReceivers != null ? mAccessibilityEventReceivers.add(eventReceiver) : false;
	}

	public boolean unregisterEvent(AccessibilityEventReceiver eventReceiver) {

        return mAccessibilityEventReceivers != null ? mAccessibilityEventReceivers.remove(eventReceiver) : false;
	}

	public void updateAccessibilityEventReceivers(AccessibilityEvent event) {

        if(mAccessibilityEventReceivers == null) return;
        for(AccessibilityEventReceiver receiver: mAccessibilityEventReceivers){
            if(checkEvent(receiver.getType(), event)) receiver.onUpdateAccessibilityEvent(event);
        }
	}

	private boolean checkEvent(int[] type, AccessibilityEvent event) {
		for (int i = 0; i < type.length; i++) {
			if (type[i] == event.getEventType())
				return true;
		}
		return false;
	}

	/*************************************************
	 * Auxiliary functions
	 * 
	 ************************************************** 
	 **/
	/**
	 * Calculate the mapped coordinate of x
	 * 
	 * @param x
	 * @return
	 */
	public int xToScreenCoord(double x) {

        return (int) (M_WIDTH / mTBBService.getScreenSize()[0] * x);
	}

	/**
	 * Calculate the mapped coordenate of y
	 * 
	 * @param
	 * @return
	 */
	public int yToScreenCoord(double y) {
		return (int) (M_HEIGHT / mTBBService.getScreenSize()[1] * y);
	}

	public void stopService() {

        // clear receivers
        mNotificationReceivers = null;
        mAccessibilityEventReceivers = null;
        mIOEventReceivers = null;
        mKeystrokeEventReceiver = null;


        if(mMonitor!=null)
            mMonitor.stop();

	}

	public void stopServiceNoBroadCast() {

        // clear receivers
//        mNotificationReceivers = null;
//        mAccessibilityEventReceivers = null;
//        mIOEventReceivers = null;
//        mKeystrokeEventReceiver = null;


        if(mMonitor!=null)
            mMonitor.stop();


	}


    public void startServiceNoBroadCast() {
        Log.v(TBBService.TAG, "STARTING MONITOR TOUCH");
        if(mMonitor!=null)
            mMonitor.monitorTouch(true);
    }

	private void startService() {
        Log.d(TBBService.TAG, SUBTAG + "starting service");
		//mTBBService.initializeService();
		//if db is chosen


		sessionInit();

        //mTBBService.registerReceiver(IOTreeLogger.sharedInstance(),IOTreeLogger.INTENT_FILTER);
        //CoreController.registerLogger(IOTreeLogger.sharedInstance(mTBBService.getApplicationContext()));
	}

private ArrayList<Float> getScreenSpecs(){
	ArrayList<Float> specs = new ArrayList<>();

	WindowManager window = (WindowManager) mTBBService.getSystemService(Context.WINDOW_SERVICE);
	DisplayMetrics metrics = new DisplayMetrics();
	window.getDefaultDisplay().getMetrics(metrics);

	Log.d("debug", "screen_density:" + metrics.density + ", screen_density_dpi:" + metrics.densityDpi +
			", screen_width:" + metrics.widthPixels + ", screen_height:" + metrics.heightPixels + ", orientation:"
			+ mTBBService.getResources().getConfiguration().orientation);
	specs.add(metrics.density);
	specs.add((float) metrics.densityDpi);
	specs.add((float)metrics.widthPixels);
	specs.add((float) metrics.heightPixels);

	Process sh = null;
	try {

		sh = Runtime.getRuntime().exec("su", null, null);
		DataOutputStream os = new DataOutputStream(sh.getOutputStream());
		DataInputStream is = new DataInputStream(sh.getInputStream());

		BufferedReader reader = new BufferedReader (new InputStreamReader(is));

		//getevent retrieves information about the touch driver
		os.writeBytes("getevent -lp "+mMonitor.getDevicePath());
		os.flush();
		os.close();

		String line;
		String xString="";
		String yString="";

		while ((line = reader.readLine()) != null) {


			if(line.contains("ABS_MT_POSITION_X")){
				xString = line;
			} else if(line.contains("ABS_MT_POSITION_Y")){
				yString = line;
			}
		}

		reader.close();
		is.close();

		specs.add(getDriverSizeFromString(xString));
		specs.add(getDriverSizeFromString(yString));

	} catch (IOException e) {
		e.printStackTrace();
	}

	if(specs.size()==6){
		return specs;
	}
	return null;
}

	private Float getDriverSizeFromString(String src){
		ArrayList<String> values = new ArrayList<>();
		Pattern p = Pattern.compile("\\d+");
		Matcher m = p.matcher(src);
		while (m.find()) {

			values.add(m.group());
		}

		return Float.parseFloat(values.get(2));
	}

	private void sessionInit(){
		//get screen density and dimensions
		ArrayList<Float> specs = getScreenSpecs();
		//setScreenSize(Math.round(specs.get(2)),Math.round(specs.get(3)));
		setScreenSize(Math.round(specs.get(4)),Math.round(specs.get(5)));

//log information about session
		if(specs != null) {
			if (mTBBService.checkStorageMethod() == 0 || mTBBService.checkStorageMethod() == 2) {
				startDBSession(System.currentTimeMillis(), specs.get(0),specs.get(1),specs.get(2),
						specs.get(3), mTBBService.getResources().getConfiguration().orientation,
						specs.get(4),specs.get(5));

			} else {
				mMessageLogger.writeAsync("\"SESSION_INIT\",\"timestamp\":\"" + System.currentTimeMillis() + "\"," +
						"\"screen_density\":\"" + specs.get(0) + "\",\"screen_density_dpi\":\"" + specs.get(1) + "\"," +
						"\"screen_width\":\"" + specs.get(2) + "\",\"screen_height\":\"" + specs.get(3) + "\"," +
						"\"orientation\":\"" + mTBBService.getResources().getConfiguration().orientation + "\"," +
						"\"driver_width\":\"" +  specs.get(4) + "\",\"driver_height\":\"" +  specs.get(5) + "\"");
				mMessageLogger.onFlush();
			}

		}



	}

	public void setAccount(String account){
		// Broadcast init event
		Intent intent = new Intent();
		intent.setAction(CoreController.ACTION_INIT);
		mTBBService.sendBroadcast(intent);

		//remove @etc, initialize with username.
		String result[] = account.split("@");
		Log.d("TbbDatabaseHelper", "user using service: " + result[0]);

			int userID = registerActiveStorage(result[0], account);
			Log.d("TbbDatabaseHelper", "user id created or fetched:" + userID);
			if (userID > 0) { //then db is being used
				mUserID = userID;
				initializeReceivers(result[0]);
				startService();
			}

		if(tbbDBHelper == null){
			initializeReceivers(result[0]);
			startService();
		}



		//initialize receivers after database; check if dbhelper is null


//TODO look for mkdirs ..
	}

	/*
* Check db preferences
* */

	public void setScreenSize(int width, int height) {
Log.d("debug","touch driver size is w:"+width+" h:"+height);
		mTBBService.storeScreenSize(width, height);
	}


	public void analyseSession(){
		//get file(s)
		//iterate over them and create objects
	}
	/**
	 * Returns to home
	 * 
	 * @return
	 */
	public boolean home() {

        return mTBBService.home();
	}

	/**
	 * Returns to home
	 */
	public boolean back() {

        return mTBBService.back();
	}

	/**
	 * Register a notification receiver
	 * 
	 * @param nr
	 * @return
	 */
	public int registerNotificationReceiver(NotificationReceiver nr) {
		mNotificationReceivers.add(nr);
		return mNotificationReceivers.size() - 1;

	}

	public int getNotificationReceiversSize() {

        return mNotificationReceivers.size();
	}

	/**
	 * Update all notifications receivers
	 * 
	 * @param note
	 */
	public void updateNotificationReceivers(String note) {
		int size = mNotificationReceivers.size();
		if (note.equals("[]")) {
			return;
		}

		note = note.substring(1, note.length() - 1);

		for (int i = 0; i < size; i++) {
			mNotificationReceivers.get(i).onNotification(note);
		}

	}

	public void registerActivateTouch(TouchRecognizer touchRecognizer) {

		mTouchRecognizer = touchRecognizer;
	}

	public int registerActiveStorage(String username,String email){
		int storage = mTBBService.checkStorageMethod();
		if(storage == 0){
			//db
			Log.d("debug","Initializing database...");
			Toast.makeText(mTBBService.getApplicationContext(),"Initializing database ...",Toast.LENGTH_LONG);
			tbbDBHelper = new TbbDatabaseHelper(mTBBService.getApplicationContext(),false);
            //user!
            return tbbDBHelper.authenticateOrRegisterUser(username, email);
		} else if (storage == 1){
			//json
			tbbDBHelper = null;
		} else if(storage == 2){
			Log.d("debug","Loading database...");
			tbbDBHelper = new TbbDatabaseHelper(mTBBService.getApplicationContext(),true);
			return tbbDBHelper.authenticateOrRegisterUser(username, email);
		}
        return -1;
	}

    public boolean checkIfDB(){
        if(tbbDBHelper!=null)
            return true;
        return false;
    }

	//run this to save to db
	/*
	public boolean checkDBStorage(){
		if(mTBBService.checkStorageMethod() == 0){
			return true;
		}
		return false;
	}
*/
	public TouchRecognizer getActiveTPR() {

        return mTouchRecognizer;
	}

	// Screen reader function convert pixels to milimeter for android nexus s
	public static int convertToMilY(int y) {
		return (124 * y) / 800 * 5;
	}

	// Screen reader function convert pixels to milimeter for android nexus s
	public static int convertToMilX(int x) {
		return (63 * x) / 480 * 5;
	}

	public static double distanceBetween(double x, double y, double x1,
			double y1) {
		return Math.sqrt(Math.pow(y - y1, 2) + Math.pow(x - x1, 2));
	}

	public int currentFileId() {
		return PreferenceManager.getDefaultSharedPreferences(mTBBService).getInt(
				"preFileSeq", 0);
	}


	public boolean getIOLogging(){
		return mMonitor.getMonitorState();
	}

    /*DB CALLS*/
    public void startDBSession(long timestamp, float density, float densityDpi, float width,
							   float height, int orientation, float driverWidth, float driverHeight){
        int id = tbbDBHelper.startSession(mUserID,timestamp,density,densityDpi,width,
				height,orientation,driverWidth,driverHeight);
//necessary to return?
    }

    public void endSession(long timestamp){
        tbbDBHelper.endSession(timestamp);
    }

	public int getPackageSessionID(){
		return tbbDBHelper.getPackageSessionID();
	}
    public void startPackageSession(String packageName, long timestamp){
        //check if package exists
        //get session id
        tbbDBHelper.createNewPackageSession(packageName, timestamp);
    }

    public void endPackageSession(String packageName, long timestamp){
        //check if package exists
        //get session id
        tbbDBHelper.endPackageSession(timestamp);
    }

	public String getPackageName(int packageID){
		return tbbDBHelper.getPackageName(packageID);
	}
    public void changeOrientation(long timestamp){
        tbbDBHelper.changeOrientation(timestamp, mTBBService.getResources().getConfiguration().orientation);
    }


    public void logIO(int id,int device,int touchType,int multitouchID,int x,int y,int pressure,int devTime,long sysTime){
        tbbDBHelper.logIO(id, device, touchType, multitouchID, x, y, pressure, devTime, sysTime);
    }
	public PackageSession getPackageSession(int packageSessionID){
		return tbbDBHelper.loadPackageSession(packageSessionID);
	}
	public void pauseDB(){
		tbbDBHelper.closeDB();
	}
	public void resumeDB(){
		tbbDBHelper.resumeDB();
	}

	public ArrayList<Integer> getScreenSpecs(int id){
		return tbbDBHelper.getScreenSpecs(id);
	}

	public ArrayList<Integer> getAllPackages(){
		return tbbDBHelper.getAllPackages();
	}

	public ArrayList<Integer> getAllPackageSessions(int packageID){
		return tbbDBHelper.getAllPackageSessions(packageID);
	}

	public long getSequenceEnd(int sequenceID){
		return tbbDBHelper.getSequenceEnd(sequenceID);
	}

	public long getPreviousTimestamp(int packageSessionID, int sequenceID){
		return tbbDBHelper.getPreviousTimeStamp(packageSessionID, sequenceID);
	}
}
