
import java.util.HashMap;
import java.util.Map;


public class UsedPriceFinder {
	public static final String FILE_NAME = "Amazon_Form.xlsm.csv";
	
	
	public static void main(String[] args) {
		Map<String, Map<String,Double>> minPriceMap = FileParser.readFileToMap(FILE_NAME);
		Map<String, Map<String,Double>> underMap = new HashMap<String, Map<String,Double>>();
		for(String asin : minPriceMap.keySet()){
			System.out.println(asin + " : " + minPriceMap.get(asin));
			UsedPriceCrawler crawler = new UsedPriceCrawler(asin, minPriceMap.get(asin));
			Map<String, Double> map = crawler.getBestPriceMap();
			System.out.println(asin + " : " + map);
			if(map.size()>0)
				underMap.put(asin, map);
		}
		Emailer.sendEmail(underMap, minPriceMap);

	}
}
