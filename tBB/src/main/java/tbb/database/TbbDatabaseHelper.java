package tbb.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import tbb.core.CoreController;
import tbb.touch.TouchRecognizer;

/**
 * Created by Anabela on 21/01/2016.
 */
public class TbbDatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "TbbDatabaseHelper";

    private static final String DATABASE_NAME = "tbbdb";

    private static final String TEXT_TYPE = " TEXT";
    private static final String INT_TYPE = " INTEGER";
    private static final String COMMA_SEP = ",";
    private static final String NOT_NULL = " NOT NULL";
    private static final String DEFAULT_NULL = " DEFAULT NULL";
    private static final String DEFAULT_ZERO = " DEFAULT 0";
    private static final String DEFAULT_NEGATIVE = " DEFAULT -1";


    /*CREATE AND DELETE USER TABLE*/
    private static final String SQL_CREATE_USER =
            "CREATE TABLE IF NOT EXISTS " + TbbContract.User.TABLE_NAME + " (" +
                    TbbContract.User._ID + INT_TYPE + " PRIMARY KEY," +
                    TbbContract.User.COLUMN_NAME_NAME + TEXT_TYPE + NOT_NULL + COMMA_SEP +
                    TbbContract.User.COLUMN_NAME_EMAIL + TEXT_TYPE + NOT_NULL +
                    " )";

    private static final String SQL_DELETE_USER =
            "DROP TABLE IF EXISTS " + TbbContract.User.TABLE_NAME;


    /*CREATE AND DELETE PACKAGE TABLE*/
    private static final String SQL_CREATE_PACKAGE =
            "CREATE TABLE IF NOT EXISTS " + TbbContract.Package.TABLE_NAME + " (" +
                    TbbContract.Package._ID + INT_TYPE + " PRIMARY KEY," +
                    TbbContract.Package.COLUMN_NAME_NAME + TEXT_TYPE + NOT_NULL + COMMA_SEP +
                    TbbContract.Package.COLUMN_NAME_SUBCATEGORY_ID + INT_TYPE + DEFAULT_NEGATIVE + COMMA_SEP +
                    TbbContract.Package.COLUMN_NAME_NUMBER_SESSIONS + INT_TYPE + DEFAULT_ZERO +
                    " )";

    private static final String SQL_DELETE_PACKAGE =
            "DROP TABLE IF EXISTS " + TbbContract.Package.TABLE_NAME;

    /*CREATE AND DELETE SESSION TABLE*/
    private static final String SQL_CREATE_SESSION =
            "CREATE TABLE IF NOT EXISTS " + TbbContract.Session.TABLE_NAME + " (" +
                    TbbContract.Session._ID + INT_TYPE + " PRIMARY KEY," +
                    TbbContract.Session.COLUMN_NAME_USER_ID + INT_TYPE + NOT_NULL + COMMA_SEP +
                    TbbContract.Session.COLUMN_NAME_START_TIMESTAMP + " DATETIME DEFAULT CURRENT_TIMESTAMP" + NOT_NULL + COMMA_SEP +
                    TbbContract.Session.COLUMN_NAME_END_TIMESTAMP + " DATETIME DEFAULT NULL" +
                    " )";

    private static final String SQL_DELETE_SESSION =
            "DROP TABLE IF EXISTS " + TbbContract.Session.TABLE_NAME;

    /*CREATE AND DELETE PACKAGE SESSION TABLE*/
    private static final String SQL_CREATE_PACKAGE_SESSION =
            "CREATE TABLE IF NOT EXISTS " + TbbContract.PackageSession.TABLE_NAME + " (" +
                    TbbContract.PackageSession._ID + INT_TYPE + " PRIMARY KEY," +
                    TbbContract.PackageSession.COLUMN_NAME_NAME + TEXT_TYPE + DEFAULT_NULL + COMMA_SEP +
                    TbbContract.PackageSession.COLUMN_NAME_SESSION_ID + INT_TYPE + NOT_NULL + COMMA_SEP +
                    TbbContract.PackageSession.COLUMN_NAME_PACKAGE_ID + INT_TYPE + NOT_NULL + COMMA_SEP +
                    TbbContract.PackageSession.COLUMN_NAME_START_TIMESTAMP + " DATETIME DEFAULT CURRENT_TIMESTAMP" + COMMA_SEP +
                    TbbContract.PackageSession.COLUMN_NAME_END_TIMESTAMP + " DATETIME" + DEFAULT_NULL + COMMA_SEP +
                    TbbContract.PackageSession.COLUMN_NAME_ORIENTATION_CHANGE_COUNT + INT_TYPE + DEFAULT_ZERO +

                    " )";

    private static final String SQL_DELETE_PACKAGE_SESSION =
            "DROP TABLE IF EXISTS " + TbbContract.PackageSession.TABLE_NAME;

    /*CREATE AND DELETE TOUCHTYPE TABLE*/
    private static final String SQL_CREATE_TOUCHTYPE =
            "CREATE TABLE IF NOT EXISTS " + TbbContract.TouchType.TABLE_NAME + " (" +
                    TbbContract.TouchType._ID + INT_TYPE + " PRIMARY KEY," +
                    TbbContract.TouchType.COLUMN_NAME_VALUE + INT_TYPE + NOT_NULL + COMMA_SEP +
                    TbbContract.TouchType.COLUMN_NAME_NAME + TEXT_TYPE + NOT_NULL +

                    " )";

    private static final String SQL_DELETE_TOUCHTYPE =
            "DROP TABLE IF EXISTS " + TbbContract.TouchType.TABLE_NAME;

    /*CREATE AND DELETE TOUCH SEQUENCE TABLE*/
    private static final String SQL_CREATE_TOUCH_SEQUENCE =
            "CREATE TABLE IF NOT EXISTS " + TbbContract.TouchSequence.TABLE_NAME + " (" +
                    TbbContract.TouchSequence._ID + INT_TYPE + " PRIMARY KEY," +
                    TbbContract.TouchSequence.COLUMN_NAME_PACKAGE_SESSION_ID + INT_TYPE + NOT_NULL + COMMA_SEP +
                    TbbContract.TouchSequence.COLUMN_NAME_SEQUENCE_NUMBER + INT_TYPE + DEFAULT_ZERO + COMMA_SEP +
                    TbbContract.TouchSequence.COLUMN_NAME_DEVICE + INT_TYPE + COMMA_SEP +
                    TbbContract.TouchSequence.COLUMN_NAME_START_TIMESTAMP + " DATETIME" + NOT_NULL + COMMA_SEP +
                    TbbContract.TouchSequence.COLUMN_NAME_END_TIMESTAMP + " DATETIME" + DEFAULT_NULL +
                    " )";

    private static final String SQL_DELETE_TOUCH_SEQUENCE =
            "DROP TABLE IF EXISTS " + TbbContract.TouchSequence.TABLE_NAME;

    /*CREATE AND DELETE TOUCHPOINT TABLE*/
    private static final String SQL_CREATE_TOUCHPOINT =
            "CREATE TABLE IF NOT EXISTS " + TbbContract.TouchPoint.TABLE_NAME + " (" +
                    TbbContract.TouchPoint._ID + INT_TYPE + " PRIMARY KEY," +
                    TbbContract.TouchPoint.COLUMN_NAME_TOUCH_SEQUENCE_ID + INT_TYPE + NOT_NULL + COMMA_SEP +
                    TbbContract.TouchPoint.COLUMN_NAME_TOUCH_TYPE_ID + INT_TYPE + NOT_NULL + COMMA_SEP +
                    TbbContract.TouchPoint.COLUMN_NAME_TREE_ID + INT_TYPE + NOT_NULL + COMMA_SEP +
                    TbbContract.TouchPoint.COLUMN_NAME_MULTITOUCH_POINT + INT_TYPE + NOT_NULL + COMMA_SEP +
                    TbbContract.TouchPoint.COLUMN_NAME_X + INT_TYPE + NOT_NULL + COMMA_SEP +
                    TbbContract.TouchPoint.COLUMN_NAME_Y + INT_TYPE + NOT_NULL + COMMA_SEP +
                    TbbContract.TouchPoint.COLUMN_NAME_PRESSURE + INT_TYPE + NOT_NULL + COMMA_SEP +
                    TbbContract.TouchPoint.COLUMN_NAME_DEVICE_TIME + INT_TYPE + COMMA_SEP +
                    TbbContract.TouchPoint.COLUMN_NAME_TIMESTAMP + " DATETIME DEFAULT CURRENT_TIMESTAMP" +
                    " )";

    private static final String SQL_DELETE_TOUCHPOINT =
            "DROP TABLE IF EXISTS " + TbbContract.TouchPoint.TABLE_NAME;


    /*CREATE AND DELETE ORIENTATION CHANGES TABLE*/
    private static final String SQL_CREATE_SCREEN_SPECS =
            "CREATE TABLE IF NOT EXISTS " + TbbContract.ScreenSpecs.TABLE_NAME + " (" +
                    TbbContract.ScreenSpecs._ID + INT_TYPE + " PRIMARY KEY," +

                    TbbContract.ScreenSpecs.COLUMN_NAME_SESSION_ID + INT_TYPE + NOT_NULL + COMMA_SEP +
                    TbbContract.ScreenSpecs.COLUMN_NAME_PACKAGE_SESSION_ID + INT_TYPE + DEFAULT_NULL + COMMA_SEP +
                    TbbContract.ScreenSpecs.COLUMN_NAME_TIMESTAMP + " DATETIME DEFAULT CURRENT_TIMESTAMP" + COMMA_SEP +
                    TbbContract.ScreenSpecs.COLUMN_NAME_DENSITY + INT_TYPE + DEFAULT_NULL + COMMA_SEP +
                    TbbContract.ScreenSpecs.COLUMN_NAME_DENSITY_DPI + INT_TYPE + DEFAULT_NULL + COMMA_SEP +
                    TbbContract.ScreenSpecs.COLUMN_NAME_WIDTH + INT_TYPE + DEFAULT_NULL + COMMA_SEP +
                    TbbContract.ScreenSpecs.COLUMN_NAME_HEIGHT + INT_TYPE + DEFAULT_NULL + COMMA_SEP +
                    TbbContract.ScreenSpecs.COLUMN_NAME_ORIENTATION + INT_TYPE + NOT_NULL +
                    " )";

    private static final String SQL_DELETE_SCREEN_SPECS =
            "DROP TABLE IF EXISTS " + TbbContract.ScreenSpecs.TABLE_NAME;

    private SQLiteDatabase tbbDB;

    private boolean sessionActive = false, packageSessionActive=false,sequenceLogging=false;
    private int userID,sessionID,packageSessionID,touchSequenceID,sequence;


    public TbbDatabaseHelper(Context context) {

        super(context, DATABASE_NAME, null, 1);
        tbbDB = getWritableDatabase();
        if (checkIfTouchTypeIsEmpty()) {
            Log.d(TAG, "Database creation complete!");
        }

    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "Creating database.");

        Log.d(TAG, "Executing " + SQL_CREATE_USER);
        db.execSQL(SQL_CREATE_USER);

        Log.d(TAG, "Executing " + SQL_CREATE_PACKAGE);
        db.execSQL(SQL_CREATE_PACKAGE);

        Log.d(TAG, "Executing " + SQL_CREATE_SESSION);
        db.execSQL(SQL_CREATE_SESSION);

        Log.d(TAG, "Executing " + SQL_CREATE_PACKAGE_SESSION);
        db.execSQL(SQL_CREATE_PACKAGE_SESSION);

        Log.d(TAG, "Executing " + SQL_CREATE_TOUCHTYPE);
        db.execSQL(SQL_CREATE_TOUCHTYPE);

        Log.d(TAG, "Executing " + SQL_CREATE_TOUCH_SEQUENCE);
        db.execSQL(SQL_CREATE_TOUCH_SEQUENCE);

        Log.d(TAG, "Executing " + SQL_CREATE_TOUCHPOINT);
        db.execSQL(SQL_CREATE_TOUCHPOINT);

        Log.d(TAG, "Executing " + SQL_CREATE_SCREEN_SPECS);
        db.execSQL(SQL_CREATE_SCREEN_SPECS);


    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_SCREEN_SPECS);
        db.execSQL(SQL_DELETE_TOUCHPOINT);
        db.execSQL(SQL_DELETE_TOUCH_SEQUENCE);
        db.execSQL(SQL_DELETE_TOUCHTYPE);
        db.execSQL(SQL_DELETE_PACKAGE_SESSION);
        db.execSQL(SQL_DELETE_SESSION);
        db.execSQL(SQL_DELETE_PACKAGE);
        db.execSQL(SQL_DELETE_USER);
        onCreate(db);


    }

    public void closeDB(){
        tbbDB.close();
    }

    /* DATABASE OPERATIONS */

    public boolean checkIfTouchTypeIsEmpty() {
        Cursor cursor = tbbDB.rawQuery("SELECT * FROM " + TbbContract.TouchType.TABLE_NAME, null);
        if (!cursor.moveToFirst()) {
            return setTouchTypes();
        }
        return true;
    }


    /*TODO IS THIS TABLE NECESSARY??*/
    public boolean setTouchTypes() {
        Log.d(TAG, "Inserting touch type data into database.");
        ContentValues contentValues = new ContentValues();
        contentValues.put(TbbContract.TouchType.COLUMN_NAME_VALUE, TouchRecognizer.DOWN);
        contentValues.put(TbbContract.TouchType.COLUMN_NAME_NAME, "DOWN");
        tbbDB.insert(TbbContract.TouchType.TABLE_NAME, null, contentValues);

        ContentValues contentValues2 = new ContentValues();
        contentValues2.put(TbbContract.TouchType.COLUMN_NAME_VALUE, TouchRecognizer.MOVE);
        contentValues2.put(TbbContract.TouchType.COLUMN_NAME_NAME, "MOVE");
        tbbDB.insert(TbbContract.TouchType.TABLE_NAME, null, contentValues2);

        ContentValues contentValues3 = new ContentValues();
        contentValues3.put(TbbContract.TouchType.COLUMN_NAME_VALUE, TouchRecognizer.UP);
        contentValues3.put(TbbContract.TouchType.COLUMN_NAME_NAME, "UP");
        tbbDB.insert(TbbContract.TouchType.TABLE_NAME, null, contentValues3);

        return true;
    }

    public int authenticateOrRegisterUser(String name, String email){
        Log.d(TAG,"Checking if user "+name+" is in tbbDB ...");

        Cursor cursor = tbbDB.query(TbbContract.User.TABLE_NAME,
                new String[] { TbbContract.User._ID },
                TbbContract.User.COLUMN_NAME_EMAIL+"=?",  new String[] { email }, null, null, null);

        if(cursor.moveToNext()){
            Log.d(TAG,name+" is in DB.");
            return cursor.getInt(0);
        } else{
            //create user
            Log.d(TAG,name+" is not in DB. Registering ...");
            ContentValues contentValues = new ContentValues();
            contentValues.put(TbbContract.User.COLUMN_NAME_NAME, name);
            contentValues.put(TbbContract.User.COLUMN_NAME_EMAIL, email);
            return (int) tbbDB.insert(TbbContract.User.TABLE_NAME, null, contentValues);
        }


    }

    public int startSession(int userID, long timestamp, float density, int densityDpi, int width, int height, int orientation) {
        Log.d(TAG,"Registering new Session at "+timestamp+"...");
        //register active session id
        ContentValues contentValues = new ContentValues();
        contentValues.put(TbbContract.Session.COLUMN_NAME_USER_ID,userID);
        contentValues.put(TbbContract.Session.COLUMN_NAME_START_TIMESTAMP,timestamp);
        sessionID = (int) tbbDB.insert(TbbContract.Session.TABLE_NAME, null, contentValues);


        Log.d(TAG,"Session registration complete. ID is "+sessionID+".");
        if(sessionID > 0){
            Log.d(TAG,"Registering device data. Values: density:"+density+" densityDpi:"+
            densityDpi+" Screen width:"+width+" Screen height:"+height+ "Initial orientation:"+
            orientation+".");
            ContentValues contentValues2 = new ContentValues();
            contentValues2.put(TbbContract.ScreenSpecs.COLUMN_NAME_SESSION_ID,sessionID);
            contentValues2.put(TbbContract.ScreenSpecs.COLUMN_NAME_TIMESTAMP,timestamp);
            contentValues2.put(TbbContract.ScreenSpecs.COLUMN_NAME_DENSITY,density);
            contentValues2.put(TbbContract.ScreenSpecs.COLUMN_NAME_DENSITY_DPI,densityDpi);
            contentValues2.put(TbbContract.ScreenSpecs.COLUMN_NAME_WIDTH, width);
            contentValues2.put(TbbContract.ScreenSpecs.COLUMN_NAME_HEIGHT,height);
            contentValues2.put(TbbContract.ScreenSpecs.COLUMN_NAME_ORIENTATION,orientation);
            tbbDB.insert(TbbContract.ScreenSpecs.TABLE_NAME, null, contentValues2);

            sessionActive = true;

            Log.d(TAG,"Session is active.");
        }
        return sessionID;
    }

    public void endSession(long timestamp) {
        Log.d(TAG,"Ending Session ...");
        //get timestamp?
        if(sessionActive) {
            endPackageSession(timestamp);

            ContentValues contentValues = new ContentValues();
            contentValues.put(TbbContract.Session.COLUMN_NAME_END_TIMESTAMP,timestamp);
            String where = TbbContract.Session._ID+"=?";
            String[] whereArgs = new String[]{String.valueOf(sessionID)};

            tbbDB.update(TbbContract.Session.TABLE_NAME, contentValues, where, whereArgs);
            sessionActive = false;
            Log.d(TAG,"Session Ended.");
        }
    }

    public int createPackageIfNotExists(String packageName) {
        Log.d(TAG,"Checking if package exists in DB ...");

        Cursor cursor = tbbDB.query(TbbContract.Package.TABLE_NAME,
                new String[] { TbbContract.Package._ID },
                TbbContract.Package.COLUMN_NAME_NAME+"=?",  new String[] { packageName }, null, null, null);

        int id = -1;
        if(cursor.moveToNext()){
            Log.d(TAG,"Package already exists in DB.");
            id= cursor.getInt(0);
        }

        if(id<1){

            Log.d(TAG,"Package doesn't exist in DB. Creating ...");
            //doesnt exist; create
            ContentValues contentValues = new ContentValues();
            contentValues.put(TbbContract.Package.COLUMN_NAME_NAME,packageName);
            contentValues.put(TbbContract.Package.COLUMN_NAME_NUMBER_SESSIONS, 0);
            id = (int)tbbDB.insert(TbbContract.Package.TABLE_NAME,null,contentValues);
        }

        Log.d(TAG,"Package ID is "+id+".");
        return id;
    }


    public void createNewPackageSession(String packageName, long timestamp) {
        Log.d(TAG,"Creating a new package session at "+timestamp+". Session active? "+sessionActive+
        ". Package name is "+packageName+".");
        if(sessionActive) {
            int id = createPackageIfNotExists(packageName);

            ContentValues contentValues = new ContentValues();
            contentValues.put(TbbContract.PackageSession.COLUMN_NAME_PACKAGE_ID, id);
            contentValues.put(TbbContract.PackageSession.COLUMN_NAME_SESSION_ID, sessionID);
            contentValues.put(TbbContract.PackageSession.COLUMN_NAME_ORIENTATION_CHANGE_COUNT,0);
            contentValues.put(TbbContract.PackageSession.COLUMN_NAME_START_TIMESTAMP,timestamp);

            packageSessionID = (int) tbbDB.insert(TbbContract.PackageSession.TABLE_NAME,null,contentValues);
            packageSessionActive = true;

            Log.d(TAG,"Package session with ID "+packageSessionID+" is active.");
        }
    }

    public void endPackageSession(long timestamp) {
        //get active session
        if(packageSessionActive) {
            Log.d(TAG,"Ending packageSession "+packageSessionID+" at "+timestamp+".");

            ContentValues contentValues = new ContentValues();
            contentValues.put(TbbContract.PackageSession.COLUMN_NAME_END_TIMESTAMP,timestamp);
            String where = TbbContract.PackageSession._ID+"=?";
            String[] whereArgs = new String[]{String.valueOf(packageSessionID)};

            tbbDB.update(TbbContract.PackageSession.TABLE_NAME, contentValues, where, whereArgs);
            packageSessionActive = false;
            touchSequenceID = -1;
        }
    }

    public void changeOrientation(long timestamp, int orientation) {

        if(sessionID > 0){
            Log.d(TAG,"Orientation change in session "+sessionID+" at "+timestamp+" to orientation "+orientation);
            ContentValues contentValues = new ContentValues();
            contentValues.put(TbbContract.ScreenSpecs.COLUMN_NAME_SESSION_ID,sessionID);
            contentValues.put(TbbContract.ScreenSpecs.COLUMN_NAME_TIMESTAMP,timestamp);
            contentValues.put(TbbContract.ScreenSpecs.COLUMN_NAME_ORIENTATION, orientation);
            if(packageSessionActive){
                contentValues.put(TbbContract.ScreenSpecs.COLUMN_NAME_PACKAGE_SESSION_ID,packageSessionID);
            }
            tbbDB.insert(TbbContract.ScreenSpecs.TABLE_NAME, null, contentValues);
        }
    }

    public void logIO(int treeID,int device,int touchType,int multitouchID,int x,int y,int pressure,int devTime,long sysTime){
        if(sessionActive && packageSessionActive){


            switch(touchType){
                case TouchRecognizer.UP: //end touch; check if theres any other active touches in tpr
                    if(!CoreController.sharedInstance().getActiveTPR().checkIfMultiTouch(multitouchID)){
                        //no more touch points; sequence ends

                        sequenceLogging = false;
                        endSequence();
                    }

                    break;

                case TouchRecognizer.DOWN:
                    //see if a sequence is already happening, else create new
                    if(!sequenceLogging ){
                        if(touchSequenceID>0){
                            touchSequenceID=createNewSequence(device);

                        } else{
                            //get from db
                            touchSequenceID = createFirstSequence(device);
                        }
                        sequenceLogging = true;
                        //get last sequence, add 1
                    }
                    break;
            }

            //log the touchpoint from the id we got above
            if(touchSequenceID>0) {
                Log.d(TAG,"Logging into tbbDB touch point in sequence "+touchSequenceID+
                        " with touch type "+touchType+" and multitouchID "+multitouchID+
                        ". Values: treeID:"+treeID+" x:"+x+" y:"+y+
                        " pressure:"+pressure+" devTime:"+devTime+" timestamp:"+sysTime);
                ContentValues contentValues = new ContentValues();
                contentValues.put(TbbContract.TouchPoint.COLUMN_NAME_TOUCH_SEQUENCE_ID, touchSequenceID);
                contentValues.put(TbbContract.TouchPoint.COLUMN_NAME_TOUCH_TYPE_ID, touchType);
                contentValues.put(TbbContract.TouchPoint.COLUMN_NAME_TREE_ID, treeID);
                contentValues.put(TbbContract.TouchPoint.COLUMN_NAME_MULTITOUCH_POINT, multitouchID);
                contentValues.put(TbbContract.TouchPoint.COLUMN_NAME_X, x);
                contentValues.put(TbbContract.TouchPoint.COLUMN_NAME_Y, y);
                contentValues.put(TbbContract.TouchPoint.COLUMN_NAME_PRESSURE, pressure);
                contentValues.put(TbbContract.TouchPoint.COLUMN_NAME_DEVICE_TIME, devTime);
                contentValues.put(TbbContract.TouchPoint.COLUMN_NAME_TIMESTAMP, sysTime);
                tbbDB.insert(TbbContract.TouchPoint.TABLE_NAME, null, contentValues);
            }
        }
    }


    //create first sequence of this packagesession
    private int createFirstSequence(int device){
        int id = -1;

        Log.d(TAG,"Creating the first sequence for packageSession "+packageSessionID);

        ContentValues contentValues = new ContentValues();
        contentValues.put(TbbContract.TouchSequence.COLUMN_NAME_DEVICE,device);

        contentValues.put(TbbContract.TouchSequence.COLUMN_NAME_START_TIMESTAMP,System.currentTimeMillis());
        contentValues.put(TbbContract.TouchSequence.COLUMN_NAME_PACKAGE_SESSION_ID,packageSessionID);
        contentValues.put(TbbContract.TouchSequence.COLUMN_NAME_SEQUENCE_NUMBER,1);
        id = (int) tbbDB.insert(TbbContract.TouchSequence.TABLE_NAME, null, contentValues);

        sequence = 1;

        return id;
    }

    //create a new sequence
    private int createNewSequence(int device){
        int id = -1;

        Log.d(TAG,"Creating a new sequence for packageSession "+packageSessionID);
        sequence++;
        ContentValues contentValues = new ContentValues();
        contentValues.put(TbbContract.TouchSequence.COLUMN_NAME_DEVICE,device);

        contentValues.put(TbbContract.TouchSequence.COLUMN_NAME_START_TIMESTAMP,System.currentTimeMillis());
        contentValues.put(TbbContract.TouchSequence.COLUMN_NAME_PACKAGE_SESSION_ID,packageSessionID);
        contentValues.put(TbbContract.TouchSequence.COLUMN_NAME_SEQUENCE_NUMBER,sequence);
        id = (int) tbbDB.insert(TbbContract.TouchSequence.TABLE_NAME, null, contentValues);



        return id;
    }

    private void endSequence(){
        Log.d(TAG,"Ending sequence "+sequence+" with ID "+touchSequenceID);
        ContentValues contentValues = new ContentValues();
        contentValues.put(TbbContract.TouchSequence.COLUMN_NAME_END_TIMESTAMP,System.currentTimeMillis());
        String where = TbbContract.TouchSequence._ID+"=?";
        String[] whereArgs = new String[]{String.valueOf(touchSequenceID)};

        tbbDB.update(TbbContract.TouchSequence.TABLE_NAME, contentValues, where, whereArgs);

    }
}

