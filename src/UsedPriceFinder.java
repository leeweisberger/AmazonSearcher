
import java.util.Map;


public class UsedPriceFinder {
	public static final String FILE_NAME = "Amazon_Form.csv";
	
	
	public static void main(String[] args) {
		Map<String, Map<String,Double>> minPriceMap = FileParser.readFileToMap(FILE_NAME);
		for(String asin : minPriceMap.keySet()){
			UsedPriceCrawler crawler = new UsedPriceCrawler(asin, minPriceMap.get(asin));
			Map<String, Double> map = crawler.getBestPriceMap();
			System.out.println(asin + " : " + map);
			if(map.size()>0)
				Emailer.sendEmail(map, asin, minPriceMap.get(asin));
		}
	}
}
