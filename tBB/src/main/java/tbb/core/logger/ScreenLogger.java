package tbb.core.logger;

import android.util.Log;

import java.io.IOException;
import java.io.OutputStream;
import java.util.TimerTask;

/**
 * Created by Anabela on 08/01/2016.
 */
public class ScreenLogger extends TimerTask {
    private String mScreenshotFolder;


    public ScreenLogger(String screenshotFolder){
        mScreenshotFolder = screenshotFolder;
    }

    @Override
    public void run() {
        Log.d("debug","attempting screenshot ...");

        String filename = mScreenshotFolder + "/" + System.currentTimeMillis() + ".raw";

        Log.d("debug", "Screenshot filename:" + filename);
        try {
            Process sh = Runtime.getRuntime().exec("su", null, null);
            OutputStream os = sh.getOutputStream();
            os.write(("/system/bin/screencap " + filename).getBytes("ASCII"));
            os.flush();
            os.close();
            sh.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }


    }
}
