package blackbox.external.logger;

import android.content.Context;

/**
 * Created by hugonicolau on 13/11/2014.
 */
public interface BaseLogger {

    public abstract void onStorageUpdate(String path, String sequence);
    public abstract void onLocationReceived(String path, String sequence);
    public abstract void onFlush();
    public abstract void start(Context context);
    public abstract void stop();

    public static String TAG = "BLACKBOX";

    public static String ACTION_UPDATE = "BB.STORAGECOORDINATOR.ACTION.UPDATE";
    public static String ACTION_IO_UPDATE = "BB.STORAGECOORDINATOR.ACTION.IO_UPDATE";
    public static String ACTION_SEND_REQUEST = "BB.STORAGECOORDINATOR.ACTION.LOCATION_REQUEST";
    public static String ACTION_LOCATION = "BB.STORAGECOORDINATOR.ACTION.LOCATION";
    public static String ACTION_FLUSH = "BB.STORAGECOORDINATOR.ACTION.FLUSH";
    public static final String ACTION_STOP = "BB.ACTION.CORECONTROLLER.STOP";
    public static String EXTRAS_FOLDER_PATH = "BB.STORAGECOORDINATOR.EXTRA_FOLDER_PATH";
    public static String EXTRAS_SEQUENCE = "BB.STORAGECOORDINATOR.EXTRA_SEQUENCE";
    public static String EXTRAS_PACKAGE_NAME = "BB.STORAGECOORDINATOR.PACKAGE_NAME";

    public static String EXTRAS_TIMESTAMP = "BB.STORAGECOORDINATOR.TIMESTAMP";

}
