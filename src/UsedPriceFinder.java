
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import com.dropbox.core.DbxException;


public class UsedPriceFinder {
	private static final String TIME_ZONE = "America/New_York";
	private static final int MS_IN_DAY = 86400000;
	public static final String LAST_DAY_TXT = "last_day.txt";
	public static final String LAST_DAY_TXT_LOCATION = "temp\\"+LAST_DAY_TXT;
	public static final String CSV_FILE_NAME = "Amazon_Form.xlsm.csv";
	private static final int BEGIN_HOUR = 6;
	private static final int END_HOUR = 23;
	private static Set<String> toWriteToFile = new HashSet<String>();


	public static void main(String[] args) throws FileNotFoundException, DbxException, IOException {
		
		if(isOutOfTimeRange(BEGIN_HOUR, END_HOUR)){
			System.out.println("Application stopped because it is out of the user's preferred contact time");
			return;
		}
		DropBoxWriter.downloadAndSaveToFile();
		System.out.println(System.currentTimeMillis());
		Map<String, Map<String,Double>> minPriceMap = FileParser.readFileToMap(CSV_FILE_NAME);
		Map<String, Map<String,Double>> underMap = new HashMap<String, Map<String,Double>>();
		Map<String, String> urlMap = new HashMap<String,String>();
		for(String asin : minPriceMap.keySet()){
			System.out.println(asin + " : " + minPriceMap.get(asin));
			UsedPriceCrawler2 crawler = new UsedPriceCrawler2(asin, minPriceMap.get(asin));
			Map<String, Double> map = crawler.getBestPriceMap();
			String url = crawler.getURL();
			urlMap.put(asin, url);
			System.out.println(asin + " : " + map);
			if(map.size()>0 && !wasSentLastDay(asin))
				underMap.put(asin, map);
		}
		if(toWriteToFile.size()>0)
			DropBoxWriter.updateFile(toWriteToFile);

		if(underMap.size()>0)
			Emailer.sendEmail(underMap, minPriceMap, urlMap);

	}

	private static boolean isOutOfTimeRange(int beginHour, int endHour) {
		TimeZone.setDefault(TimeZone.getTimeZone(TIME_ZONE));
		Calendar cal = Calendar.getInstance();
		
		System.out.println(cal.get(cal.HOUR_OF_DAY));
		System.out.println(beginHour);
		System.out.println(endHour);
		
		if(cal.get(cal.HOUR_OF_DAY)<beginHour || cal.get(cal.HOUR_OF_DAY)>endHour)
			return true;
		return false;

	}


	private static boolean wasSentLastDay(String asin) {
		FileInputStream fstream=null;
		try {
			fstream = new FileInputStream(LAST_DAY_TXT_LOCATION);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		BufferedReader br = new BufferedReader(new InputStreamReader(fstream));

		String line;
		boolean isExpired=false;
		boolean isInFile=false;
		List<String> linesToKeep = new ArrayList<String>();
		//Read File Line By Line
		try {
			while ((line = br.readLine()) != null){
				String[] asinTimeList = line.split(",");
				if(asinTimeList.length<2)continue;
				if(asinTimeList[0].equals(asin)){
					if(System.currentTimeMillis()-Long.valueOf(asinTimeList[1])>MS_IN_DAY)
						isExpired=true;
					isInFile=true;
				}
				if(System.currentTimeMillis()-Long.valueOf(asinTimeList[1])<MS_IN_DAY){
					toWriteToFile.add(line + "\n");
				}
			}
			br.close();

			if(!isInFile || isExpired)
				toWriteToFile.add(asin + "," + System.currentTimeMillis()+"\n");

		}
		catch (IOException e) {
			e.printStackTrace();
		}
		 
		return isInFile && !isExpired;

	}
}
