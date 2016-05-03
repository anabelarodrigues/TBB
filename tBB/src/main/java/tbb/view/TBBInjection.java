package tbb.view;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;

import blackbox.tinyblackbox.R;
import tbb.core.CoreController;
import tbb.core.service.TBBService;
import tbb.touch.PackageSession;

/**
 * Created by Anabela on 08/01/2016.
 */
public class TBBInjection extends Activity {
    private PackageSession session;
    private int currentIndex = 0;
    private ArrayList<Integer> sequenceKeys;
    private TouchTest test;
    private Button mode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tbbinjection);

        mode = (Button) findViewById(R.id.button3);

        CoreController.sharedInstance().monitorTouch(false);
        CoreController.sharedInstance().unregisterReceivers();


        int packageSessionID = getIntent().getIntExtra("packageSessionID", -1);
        session = CoreController.sharedInstance().getPackageSession(packageSessionID);

        getSequenceKeys();

        test = new TouchTest();
        CoreController.sharedInstance().registerIOEventReceiver(test);

        mode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                test.lastTime = 0;
                if (!test.saved) {
                    if (!test.saving) {
                        test.saving = true;
                        Log.d(TBBService.TAG, "SAVING...");
                        CoreController.sharedInstance().showToast(
                                "SAVING...");

                    } else {
                        Log.d(TBBService.TAG, "SAVED!");
                        CoreController.sharedInstance().showToast("SAVED");

                        test.saving = false;
                        test.saved = true;
                    }
                } else {
                    test.adapt = !test.adapt;
                    Log.d(TBBService.TAG, "ENABLE ADAPT!");

                    if (!test.adapt) {
                        Log.d(TBBService.TAG, "RESET");
                        CoreController.sharedInstance().showToast("STOPPED");


                        test.saved = false;
                        test.isInjecting = false;
                        test.toReproduce.clear();
                    } else {
                        CoreController.sharedInstance().showToast("ADAPT");

                        test.landscape = CoreController.sharedInstance().landscape;


                    }
                }
              /*  CoreController.sharedInstance().commandIO(
                        CoreController.SET_BLOCK, test.mTouchDevice,
                        test.adapt&&test.saved);*/
            }
        });
    }
/*
* CoreController.sharedInstance().commandIO(
                        CoreController.SET_BLOCK, test.mTouchDevice,
                        test.adapt | test.saving);*/
   /* @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
          /*  case MotionEvent.ACTION_DOWN:
                // Log.d(TAG, String.format("ACTION_DOWN | x:%s y:%s",
                break;
            case MotionEvent.ACTION_MOVE:
                //Log.d(TAG, String.format("ACTION_MOVE | x:%s y:%s",
                break;*/
       /*     case MotionEvent.ACTION_UP:

                break;
        }
        return true;
    }*/
/*
                 //  session.reproduceOnPoint(CoreController.sharedInstance().getTBBService().getResources().getConfiguration().orientation, (int)event.getX(),(int)event.getY());


                test.lastTime = 0;
                if (!test.saved) {
                    if (!test.saving) {
                        test.saving = true;
                        Log.d(TBBService.TAG, "SAVING...");
                        CoreController.sharedInstance().showToast(
                                "SAVING...");

                    } else {
                        Log.d(TBBService.TAG, "SAVED!");
                        CoreController.sharedInstance().showToast("SAVED");

                        test.saving = false;
                        test.saved = true;
                    }
                } else {
                    test.adapt = !test.adapt;
                    Log.d(TBBService.TAG, "ENABLE ADAPT!");

                    if (!test.adapt) {
                        Log.d(TBBService.TAG, "RESET");
                        CoreController.sharedInstance().showToast("STOPPED");


                        test.saved = false;
                        test.isInjecting = false;
                        test.toReproduce.clear();
                    } else {
                        CoreController.sharedInstance().showToast("ADAPT");

							int orientation = TBBService.getDisplay()
									.getRotation();

							if (orientation == Surface.ROTATION_90
									|| orientation == Surface.ROTATION_270) {
test.landscape = true;
							} else {
								landscape = false;
							}
        }
        }
                    CoreController.sharedInstance().commandIO(
                            CoreController.SET_BLOCK, test.mTouchDevice,
                            test.adapt | test.saving);
*/
private void getSequenceKeys(){
    sequenceKeys = new ArrayList<>();

    if(session.touches.size()>0) {
        for (int key : session.touches.keySet()) {
            sequenceKeys.add(key);
        }
    }
}
}
