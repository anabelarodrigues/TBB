package tbb.database;

import android.provider.BaseColumns;

/**
 * Created by Anabela on 21/01/2016.
 */
public class TbbContract {

    public TbbContract() {}


    public static abstract class User implements BaseColumns {
        public static final String TABLE_NAME = "user";
        public static final String COLUMN_NAME_NAME = "name";
        public static final String COLUMN_NAME_EMAIL = "email";
    }

    public static abstract class Package implements BaseColumns {
        public static final String TABLE_NAME = "package";
        public static final String COLUMN_NAME_NAME = "name";
        public static final String COLUMN_NAME_SUBCATEGORY_ID = "subcategoryID"; // can be null
        public static final String COLUMN_NAME_NUMBER_SESSIONS = "numberSessions"; // can be null
    }

    public static abstract class Session implements BaseColumns {
        public static final String TABLE_NAME = "session";
        public static final String COLUMN_NAME_START_TIMESTAMP = "startTime";
        public static final String COLUMN_NAME_END_TIMESTAMP = "endTime"; // can be null
        public static final String COLUMN_NAME_USER_ID = "userID";
    }



    public static abstract class PackageSession implements BaseColumns {
        public static final String TABLE_NAME = "packagesession";
        public static final String COLUMN_NAME_NAME = "name";
        public static final String COLUMN_NAME_SESSION_ID = "sessionID";
        public static final String COLUMN_NAME_PACKAGE_ID = "packageID";
        public static final String COLUMN_NAME_START_TIMESTAMP = "startTime";
        public static final String COLUMN_NAME_END_TIMESTAMP = "endTime"; // can be null
        public static final String COLUMN_NAME_ORIENTATION_CHANGE_COUNT = "orientationCount";

    }

    public static abstract class TouchType implements BaseColumns {
        public static final String TABLE_NAME = "touchType";
        public static final String COLUMN_NAME_NAME = "name";
        public static final String COLUMN_NAME_VALUE = "value";
    }

    public static abstract class TouchPoint implements BaseColumns {
        public static final String TABLE_NAME = "touchPoint";
        public static final String COLUMN_NAME_TOUCH_SEQUENCE_ID = "touchSequenceID";
        public static final String COLUMN_NAME_TOUCH_TYPE_ID = "touchTypeID";
        public static final String COLUMN_NAME_TREE_ID = "treeID";
        public static final String COLUMN_NAME_MULTITOUCH_POINT = "multitouchPoint"; //this should really be slot
        public static final String COLUMN_NAME_X = "x";
        public static final String COLUMN_NAME_Y = "y";
        public static final String COLUMN_NAME_PRESSURE = "pressure";
        public static final String COLUMN_NAME_DEVICE_TIME = "devTime";
        public static final String COLUMN_NAME_TIMESTAMP = "timestamp";

    }

    public static abstract class ScreenSpecs implements BaseColumns {
        public static final String TABLE_NAME = "screenSpecs";
        public static final String COLUMN_NAME_SESSION_ID = "sessionID";
        public static final String COLUMN_NAME_PACKAGE_SESSION_ID = "packageSessionID";
        public static final String COLUMN_NAME_TIMESTAMP = "timestamp";
        public static final String COLUMN_NAME_DENSITY = "density";
        public static final String COLUMN_NAME_DENSITY_DPI= "densityDpi";
        public static final String COLUMN_NAME_WIDTH= "width";
        public static final String COLUMN_NAME_HEIGHT = "height";
        public static final String COLUMN_NAME_ORIENTATION = "orientation";

    }

    public static abstract class TouchSequence implements BaseColumns {
        public static final String TABLE_NAME = "touchSequence";
        public static final String COLUMN_NAME_PACKAGE_SESSION_ID = "packageSessionID";
        public static final String COLUMN_NAME_DEVICE = "device";

        public static final String COLUMN_NAME_SEQUENCE_NUMBER = "sequenceNumber";
        public static final String COLUMN_NAME_START_TIMESTAMP = "startTime";
        public static final String COLUMN_NAME_END_TIMESTAMP = "endTime";
    }

}
