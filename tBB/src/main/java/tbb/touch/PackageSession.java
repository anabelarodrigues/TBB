package tbb.touch;

import android.graphics.Path;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;

import tbb.core.CoreController;

/**
 * Created by Anabela on 28/01/2016.
 */
public class PackageSession {
    public HashMap<Integer,TouchSequence> touches;
    private int id;
    private String packageName;
    private String startTime, endTime;

    public PackageSession(int id, String packageName,String timestamp){
        this.id = id;
        this.packageName = packageName;
        this.startTime = timestamp;
        touches = new HashMap<Integer,TouchSequence>();
    }
    public void addSequence(int id,TouchSequence sequence){
        touches.put(id, sequence);

    }
    public void addTouchPointToSequence(int sequenceID, TouchPoint tp){
        touches.get(sequenceID).addTouchPoint(tp);

    }

    public void reproduceOnPoint(int orientation, int x, int y){
        if(touches.size()>0){
            for (TouchSequence value : touches.values()) {
                value.reproduceSequence(orientation,x,y);

            }
        }
    }

    public int getNumberOfSequences(){
        return touches.size();
    }

    //receives canvas w and h
    public ArrayList<Path> getSequenceByIndex(int index,int w, int h,int orientation){
        ArrayList<Integer> specs = CoreController.sharedInstance().getScreenSpecs(id);
        Log.d("debug","Specs retrieved: density:"+specs.get(0)+" densityDPI:"+specs.get(1)+" width:"+
                specs.get(2)+" height"+specs.get(3) +
                " driverWidth:"+specs.get(4) + " driverHeight:"+specs.get(5));

        return touches.get(index).getSequencePath(w,h,orientation,specs); //
    }

    //receives canvas w and h
    public ArrayList<Path> getAllSequences(int w, int h, int orientation){

        ArrayList<Integer> specs = CoreController.sharedInstance().getScreenSpecs(id);

        Log.d("debug","Specs retrieved: density:"+specs.get(0)+" densityDPI:"+specs.get(1)+" width:"+
                specs.get(2)+" height"+specs.get(3) +
                " driverWidth:"+specs.get(4) + " driverHeight:"+specs.get(5));

        ArrayList<Path> paths = new ArrayList<>();
       for(TouchSequence ts : touches.values()){
           for(Path p : ts.getSequencePath(w,h,orientation,specs) ){
               paths.add(p);
           }
       }

        return paths;
    }
}
