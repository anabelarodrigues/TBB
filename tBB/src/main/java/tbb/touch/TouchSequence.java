package tbb.touch;

import java.util.ArrayList;

/**
 * Created by Anabela on 28/01/2016.
 */
public class TouchSequence {
    //representing each finger; slots?
    ArrayList<TouchPoint> touchPoints;
    long startTime,endTime;
    int duration,sequenceNumber;
    boolean multiTouch;

    public TouchSequence(String timestamp){
        this.startTime = Long.valueOf(timestamp);
        touchPoints = new ArrayList<TouchPoint>();
    }

    public void addTouchPoint(TouchPoint touch){
    /*    if(touch.multitouchPoint != touchPoints.get(0).multitouchPoint){
            multiTouch = true;
        }
    */
        touchPoints.add(touch);


    }

}
