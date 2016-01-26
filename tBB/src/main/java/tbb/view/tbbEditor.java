package tbb.view;

import android.app.Activity;
import android.os.Bundle;

import blackbox.tinyblackbox.R;

/**
 * Created by Anabela on 08/01/2016.
 */
public class tbbEditor extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //stop logging?
        //get filename & current log
        //check timestamps on current log
        setContentView(R.layout.tbbeditor);

        //get intent of filename
        //reverse engineer gestures into timeline
    }
}
