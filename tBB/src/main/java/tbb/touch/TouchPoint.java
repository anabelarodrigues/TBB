package tbb.touch;

/**
 * Created by Anabela on 27/01/2016.
 */
public class TouchPoint {
    int x,y,multitouchPoint,touchType;
    String timestamp;

    public TouchPoint(int touchType, int x, int y, String timestamp,int multitouch){
        this.x = x;
        this.y = y;
        this.multitouchPoint = multitouch;
        this.timestamp = timestamp;
    }


}
