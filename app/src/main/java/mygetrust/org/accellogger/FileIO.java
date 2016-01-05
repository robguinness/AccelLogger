package mygetrust.org.accellogger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.nio.channels.FileChannel;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;


public class FileIO {
	
	boolean mExternalStorageAvailable = false;
	boolean mExternalStorageWriteable = false;

	// store the directory of the SD card.
	public static String baseDir = Environment.getExternalStorageDirectory().getAbsolutePath();
	//public static String baseDir = Environment.getDataDirectory().getAbsolutePath();
	//public static String baseDirInternal = Environment.getDataDirectory().getAbsolutePath();
	public static File accFile;
	private static int test;
		
	// specify output file name. TODO: Instead of hardcoding this, the value should be taken from the UI and stamped with a timetag. -DONE! - see below.
	//public static File dir = new File(baseDirInternal); 
	//private static SimpleDateFormat dateTimeForFilename = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
	//public static String dateTimeForFilenameAsString = dateTimeForFilename.format(System.currentTimeMillis());
	
	//public static File dir = new File(baseDir + "/" + dateTimeForFilenameAsString);



	public static File getAccFile() {
		return accFile;
	}



	public static void setAccFile(String accFileName) {

		File tempFile = new File(newDir("AccelLogger"), accFileName + "_acc.txt");
		Log.d("setAccFile", "File " + tempFile.toString() + " created!!!!");
		accFile = tempFile;
	}

	File file;
	
	
	public static File newDir(String dirname){
		File file = new File(baseDir + "/" + dirname); 
		file.mkdirs();
		Log.d("newDir", "Making directory " + baseDir + "/" + dirname);
		return file;
	}
	
	

	public boolean checkIfExternalStorageIsAvailable() {
		
		String state = Environment.getExternalStorageState();
	
		if (Environment.MEDIA_MOUNTED.equals(state)) {
		    // We can read and write the media
		    mExternalStorageAvailable = mExternalStorageWriteable = true;
		} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
		    // We can only read the media
		    mExternalStorageAvailable = true;
		    mExternalStorageWriteable = false;
		} else {
		    // Something else is wrong. It may be one of many other states, but all we need
		    //  to know is we can neither read nor write
		    mExternalStorageAvailable = mExternalStorageWriteable = false;
		}
		return mExternalStorageAvailable;
	}
	
	static void writeToFile(StringBuilder sb, File file) {

		String logEntry = sb + "";
		// Execute writing of results to external storage.
	    writeFileToStorage(logEntry, file);
		
	}

	static void writeToAccFile(StringBuilder sb) {

		String logEntry = sb + "";
		// Execute writing of results to external storage.
	    writeAccToStorage(logEntry);
		
	}
	
	
	public static void writeFileToStorage(String contents, File file) {
		  	
		    // Get end-of-line separator for system (On Linux systems this is always \n, but it is better to check...
		  	String eol = System.getProperty("line.separator");
		  	//Log.d("writeFileToStorage",file.toString());
		  	Writer writer = null;
		  	
		  	// Append result to file, or print exception if fail.
	      	try {
	      		if (file != null){
	      			writer = new PrintWriter(new BufferedWriter(new FileWriter(file, true)));
	      			writer.write(contents + eol);
	      			writer.close();
	      		}
	      		else {
	      			Log.d("FileIO", "File is null value!!!");
	      		}
	      	} catch (Exception e) {
	    		e.printStackTrace();
	    	}
		  	
	  }
	
	public static void writeAccToStorage(String contents) {
	  	
	    // Get end-of-line separator for system (On Linux systems this is always \n, but it is better to check...
	  	String eol = System.getProperty("line.separator");
	  	//Log.d("writeFileToStorage",getAccFile().toString());
	  	Writer writer = null;
	  	
	  	// Append result to file, or print exception if fail.
      	try {
      		if (FileIO.accFile != null){
      			writer = new PrintWriter(new BufferedWriter(new FileWriter(accFile, true)));
      			writer.write(contents + eol);
      			writer.close();
      		}
      		else {
      			Log.d("FileIO", "File is null value!!!");
      		}
      	} catch (Exception e) {
    		e.printStackTrace();
    	}
	  	
  }
	
	public static void copyFile(File src, File dst) throws IOException
	{
	    FileChannel inChannel = new FileInputStream(src).getChannel();
	    FileChannel outChannel = new FileOutputStream(dst).getChannel();
	    try
	    {
	        inChannel.transferTo(0, inChannel.size(), outChannel);
	    }
	    finally
	    {
	        if (inChannel != null)
	            inChannel.close();
	        if (outChannel != null)
	            outChannel.close();
	    }
	}
	
	public static void scanNewFiles(File file, Context context){
		String paths[] = {file.getPath()};
		MediaScannerConnection.scanFile(context, paths, null, new MediaScannerConnection.OnScanCompletedListener() {
		      public void onScanCompleted(String path, Uri uri) {
		          Log.i("ExternalStorage", "Scanned " + path + ":");
		          Log.i("ExternalStorage", "-> uri=" + uri);
		      } 
		});
	}

	//Utility function to find and recovery files that may be "lost" on the internal storage.
	public static void findFiles(){
		final File dir = new File("/data/data/org.mygeotrust.accellogger/");
		
		File recoveryDir = newDir("recovery");
		recoveryDir.mkdirs();
		for(final File file : dir.listFiles()){ 
			String filePath = file.getPath();
			String fileName = file.getName();
			Log.d("findFiles", filePath);
			try {
				copyFile(file, new File(recoveryDir,fileName));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}
}

