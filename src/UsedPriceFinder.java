
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class UsedPriceFinder {
	private static final int MS_IN_DAY = 86400000;
	private static final String LAST_DAY_TXT = "last_day.txt";
	public static final String FILE_NAME = "Amazon_Form.xlsm.csv";
	public static final String BEGIN_TIME = "06:00:00";
	public static final String END_TIME = "23:00:00";


	public static void main(String[] args) {
		if(isOutOfTimeRange(BEGIN_TIME, END_TIME))
			return;
		System.out.println(System.currentTimeMillis());
		Map<String, Map<String,Double>> minPriceMap = FileParser.readFileToMap(FILE_NAME);
		Map<String, Map<String,Double>> underMap = new HashMap<String, Map<String,Double>>();
		Map<String, String> urlMap = new HashMap<String,String>();
		for(String asin : minPriceMap.keySet()){
			System.out.println(asin + " : " + minPriceMap.get(asin));
			UsedPriceCrawler crawler = new UsedPriceCrawler(asin, minPriceMap.get(asin));
			Map<String, Double> map = crawler.getBestPriceMap();
			String url = crawler.getURL();
			urlMap.put(asin, url);
			System.out.println(asin + " : " + map);
			if(map.size()>0 && !wasSentLastDay(asin))
				underMap.put(asin, map);
		}
		if(underMap.size()>0)
			Emailer.sendEmail(underMap, minPriceMap, urlMap);

	}


	@SuppressWarnings("deprecation")
	private static boolean isOutOfTimeRange(String beginTime, String endTime) {
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
		Calendar cal = Calendar.getInstance();
		cal.getTime();
		String currentTime = sdf.format(cal.getTime());
		Date beginDate=null;
		Date endDate=null;
		Date currentDate=null;
		try {
			beginDate = sdf.parse(beginTime);
			endDate = sdf.parse(endTime);
			currentDate = sdf.parse(currentTime);

		} catch (ParseException e) {
			e.printStackTrace();
		}
		if(currentDate.getHours()<beginDate.getHours() || currentDate.getHours()>endDate.getHours())
			return true;
		return false;

	}


	private static boolean wasSentLastDay(String asin) {
		FileInputStream fstream=null;
		try {
			fstream = new FileInputStream(LAST_DAY_TXT);
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
					linesToKeep.add(line);
				}
			}
			br.close();
			PrintWriter writer = new PrintWriter(LAST_DAY_TXT);
			writer.print("");
			writer.close();
			FileWriter fw = new FileWriter(LAST_DAY_TXT,true);
			for(String str : linesToKeep){
				fw.write(str+"\n");
			}
			if(!isInFile || isExpired)
				fw.write(asin + "," + System.currentTimeMillis()+"\n");
			fw.close();


		}
		catch (IOException e) {
			e.printStackTrace();
		}
		return isInFile && !isExpired;

	}
}
