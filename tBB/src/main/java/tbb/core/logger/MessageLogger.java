package tbb.core.logger;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

import blackbox.external.logger.BaseLogger;
import blackbox.external.logger.DataWriter;
import blackbox.external.logger.Logger;
import tbb.core.CoreController;
import tbb.core.service.TBBService;

/**
 * Created by kylemontague on 29/12/14.
 */
public class MessageLogger extends Logger {

    private static final String _Name = "MessageLogger";
    private static MessageLogger mSharedInstance;
    private String mMsgFilename="";
    private String mAdjust=null;

    public static MessageLogger sharedInstance(){
        if(mSharedInstance == null)
            mSharedInstance = new MessageLogger();
        return mSharedInstance;
    }


    protected MessageLogger(){
        super(_Name,2);

    }

    @Override
    public void onStorageUpdate(String path, String sequence) {
        try {
            super.onStorageUpdate(path, sequence);
            // Log.v(TBBService.TAG, SUBTAG + "onStorageUpdate");
            setMsgFileInfo();
            //setInteractionFileInfo(path, sequence);
        } catch (Exception e) {
            Toast.makeText(CoreController.sharedInstance().getTBBService(),
                    "TBB Exception", Toast.LENGTH_LONG).show();
            TBBService.writeToErrorLog(e);
        }
    }

    @Override
    public void onFlush(){
        //Log.v(BaseLogger.TAG, SUBTAG + "onFlush");
        msgFlush();
    }

    private void msgFlush() {

        DataWriter w = new DataWriter(mFolderName, mMsgFilename, true);
        synchronized (mLock) {
            w.execute(mData.toArray(new String[mData.size()]));// data is passed to background thread
            mData = new ArrayList<String>(); // initialization
        }

    }

    private void setMsgFileInfo() {
        // Log.v(TBBService.TAG, SUBTAG + "SetIOFileInfo: "+path);
        //this way we can search both by session and datetime
        mMsgFilename = mFolderName + "/Sessions/" + mSequence + "/Log.json";

        File folder = new File(mFolderName + "/Sessions/" + mSequence);
        if (!folder.exists())
            folder.mkdirs();

        Log.d("DEBUG","message logger filename"+mMsgFilename);

        sessionInit();
    }

    @Override
    public void writeAsync(String data){
        mData.add("{\"message\":" + data + "},");
        //Log.v(BaseLogger.TAG, SUBTAG + "mData size:"+mData.size());
        if(mData.size() >= mFlushThreshold)
            onFlush();
    }

    /**
     * Make a request to TBB for the current sequence folder location.
     */
    public void requestStorageInfo(Context context){
        Intent i= new Intent();
        i.setAction(BaseLogger.ACTION_SEND_REQUEST);
        i.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
        context.sendBroadcast(i);
        Log.v(BaseLogger.TAG, _Name+": Requested Storage Location");
    }


    private void sessionInit(){
        //get screen density and dimensions
        Log.d("debug","session init.");


        int orientation = CoreController.sharedInstance().getTBBService().getResources().getConfiguration().orientation;
        if(  orientation == Configuration.ORIENTATION_LANDSCAPE ){
            CoreController.sharedInstance().landscape=true;
        }else{
            CoreController.sharedInstance().landscape=false;
        }

        WindowManager window = (WindowManager) CoreController.sharedInstance().getTBBService().getSystemService(Context.WINDOW_SERVICE);

        DisplayMetrics metrics = new DisplayMetrics();
        window.getDefaultDisplay().getMetrics(metrics);
        //log information about session
        this.writeAsync("\"SESSION_INIT\",\"timestamp\":\"" + System.currentTimeMillis() + "\"," +
               "\"screen_density\":\"" + metrics.density + "\",\"screen_density_dpi\":\"" + metrics.densityDpi + "\"," +
               "\"screen_width\":\"" + metrics.widthPixels + "\",\"screen_height\":\"" + metrics.heightPixels + "\"," +
               "\"orientation\":\"" + orientation + "\"");
        this.onFlush();



    }

}
