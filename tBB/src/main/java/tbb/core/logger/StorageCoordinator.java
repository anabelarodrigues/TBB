package tbb.core.logger;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

import blackbox.external.logger.BaseLogger;
import tbb.core.CoreController;
import tbb.core.service.TBBService;
import tbb.core.service.configuration.DataPermissions;


/**
 * Created by kyle montague on 10/11/2014.
 */
public class StorageCoordinator extends BroadcastReceiver {
	private static String SUBTAG = "StorageCoordinator: ";

	static String PREF_SEQUENCE_NUMBER = "BB.STORAGECOORDINATOR.PREFERENCE.SEQUENCE_NUMBER";

	private SharedPreferences mPref;
	private static int mSequence;
	private static String mAdjust=null;
	// is charging
	private static boolean isCharging = false;
	private String folderPath = null;


	@Override
	public void onReceive(Context context, Intent intent) {
		try {
			
			//on service init
			if (intent.getAction().equals(TBBService.ACTION_SCREEN_ON)
					||intent.getAction().equals(CoreController.ACTION_INIT)) {

                Log.d("debug","storagecoordinator init");
				mPref = PreferenceManager
						.getDefaultSharedPreferences(CoreController
								.sharedInstance()
                                .getTBBService());

				mSequence = mPref.getInt(PREF_SEQUENCE_NUMBER, 0);
				mAdjust = adjust(mSequence);

				// CREATE FOLDER
				File folder = new File(TBBService.STORAGE_FOLDER);
				if (!folder.exists())
					folder.mkdirs();

				Intent intentUpdate = new Intent();
				intentUpdate.putExtra(BaseLogger.EXTRAS_FOLDER_PATH, TBBService.STORAGE_FOLDER);
				intentUpdate.putExtra(BaseLogger.EXTRAS_SEQUENCE, "" + mAdjust);

				intentUpdate.setAction(BaseLogger.ACTION_UPDATE);

				intentUpdate.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);

				context.sendBroadcast(intentUpdate);


				// UPDATE THE SEQUENCE NUMBER FOR NEXT TIME
				mPref.edit().putInt(PREF_SEQUENCE_NUMBER, mSequence + 1)
						.commit();

			}
			else if (intent.getAction().equals(AssistivePlay.ACTION_APP_RESUME)) {


				String packageName = intent.getStringExtra("packageName");
				long timeStamp = System.currentTimeMillis();

				Log.d("DEBUG", "BROADCAST RECEIVED: APP_RESUME: package name:"+packageName+" timestamp:"+timeStamp);

				//write into messagelogger
				MessageLogger.sharedInstance().writeAsync("\"APP_RESUME\",\"packageName\":\"" + packageName + "\",\"timestamp\":\"" + timeStamp+"\"");
				MessageLogger.sharedInstance().onFlush();


				//set file name for logging
				Intent intentUpdate = new Intent();
				intentUpdate.putExtra(BaseLogger.EXTRAS_PACKAGE_NAME, packageName);

				intentUpdate.setAction(BaseLogger.ACTION_IO_UPDATE);

				intentUpdate.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);

				context.sendBroadcast(intentUpdate);


				//starts monitoring touch
                CoreController.sharedInstance().startServiceNoBroadCast();

				// CREATE FOLDER
				/*folderPath = TBBService.STORAGE_FOLDER + "/Packages/"+ packageName;
				File folder = new File(folderPath);
				String filePath="";
*/



				// UPDATE THE SEQUENCE NUMBER
				//mPref.edit().putInt(PREF_SEQUENCE_NUMBER, mSequence ).commit();

			}
			else if (intent.getAction().equals(AssistivePlay.ACTION_APP_PAUSE)) {
				String packageName = intent.getStringExtra("packageName");
				long timeStamp = intent.getLongExtra("timestamp", -1);

				Log.d("DEBUG", "BROADCAST RECEIVED: APP_PAUSE: package name:" + packageName + " timestamp:" + timeStamp);

				Intent intentFlush = new Intent();
				intentFlush.setAction(BaseLogger.ACTION_FLUSH);
				intentFlush.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
				context.sendBroadcast(intentFlush);

				//stop logging
				CoreController.sharedInstance().stopServiceNoBroadCast();


				//write into messagelogger
				MessageLogger.sharedInstance().writeAsync("\"APP_PAUSE\",\"packageName\":\"" + packageName + "\",\"timestamp\":\"" + timeStamp + "\"");
				MessageLogger.sharedInstance().onFlush();




			}
			else if (intent.getAction().equals(TBBService.ACTION_SCREEN_OFF)
					|| intent.getAction().equals(CoreController.ACTION_STOP)) {
				
				Log.v(TBBService.TAG, SUBTAG + "ACTION_STOP");

				//Logs first screen off
				if(!TBBService.isRunning){
					MessageLogger.sharedInstance().writeAsync("\"TBB Service init\"");
					MessageLogger.sharedInstance().onFlush();
					TBBService.isRunning=true;
				}

				if(CoreController.sharedInstance().getIOLogging()){
					Intent intentFlush = new Intent();
					intentFlush.setAction(BaseLogger.ACTION_FLUSH);
					intentFlush.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
					context.sendBroadcast(intentFlush);

					//stop logging
					CoreController.sharedInstance().stopServiceNoBroadCast();
				}

				if(DataPermissions.getSharedInstance(context).loggingMode()!=DataPermissions.type.DO_NOT_LOG) {
					// encrypt all descriptions and text while charging and screen
					// off or if it has passed 3 days and battery at 90+%
					Encryption.sharedInstance().encryptFolders(isCharging, context, mSequence);
				}

				// Tell the cloud storage to sync
				//TODO later use this
				/*CloudStorage.sharedInstance().cloudSync(
						TBBService.STORAGE_FOLDER, mSequence, false);*/
				


			}


			//TODO do we need to distinguish between landscapes? left/right
			else if(intent.getAction().equals(TBBService.ACTION_CONFIGURATION_CHANGED)){
				Log.d("DEBUG", "RECEIVED CONFIG CHANGED");
				//orientation check
				if(CoreController.sharedInstance().getTBBService().getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE
						&& !CoreController.sharedInstance().landscape){
					// it's Landscape
					Log.d("DEBUG", "LANDSCAPE");
					CoreController.sharedInstance().landscape = true;
					MessageLogger.sharedInstance().writeAsync("\"ORIENTATION_CHANGE\",\"timestamp\":\"" + System.currentTimeMillis() + "\"," +
							"\"orientation\":\"" + CoreController.sharedInstance().getTBBService().getResources().getConfiguration().orientation + "\"");
					MessageLogger.sharedInstance().onFlush();
				}
				else if (CoreController.sharedInstance().getTBBService().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT
						&& CoreController.sharedInstance().landscape){

					Log.d("DEBUG", "PORTRAIT");
					CoreController.sharedInstance().landscape = false;
					MessageLogger.sharedInstance().writeAsync("\"ORIENTATION_CHANGE\",\"timestamp\":\"" + System.currentTimeMillis() + "\"," +
							"\"orientation\":\"" + CoreController.sharedInstance().getTBBService().getResources().getConfiguration().orientation + "\"");
					MessageLogger.sharedInstance().onFlush();
				}
			}
			else if (intent.getAction()
					.equals(BaseLogger.ACTION_SEND_REQUEST)) {
				// An external logger has been started manually and is
				// requesting the current sequence and location info.
				Log.v(TBBService.TAG, SUBTAG + "received location request");


				// ANNOUNCE THE FOLDER AND SEQUENCE
				Intent intentLocation = new Intent();
				intentLocation.putExtra(BaseLogger.EXTRAS_FOLDER_PATH,
						folderPath);
				intentLocation.putExtra(BaseLogger.EXTRAS_SEQUENCE, ""
						+ mSequence);
				intentLocation.setAction(BaseLogger.ACTION_LOCATION);
				intentLocation.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
				context.sendBroadcast(intentLocation);

			} else if (intent.getAction().equals(
					TBBService.ACTION_POWER_CONNECTED)) {
				isCharging = true;
			} else if (intent.getAction().equals(
					TBBService.ACTION_POWER_DISCONNECTED)) {
				isCharging = false;

			}

		} catch (Exception e) {
			Toast.makeText(CoreController.sharedInstance().getTBBService(),
					"TBB Exception", Toast.LENGTH_LONG).show();
			TBBService.writeToErrorLog(e);
			Log.e("LISTERROR",e.toString());
		}
		
		
	}


	/*
    private JSONObject parseJSONFile(File file,String folderPath){
        JSONObject json = null;
        //fazemos já a contar com q possam existir varios ficheiros
        ArrayList<String> messageLoggerFiles = getList(file,folderPath);

        for(String messageLogger : messageLoggerFiles){
            InputStream inputStream = null;
            Log.d("DEBUG","iterating: "+messageLogger);
            try {
                String ret = "";
                inputStream = new FileInputStream(messageLogger);

                if (inputStream != null) {
                    InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                    String receiveString = "";
                    StringBuilder stringBuilder = new StringBuilder();

                    while ((receiveString = bufferedReader.readLine()) != null) {
                        stringBuilder.append(receiveString);
                    }

                    inputStream.close();
                    ret = stringBuilder.toString();
                    Log.d("DEBUG","MessageLogger content: "+ret);
                    JSONObject temp = new JSONObject(ret);

                    Log.d("DEBUG","json: "+temp.toString());
					//worked. parse json.
                    //grab anything that is app_resume,app_pause,orientation_change within these timestamps and add to final json
                    //wouldnt it be better to originally write the file like this..? we'd still need to parse orientation changes

					//stream input

                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }

        }

        return json;
    }

	*/
	//may still be useful

    private ArrayList<String> getList(File parentDir, String pathToParentDir) {

        ArrayList<String> inFiles = new ArrayList<String>();
		if(parentDir.exists()) {
			String[] fileNames = parentDir.list();

			if (fileNames != null && fileNames.length > 0) {
				for (String fileName : fileNames) {
					if (fileName.toLowerCase().endsWith(".json")) {
						Log.d("DEBUG", "file to get: "+pathToParentDir + "/" + fileName);
						inFiles.add(fileName);
					}

				}
			}
			return inFiles;
		}
		return null;
    }

	private String adjust(int sequence) {

		if (sequence < 10) {
			return "0000" + sequence;
		} else if (sequence < 100) {
			return "000" + sequence;
		} else if (sequence < 1000)
			return "00" + sequence;
		else if (sequence < 10000)
			return "0" + sequence;

		return "" + sequence;

	}



	public static boolean createDirIfNotExists(String path) {
		boolean ret = true;

		File file = new File(path);
	//Log.d("FILE PATH",file.getPath()+" abspath:" + file.getAbsolutePath());

		if (!file.exists()) {
			if (!file.mkdirs()) {
				Log.e("Mkdir Error: ", "Problem creating "+path+" folder");
				ret = false;
			}
		}

		return ret;
	}

}
