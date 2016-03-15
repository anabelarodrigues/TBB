package tbb.touch;

import android.content.res.Configuration;
import android.graphics.Path;
import android.graphics.Point;
import android.os.Handler;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Anabela on 28/01/2016.
 */
public class TouchSequence {
    //representing each finger; slots?
    ArrayList<TouchPoint> touchPoints;
    long startTime,endTime;
    int duration,sequenceNumber;
    boolean multiTouch;
    private static final float TOLERANCE = 5;

    int orientation,centerX=-1,centerY=-1;
private int diffX,diffY;
    public TouchSequence(String timestamp, int orientation){
        this.startTime = Long.valueOf(timestamp);
        touchPoints = new ArrayList<TouchPoint>();
        this.orientation = orientation;
    }

    public void addTouchPoint(TouchPoint touch){
    /*    if(touch.multitouchPoint != touchPoints.get(0).multitouchPoint){
            multiTouch = true;
        }
    */
        touchPoints.add(touch);


    }

    public void reproduceSequence(int orientation,int touchX,int touchY){
        if(centerX == -1 && centerY == -1){
            getSequenceCenter();
        }
        diffX = centerX - touchX;
        diffY = centerY - touchY;
        Log.d("debug", "REPRODUCESEQUENCE: centerX:" + centerX + " centerY:" + centerY + " touchX:" + touchX +
                " touchY:" + touchY + " diffX:" + diffX + " diffY:" +
                diffY);

        reproduce(0, diffX, diffY);

    }

    private void getSequenceCenter(){
        int minX=touchPoints.get(0).x,maxX=touchPoints.get(0).x,minY=touchPoints.get(0).y,maxY=touchPoints.get(0).y;

        for(TouchPoint tp : touchPoints){

            if(tp.x < minX){
                minX=tp.x;
            } else if(tp.x>maxX){
                maxX=tp.x;
            }

            if(tp.y<minY){
                minY=tp.y;
            }else if(tp.y>maxY){
                maxY=tp.y;

            }
        }

        Log.d("debug","Sequence:"+startTime+" minX:"+minX+" maxX:"+maxX+" minY:"+minY+" maxY:"+maxY);
        centerX = minX + ( (maxX-minX)/2 );
        centerY = minY + ( (maxY-minY)/2 );
    }


    public class MyRunnable implements Runnable {
        private int index,x,y;
        public MyRunnable(int index, int x, int y) {
            this.index = index;
            this.x = x;
            this.y = y;
        }

        public void run() {
            reproduce(index,x,y);
        }
    }



    private void reproduce(int index, int x, int y) {
        Log.d("debug", "adapting with difference:" + x + " " + y); //this prints

        //calculate center

        if (index < touchPoints.size()) {
            TouchPoint tp = touchPoints.get(index);

            //here we would check orientation & screen sizes
            tp.reproduce(x, y);

            index++;
            if (index < touchPoints.size()) {
                long delay = touchPoints.get(index).getTimestamp()
                        - tp.getTimestamp();
                 Log.d("debug", "Delay: " + delay);

                if (delay > 50) {
                    Handler handler = new Handler();

                    handler.postDelayed(new MyRunnable(index, x, y), delay);

                } else {
                    reproduce(index, x,y);
                }
            }

        }
    }
//get direct unchanged touches

    public ArrayList<Path> getSequencePath(int w, int h, int orientation, ArrayList<Integer> specs){
        Log.d("debug","Getting sequence path");
        ArrayList<Path> pathList = new ArrayList<>();

        HashMap<Integer,Path> paths = new HashMap<Integer,Path>();
        HashMap<Integer,Point> lastPoints = new HashMap<Integer,Point>();

        for (TouchPoint tp : touchPoints){
            Point realDimens = getRealDimens(tp.x,tp.y,specs.get(4),specs.get(5),specs.get(2),specs.get(3));
            Point newP = calculateNewPoint(realDimens,this.orientation,w,h,orientation,specs.get(2),specs.get(3));

            Log.d("debug", "Touch point at MTpoint " + tp.multitouchPoint+": x:"+newP.x+" y:"+newP.y+
            " oldX:"+tp.x+" oldY:"+tp.y);

            Path temp;
            if(paths.containsKey(tp.multitouchPoint)){ //move or up
                 temp= paths.get(tp.multitouchPoint);
                temp.lineTo(newP.x,newP.y); // quad
            } else { //DOWN
                temp = new Path();
                temp.moveTo(newP.x,newP.y);
            }

            paths.put(tp.multitouchPoint, temp);
        }
        Log.d("debug","Paths size is "+paths.size());
        if(paths.size()>0){

            for(Path p:paths.values()){
                pathList.add(new Path(p));
            }
        }

        return pathList;
    }

    private Point getRealDimens(int x,int y,int driverWidth,int driverHeight,int screenWidth,int screenHeight){
        Point dimens = new Point();
        dimens.x = (x*screenWidth)/driverWidth;
        dimens.y = (y*screenHeight)/driverHeight;
        return dimens;
    }
    /*specs: Log.d("debug","Specs retrieved: density:"+specs.get(0)+" densityDPI:"+specs.get(1)+" width:"+
    specs.get(2)+" height"+specs.get(3) + " driverWidth:"+specs.get(4) +
            " driverHeight:"+specs.get(5)  */
    //take orientation into account
    //keep aspect ratio!
    private Point calculateNewPoint(Point originalPoint,int thisOrientation,int thisWidth, int thisHeight,
                                    int originalOrientation, int originalWidth, int originalHeight){
        Point p = new Point();


        switch(thisOrientation){
            case Configuration.ORIENTATION_LANDSCAPE: //DONT TOUCH THIS IT'S PERFECT
                int[] values = getScaledDimension(thisWidth, thisHeight, originalWidth, originalHeight);


                p.x=(originalPoint.y*values[0])/originalHeight;
                p.y=(-((originalPoint.x*values[1])/originalWidth))+values[1]; //flipped this + translated down cuz it was upside down

                break;
            case Configuration.ORIENTATION_PORTRAIT: //PLS DONT TOUCH THIS IT'S V PERFECT AND TOOK HOURS
                int[] valuesP = getScaledDimension(thisWidth, thisHeight, originalWidth, originalHeight);

                p.y=(originalPoint.y*valuesP[1])/originalHeight;
                p.x=(originalPoint.x*valuesP[0])/originalWidth;
                break;
        }


        return p;
    }


    /*old version
    *  switch(thisOrientation){
            case Configuration.ORIENTATION_LANDSCAPE: //DONT TOUCH THIS IT'S PERFECT
                int[] values = getScaledDimension(thisWidth, thisHeight, originalWidth, originalHeight);


                    p.x=(originalPoint.x*values[0])/originalWidth;
                    p.y=(originalPoint.y*values[1])/originalHeight;

                break;
            case Configuration.ORIENTATION_PORTRAIT: //PLS DONT TOUCH THIS IT'S V PERFECT AND TOOK HOURS
                int[] valuesP = getScaledDimension(thisWidth, thisHeight, originalWidth, originalHeight);

                p.y=(originalPoint.x*valuesP[1])/originalWidth;
                p.x=(originalPoint.y*valuesP[0])/originalHeight;
                break;
        }*/
    public static int[] getScaledDimension(int w, int h, int oW, int oH) {

        int new_width = oW;
        int new_height = oH;

        // first check if we need to scale width
        if (oW > w) {
            //scale width to fit
            new_width = w;
            //scale height to maintain aspect ratio
            new_height = (new_width * oH) / oW;
        }

        // then check if we need to scale even with the new height
        if (new_height > h) {
            //scale height to fit instead
            new_height = h;
            //scale width to maintain aspect ratio
            new_width = (new_height * oW) / oH;
        }

        int[] value = new int[2];
        value[0] = new_width;
        value[1] = new_height;

        return value;
    }
}
