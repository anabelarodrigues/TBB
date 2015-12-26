package blackbox.external.logger;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Created by hugonicolau on 13/11/2014.
 * 
 * Writes data to log file
 */

public class DataWriter extends AsyncTask<String, Void, String> {

	private final static String SUBTAG = "DataWriter: ";
	public static final String ACTION_ANALYSE_DATA = "BB.ACTION.ANALYSE_DATA";

	private String mFilePath;
	private String mFolderPath;
	private File rename;
	private boolean toAppend;
	private final String mJsonHeader="{\"records\":[";
	private final String mJsonEnd="{}]}";
	private boolean headers=true;
	private Context loggerContext=null;

	public DataWriter(String folderPath, String filePath, boolean toAppend) {
		mFilePath = filePath;
		mFolderPath = folderPath;
		rename = null;
		this.toAppend = toAppend;
	}

	public DataWriter(String folderPath, String filePath, boolean toAppend, File renamePostExecute) {
		mFilePath = filePath;
		mFolderPath = folderPath;
		rename = renamePostExecute;
		this.toAppend = toAppend;
	}

	public DataWriter(String folderPath, String filePath, boolean toAppend, File renamePostExecute, boolean headers) {
		mFilePath = filePath;
		mFolderPath = folderPath;
		rename = renamePostExecute;
		this.toAppend = toAppend;
		this.headers=headers;
	}

	public DataWriter(String folderPath, String filePath, boolean toAppend, Context context) {
		mFilePath = filePath;
		mFolderPath = folderPath;
		rename = null;
		this.toAppend = toAppend;
		loggerContext = context;
	}

	@Override
	protected String doInBackground(String... data) {

		if (data != null && data.length > 0) {

			// creates file
			writeFile(data, toAppend);
		}

		return null;
	}

	@Override
	protected void onPostExecute(String result) {
		if (rename != null) {
			File file = new File(mFilePath);
			file.renameTo(rename);
			Log.v(BaseLogger.TAG, SUBTAG + "Successfully renamed");
		}
		if(loggerContext!=null){
			Intent toBroadcastIntent = new Intent();
			toBroadcastIntent.setAction(ACTION_ANALYSE_DATA);
			loggerContext.sendBroadcast(toBroadcastIntent);
		}

	}

	private void writeFile(String[] data, boolean toAppend) {

		if (mFolderPath != null && !mFolderPath.isEmpty() && mFilePath!=null) {
			// creates folder
			File folder = new File(mFolderPath);
			if (!folder.exists()) {
				folder.mkdirs();
			}
			
			File file = new File(mFilePath);
			String header="";
			//if file does not exist add json header
			if(!file.exists()&&headers){
				header =mJsonHeader;
			}else{//if file exists remove closing header
				deleteLastLine(file);
			}

			FileWriter fw;

			try {
				fw = new FileWriter(file, toAppend);
				if(headers)
					fw.write(header);
				Log.v(BaseLogger.TAG, SUBTAG + header);

				for (String line : data) {
					Log.v(BaseLogger.TAG, SUBTAG + line);
					fw.write(line + "\n");
				}
				if(headers)
					fw.write(mJsonEnd);

				fw.flush();
				fw.close();
				Log.v(BaseLogger.TAG, SUBTAG + data.length
						+ " Data write ok: " + file.getAbsolutePath()
						+ " toAppend: " + toAppend);

			} catch (IOException e) {
				Log.v(BaseLogger.TAG,
						SUBTAG + "Data write BROKEN: " + file.getAbsolutePath()
								+ " " + e.getMessage());
			}
		}
	}

	

	private void deleteLastLine(File fileName){
		RandomAccessFile f = null;
		byte b;
		try {
			f = new RandomAccessFile(fileName, "rw");
			long length = f.length() - 1;
			do {
				length -= 1;
				f.seek(length);
				 b = f.readByte();
			} while(b != 10 && length>0);
			f.setLength(length+1);
			f.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}


}