package tbb.touch;

import android.content.Context;
import android.graphics.Point;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import tbb.core.CoreController;
import tbb.core.service.TBBService;
import tbb.view.analitics.DelegateInterface;

/**
 * Created by Anabela on 04/04/2016.
 */
public class TouchInjector {
    public ArrayList<IOEvent> toReproduce;
    public boolean isInjecting=false,landscape;
    private int MAX_FINGERS = 10, MIN_DELAY=50, DEFAULT_TOUCH_MAJOR=10;
    //for ON POINT reproduction
    private int lastX,lastY,centerX,centerY,protocol,mTouchDevice,topBorder, bottomBorder,
            vDriverWidth,vDriverHeight;
    private DelegateInterface d;

    private int TIMESTAMP_DELAY=0;



    public TouchInjector(DelegateInterface d, int protocol){
        this.d = d;
        this.protocol = protocol;
        this.mTouchDevice = CoreController.sharedInstance().getTouchDevice();

      //  CoreController.sharedInstance().monitorTouch(false);
     //   CoreController.sharedInstance().unregisterReceivers();


       /* CoreController.sharedInstance().commandIO(
                CoreController.CREATE_VIRTUAL_TOUCH, protocol, true);*/



    }

    public void reproduceSequence(TouchSequence ts, Context context, int topBorder, int bottomBorder, int driverHeight){
        vDriverWidth = CoreController.sharedInstance().getTBBService().getScreenSize()[0];
        vDriverHeight = CoreController.sharedInstance().getTBBService().getScreenSize()[1];
        Log.d("debug", "driver width is:" + vDriverWidth + " and driver height is:" + vDriverHeight);

        calculateBorder(context, topBorder, bottomBorder, driverHeight);
        isInjecting = true;
        this.toReproduce = convertToProtocol(ts);



        Log.d("debug", "toReproduce size is " + this.toReproduce.size());
      /*  CoreController.sharedInstance().commandIO(
                CoreController.SET_BLOCK, mTouchDevice, true);*/


        reproduce(0, ts.touchPoints.get(0).x, ts.touchPoints.get(0).y);




    }

    private void calculateBorder(Context context, int topBorder, int bottomBorder, int driverHeight){
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int height = size.y;


        this.topBorder = (driverHeight*topBorder)/height;
        this.bottomBorder = (driverHeight*bottomBorder)/height;

    }
    private void reproduce(int index, int x, int y) {
        Log.d(TBBService.TAG, "adapting:" + x + " " + y + " at index "+index + " in toReproducce with size "
        +toReproduce.size());


        if(toReproduce.size()>0) {
            if (index < toReproduce.size()) {
                IOEvent io = toReproduce.get(index);
                Log.d("debug", "io code:" + io.getCode() + " type:" + io.getType() + " value:" + io.getValue());
                int value = io.getValue();
                int code = io.getCode();

                boolean yChanged = false, xChanged = false;

                //X OR Y
                int diffX = centerX - x;
                int diffY = centerY - y;

                if ((landscape && code == TouchRecognizer.ABS_MT_POSITION_Y)
                        || (!landscape && code == TouchRecognizer.ABS_MT_POSITION_X)) {
                    x = value;
                    xChanged = true;
                    lastX = io.getValue();
                } else if ((landscape && code == TouchRecognizer.ABS_MT_POSITION_X)
                        || (!landscape && code == TouchRecognizer.ABS_MT_POSITION_Y)) {
                    y = value;
                    yChanged = true;
                    lastY = io.getValue();
                }

                CoreController.sharedInstance().injectToTouch(io.getType(),
                        io.getCode(), value);


                //NEXT
                index++;

                if (index < toReproduce.size()) {
                    int delay = (int) (toReproduce.get(index).getTimestamp()
                            - io.getTimestamp());
                        Log.d("debug","delay is "+delay);
                        Timer t = new Timer();
                        if(delay<0){
                            delay = -delay;
                        }
                        t.schedule(new ReproduceTimer(index, x, y), delay);


                } else {

                    isInjecting = false;

                   /* CoreController.sharedInstance().commandIO(
                            CoreController.SET_BLOCK, mTouchDevice, false);*/

                    final Handler handler = new Handler(Looper.getMainLooper());
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {

                            d.delegateVirtualTouchComplete();
                        }
                    }, TIMESTAMP_DELAY);


                }

            }
        } else {
          /*  CoreController.sharedInstance().commandIO(
                    CoreController.SET_BLOCK, mTouchDevice, false);*/
            d.delegateVirtualTouchComplete();
        }

    }

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
    *  Convert touch points in database to android driver format, depending on protocol used
    *  @params int protocol: A: 1 ; B: 2
    *
    * */
    public ArrayList<IOEvent> convertToProtocol(TouchSequence ts){
        long lastTimestamp = 0;
        ArrayList<IOEvent> result = new ArrayList<IOEvent>();
        TouchPoint[] previous = new TouchPoint[MAX_FINGERS]; //representing slots // hashmap?

        int currentSlot = 0,previousSlot=0;
Log.d("debug","topborder:"+topBorder+" bottomBorder:"+bottomBorder);
        //TODO analyse protocol A in action to see what mt_syn_report and syn_report are sending
        switch(protocol){
            case 1: //Protocol A
/*
                for(TouchPoint tp : ts.touchPoints) {
                    if(tp.y>topBorder && tp.y<bottomBorder){ //if not within border, we ignore
                    long timestamp = Long.parseLong(tp.timestamp);

                    switch (tp.touchType) {
                        case TouchRecognizer.DOWN:
                            if(checkIfEmpty(previous)){ //is empty; single touch!
                                previous[0] = tp;
                                currentSlot = 0;

                                //create event sequence
                                result.add(new IOEvent(TouchRecognizer.ABS_MT_POSITION_X,3,tp.x,
                                        timestamp));
                                result.add(new IOEvent(TouchRecognizer.ABS_MT_POSITION_Y,3,tp.y,
                                        timestamp));
                                result.add(new IOEvent(TouchRecognizer.SYN_REPORT,0,0,timestamp));

                            } else { //add new slot to array
                                for (int i=0; i<previous.length; i++) {
                                    if (previous[i] == null) {
                                        currentSlot = i;
                                        break;
                                    }
                                }
                                previous[currentSlot] = tp;

                                result.add(new IOEvent(TouchRecognizer.ABS_MT_POSITION_X,3,tp.x,
                                        timestamp));
                                result.add(new IOEvent(TouchRecognizer.ABS_MT_POSITION_Y,3,tp.y,
                                        timestamp));
                                // TODO unsure of what values mt report & syn report take in this case
                                result.add(new IOEvent(TouchRecognizer.SYN_MT_REPORT,3,currentSlot,timestamp));
                                result.add(new IOEvent(TouchRecognizer.SYN_REPORT,0,0,timestamp));
                            }
                            break;
                        case TouchRecognizer.MOVE:
                            currentSlot = existsInArray(tp.multitouchPoint,previous);
                            if(numberOfFingers(previous)>1){
                                //multitouch
                                result.add(new IOEvent(TouchRecognizer.ABS_MT_POSITION_X,3,tp.x,
                                        timestamp));
                                result.add(new IOEvent(TouchRecognizer.ABS_MT_POSITION_Y,3,tp.y,
                                        timestamp));
                                // TODO unsure of what values mt report takes in this case
                                result.add(new IOEvent(TouchRecognizer.SYN_MT_REPORT,3,currentSlot,timestamp));
                                result.add(new IOEvent(TouchRecognizer.SYN_REPORT,0,0,timestamp));
                            } else { //single touch
                                result.add(new IOEvent(TouchRecognizer.ABS_MT_POSITION_X,3,tp.x,
                                        timestamp));
                                result.add(new IOEvent(TouchRecognizer.ABS_MT_POSITION_Y,3,tp.y,
                                        timestamp));
                                result.add(new IOEvent(TouchRecognizer.SYN_REPORT,0,0,timestamp));
                            }
                            break;
                        case TouchRecognizer.UP:
                            currentSlot = existsInArray(tp.multitouchPoint,previous);
                            if(numberOfFingers(previous)>1){
                                result.add(new IOEvent(TouchRecognizer.ABS_MT_POSITION_X,3,tp.x,
                                        timestamp));
                                result.add(new IOEvent(TouchRecognizer.ABS_MT_POSITION_Y,3,tp.y,
                                        timestamp));
                                result.add(new IOEvent(TouchRecognizer.SYN_MT_REPORT,3,currentSlot,timestamp));
                                result.add(new IOEvent(TouchRecognizer.SYN_REPORT,0,0,timestamp));
                            } else { //last finger to lift
                                result.add(new IOEvent(TouchRecognizer.SYN_MT_REPORT,3,currentSlot,timestamp));
                                result.add(new IOEvent(TouchRecognizer.SYN_REPORT,0,0,timestamp));
                            }
                            break;
                        }
                    }
                }*/
                break;

            case 2: //Protocol B
               // int count = 0;

                for(TouchPoint tp : ts.touchPoints) {
                    long timestamp = Long.parseLong(tp.timestamp);
                    lastTimestamp=timestamp;
                    //exclude touch points in menu or top bar
                    if (topBorder>0 && bottomBorder>0 &&(tp.y > topBorder && tp.y < bottomBorder)) {


                        //Log.d("debug","timestamp is "+timestamp);
                        switch (tp.touchType) {
                            case TouchRecognizer.DOWN:
                             //   count = 0;
                             /*   if (checkIfEmpty(previous)) { //is empty; single touch!
                                    previous[0] = tp;
                                    currentSlot = 0;
                                 //   previousSlot=currentSlot;

                                    //create event sequence
                                   // if(ts.multiTouch){
                                        result.add(new IOEvent(TouchRecognizer.ABS_MT_SLOT, 3, currentSlot,
                                                timestamp));
                                 //   }

                                    result.add(new IOEvent(TouchRecognizer.ABS_MT_TRACKING_ID, 3,
                                            tp.multitouchPoint, timestamp));
                                    result.add(new IOEvent(TouchRecognizer.ABS_MT_PRESSURE, 3,
                                            tp.pressure, timestamp));
                                    result.add(new IOEvent(TouchRecognizer.ABS_MT_POSITION_X, 3, tp.x,
                                            timestamp));
                                    result.add(new IOEvent(TouchRecognizer.ABS_MT_POSITION_Y, 3, tp.y,
                                            timestamp));
                                   // timestamp+=TIMESTAMP_DELAY;
                                    result.add(new IOEvent(TouchRecognizer.SYN_REPORT, 0, 0, timestamp));

                                } else { //add new slot to array*/

                                    //find free slot
                                    for (int i = 0; i < previous.length; i++) {
                                        if (previous[i] == null) {
                                            currentSlot = i;
                                            break;
                                        }
                                    }

                                    previous[currentSlot] = tp;
                                 //   previousSlot=currentSlot;

                                    //create event sequence
                                    result.add(new IOEvent(TouchRecognizer.ABS_MT_SLOT, 3, currentSlot,
                                            timestamp));
                                    result.add(new IOEvent(TouchRecognizer.ABS_MT_TRACKING_ID, 3,
                                            tp.multitouchPoint, timestamp));
                                    result.add(new IOEvent(TouchRecognizer.ABS_MT_PRESSURE, 3,
                                            tp.pressure, timestamp));
                                    result.add(new IOEvent(TouchRecognizer.ABS_MT_POSITION_X, 3, tp.x,
                                            timestamp));
                                    result.add(new IOEvent(TouchRecognizer.ABS_MT_POSITION_Y, 3, tp.y,
                                            timestamp));
                                    //timestamp+=TIMESTAMP_DELAY;
                                    result.add(new IOEvent(TouchRecognizer.SYN_REPORT, 0, 0, timestamp));
                                //}
                                break;

                            case TouchRecognizer.MOVE:
                              //  count++;
                                currentSlot = existsInArray(tp.multitouchPoint, previous);
/*
                                int difference = tp.x - previous[currentSlot].x;
                                if (difference < 0) {
                                    difference = -difference;
                                }*/

                                if (currentSlot > -1 ) {
                                /*    if (currentSlot==previousSlot) {

                                            result.add(new IOEvent(TouchRecognizer.ABS_MT_PRESSURE, 3,
                                                    tp.pressure, timestamp));

                                        if (previous[currentSlot].x != tp.x) {
                                            result.add(new IOEvent(TouchRecognizer.ABS_MT_POSITION_X, 3, tp.x,
                                                    timestamp));
                                        }
                                        if(previous[currentSlot].y != tp.y){
                                            result.add(new IOEvent(TouchRecognizer.ABS_MT_POSITION_Y, 3, tp.y,
                                                    timestamp));
                                        }


                                            result.add(new IOEvent(TouchRecognizer.SYN_REPORT, 0, 0, timestamp));

                                    } else {
*/

                                            result.add(new IOEvent(TouchRecognizer.ABS_MT_SLOT, 3, currentSlot,
                                                    timestamp));
                                            result.add(new IOEvent(TouchRecognizer.ABS_MT_PRESSURE, 3,
                                                    tp.pressure, timestamp));
                                            if (previous[currentSlot].x != tp.x) {
                                                result.add(new IOEvent(TouchRecognizer.ABS_MT_POSITION_X, 3, tp.x,
                                                        timestamp));
                                            }
                                            if(previous[currentSlot].y != tp.y){
                                                result.add(new IOEvent(TouchRecognizer.ABS_MT_POSITION_Y, 3, tp.y,
                                                        timestamp));
                                            }
                                            result.add(new IOEvent(TouchRecognizer.SYN_REPORT, 0, 0, timestamp));


                                  //  }
                                  /*  if(count > 6){
                                        result.add(new IOEvent(TouchRecognizer.SYN_REPORT, 0, 0, timestamp));
                                        count = 0;
                                    }
*/
                                    previous[currentSlot] = tp;

                                }
                             //   previousSlot=currentSlot;
                                break;
                            case TouchRecognizer.UP:
                              //  count =0;
                                currentSlot = existsInArray(tp.multitouchPoint, previous);

                                if(currentSlot>-1) {
                                  /*  if (currentSlot == previousSlot ) {
                                      //  result.add(new IOEvent(TouchRecognizer.SYN_REPORT, 0, 0, timestamp));
                                        result.add(new IOEvent(TouchRecognizer.ABS_MT_TRACKING_ID, 3,
                                                -1, timestamp));
                                       // timestamp+=TIMESTAMP_DELAY;
                                        result.add(new IOEvent(TouchRecognizer.SYN_REPORT, 0, 0, timestamp));
                                    } else {*/
                                    // result.add(new IOEvent(TouchRecognizer.SYN_REPORT, 0, 0, timestamp));
                                        result.add(new IOEvent(TouchRecognizer.ABS_MT_SLOT, 3, currentSlot,
                                                timestamp));
                                        result.add(new IOEvent(TouchRecognizer.ABS_MT_TRACKING_ID, 3,
                                                -1, timestamp));
                                      //  timestamp+=TIMESTAMP_DELAY;
                                        result.add(new IOEvent(TouchRecognizer.SYN_REPORT, 0, 0, timestamp));
                                  //  }
                                    previous[currentSlot] = null;
                                }
                             //   previousSlot=currentSlot;

                                break;
                        }
                    }
                }
                break;
        }

        //lasttimestamp for each touch point ? - will mess it all up naw
        if (!checkIfEmpty(previous) && lastTimestamp>0) {

            for (int i=0; i<previous.length; i++) {
                if (previous[i] != null) {
                    result.add(new IOEvent(TouchRecognizer.ABS_MT_SLOT, 3, i,
                            lastTimestamp));
                    result.add(new IOEvent(TouchRecognizer.ABS_MT_TRACKING_ID, 3,
                            -1, lastTimestamp));
                    result.add(new IOEvent(TouchRecognizer.SYN_REPORT, 0, 0, lastTimestamp));
                    break;
                }
            }
        }
        return result;
    }

    private boolean checkIfEmpty(Object[] arr){
        boolean empty = true;
        for (int i=0; i<arr.length; i++) {
            if (arr[i] != null) {
                empty = false;
                break;
            }
        }
        return empty;
    }
    private int numberOfFingers(Object[] arr){
        int fingers = 0;
        for (int i=0; i<arr.length; i++) {
            if (arr[i] != null) {
                fingers++;
                break;
            }
        }
        return fingers;
    }
    private int existsInArray(int mtPoint,TouchPoint[] arr){
        int exists = -1;
        for (int i=0; i<arr.length; i++) {
            if (arr[i] != null && arr[i].multitouchPoint == mtPoint) {
                exists = i;
                break;
            }
        }
        return exists;
    }
}
