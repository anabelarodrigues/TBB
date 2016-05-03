package tbb.view.analitics;

import android.app.Activity;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import blackbox.tinyblackbox.R;
import tbb.core.CoreController;
import tbb.core.service.TBBService;
import tbb.touch.PackageSession;
import tbb.touch.TouchInjector;
import tbb.touch.TouchPoint;
import tbb.touch.TouchSequence;

/**
 * Created by Anabela on 30/03/2016.
 */
public class DataAnalysis extends Activity implements DelegateInterface {
    private AnnotationView canvas;
    private TouchInjector injector;
    private int currentPackageIndex = -1, currentPackageSessionIndex = -1,
            currentTouchSequenceIndex = -1;
    private PackageSession session;
    private ArrayList<Path> sequencePaths;
    private ArrayList<Integer> packages, packageSessions, touchSequences;
    private int protocol = 2;
    private String folder, csvFilename;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.analysiscanvas);

        canvas = (AnnotationView) findViewById(R.id.canvasview);


        injector = new TouchInjector(this, protocol);

        //get packages, packagesessions & sequences
       /* if(fragment==null) {
            fragment = new AnalysisFragment();
        } else { //orientation just changed! analyse
            analyseTouchSequence(fragment.session.touches.
               *     get(fragment.touchSequences.get(fragment.currentTouchSequenceIndex)));
        }*/
        packages = CoreController.sharedInstance().getAllPackages();

        if (getNextPackageIDs()) {
            canvas.setDelegate(this);
        }


        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
       // client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    public boolean getNextPackageIDs() {
        boolean next = false;
        currentPackageIndex++;
        Log.d("debug", "Getting Package IDs. Packages size:" + packages.size() +
                " and currentPackageIndex is:" + currentPackageIndex);
        if (currentPackageIndex < packages.size()) {
            packageSessions = CoreController.sharedInstance().
                    getAllPackageSessions(packages.get(currentPackageIndex));
            Log.d("debug", "PackageSessions size:" + packageSessions.size());
            if (packageSessions.size() > 0) {
                currentPackageSessionIndex = 0;
                next = true;
            } else {
                return getNextPackageIDs();
            }
        }
        return next;
    }
   /* @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
//newConfig.screenWidthDp
        canvas.setup();
        canvas.onSizeChanged(canvas.height, canvas.width, canvas.width, canvas.height);

    }*/

    /*   private void setOrientation(){
           if(sequence.orientation == Configuration.ORIENTATION_LANDSCAPE){
               setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
               injector.landscape = true;
           } else {
               setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
               injector.landscape = false;
           }
       }*/
    private void analyseTouchSequence(TouchSequence sequence) {

        Log.d("debug", "TouchSequence values: TPsize:" + sequence.touchPointsSize() +
                " orientation:" + sequence.orientation + " startTime:" + sequence.startTime);


        ArrayList<Integer> specs = CoreController.sharedInstance().getScreenSpecs(sequence.getId());
        sequencePaths = sequence.getSequencePath(canvas.getWidth(),
                canvas.getHeight(), getResources().getConfiguration().orientation, specs);


        canvas.setmPath(this, sequencePaths);
        //canvas.invalidate();

        //TODO VIRTUAL DRAW HERE!
        //eventually get this from somewhere

        injector.reproduceSequence(sequence, getApplicationContext(),
                canvas.topBorder, canvas.bottomBorder, specs.get(5));

    }


    /*
    * call this after virtual touch has ended
    * */
    private void saveCSV(TouchSequence ts) {
        long lastTimestamp = 0;
        if (packages.size() > currentPackageIndex) {
            canvas.setMode(AnnotationView.mode.SAVING);
            File csvFilePath = new File(folder, csvFilename);
         /*   String filename = "sequence-" + ts.getId() + ".png";

            Log.d("debug", "Filename will be: " + filename);

            File file = new File(folder);
            if (!file.exists())
                file.mkdirs();

            File newFilePath = new File(folder, filename);
            canvas.saveToFile(newFilePath);
*/

            if (currentPackageSessionIndex > 0) {//is not first in packagesession
                lastTimestamp = getPreviousTimeStamp(packageSessions.
                        get(currentPackageSessionIndex), ts.getId());
                Log.d("debug", "Fetched previous timestamp:" + lastTimestamp);
            }
            getRelevantData(csvFilePath, ts.getId(), ts, lastTimestamp);
        }
        clearCanvas();

        nextSequence();
    }

    private void clearCanvas() {

        canvas.clear();
    }

    private void nextSequence() {

        //try to increment touch sequence
        canvas.setMode(AnnotationView.mode.DRAW);
        currentTouchSequenceIndex++;
        if (touchSequences.size() > currentTouchSequenceIndex) {
            //get next sequence
            analyseTouchSequence(session.touches.
                    get(touchSequences.get(currentTouchSequenceIndex)));
        } else {
            //try to increment package session
            analyseSession();

            //fill in first line
            currentPackageSessionIndex++;
            if (packageSessions.size() > currentPackageSessionIndex) {
                //get next session, load first sequence
                startNextPackageSessionAnalysis();
            } else {
                //try to increment package
                if (getNextPackageIDs()) {
                    startNextPackageSessionAnalysis();
                } else {
                    Log.d("debug", "TBB Database analysis complete!");
                    Toast.makeText(getApplicationContext(), "Analysis complete!", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    private long getPreviousTimeStamp(int packageSessionID, int sequenceID) {
        return CoreController.sharedInstance().getPreviousTimestamp(packageSessionID, sequenceID);
    }


    //we can reuse screen specs ..
    private void getRelevantData(File file, int sequenceID, TouchSequence ts,
                                 long lastSequenceTimestamp) {
        int offsetX, offsetY, xDOWN, yDOWN, xUP, yUP;
        float travelledDistance, speed;
        long duration, intervalPrev, timestampDOWN, timestampUP;
        boolean multitouch = false;

        if (sequencePaths.size() > 1) {
            multitouch = true;
        }

        // TODO GET GESTURE RECOGNITION DATA FROM ANNOTATIONVIEW (canvas) HERE

        //DURATION OF SEQUENCE
        duration = ts.endTime - ts.startTime;

        //INTERVAL FROM LAST SEQUENCE TO THIS ONE
        if (lastSequenceTimestamp > 0) {
            intervalPrev = ts.startTime - lastSequenceTimestamp;
        } else {
            intervalPrev = 0;
        }

        //CONSIDER MULTITOUCH - ONE CSV LINE PER FINGER
        HashMap<Integer, ArrayList<TouchPoint>> allPoints = ts.separateByMultitouch();

        int pos = 0;

        try {
            FileWriter fw = new FileWriter(file,true);


            for (ArrayList<TouchPoint> points : allPoints.values()) {


                //TRAVELLED DISTANCE
                PathMeasure pm = new PathMeasure(sequencePaths.get(pos), false);
                travelledDistance = pm.getLength();

                //START AND END POINTS
                timestampDOWN = points.get(0).getTimestamp();
                xDOWN = points.get(0).x;
                yDOWN = points.get(0).y;

                timestampUP = points.get(points.size() - 1).getTimestamp();
                xUP = points.get(points.size() - 1).x;
                yUP = points.get(points.size() - 1).y;

                //OFFSETS ; POSITIVE OR NEGATIVE WILL DEFINE DIRECTION!
            /*if(xDOWN>xUP){
                offsetX = xDOWN - xUP;
            }else {*/
                offsetX = xUP - xDOWN;
                //}

           /* if(yDOWN > yUP){
                offsetY = yDOWN - yUP;
            } else {*/
                offsetY = yUP - yDOWN;
                //}

                //SPEED OF THIS
                speed = (travelledDistance / duration);

                String direction;
                if(canvas.gesture.contains(AnnotationView.FLING) || canvas.gesture.contains(AnnotationView.SCROLL)){
                        if (Math.abs(offsetX) > Math.abs(offsetY)) {
                            if (offsetX > 0) { //right
                                direction = AnnotationView.RIGHT;
                            } else { //LEFT
                                direction = AnnotationView.LEFT;
                            }
                        } else {
                            if (offsetY > 0) {//DOWN
                                direction = AnnotationView.DOWN;
                            } else { //up
                                direction = AnnotationView.UP;
                            }
                        }

                } else {
                    direction = AnnotationView.NONE;
                }
                //WRITE!
                fw.append(sequenceID + "," + //0
                        multitouch + "," + //1
                        duration + "," + //2
                        speed + "," + //3
                        travelledDistance + "," + //4
                        timestampDOWN + "," + //5
                        xDOWN + "," + //6
                        yDOWN + "," + //7
                        timestampUP + "," + //8
                        xUP + "," + //9
                        yUP + "," + //10
                        offsetX + "," + //11
                        offsetY + "," + //12
                        canvas.scroll + "," + //13
                        canvas.fling + "," + //14
                        intervalPrev + "," + //15
                        canvas.gesture + "," + //16
                        direction); //17
                if (canvas.gesture.equals(canvas.TAP) || travelledDistance < 30) {
                    fw.append("," + xDOWN + "," + yDOWN);
                } else {
                    fw.append(", , ");
                }
                fw.append('\n');

                pos++;
            }

            //SAVE FILE
            fw.flush();
            fw.close();


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void delegateVirtualTouchComplete() {
        // canvas.isDrawing = false;
        saveCSV(session.touches.get(touchSequences.
                get(currentTouchSequenceIndex)));
    }

    @Override
    public void startNextPackageSessionAnalysis() {
        session = CoreController.sharedInstance().getPackageSession(packageSessions.
                get(currentPackageSessionIndex));

        if (session.touches.size() > 0) {
            prepareNextFile();

            currentTouchSequenceIndex = 0;

            touchSequences = new ArrayList<>(session.touches.keySet());
            if (touchSequences.size() > currentTouchSequenceIndex) {
                analyseTouchSequence(session.touches.get(touchSequences.
                        get(currentTouchSequenceIndex)));
            }
        } else {
            currentPackageSessionIndex++;
            if (packageSessions.size() > currentPackageSessionIndex) {
                //get next session, load first sequence
                startNextPackageSessionAnalysis();
            } else {
                //try to increment package
                if (getNextPackageIDs()) {
                    startNextPackageSessionAnalysis();
                } else {
                    Log.d("debug", "TBB Database analysis complete!");
                    Toast.makeText(getApplicationContext(), "Analysis complete!", Toast.LENGTH_LONG).show();
                }
            }

        }
    }

    private void prepareNextFile() {
        folder = TBBService.STORAGE_FOLDER + "/" +
                CoreController.sharedInstance().getPackageName(packages.get(currentPackageIndex))
                + "/session-" + packageSessions.get(currentPackageSessionIndex)
                + "/";
        csvFilename = "session-" + packageSessions.get(currentPackageSessionIndex) + ".csv";
        File folderTemp = new File(folder);
        File file = new File(folder, csvFilename);

        if (!folderTemp.exists()) {
            folderTemp.mkdirs();
        }

        FileWriter fw = null;

        try {
            fw = new FileWriter(file);

            fw.append("Sequence ID,Multitouch,Duration,Speed," +
                    "Travelled Distance,DOWN timestamp,DOWN x,DOWN y,UP timestamp,UP x,UP y," +
                    "Offset x,Offset y,Number of Scrolls," +
                    "Number of Flings,Interval From Previous,Gesture Detected,Direction," +
                    "Tap Coordinate X,Tap Coordinate Y"); //first title line
            fw.append('\n');

            fw.flush();
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void analyseSession() {
        File file = new File(folder, csvFilename);
        int tap = 0, scroll = 0, fling = 0, doubletap = 0, longpress = 0,
                sright = 0, sleft = 0, sup = 0, sdown = 0,
                fright = 0, fleft = 0, fup = 0, fdown = 0;
        ArrayList<Integer> tapIntervals = new ArrayList<>(),scrollIntervals = new ArrayList<>(),
                flingIntervals = new ArrayList<>();
        ArrayList<Float> scrollSizes = new ArrayList<>(),flingSizes = new ArrayList<>(),
                flingSpeeds = new ArrayList<>(),scrollSpeeds = new ArrayList<>();

        ////////////// analyse session csv ////////////////////////////
        String splitBy = ",";
        BufferedReader br = null;

        try {
            br = new BufferedReader(new FileReader(file));

            boolean firstLine = true;
            String line;
            while ((line = br.readLine()) != null) {
                if(firstLine){
                    firstLine=false;
                } else {
                    // LINE:1747,false,211,2.2411773,472.88843,1461835696987,1227,17,1461835697199,1178,608,-49,591,0,0,0,UNKNOWN,NONE, ,
                   // Log.d("debug","LINE:"+line);
                    String[] b = line.split(splitBy);
                    String gestureName = b[16];
                    String direction = b[17];
                    int interval = Integer.parseInt(b[15]);
                    float travelledDistance = Float.parseFloat(b[4]);
                    float velocity = Float.parseFloat(b[3]);
                    String tapCheck = b[18];

                    if (!tapCheck.equals("") && !tapCheck.equals(" ")) { //tap
                        tap++;
                        tapIntervals.add(interval);
                    }
                    //CONTAINS is more reliable from here on
                    else if (gestureName.contains(AnnotationView.DOUBLETAP)) { //
                        doubletap++;
                    } else if (gestureName.contains(AnnotationView.LONGPRESS)) {
                        longpress++;
                    } else if (gestureName.contains(AnnotationView.SCROLL)) {
                        scroll++;

                        scrollIntervals.add(interval);
                        scrollSizes.add(travelledDistance);
                        scrollSpeeds.add(velocity);
                        //check direction
                        if (direction.contains(AnnotationView.UP)) {
                            sup++;
                        } else if (direction.contains(AnnotationView.DOWN)) {
                            sdown++;
                        } else if (direction.contains(AnnotationView.RIGHT)) {
                            sright++;
                        } else if (direction.contains(AnnotationView.LEFT)) {
                            sleft++;
                        }
                    } else if (gestureName.contains(AnnotationView.FLING)) {
                        fling++;

                        flingIntervals.add(interval);
                        flingSizes.add(travelledDistance);
                        flingSpeeds.add(velocity);
                        //check direction
                        if (direction.contains(AnnotationView.UP)) {
                            fup++;
                        } else if (direction.contains(AnnotationView.DOWN)) {
                            fdown++;
                        } else if (direction.contains(AnnotationView.RIGHT)) {
                            fright++;
                        } else if (direction.contains(AnnotationView.LEFT)) {
                            fleft++;
                        }
                    }
                }

            }
            br.close();

            /////////////////////////// calculate min max values /////////////////////////////////
            int mintapinterval = -1;
            int maxtapinterval = -1;
            int minscrollinterval = -1;
            int maxscrollinterval = -1;
            int minflinginterval = -1;
            int maxflinginterval = -1;
            float minscrollvelocity = -1;
            float maxscrollvelocity = -1;
            float minflingvelocity = -1;
            float maxflingvelocity = -1;
            float minscrollsize = -1;
            float maxscrollsize = -1;
            float minflingsize = -1;
            float maxflingsize = -1;

            //tap intervals max min
            if(tapIntervals.size()>0) {
                //mintapinterval = tapIntervals.get(0);
                //maxtapinterval = tapIntervals.get(0);

                for (Integer i : tapIntervals) {
                    if(mintapinterval == -1 && i>0){
                        mintapinterval=i;
                    }
                    if(maxtapinterval == -1 && i<5000){
                        maxtapinterval=i;
                    }
                    if (i < mintapinterval && i>0) mintapinterval = i;
                    if (i > maxtapinterval && i<5000) maxtapinterval = i;
                }
            }

            //scroll intervals max min
            if(scrollIntervals.size()>0) {
                for (Integer i : scrollIntervals) {
                    if(minscrollinterval == -1 && i>0){
                        minscrollinterval=i;
                    }
                    if(maxscrollinterval == -1 && i<5000){
                        maxscrollinterval=i;
                    }
                    if (i < minscrollinterval && i>0) minscrollinterval = i;
                    if (i > maxscrollinterval && i<5000) maxscrollinterval = i;
                }
            }

            //fling intervals max min
            if(flingIntervals.size()>0) {

                for (Integer i : flingIntervals) {
                    if(minflinginterval == -1 && i>0){
                        minflinginterval=i;
                    }
                    if(maxflinginterval == -1 && i<5000){
                        maxflinginterval=i;
                    }
                    if (i < minflinginterval && i>0) minflinginterval = i;
                    if (i > maxflinginterval && i<5000) maxflinginterval = i;
                }
            }

            //scroll speeds max min
            if(scrollSpeeds.size()>0) {

                for (Float i : scrollSpeeds) {
                    if(minscrollvelocity == -1 && i>0){
                        minscrollvelocity=i;
                    }
                    if(maxscrollvelocity == -1 && i<5000){
                        maxscrollvelocity=i;
                    }
                    if (i < minscrollvelocity && i>0) minscrollvelocity = i;
                    if (i > maxscrollvelocity && i<5000) maxscrollvelocity = i;
                }
            }

            //fling velocity max min
            if(flingSpeeds.size()>0) {

                for (Float i : flingSpeeds) {
                    if(minflingvelocity == -1 && i>0){
                        minflingvelocity=i;
                    }
                    if(maxflingvelocity == -1 && i<5000){
                        maxflingvelocity=i;
                    }
                    if (i < minflingvelocity && i>0) minflingvelocity = i;
                    if (i > maxflingvelocity && i<5000) maxflingvelocity = i;
                }
            }

            //scroll travelled distance max min
            if(scrollSizes.size()>0) {
                for (Float i : scrollSizes) {
                    if(minscrollsize == -1 && i>0){
                        minscrollsize=i;
                    }
                    if(maxscrollsize == -1 && i<5000){
                        maxscrollsize=i;
                    }
                    if (i < minscrollsize && i>0) minscrollsize = i;
                    if (i > maxscrollsize && i<5000) maxscrollsize = i;
                }
            }

            //fling travelled distance max min
            if(flingSizes.size()>0) {

                for (Float i : flingSizes) {
                    if(minflingsize == -1 && i>0){
                        minflingsize=i;
                    }
                    if(maxflingsize == -1 && i<5000){
                        maxflingsize=i;
                    }
                    if (i < minflingsize && i>0) minflingsize = i;
                    if (i > maxflingsize && i<5000) maxflingsize = i;
                }
            }

            ///////////////////////// write values into new csv ///////////////////////////
            String dataFilename = "session-" + packageSessions.get(currentPackageSessionIndex) + "-DATA.csv";

            File dataFile = new File(folder, dataFilename);



            FileWriter fw = null;


            fw = new FileWriter(dataFile);

            fw.append("Frequency of Taps,Frequency of Double Taps,Frequency of Long Presses," +
                        "Frequency of Scrolls,Frequency of Flings"); //first title line
            fw.append('\n');
            fw.append(tap+","+doubletap+","+longpress+","+scroll+"," +fling); //first title line
            fw.append('\n');

            fw.append('\n');
            fw.append("Frequency of UP scrolls,Frequency of DOWN scrolls,Frequency of RIGHT scrolls," +
                    "Frequency of LEFT scrolls");
            fw.append('\n');
            fw.append(sup+","+sdown+","+sright+","+sleft);
            fw.append('\n');

            fw.append('\n');
            fw.append("Frequency of UP flings,Frequency of DOWN flings,Frequency of RIGHT flings," +
                    "Frequency of LEFT flings");
            fw.append('\n');
            fw.append(fup+","+fdown+","+fright+","+fleft);
            fw.append('\n');

            if(mintapinterval>-1 && maxtapinterval>-1) {
                fw.append('\n');
                fw.append("Tap Intervals");
                fw.append('\n');
                fw.append("MIN,MAX");
                fw.append('\n');
                fw.append(mintapinterval + "," + maxtapinterval);
                fw.append('\n');
            }

            if(minscrollinterval>-1 && maxscrollinterval>-1) {
                fw.append('\n');
                fw.append("Scroll Intervals");
                fw.append('\n');
                fw.append("MIN,MAX");
                fw.append('\n');
                fw.append(minscrollinterval + "," + maxscrollinterval);
                fw.append('\n');
            }

            if(minflinginterval>-1 && maxflinginterval>-1) {
                fw.append('\n');
                fw.append("Fling Intervals");
                fw.append('\n');
                fw.append("MIN,MAX");
                fw.append('\n');
                fw.append(minflinginterval + "," + maxflinginterval);
                fw.append('\n');
            }

            if(minscrollsize>-1 && maxscrollsize>-1) {
                fw.append('\n');
                fw.append("Scroll Travelled Distance");
                fw.append('\n');
                fw.append("MIN,MAX");
                fw.append('\n');
                fw.append(minscrollsize + "," + maxscrollsize);
                fw.append('\n');
            }

            if(minflingsize>-1 && maxflingsize>-1) {
                fw.append('\n');
                fw.append("Fling Travelled Distance");
                fw.append('\n');
                fw.append("MIN,MAX");
                fw.append('\n');
                fw.append(minflingsize + "," + maxflingsize);
                fw.append('\n');
            }

            if(minscrollvelocity>-1 && maxscrollvelocity>-1) {
                fw.append('\n');
                fw.append("Scroll Velocity");
                fw.append('\n');
                fw.append("MIN,MAX");
                fw.append('\n');
                fw.append(minscrollvelocity + "," + maxscrollvelocity);
                fw.append('\n');
            }

            if(minflingvelocity>-1 && maxflingvelocity>-1) {
                fw.append('\n');
                fw.append("Fling Velocity");
                fw.append('\n');
                fw.append("MIN,MAX");
                fw.append('\n');
                fw.append(minflingvelocity + "," + maxflingvelocity);
            }



                fw.flush();
                fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //use values to write new file
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
      /*  client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "DataAnalysis Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://tbb.view.analitics/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);*/
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
       /* Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "DataAnalysis Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://tbb.view.analitics/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();*/
    }
}
