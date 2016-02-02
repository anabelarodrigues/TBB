package tbb.core.replay;

import android.util.Log;

import java.util.TimerTask;

import tbb.core.service.TBBService;

/**
 * Created by Anabela on 26/01/2016.
 */
public class IOReplayer {
    //get io from database. 5000+ entries ..!
    private int packageSession;
    private int userID;
    private int totalModes=2;
    private int mode=0;
    private final int REPLICATE=0;
    private final int REPLICATE_ON_POINT=1;

    public IOReplayer(int userID){
        this.userID = userID;
    }

    public void getSessionIO(int packageSession){
      //  CoreController.sharedInstance().
    }

    private void reproduce(int index, int x, int y) {
        Log.d(TBBService.TAG, "adapting:" + x + " " + y);
    //multi touch point, x, y, timestamp

/*
        if (index < toReproduce.size()) {
            IOEvent io = toReproduce.get(index);
            int value = io.getValue();
            int code = io.getCode();
            // adapting to the real screen coords

            if ((landscape && code == 54) || (!landscape && code == 53)) {
                value = getAdaptedCoord(value, x ,lastX,  index); //CoreController.sharedInstance().xToScreenCoord(value);
                x=value;
                lastX=io.getValue();
            } else if ((landscape && code == 53) || (!landscape && code == 54)) {
                value = getAdaptedCoord(value,y,lastY, index);//CoreController.sharedInstance().yToScreenCoord(value);
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
        */
    }

    private int getAdaptedCoord(int value, int  coord, int lastCoord, int index) {
        switch (mode){
            case REPLICATE:
                return value;
            case REPLICATE_ON_POINT:
                //Log.d (TBBService.TAG, "adapting:" + value +" to:" + (coord + (value-lastCoord)));
                return coord + (value-lastCoord);
            default:
                return value;

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


}
