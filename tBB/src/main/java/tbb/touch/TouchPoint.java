package tbb.touch;

/**
 * Created by Anabela on 27/01/2016.
 */
public class TouchPoint {
    public int x,y,multitouchPoint,touchType,orientation,pressure;
    String timestamp;

    public TouchPoint(int touchType, int x, int y, String timestamp,int multitouch, int pressure){
        this.touchType = touchType;
        this.x = x;
        this.y = y;
        this.multitouchPoint = multitouch;
        this.timestamp = timestamp;
        this.pressure = pressure;
    }
    public long getTimestamp(){
        return Long.parseLong(timestamp);
    }


    private void reproduceRotated(){
//regra tres simples com max min, x=y y=x
    }

}
