package tbb.view.analitics;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Anabela on 23/02/2016.
 */
public class CanvasView extends View {
    public int width;
    public int height;
    private Bitmap mBitmap;
    private Canvas mCanvas;
    private ArrayList<Path> pathList;
    private Paint mPaint,mDrawPaint;

    //draw


    public CanvasView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public CanvasView(Context context) {
        super(context);
    }

    public void setmPath(ArrayList<Path> paths) {
        Log.d("debug","SETTING PATH; paths size is: "+paths.size());

        pathList=paths;

        invalidate();

    }


    @Override
    protected void onDraw(Canvas canvas) {
     //   canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
        canvas.drawColor(Color.WHITE);
        if(pathList!= null && pathList.size()>0) {
            for (Path path : pathList) {
                //   path.close();
              //  mBitmap.setDensity(0);
                Log.d("debug", "Iterating .. BMheight:" + mBitmap.getScaledHeight(mCanvas) +
                        " BMwidth:" + mBitmap.getScaledWidth(mCanvas) + " BMdensity:" + mBitmap.getDensity());
                // mCanvas.drawPaint(mPaint);
              //  canvas.drawColor(Color.WHITE);
               // canvas.drawBitmap( mBitmap, 0, 0, mBitmapPaint);
                canvas.drawPath(path, mPaint);


            }
        }

        //canvas.drawPath(virtualPaths,mDrawPaint);
        //canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
    }


    public void initialize() {

       // pathList = new ArrayList<>();
        // and we set a new Paint with the desired attributes
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(Color.CYAN);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeWidth(4);
        mPaint.setDither(true);
        mPaint.setStrokeCap(Paint.Cap.ROUND);

        mDrawPaint = new Paint();
        mDrawPaint.setAntiAlias(true);
        mDrawPaint.setColor(Color.GREEN);
        mDrawPaint.setStyle(Paint.Style.STROKE);
        mDrawPaint.setStrokeJoin(Paint.Join.ROUND);
        mDrawPaint.setStrokeWidth(4);
        mDrawPaint.setDither(true);
        mDrawPaint.setStrokeCap(Paint.Cap.ROUND);

       // mBitmapPaint = new Paint(Paint.DITHER_FLAG);
      //  mBitmapPaint.setColor(Color.WHITE);


    }

    // override onSizeChanged

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        // your Canvas will draw onto the defined Bitmap
        Log.d("debug", "CanvasView size has changed! w:"+w+" h:"+h);
        mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
       // Bitmap.createScaledBitmap(mBitmap, w, h, true);
        mCanvas = new Canvas(mBitmap);
        this.height = h;
        this.width = w;
        Log.d("debug","CanvasView size is w:"+mCanvas.getClipBounds().right+" h:"+
                mCanvas.getClipBounds().bottom);
        //mCanvas.scale(xScale,yScale);
    }



    public void clearCanvas(){
        for (Path path : pathList) {
            path.reset();
        }

        invalidate();
    }

    //this works for some reason yay
    public void setCanvasOnBitmap() {
        //Define a bitmap with the same size as the view
       /* Bitmap returnedBitmap = Bitmap.createBitmap(CanvasView.this.getWidth(),
                CanvasView.this.getHeight(),Bitmap.Config.ARGB_8888);
        //Bind a canvas to it
        Canvas canvas = new Canvas(returnedBitmap);*/
        //Get the view's background

        Drawable bgDrawable =CanvasView.this.getBackground();
        if (bgDrawable!=null)
            //has background drawable, then draw it on the canvas
            bgDrawable.draw(mCanvas);
        //else
            //does not have background drawable, then draw white background on the canvas
 //           canvas.drawColor(Color.GREEN);
        // draw the view on the canvas
        CanvasView.this.draw(mCanvas);


        //return the bitmap
       // return returnedBitmap;
    }

    public void saveToFile(File file){
        FileOutputStream out = null;
      //  Bitmap bitmapToSave = Bitmap.createBitmap(mBitmap.getWidth(),mBitmap.getHeight(),mBitmap.getConfig());

        try {
            out = new FileOutputStream(file);
            setCanvasOnBitmap();
            Bitmap toSave = Bitmap.createScaledBitmap(mBitmap, mBitmap.getWidth()/2,
                    mBitmap.getHeight()/2, false);
            toSave.compress(Bitmap.CompressFormat.PNG, 70, out);

            out.flush();
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }






}
