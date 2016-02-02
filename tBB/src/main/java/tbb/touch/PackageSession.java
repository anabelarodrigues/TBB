package tbb.touch;

import java.util.HashMap;

/**
 * Created by Anabela on 28/01/2016.
 */
public class PackageSession {
    private HashMap<Integer,TouchSequence> touches;
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
}
