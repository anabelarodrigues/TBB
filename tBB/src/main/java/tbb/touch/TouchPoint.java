package tbb.touch;

import android.util.Log;

import tbb.core.CoreController;

/**
 * Created by Anabela on 27/01/2016.
 */
public class TouchPoint {
    int x,y,multitouchPoint,touchType,orientation;
    String timestamp;

    public TouchPoint(int touchType, int x, int y, String timestamp,int multitouch){
        this.touchType = touchType;
        this.x = x;
        this.y = y;
        this.multitouchPoint = multitouch;
        this.timestamp = timestamp;
    }
    public long getTimestamp(){
        return Long.parseLong(timestamp);
    }

    public void reproduce(int diffX,int diffY/*, int newOrientation,int originalX, int originalY,int maxY, int maxX*/) {

        int newX = x-diffX;
        int newY = y-diffY;
        Log.d("debug","Reproducing to x:"+newX+" y:"+newY+" timestamp:"+timestamp);
        if(touchType != 1){
            CoreController.sharedInstance().injectToTouch(3,
                    57, multitouchPoint);
        }
        CoreController.sharedInstance().injectToTouch(3,
                48, 3);
        CoreController.sharedInstance().injectToTouch(3,
                58, 53);
        CoreController.sharedInstance().injectToTouch(3,
                53, 52);
        CoreController.sharedInstance().injectToTouch(3,
                54, 52);
        CoreController.sharedInstance().injectToTouch(3,
                61, 1);


    }
    private void reproduceRotated(){
//regra tres simples com max min, x=y y=x
    }

}
