package tbb.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.io.File;
import java.util.ArrayList;

import blackbox.tinyblackbox.R;
import tbb.core.CoreController;
import tbb.core.service.TBBService;
import tbb.touch.PackageSession;
import tbb.view.analitics.CanvasView;

/**
 * Created by Anabela on 08/01/2016.
 */
public class TBBCanvasDraw extends Activity {
    private PackageSession session;
    private CanvasView canvas;
    private int currentIndex = 0;
    private ArrayList<Integer> sequenceKeys;
  //  private TouchTest test;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.tbbcanvas);
        Button draw = (Button) findViewById(R.id.drawbutton);
        Button save = (Button) findViewById(R.id.savebutton);

        CoreController.sharedInstance().monitorTouch(false);
        CoreController.sharedInstance().unregisterReceivers();

        canvas = (CanvasView) findViewById(R.id.tbbcanvas);
        canvas.initialize();

        final int packageSessionID = getIntent().getIntExtra("packageSessionID", -1);
        session = CoreController.sharedInstance().getPackageSession(packageSessionID);

        getSequenceKeys();
       // test = new TouchTest();
       // CoreController.sharedInstance().registerIOEventReceiver(test);
/*
        prev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
Log.d("debug","PREVIOUS; current index is "+currentIndex);
                if(currentIndex >0){
                    currentIndex--;
                 //   canvas.clearCanvas();
                    canvas.setmPath(session.getSequenceByIndex(sequenceKeys.get(currentIndex)));


                }

            }
        });*/

        canvas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//clear canvas, if next, draw
              /*  Log.d("debug","NEXT; current index is "+currentIndex);
                if(currentIndex < sequenceKeys.size()){
                    canvas.setmPath(session.getSequenceByIndex(sequenceKeys.get(currentIndex),
                            canvas.width,canvas.height,
                            CoreController.sharedInstance().getTBBService().getResources().getConfiguration().orientation));
                    currentIndex++;

                }*/

                int[] coordinates = new int[2];
                canvas.getLocationOnScreen(coordinates);//gets top corner in [1] !
            }
        });

        draw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                canvas.setmPath(session.getAllSequences(canvas.width, canvas.height,
                        CoreController.sharedInstance().getTBBService().getResources().getConfiguration().orientation));//rotation

            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater layoutInflater = LayoutInflater.from(TBBCanvasDraw.this);

                View promptView = layoutInflater.inflate(R.layout.prompt, null);
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(TBBCanvasDraw.this);

                // set prompts.xml to be the layout file of the alertdialog builder
                alertDialogBuilder.setView(promptView);
                final EditText input = (EditText) promptView.findViewById(R.id.userInput);

                // setup a dialog window
                alertDialogBuilder
                        .setCancelable(false)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                                // get user input and set it to result
                                String folder = TBBService.STORAGE_FOLDER + "/bitmaps/";
                                String filename = packageSessionID +
                                        "-"+input.getText().toString() + ".png";

                                Log.d("debug","Filename will be: "+filename);

                                File file = new File(folder);
                                File newFilePath = new File(folder, filename);
                                if (!file.exists())
                                    file.mkdirs();
                                //save..

                                canvas.saveToFile(newFilePath);


                            }
                        })
                        .setNegativeButton("Cancel",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                });

                // create an alert dialog
                AlertDialog alertD = alertDialogBuilder.create();
                alertD.show();


            }
        });

    }



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
