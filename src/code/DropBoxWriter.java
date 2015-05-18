package code;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Locale;

import com.dropbox.core.DbxAppInfo;
import com.dropbox.core.DbxClient;
import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.DbxWebAuthNoRedirect;
import com.dropbox.core.DbxWriteMode;

public class DropBoxWriter {
	private final static String APP_KEY = "50cga9drlr882pv";
	private final static String APP_SECRET = "11ik33lfctnmhau";
	private final static String ACCESS_CODE = "SdLUWurLJgAAAAAAAAAABw36QcsYi0NnYcehS3omLNcibdBnqlr6bAgDgGPgrjYT";

	/**
	 * Update file on drop box.
	 *
	 * @param fileName the file name on DropBox
	 * @param toBeWritten the list of strings to be written to the file
	 * @throws DbxException the dbx exception
	 * @throws FileNotFoundException the file not found exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */

	public static void downloadAndSaveToFile() throws IOException, DbxException{
		DbxClient client = authorize();
		PrintWriter writer = new PrintWriter(UsedPriceFinder.LAST_DAY_TXT_LOCATION);
		writer.close();
		FileOutputStream outputStream = new FileOutputStream(UsedPriceFinder.LAST_DAY_TXT_LOCATION);
		try {
			client.getFile("/" + UsedPriceFinder.LAST_DAY_TXT, null,outputStream);
			outputStream.flush();		
			} finally {
				outputStream.close();
			}


	}
	public static void updateFile(Collection<String> toBeWritten) throws FileNotFoundException,
	DbxException, IOException {
		DbxClient client = authorize();
		PrintWriter pw = null;
		try {
			pw = new PrintWriter(UsedPriceFinder.LAST_DAY_TXT_LOCATION);
			for(String str : toBeWritten){
				pw.write(str);
			}

		} finally {
			pw.close();
		}
		uploadFileToDropBox(client);
	}

	private static void uploadFileToDropBox(DbxClient client)
			throws FileNotFoundException, DbxException, IOException {
		File inputFile = new File(UsedPriceFinder.LAST_DAY_TXT_LOCATION);
		FileInputStream inputStream = new FileInputStream(inputFile);
		try {
			try{
				client.delete("/" + UsedPriceFinder.LAST_DAY_TXT);
			}
			catch(DbxException e){
			}
			client.uploadFile("/" + UsedPriceFinder.LAST_DAY_TXT,DbxWriteMode.force(), inputFile.length(), inputStream);

		} finally {
			inputStream.close();
		}
	}

	private static DbxClient authorize() throws DbxException {
		DbxAppInfo appInfo = new DbxAppInfo(APP_KEY, APP_SECRET);
		DbxRequestConfig config = new DbxRequestConfig(
				"JavaTutorial/1.0", Locale.getDefault().toString());
		DbxWebAuthNoRedirect webAuth = new DbxWebAuthNoRedirect(config, appInfo);
		DbxClient client = new DbxClient(config, ACCESS_CODE);
		return client;
	}
}
