package tbb.view;

import android.util.Log;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;

import tbb.core.CoreController;
import tbb.core.service.TBBService;
import tbb.interfaces.IOEventReceiver;
import tbb.touch.IOEvent;
import tbb.touch.TouchEvent;
import tbb.touch.TouchRecognizer;

public class TouchTest implements IOEventReceiver {
	private TouchRecognizer mTPR;
	private int mTouchDevice;

	private long mTimestamp = 0;

	private int mDevSpecialKeys;
	private int mDevHomeAndVolume;

	private boolean adapt;
	private boolean saved;
	private boolean saving;
	private boolean isInjecting;
	private boolean landscape;

	private ArrayList<IOEvent> toReproduce;
	private LinkedList<Long> timeouts;
	long lastTime;

	private int totalModes=2;
	private int mode=0;
	private final int REPLICATE=0;
	private final int REPLICATE_ON_POINT=1;

	private int lastY=0,minY=-1,maxY=-1;
	private int lastX=0,minX=-1,maxX=-1;
	private int centerX=-1,centerY=-1;
	//private int diffX=-1,diffY=-1;


	public TouchTest() {
		timeouts = new LinkedList<Long>();
		toReproduce = new ArrayList<IOEvent>();

		mTPR = CoreController.sharedInstance().getActiveTPR();
		mTouchDevice = CoreController.sharedInstance().monitorTouch(true);

		mDevSpecialKeys = 8;

		// monitor devices
		CoreController.sharedInstance().commandIO(CoreController.MONITOR_DEV,
				mDevSpecialKeys, true);

		CoreController.sharedInstance().commandIO(CoreController.SET_BLOCK,
				mDevSpecialKeys, true);

		int protocol = 1;
		CoreController.sharedInstance().commandIO(
				CoreController.CREATE_VIRTUAL_TOUCH, protocol, true);

	}

	@Override
	public void onUpdateIOEvent(int device, int type, int code, int value,
			int timestamp) {

		if (mTouchDevice == device) {
			int touchType;
			// Log.d(TBBService.TAG, " t:" + type + " c:" + code + " v:" + value
			// + " t:" + timestamp);
			if (saving) {
				toReproduce.add(new IOEvent(type, code, value, timestamp));
				return;
			}

			if ((touchType = mTPR
					.identifyOnChange(type, code, value, timestamp)) != -1) {

				TouchEvent te = mTPR.getlastTouch();
				if (touchType == mTPR.UP) {
					if (adapt && !isInjecting) {
						Log.d(TBBService.TAG, "REPRODUCING!");
						CoreController.sharedInstance()
								.showToast("REPRODUCING");

						isInjecting = true;
						if(minY < 0 || minX < 0 || maxY <0 || maxX <0){
							getMinMaxValues();
						}
						reproduce(0, te.getX(), te.getY());
					}
				}

			}
		} else {
			if (type != 0) {
				if (code == 139 && value == 0) {
					lastTime = 0;
					if (!saved) {
						if (!saving) {
							saving = true;
							Log.d(TBBService.TAG, "SAVING...");
							CoreController.sharedInstance().showToast(
									"SAVING...");

						} else {
							Log.d(TBBService.TAG, "SAVED!");
							CoreController.sharedInstance().showToast("SAVED");

							saving = false;
							saved = true;
						}
					} else {
						adapt = !adapt;
						Log.d(TBBService.TAG, "ENABLE ADAPT!");

						if (!adapt) {
							Log.d(TBBService.TAG, "RESET");
							CoreController.sharedInstance().showToast("STOPPED");


							saved = false;
							isInjecting = false;
							toReproduce.clear();
						} else {
							CoreController.sharedInstance().showToast("ADAPT");

						/*	int orientation = TBBService.getDisplay()
									.getRotation();

							if (orientation == Surface.ROTATION_90
									|| orientation == Surface.ROTATION_270) {*/
								landscape = true;
							/*} else {
								landscape = false;
							}*/
						}
					}
					CoreController.sharedInstance().commandIO(
							CoreController.SET_BLOCK, mTouchDevice,
							adapt | saving);

				} else {
					if (value == 0 && !adapt) {
						CoreController.sharedInstance().back();
					} else if (value == 0) {
						changeMode();
					}

				}

			}
		}
	}

	private void changeMode() {

		mode+=1;
		mode=mode%totalModes;
		CoreController.sharedInstance().showToast("CHANGE MODE: " + mode);

		
	}

	private void getMinMaxValues(){
		/*
		* Display mdisp = getWindowManager().getDefaultDisplay();
		Point mdispSize = new Point();
		mdisp.getSize(mdispSize);
		int maxX = mdispSize.x;
		int maxY = mdispSize.y;
		* */


	/*
		if ((landscape && code == 54) || (!landscape && code == 53)) {
			value = getAdaptedCoord(value, x ,lastX,  index); //CoreController.sharedInstance().xToScreenCoord(value);
			x=value;
			lastX=io.getValue();
		} else if ((landscape && code == 53) || (!landscape && code == 54)) {

		}*/
		for(IOEvent event : toReproduce){
			int value = event.getValue();
			if(event.getCode() == 53){
				if(landscape){
					//x
					if(minY<0 && maxY<0){
						minY=event.getValue();
						maxY=event.getValue();
					}else if(value<minY){
						minY=value;
					} else if(value>maxY){
						maxY=value;
					}
				}else{
					//y
					if(minX<0 && maxX<0){
						minX=value;
						maxX=value;
					}else if(value<minX){
						minX=value;
					} else if(value>maxX){
						maxX=value;
					}

				}
			}else if(event.getCode() == 54){
				if(landscape){
					//x
					if(minX<0 && maxX<0){
						minX=value;
						maxX=value;
					}else if(value<minX){
						minX=value;
					} else if(value>maxX){
						maxX=value;
					}

				}else{
					//y
					if(minY<0 && maxY<0){
						minY=event.getValue();
						maxY=event.getValue();
					}else if(value<minY){
						minY=value;
					} else if(value>maxY){
						maxY=value;
					}
				}
			}
		}
		//Log.d("debug","minX:"+minX+" minY:"+minY+"maxX:"+maxX+"maxY"+maxY);
		centerX = (maxX-minX)/2;
		centerY = (maxY-minY)/2;

	}

	private void reproduce(int index, int x, int y) {
		Log.d(TBBService.TAG, "adapting:" + x + " " + y);
		/*Display mdisp = CoreController.sharedInstance().getWindowManager().getDefaultDisplay();
		Point mdispSize = new Point();
		mdisp.getSize(mdispSize);
		int maxX = mdispSize.x;
		int maxY = mdispSize.y;*/

		if (index < toReproduce.size()) {
			IOEvent io = toReproduce.get(index);
			Log.d("debug","io code:"+io.getCode()+" type:"+io.getType()+" value:"+io.getValue());
			//if(io.getCode() == 57 || io.getCode() == 53 || io.getCode() == 54 || io.getCode() == 58 || io.getCode() == 48){
				int value = io.getValue();
				int code = io.getCode();
				// adapting to the real screen coords
				int diffX = centerX-x;
				int diffY=centerY-y;


				if ((landscape && code == 54) || (!landscape && code == 53)) {
					value = getAdaptedCoordX(value, diffX); //CoreController.sharedInstance().xToScreenCoord(value);
					x=value;
					lastX=io.getValue();
				} else if ((landscape && code == 53) || (!landscape && code == 54)) {
					value = getAdaptedCoordY(value, diffY);//CoreController.sharedInstance().yToScreenCoord(value);
					y=value;
					lastY=io.getValue();
				}


				CoreController.sharedInstance().injectToVirtual(io.getType(),
						io.getCode(), value);
				index++;
				if (index < toReproduce.size()) {
					int delay = toReproduce.get(index).getTimestamp()
							- io.getTimestamp();
					// Log.d(TBBService.TAG, "Delay: " + delay);
					if (delay > 0) {
						Timer t = new Timer();
						if (delay < 50) {
							t.schedule(new ReproduceTimer(index,x,y), 0);
						} else {
							t.schedule(new ReproduceTimer(index,x,y), delay);
						}
					} else {
						reproduce(index, x,y);
					}
				} else {
					isInjecting = false;
				}

			}

		//}
	}

	private int getAdaptedCoordX(int value, int coord) {
		switch (mode){
		case REPLICATE: 
				return value;
			case REPLICATE_ON_POINT:
				return value+coord;
		default:
			return value;

		}
	}
	private int getAdaptedCoordY(int value, int  coord) {
		switch (mode){
			case REPLICATE:
				return value;
			case REPLICATE_ON_POINT:
				return value+coord;
			default:
				return value;

		}
	}

	//this goes to sequence
	class ReproduceTimer extends TimerTask {
		int index;
		int x;
		int y;
		public ReproduceTimer(int index, int x, int y) {
			this.index = index;
			this.x=x;
			this.y=y;
		}

		@Override
		public void run() {
			reproduce(index,x,y);
		}
	}

	/*
	 * private void performTap(final TouchEvent te, final int width) { int x =
	 * CoreController.sharedInstance().xToScreenCoord(te.getX()); int y =
	 * CoreController.sharedInstance().yToScreenCoord(te.getY());
	 * Log.d(TBBService.TAG, " x:" + x + " y:" + y);
	 * 
	 * CoreController.sharedInstance().injectToVirtual(3,
	 * TouchRecognizer.ABS_MT_TRACKING_ID, te.getIdentifier());
	 * 
	 * CoreController.sharedInstance().injectToVirtual(3,
	 * TouchRecognizer.ABS_MT_TOUCH_MAJOR, te.getTouchMajor());
	 * 
	 * CoreController.sharedInstance().injectToVirtual(3,
	 * TouchRecognizer.ABS_MT_PRESSURE, te.getPressure());
	 * CoreController.sharedInstance().injectToVirtual(3,
	 * TouchRecognizer.ABS_MT_POSITION_X,x);// te.getX());
	 * 
	 * CoreController.sharedInstance().injectToVirtual(3,
	 * TouchRecognizer.ABS_MT_POSITION_Y,y);// te.getY());
	 * 
	 * CoreController.sharedInstance().injectToVirtual(0, 0, 0);
	 * 
	 * CoreController.sharedInstance().injectToVirtual(3,
	 * TouchRecognizer.ABS_MT_TRACKING_ID, -1);
	 * CoreController.sharedInstance().injectToVirtual(0, 0, 0);
	 * 
	 * }
	 */

	@Override
	public void onTouchReceived(int type) {
		// TODO Auto-generated method stub

	}

}
