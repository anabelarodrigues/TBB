package tbb.core.ioManager;

import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;

import tbb.core.CoreController;
import tbb.core.ioManager.Events.InputDevice;
import tbb.core.service.TBBService;


public class Monitor {

	// debugging tag
	private final String SUBTAG = "Monitor";

	// List of internal devices (touch, accelerometer)
	ArrayList<InputDevice> dev;

	private int touchIndex = -1;
	boolean monitoring[];
	private boolean virtualDriveEnable = true;//todo
	private boolean rooted = false;
	/**
	 * Initialises list of devices
	 *
	 * @param touchIndex
	 * returns null if it wasnt able to open the devices (probably meaning te device is not rooted)
	 *
	 */
	public Monitor(int touchIndex) {
		this.touchIndex = touchIndex;
		Events ev = new Events();
		dev = ev.Init();

		//if a device was successfuly opened then the deviec is rooted
		if(dev.size()>0) {
			rooted=true;
			monitoring = new boolean[dev.size()];
		}
		//this.ioLogging=ioLogging;
	}

	public boolean getMonitorState(){
		boolean state = false;
		if (touchIndex != -1 && monitoring != null && monitoring.length>0) {
			state = monitoring[touchIndex];
		}
		return state;
	}
	public int getTouchIndex(){
		return touchIndex;
	}

	/**
	 * Log keystroke if logger is enable TODO
	 * 
	 * @param keypressed 
	 */
	public void registerKeystroke(String keypressed, long timestamp, String text) {

		// Log keystrokes
		CoreController.sharedInstance().updateKeystrokeEventReceivers(keypressed, timestamp, text);
	}

	/**
	 * Blocks or unblock the device in index position
	 * 
	 * @param b
	 *            - true to block, false to unblock
	 * @param index
	 *            - device index
	 */
	public void setBlock(int index, boolean b) {
		dev.get(index).takeOver(b);
	}

	/**
	 * Inject event into the device in the position index
	 * 
	 * @param index
	 *            - device index
	 * @param type
	 * @param code
	 * @param value
	 */
	public void inject(int index, int type, int code, int value) {
		dev.get(index).send(index, type, code, value);
	}

	/**
	 * Inject events into the virtual touch device
	 * 
	 * @requires createVirtualTouchDrive() before
	 * @param type
	 * @param code
	 * @param value
	 */
	public void injectToVirtual(int type, int code, int value) {
		if (virtualDriveEnable) {
			Log.d("debug","virtual drive enabled");
			Events.sendVirtual(type, code, value);
		}
	}

	/**
	 * Inject events into the touch device
	 * 
	 * @requires createVirtualTouchDrive() before
	 * @param type
	 * @param code
	 * @param value
	 */
	public void injectToTouch(int type, int code, int value) {
		// Log.d(SUBTAG, "touch index:" + touchIndex);
		int thing = dev.get(touchIndex).send(touchIndex, type, code, value);
		Log.d("debug","injectToTouch:"+thing);
	}

	/**
	 * Starts or stops monitoring the device in position index
	 * 
	 * @param index - device index
	 * @param state - true to monitor, false to stop monitoring
	 * @requires    rooted=true
	 */
	public void monitorDevice(final int index, final boolean state) {
		if(monitoring==null || index>monitoring.length){
			return ;
		}
		Log.d(TBBService.TAG, "index device " +index);

		if (state != monitoring[index]) {
			monitoring[index] = state;
			if (monitoring[index]) {

				Thread b = new Thread(new Runnable() {

					public void run() {

                        try {
                            Looper.prepare();
                            InputDevice idev = dev.get(index);


                            while (monitoring[index]) {

                                if (idev.getOpen() && idev.getPollingEvent() == 0) {
                                    int type = idev.getSuccessfulPollingType();
                                    int code = idev.getSuccessfulPollingCode();
                                    int value = idev.getSuccessfulPollingValue();
                                    int timestamp = idev.getTimeStamp();
//								 Log.d(SUBTAG, type + " " + code + " " + value +
//								 " "
//								 + timestamp);
                                    CoreController.sharedInstance().updateIOReceivers(index, type,
                                            code, value, timestamp, System.currentTimeMillis());

                                }
                            }
                        }
                        catch (Exception e){
                            Toast.makeText(CoreController.sharedInstance().getTBBService(),
                                    "TBB Exception", Toast.LENGTH_LONG).show();
                            TBBService.writeToErrorLog(e);
                        }

					}
				});
				b.start();
			}
		}

	}

	/**
	 * Returns a String array of the internal devices
	 * 
	 * @return String [] - internal devices names
	 */
	public String[] getDevices() {
		String[] s = new String[dev.size()];
		for (int i = 0; i < dev.size(); i++) {

			s[i] = dev.get(i).getName();
//			Log.d(SUBTAG, "Devices: " + s[i]);
		}
		return s;
	}

	public String getDevicePath(){
		return dev.get(touchIndex).getPath();
	}
	/**
	 * Stop all monitoring
	 */
	public void stop() {
		for (int i = 0; i < dev.size(); i++) {
			setBlock(i, false);
			monitoring[i] = false;
		}

	}

	/**
	 * Setup index for touchscreen device
	 */
	public void setupTouch(int index) {
		touchIndex = index;
	}

	/**
	 * Creates a virtual touch drive
	 */

	public void createVirtualTouchDrive(int protocol) {
		Log.d("debug","TOUCH INDEX");
		Log.d("debug","index:"+touchIndex+" device_name:"+dev.get(touchIndex).getName());



		int success = dev.get(0).createVirtualDrive(
				"sec_touchscreen", protocol,
				CoreController.sharedInstance().getTBBService().getScreenSize()[0],
				CoreController.sharedInstance().getTBBService().getScreenSize()[1]);

		Log.d(SUBTAG,
				"Virtual drive created "
						+ success);

		virtualDriveEnable = true;
	}

	/**
	 * Starts monitoring the touch device
	 * 
	 * @return index of the touch device if successful -1 if not
	 */
	public int monitorTouch(boolean state) {
		//if the device is not rooted ignores the request for monitoring
		if(!rooted)
			return -1;
		if (touchIndex != -1) {
			monitorDevice(touchIndex, state);
			return touchIndex;
		} else {
			String devices[] = getDevices();
            if(devices == null || devices.length < 1)
                return -1;
			for (int i = 0; i < devices.length; i++) {
				Log.d(TBBService.TAG, "device " +devices[i] + " " + i);
				if (devices[i] != null && devices[i].contains("touchscreen")){
					touchIndex = i; //TODO
					monitorDevice(i, state);
					return i;
				}
			}

			return -1;
		}

	}




}
