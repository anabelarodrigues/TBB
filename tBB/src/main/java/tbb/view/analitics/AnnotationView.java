package tbb.view.analitics;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;


/**
 * Created by Kyle Montague on 16/07/15.
 */
public class AnnotationView extends View implements View.OnTouchListener,
GestureDetector.OnGestureListener,GestureDetector.OnDoubleTapListener,ScaleGestureDetector.OnScaleGestureListener {
    public static String UNKNOWN = "UNKNOWN",NONE="NONE";
    public static String TAP="TAP", SCROLL="SCROLL", DOUBLETAP="DOUBLETAP", FLING="FLING",
            LONGPRESS="LONGPRESS",PINCH="PINCH",ZOOM="ZOOM";
    public static String LEFT="LEFT",RIGHT="RIGHT",UP="UP",DOWN="DOWN";

    public static mode[] modes = new mode[]{mode.DRAW, mode.ORIGINAL, mode.SAVING};
    private DelegateInterface d;
    private boolean analysedDB = false;
    private ArrayList<Path> pathList, mAnnotations = new ArrayList<>();
    private Bitmap background;
    private BitmapFactory.Options bitmapOptions;
    boolean hasBitmap = false;
    private Canvas mCanvas;
public int bottomBorder, topBorder;
public int width=0, height=0;
    Paint paint,dbPaint;
    private mode mMode = mode.DRAW;
    Paint bg;

    boolean changedDirection=false;
    public String gesture = UNKNOWN,direction=NONE;

    public static final int MAX_FINGERS = 10;
    private Path[] mFingerPaths = new Path[MAX_FINGERS];

    float startScale,endScale;
    RectF boundsF;

    private GestureDetector mDetector;
    public int scroll=0,fling=0;

    @Override
    public boolean onScale(ScaleGestureDetector detector) {
        startScale = detector.getScaleFactor();
        return false;
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
        return false;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {
        if (startScale > endScale) {
            Log.d("debug", "Pinch.");
           // Toast.makeText(getContext(), "Pinch", Toast.LENGTH_SHORT).show();
            gesture = PINCH;
        } else if (startScale < endScale) {
            Log.d("debug", "Zoom.");
           // Toast.makeText(getContext(), "Zoom", Toast.LENGTH_SHORT).show();
            gesture = ZOOM;
        }
    }


    public enum mode{
        DRAW,
        SAVING,
        ORIGINAL
    }

    public AnnotationView(Context context) {
        super(context);
        setup();
    }


    public AnnotationView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setup();

     /*   String imageURI = attrs.getAttributeValue(0);
        if(imageURI !=null){
            //has image

            background = BitmapFactory.decodeFile(imageURI,bitmapOptions);
            hasBitmap = true;
        }*/

    }
    public void setDelegate(DelegateInterface d){
        this.d=d;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event){
       // this.mDetector.onTouchEvent(event);
       // Log.d("debug","TOUCHEVENT IN GESTURE DETECTOR");
        // Be sure to call the superclass implementation

        return super.onTouchEvent(event);
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        Log.d("debug", "Single tap.");
      //  Toast.makeText(getContext(), "Single Tap", Toast.LENGTH_SHORT).show();
        gesture = TAP;
        return true;
    }

    @Override
    public boolean onDoubleTap(MotionEvent e) {
        Log.d("debug", "Double tap.");
      //  Toast.makeText(getContext(), "Double Tap", Toast.LENGTH_SHORT).show();
        gesture = DOUBLETAP;
        return true;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return true;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        Log.d("debug","Scroll event.");
        scroll++;


        //direction watching
        if(!changedDirection) {
    //down or move
                if (Math.abs(distanceX) > Math.abs(distanceY)) {
                    if (distanceX > 0) { //right
                        if (direction.equals(NONE)) {
                            direction = RIGHT;
                            gesture = SCROLL;
                        } else if (!direction.contains(RIGHT)) {
                                changedDirection = true;
                                gesture = UNKNOWN;
                                direction=NONE;
                        }
                    } else { //LEFT
                        if (direction.equals(NONE)) {
                            direction = LEFT;
                            gesture = SCROLL;
                        } else if (!direction.contains(LEFT)) {
                                changedDirection = true;
                                gesture = UNKNOWN;
                                direction = NONE;
                        }
                    }
                } else {
                    if (distanceY > 0) {//DOWN
                        if (direction.equals(NONE)) {
                            direction = DOWN;
                            gesture = SCROLL;
                        } else if (!direction.contains(DOWN)) {
                            changedDirection = true;
                            gesture = UNKNOWN;
                            direction = NONE;
                        }
                    } else { //up
                        if (direction.equals(NONE)) {
                            direction = UP;
                            gesture = SCROLL;
                        } else if (!direction.contains(UP)) {
                            changedDirection = true;
                            gesture = UNKNOWN;
                            direction = NONE;

                        }
                    }
                }

        }

        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {
        Log.d("debug", "Long press.");
     //   Toast.makeText(getContext(), "Long Press", Toast.LENGTH_SHORT).show();
        if (gesture.equals(UNKNOWN)) {
            gesture = LONGPRESS;
        }
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
       // Log.d("debug","Fling.");
        fling++;
        float distanceX = e2.getX() - e1.getX();
        float distanceY = e2.getY() - e1.getY();
        if(!changedDirection) {
            //down or move
            if (Math.abs(distanceX) > Math.abs(distanceY)) {
                if (distanceX > 0) { //right
                    if (direction.equals(NONE)) {
                        direction = RIGHT;
                    } else if (!direction.equals(RIGHT)) {
                        changedDirection = true;
                        direction = NONE;
                    }
                } else { //LEFT
                    if (direction.equals(NONE)) {
                        direction = LEFT;
                    } else if (!direction.equals(LEFT)) {
                        changedDirection = true;
                        direction = NONE;
                    }
                }
            } else {
                if (distanceY > 0) {//DOWN
                    if (direction.equals(NONE)) {
                        direction = DOWN;
                    } else if (!direction.equals(DOWN)) {
                        changedDirection = true;
                        direction = NONE;
                    }
                } else { //up
                    if (direction.equals(NONE)) {
                        direction = UP;
                    } else if (!direction.equals(UP)) {
                        changedDirection = true;
                        direction = NONE;

                    }
                }
            }
        }

            if(e2.getAction()==MotionEvent.ACTION_UP && !changedDirection && (gesture.equals(UNKNOWN) || gesture.equals(SCROLL))){
                Log.d("debug", "Fling! velocityX:" + velocityX + " velocityY:" + velocityY);
               // Toast.makeText(getContext(), "Fling!", Toast.LENGTH_SHORT).show();

                gesture=FLING;
                return true;
            }

        return false;
    }

    @Override
    public boolean dispatchTouchEvent (MotionEvent event) {
        return super.dispatchTouchEvent(event);
    }

    //////////////////////////////////////////////////////////////
    public void setMode(mode mode){
        mMode=mode;
        this.invalidate();
    }

    public void scaleImage(float width, float height){
        if(!hasBitmap || background==null || width <= 0 || height <= 0)
            return;
        background = Bitmap.createScaledBitmap(background, (int) width, (int) height, true);
    }

    public void setColor(int color){
        paint.setColor(color);
    }


    protected void setup(){
        mDetector = new GestureDetector(getContext(),this);
        mDetector.setOnDoubleTapListener(this);
        this.setOnTouchListener(this);

        //touch paint
        paint = new Paint();
        paint.setStrokeWidth(5f);
        paint.setColor(Color.GREEN);
        paint.setStyle(Paint.Style.STROKE);
        paint.setAntiAlias(true);

        //db path paint
        dbPaint = new Paint();
        dbPaint.setColor(Color.CYAN);
        dbPaint.setStyle(Paint.Style.STROKE);
        dbPaint.setStrokeWidth(5f);

        bg= new Paint();
        bg.setColor(Color.WHITE);
        bg.setStyle(Paint.Style.FILL);

        bitmapOptions = new BitmapFactory.Options();
        bitmapOptions.inScaled = true;
        bitmapOptions.inSampleSize = 2;
    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        Log.d("debug","onSizeChanged w:"+w+" h:"+h+" oldw:"+oldw+" oldh:"+oldh);
        super.onSizeChanged(w, h, oldw, oldh);
        width = w;
        height = h;
        boundsF = new RectF(0,0,w,h);


        background = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        // Bitmap.createScaledBitmap(mBitmap, w, h, true);
        mCanvas = new Canvas(background);
        mCanvas.drawRect(boundsF, bg);

        int[] coordinates = new int[2];
        getLocationOnScreen(coordinates);
        topBorder = coordinates[1];


        int navBarHeight=0;

        Resources resources = getContext().getResources();
        int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            navBarHeight= resources.getDimensionPixelSize(resourceId);
        }

        bottomBorder = (topBorder+h)-navBarHeight;

        if(!analysedDB && d!=null){
            d.startNextPackageSessionAnalysis();
            analysedDB=true;
        }

    }

    //used by activity to set path from TouchSequence
    public void setmPath(Activity a,ArrayList<Path> paths) {
        pathList=paths;
        a.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                invalidate();
            }
        });

    }

    public void clear(){
       // Toast.makeText(getContext(), "unknown", Toast.LENGTH_SHORT).show();
        gesture=UNKNOWN;direction=NONE;changedDirection=false;
        scroll=0;fling=0;
        mAnnotations.clear();
        for(int i=0; i<MAX_FINGERS; i++){
            mFingerPaths[i] = null;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);


        if(background != null && hasBitmap) {
           // canvas.drawBitmap(background, 0, 0, bg);
            //canvas.drawBitmap(background, 0, 0, dbPaint);
        }else{
            if(boundsF == null)
                boundsF = new RectF(0,0,width,height);
            canvas.drawRect(boundsF,bg);
        }

        //db paths
        if(pathList!= null && pathList.size()>0) {
            for (Path path : pathList) {
                canvas.drawPath(path, dbPaint);
            }
        }

        //injection drawing
        if(mMode == mode.DRAW) {
            for (Path fingerPath : mAnnotations) {
                canvas.drawPath(fingerPath, paint);
            }

            for (Path fingerPath : mFingerPaths) {
                if (fingerPath != null) {
                    canvas.drawPath(fingerPath, paint);
                }
            }

        }


    }




    public void finalizeStroke(int id){
        mAnnotations.add(mFingerPaths[id]);
        mFingerPaths[id] = null;
    }
    public void append(MotionEvent event, int id, int actionIndex){
        if(mMode == mode.DRAW && mFingerPaths[id] != null) {

            mFingerPaths[id].lineTo(event.getX(actionIndex), event.getY(actionIndex));
        } else if(mFingerPaths[id] == null){
            mFingerPaths[id] = new Path();
            mFingerPaths[id].moveTo(event.getX(actionIndex), event.getY(actionIndex));
        }

    }

    public void add(MotionEvent event, int id, int actionIndex){
        if(mMode == mode.DRAW) {
            mFingerPaths[id] = new Path();
            mFingerPaths[id].moveTo(event.getX(actionIndex), event.getY(actionIndex));
        }

    }
    private void processEvent(MotionEvent event){
        int pointerCount = event.getPointerCount();
        int actionIndex = event.getActionIndex();
        int id = event.getPointerId(actionIndex);


        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
                add(event, id, actionIndex);
                break;
            case MotionEvent.ACTION_MOVE:
                for (int t = 0; t < pointerCount; t++) {
                    if(mFingerPaths[t]!=null) {
                        int mID = event.getPointerId(t);
                        append(event, mID, t);
                    }
                }
                break;
            case MotionEvent.ACTION_POINTER_UP:
            case MotionEvent.ACTION_UP:
                append(event, id, actionIndex);
                finalizeStroke(id);

                if(!changedDirection && gesture.contains(SCROLL)){
                    Log.d("debug", "Scroll!");
                    gesture = SCROLL;
                }
                break;
        }

        this.invalidate();

    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        processEvent(event);
        mDetector.onTouchEvent(event);

        return true;
    }

    public void setCanvasOnBitmap() {

        this.setDrawingCacheEnabled(true);
        this.buildDrawingCache();
        background = Bitmap.createBitmap(this.getDrawingCache());
        this.setDrawingCacheEnabled(false);

       /* Drawable bgDrawable =AnnotationView.this.getBackground();
        if (bgDrawable!=null)
            bgDrawable.draw(mCanvas);

        AnnotationView.this.draw(mCanvas);*/


    }

    public void saveToFile(File file) {
        if (mMode == mode.SAVING && background != null) {
            FileOutputStream out;
            try {
                out = new FileOutputStream(file);
                mCanvas=new Canvas(background);



                AnnotationView.this.draw(mCanvas);
                for (Path fingerPath : mAnnotations) {
                    mCanvas.drawPath(fingerPath, paint);
                }
              //  setCanvasOnBitmap();
               /* Canvas now = new Canvas(background);
                //now.drawRect(new Rect(0, 0, background.getWidth(), background.getHeight()), bg);
                now.drawBitmap(background, new Rect(0, 0, width, height),
                        new Rect(0, 0, width,height), null);*/

                scaleImage(background.getWidth() / 2, background.getHeight() / 2);
                background.compress(Bitmap.CompressFormat.PNG, 70, out);
               /* */


                out.flush();
                out.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}