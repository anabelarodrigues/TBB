package tbb.core.logger;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.support.v4.view.accessibility.AccessibilityEventCompat;
import android.util.Log;
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
       // lastEvent=-1;
       // lastPackage="";
        appActive = false;
    }

    @Override
    public void onUpdateAccessibilityEvent(AccessibilityEvent event) {

        String packageName = event.getPackageName().toString();
        int eventType=event.getEventType();

        //printEventType(event);

        if((eventType== AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
                || eventType== AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED)

                && !packageName.equals("blackbox.tinyblackbox")){
            identifyEvent(event,eventType);

        }

      //  lastEvent = eventType;
       // lastPackage = packageName;

    }

    public void printEventType(AccessibilityEvent event){
        //Toast mToast = null;
        String typeName = "";

        switch(event.getEventType()) {
            case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED:
                typeName = "TYPE_NOTIFICATION_STATE_CHANGED";
                break;
            case AccessibilityEvent.TYPE_VIEW_CLICKED:
                typeName = "TYPE_VIEW_CLICKED";
                break;
            case AccessibilityEvent.TYPE_VIEW_FOCUSED:
                typeName = "TYPE_VIEW_FOCUSED";
                break;
            case AccessibilityEvent.TYPE_VIEW_LONG_CLICKED:
                typeName = "TYPE_VIEW_LONG_CLICKED";
                break;
            case AccessibilityEvent.TYPE_VIEW_SELECTED:
                typeName = "TYPE_VIEW_SELECTED";
                break;
            case AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED:
                typeName = "TYPE_VIEW_TEXT_CHANGED";
                break;
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
                typeName = "TYPE_WINDOW_STATE_CHANGED";
                break;
            case AccessibilityEvent.TYPE_ANNOUNCEMENT:
                typeName = "TYPE_ANNOUNCEMENT";
                break;
            case AccessibilityEventCompat.TYPE_GESTURE_DETECTION_END:
                typeName = "TYPE_GESTURE_DETECTION_END";
                break;
            case AccessibilityEventCompat.TYPE_GESTURE_DETECTION_START:
                typeName = "TYPE_GESTURE_DETECTION_START";
                break;
            case AccessibilityEventCompat.TYPE_TOUCH_EXPLORATION_GESTURE_END:
                typeName = "TYPE_TOUCH_EXPLORATION_GESTURE_END";
                break;
            case AccessibilityEventCompat.TYPE_TOUCH_EXPLORATION_GESTURE_START:
                typeName = "TYPE_TOUCH_EXPLORATION_GESTURE_START";
                break;
            case AccessibilityEventCompat.TYPE_TOUCH_INTERACTION_END:
                typeName = "TYPE_TOUCH_INTERACTION_END";
                break;
            case AccessibilityEventCompat.TYPE_TOUCH_INTERACTION_START:
                typeName = "TYPE_TOUCH_INTERACTION_START";
                break;
            case AccessibilityEventCompat.TYPE_VIEW_ACCESSIBILITY_FOCUS_CLEARED:
                typeName = "TYPE_VIEW_ACCESSIBILITY_FOCUS_CLEARED";
                break;
            case AccessibilityEventCompat.TYPE_VIEW_ACCESSIBILITY_FOCUSED:
                typeName = "TYPE_VIEW_ACCESSIBILITY_FOCUSED";
                break;
            case AccessibilityEventCompat.TYPE_VIEW_HOVER_ENTER:
                typeName = "TYPE_VIEW_HOVER_ENTER";
                break;
            case AccessibilityEventCompat.TYPE_VIEW_HOVER_EXIT:
                typeName = "TYPE_VIEW_HOVER_EXIT";
                break;
            case AccessibilityEventCompat.TYPE_VIEW_SCROLLED:
                typeName = "TYPE_VIEW_SCROLLED";
                break;
            case AccessibilityEventCompat.TYPE_VIEW_TEXT_SELECTION_CHANGED:
                typeName = "TYPE_VIEW_TEXT_SELECTION_CHANGED";
                break;
            case AccessibilityEventCompat.TYPE_VIEW_TEXT_TRAVERSED_AT_MOVEMENT_GRANULARITY:
                typeName = "TYPE_VIEW_TEXT_TRAVERSED_AT_MOVEMENT_GRANULARITY";
                break;
            case AccessibilityEventCompat.TYPE_WINDOW_CONTENT_CHANGED:
                typeName = "TYPE_WINDOW_CONTENT_CHANGED";
                break;
            default:
                typeName = "UNKNOWN_TYPE";
        }


        Log.d("debug ", "EVENT TYPE:"+typeName + " PACKAGE: " + event.getPackageName().toString());

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
    //find the package in list of running apps to see state
                if (info.processName.equals(packageName) && info.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
                        &&eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {

                    //an new app opened, that isn't a system app, and without another one being active
                    if (!packageName.startsWith("com.android.")
                            && !packageName.startsWith("com.google.")
                            && !packageName.startsWith("com.sec.android.")
                            && !packageName.equals("android")
                            && !packageName.equals("system")
                            && !appActive) {


                        //is this distinction necessary? open for the first time
                        /*if (eventType == AccessibilityEventCompat.TYPE_WINDOW_CONTENT_CHANGED
                                && lastEvent == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
                                && lastPackage.equals(packageName)) {
                            Toast.makeText(context, "Opened for first time.", Toast.LENGTH_SHORT).show();
                            activePackage = packageName;
                            appActive = true;
                            broadcast = 0;

                        }*/


                        Log.d("debug", packageName + " in foreground.");
                            activePackage = packageName;

                        Toast.makeText(context, activePackage + " in foreground.", Toast.LENGTH_SHORT).show();
                            appActive = true;
                            broadcast = 0;
                    }

                    //a new app opened which isnt the one we were monitoring. time to close.
                    //but if new app isn't a system app, time to open too.
                    else if (appActive
                            && !activePackage.equals(packageName)) {
                        Log.d("debug", "active package:" + activePackage + " packageName:" + packageName);
                        if (!packageName.startsWith("com.android.")
                                && !packageName.startsWith("com.google.")
                                && !packageName.startsWith("com.sec.android.")
                                && !packageName.equals("android")
                                && !packageName.equals("system")){
                            //close this and open another!
                            Toast.makeText(context, activePackage + " closed.", Toast.LENGTH_SHORT).show();
                            broadcastIntent(1, activePackage);
                            activePackage = packageName;
                            broadcast = 0;
                            Toast.makeText(context, activePackage + " in foreground.", Toast.LENGTH_SHORT).show();
                        }else{
                            //just close this
                            appActive=false;
                            Toast.makeText(context, activePackage + " closed.", Toast.LENGTH_SHORT).show();
                            broadcast = 1;
                        }



                    }

                }
            }

            if(broadcast>-1){
                broadcastIntent(broadcast,activePackage);
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
