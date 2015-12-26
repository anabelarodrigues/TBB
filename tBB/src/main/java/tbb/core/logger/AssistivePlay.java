package tbb.core.logger;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Toast;

import java.util.List;

import tbb.interfaces.AccessibilityEventReceiver;

/**
 * Created by Anabela on 08/12/2015.
 * detects enter/exit of game
 */
public class AssistivePlay implements AccessibilityEventReceiver {
    public static final String ACTION_APP_RESUME = "BB.ACTION.APP_RESUME";
    public static final String ACTION_APP_PAUSE = "BB.ACTION.APP_PAUSE";

    private Context context;
    private int lastEvent;
    private String lastPackage,activePackage;
    private Boolean appActive;

    public AssistivePlay(Context context){
        this.context = context;
        lastEvent=-1;
        lastPackage="";
        appActive = false;
    }

    @Override
    public void onUpdateAccessibilityEvent(AccessibilityEvent event) {

        String packageName = event.getPackageName().toString();
        int eventType=event.getEventType();

        if((eventType== AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
                || eventType== AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED)

                && !packageName.equals("blackbox.tinyblackbox")){
            identifyEvent(event,eventType);

        }

        lastEvent = eventType;
        lastPackage = packageName;

    }

    //types of acc events this handles
    @Override
    public int[] getType() {
        int[] type = new int[5];
        type[0] = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED;
        type[1] = AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED;
        type[2] = AccessibilityEvent.TYPE_VIEW_CLICKED;
        return type;

    }

    public void identifyEvent(AccessibilityEvent event, int eventType){
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> runningTasks = activityManager.getRunningAppProcesses();

        String packageName = event.getPackageName().toString();
        int broadcast = -1;



            for (ActivityManager.RunningAppProcessInfo info : runningTasks) {

                if (info.processName.equals(packageName) && info.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {

                    if (!packageName.startsWith("com.android.")
                            && !packageName.startsWith("com.google.")
                            && !packageName.equals("android")
                            && !packageName.equals("system")

                            && eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {


                        //is this distinction necessary? open for the first time
                        /*if (eventType == AccessibilityEventCompat.TYPE_WINDOW_CONTENT_CHANGED
                                && lastEvent == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
                                && lastPackage.equals(packageName)) {
                            Toast.makeText(context, "Opened for first time.", Toast.LENGTH_SHORT).show();
                            activePackage = packageName;
                            appActive = true;
                            broadcast = 0;

                        }*/


                            Toast.makeText(context, packageName + " in foreground.", Toast.LENGTH_SHORT).show();
                            activePackage = packageName;
                            appActive = true;
                            broadcast = 0;
                    }

                    //PAUSE/EXIT APP
                    else if (eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
                            && appActive
                            && activePackage != packageName) {
                        appActive = false;
                        activePackage = "";
                        Toast.makeText(context, packageName + " closed.", Toast.LENGTH_SHORT).show();
                        broadcast = 1;
                    }

                }
            }

            if(broadcast>-1){
                broadcastIntent(broadcast,packageName);
            }

    }

    private void broadcastIntent(int type, String packageName){
        Intent toBroadcastIntent = new Intent();
        switch (type){
            case 0:
                toBroadcastIntent.setAction(ACTION_APP_RESUME);
                break;
            case 1:
                toBroadcastIntent.setAction(ACTION_APP_PAUSE);
                break;

        }
        toBroadcastIntent.putExtra("packageName",packageName);
        toBroadcastIntent.putExtra("timestamp",System.currentTimeMillis());
        context.sendBroadcast(toBroadcastIntent);

    }
}
