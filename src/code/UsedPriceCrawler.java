package code;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;



public class UsedPriceCrawler {

	private static final String CONDITION_LABEL = "h3[class=a-spacing-small olpCondition]";
	private static final String SHIPPING_LABEL = "p[class=olpShippingInfo]";
	private static final String PRICE_LABEL = "span[class=a-size-large a-color-price olpOfferPrice a-text-bold]";
	private static final String FREE = "FREE";
	private static final String SHIPPING = "shipping";

	private Map<String, Double> minPriceMap;
	private String asin;
	//This is the lowest of the min Prices so that we know when to stop searching
	private double maxMinPrice=0;
	private boolean exceededMaxMin=false;
	private Set<String> seenConditions = new HashSet<String>();
	private AmazonPageNavigator pageNavigator;


	/**
	 * Instantiates a new used price crawler.
	 *
	 * @param minPriceMap maps sub-conditions to min price thresholds
	 * @param asin the asin of the target product
	 */
	public UsedPriceCrawler(String asin, Map<String, Double> minPriceMap){
		this.minPriceMap = minPriceMap;
		this.asin = asin;
		for(String key : minPriceMap.keySet()){
			if(minPriceMap.get(key)>maxMinPrice)
				maxMinPrice = minPriceMap.get(key);
		}
		pageNavigator = new AmazonPageNavigator(asin);
	}

	/**
	 * Gets the best price map.
	 *
	 * @return a map mapping sub-conditions to their lowest Amazon price if less than the threshold price, null if error
	 */
	public Map<String, Double> getBestPriceMap(){
		Map<String, Double> priceMap = new HashMap<String, Double>();
		if(minPriceMap.containsKey(ConditionConstants.Condition.NEW.getFileName())){
			double bestNewPrice = getBestNewPrice();
			addToMap(priceMap, bestNewPrice,ConditionConstants.Condition.NEW.getFileName());
			seenConditions.add(ConditionConstants.Condition.NEW.getFileName());
		}
		
		Document doc=null;
		while(seenConditions.size()!=minPriceMap.size() && !exceededMaxMin){
			Map<String, Double> map = getBestUsedPrices(doc);
			addToMap(priceMap, map);
			if(pageNavigator.isOnLastUsedPricePage())
				break;
		}
		return priceMap;
	}
	
	private double getBestNewPrice(){
		Document doc = pageNavigator.navigateToNewPricePage();
		if(doc==null)return Double.MAX_VALUE;
		Map<String, Double> map = getPricesOnPage(doc);
		return map.getOrDefault(ConditionConstants.Condition.NEW.getFileName(), Double.MAX_VALUE);
	}

	private Map<String, Double> getBestUsedPrices(Document doc) {
		doc = pageNavigator.navigateToNextUsedPricePage();
		if(doc==null)
			return new HashMap<String, Double>();
		Map<String, Double> map = getPricesOnPage(doc);
		return map;
	}
	
	private Map<String, Double> getPricesOnPage(Document doc) {
		Map<String, Double> map = new HashMap<String, Double>();
		Elements priceElements = doc.select(PRICE_LABEL);
		Elements shippingElements = doc.select(SHIPPING_LABEL);
		Elements conditionElements = doc.select(CONDITION_LABEL);

		for(int i=0; i<priceElements.size(); i++){
			double price = formatPrice(priceElements.get(i).text());
			double shipping = formatShipping(shippingElements.get(i).text());
			String condition = conditionElements.get(i).text().replaceAll(" ","");
			if(!map.containsKey(condition))
				map.put(condition, price+shipping);
		}

		return map;
	}

	//for new prices because we only want to input a single double for bestNewPrice
	private void addToMap(Map<String, Double> priceMap, double bestNewPrice,String condition) {
		if(bestNewPrice<minPriceMap.get(condition))
			priceMap.put(condition, bestNewPrice);
	}

	//for old prices because we want to input a map for the best old price
	private void addToMap(Map<String, Double> priceMap, Map<String, Double> usedMap) {
		for(String condition : usedMap.keySet() ){
			if(minPriceMap.containsKey(condition))
				seenConditions.add(condition);
			if(minPriceMap.containsKey(condition) && usedMap.get(condition)<minPriceMap.get(condition) && usedMap.get(condition)<priceMap.getOrDefault(condition, Double.MAX_VALUE)){
				priceMap.put(condition, usedMap.get(condition));
			}
			if(usedMap.get(condition)>maxMinPrice){
				exceededMaxMin=true;
			}
		}
	}

	private double formatShipping(String text) {
		if(text.contains(FREE))
			return 0.0;
		text=text.substring(3);
		text = text.replaceAll(",","").replaceAll(" ","").replaceAll(SHIPPING,"");
		return Double.valueOf(text);
	}

	private double formatPrice(String price) {
		return Double.valueOf(price.substring(1).replaceAll(",", ""));
	}

	/**
	 * Gets the url of the new price page for the given asin.
	 *
	 * @return the url
	 */
	public String getURL(){
		return pageNavigator.getURL();
	}
}
