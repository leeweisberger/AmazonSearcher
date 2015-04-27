
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.jaunt.Element;
import com.jaunt.Elements;
import com.jaunt.JauntException;
import com.jaunt.NotFound;
import com.jaunt.ResponseException;
import com.jaunt.UserAgent;

public class UsedPriceCrawler {
	private  final String AWS_ACCESS_KEY_ID = "AKIAICPTVFQ6GMX4B66Q";
	private  final String AWS_SECRET_KEY = "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";
	private  final String ENDPOINT = "webservices.amazon.com";
	private  final String ASSOCIATE_TAG = "bookseller03-20";

	private Map<String, Double> minPriceMap;
	private String asin;
	//This is the lowest of the min Prices so that we know when to stop searching
	private double maxMinPrice=0;
	private boolean exceededMaxMin=false;
	

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
	}

	/**
	 * Gets the best price map.
	 *
	 * @return a map mapping sub-conditions to their lowest Amazon price, null if error
	 */
	public Map<String, Double> getBestPriceMap(){
		SignedRequestsHelper helper;
		try {
			helper = SignedRequestsHelper.getInstance(ENDPOINT, AWS_ACCESS_KEY_ID, AWS_SECRET_KEY);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		String url = requestURLFromASIN(asin, helper);
		Map<String, Double> priceMap = new HashMap<String, Double>();
		if(minPriceMap.containsKey(ConditionConstants.Condition.NEW.getFileName())){
			Double bestNewPrice = getBestNewPrice(url);
			if(bestNewPrice<=minPriceMap.get(ConditionConstants.Condition.NEW.getFileName()))
				priceMap.put(ConditionConstants.Condition.NEW.getFileName(), bestNewPrice);
			if(minPriceMap.size()==1)
				return priceMap;
		}
		
		priceMap.putAll(getBestUsedPrices(url));
		return priceMap;
	}

	private Double getBestNewPrice(String url) {
		UserAgent userAgent = new UserAgent();
		try {
			userAgent.visit(url);
		} catch (ResponseException e) {
			e.printStackTrace();
			return null;
		}
		userAgent = navigateToNewPricePage(userAgent);
		Element priceElement;
		Element shippingElement;
		try {
			priceElement = userAgent.doc.findFirst("<span class=\"a-size-large a-color-price olpOfferPrice a-text-bold\">");
			shippingElement = userAgent.doc.findFirst("<p class=\"olpShippingInfo\">");
		} catch (NotFound e) {
			return Double.MAX_VALUE;
		}
		Double price = Double.valueOf(priceElement.getText().trim().replaceAll(" ", "").substring(1));
		Double shipping =0.0;
		if(!shippingElement.innerText().toLowerCase().contains("free")){
			String shippingPrice = shippingElement.innerText().trim().replaceAll(" ","").replaceAll("\n","").replaceAll("shipping", "");
			shippingPrice = shippingPrice.substring(2);
			shipping = Double.valueOf(shippingPrice);
		}
		return price+shipping;
	}

	private UserAgent navigateToUsedPricePage(UserAgent userAgent) {
		try {
			userAgent.visit("http://www.amazon.com/gp/offer-listing/" + asin + "/ref=dp_olp_used?ie=UTF8&condition=used");
		} catch (ResponseException e) {
			e.printStackTrace();
		}
		return userAgent;
	}
	
	

	private Map<String, Double> getBestUsedPrices(String url){
		UserAgent userAgent = new UserAgent();
		try {
			userAgent.visit(url);
		} catch (ResponseException e) {
			e.printStackTrace();
			return null;
		}

		userAgent = navigateToUsedPricePage(userAgent);
		Map<String, Double> priceMap = new HashMap<String, Double>();
		getBestUsedPrices(userAgent, priceMap);
		return priceMap;
	}

	private void getBestUsedPrices(UserAgent userAgent, Map<String, Double> priceMap){
		if(exceededMaxMin)return;
		List<String> conditionList = new ArrayList<String>();
		List<Double> preShipPriceList = new ArrayList<Double>();
		List<Double> priceList = new ArrayList<Double>();
		List<Double> shippingList = new ArrayList<Double>();

		if(isOnLastPage(userAgent))
			return;
		Elements conditions = userAgent.doc.findEvery("<h3 class=\"a-spacing-small olpCondition\">");
		for(Element condition : conditions){
			conditionList.add(condition.getText().trim().replaceAll(" ", ""));
		}

		Elements prices = userAgent.doc.findEvery("<span class=\"a-size-large a-color-price olpOfferPrice a-text-bold\">");
		for(Element price : prices){
			preShipPriceList.add(Double.valueOf(price.getText().trim().replaceAll(" ", "").replaceAll(",", "").substring(1)));
		}


		Elements shippings = userAgent.doc.findEvery("<p class=\"olpShippingInfo\">");
		for(Element shipping : shippings){
			if(shipping.innerText().toLowerCase().contains("free")){
				shippingList.add(0.0);
			}
			else{
				String shippingPrice = shipping.innerText().trim().replaceAll(" ","").replaceAll("\n","").replaceAll("shipping", "");
				shippingPrice = shippingPrice.substring(2);
				shippingList.add(Double.valueOf(shippingPrice));
			}
		}

		for(int i=0; i<shippingList.size(); i++){
			double newPrice = preShipPriceList.get(i) + shippingList.get(i);
			if(newPrice>maxMinPrice)
				exceededMaxMin=true;
			priceList.add(i, newPrice);
		}

		addToMap(priceMap, conditionList, priceList);
		if(priceMap.size() != minPriceMap.size()){
			userAgent = navigateToNextPricePage(userAgent);
			getBestUsedPrices(userAgent, priceMap);
		}

	}

	private boolean isOnLastPage(UserAgent userAgent) {
		Elements disabledButtons=null;
		try{
			disabledButtons = userAgent.doc.findEvery("<li class=\"a-disabled a-last\">");
		}
		catch (Exception e){
			return false;
		}
		if(disabledButtons==null)return false;
		for(Element disabledButton : disabledButtons){
			if(disabledButton.getText().contains("Next"))
				return true;
		}
		return false;
	}

	private void addToMap(Map<String, Double> priceMap,
			List<String> conditionList, List<Double> priceList) {
		for(int i=0; i<conditionList.size(); i++){
			if(!minPriceMap.containsKey(conditionList.get(i)))
				continue;
			if(minPriceMap.get(conditionList.get(i)) < priceList.get(i))
				continue;
			if(!priceMap.containsKey(conditionList.get(i))){
				priceMap.put(conditionList.get(i), priceList.get(i));
			}
		}
		recalculateMaxMinPrice(priceMap);
	}

	private void recalculateMaxMinPrice(Map<String, Double> priceMap) {
		maxMinPrice=0;
		for(String key : minPriceMap.keySet()){
			if(minPriceMap.get(key)>maxMinPrice && !priceMap.containsKey(key))
				maxMinPrice = minPriceMap.get(key);
		}
		
	}

	private UserAgent navigateToNextPricePage(UserAgent userAgent){
		Elements links = userAgent.doc.findEvery("<a>");
		for(Element link : links){
			try{
				String linkName = link.getAt("href");
				if(linkName.contains("ref=olp_page_next") && linkName.charAt(linkName.length()-1)!='#'){
					userAgent.visit(linkName);
					return userAgent;
				}
			}
			catch(JauntException e){
				continue;
			}
		}
		return null;
	}

	private UserAgent navigateToNewPricePage(UserAgent userAgent){
		try {
			userAgent.visit("http://www.amazon.com/gp/offer-listing/" + asin +"/ref=olp_tab_new?ie=UTF8&condition=new");
		} catch (ResponseException e) {
			e.printStackTrace();
		}
		return userAgent;
	}
	private String requestURLFromASIN(String asin, SignedRequestsHelper helper){
		Map<String, String> request = new HashMap<String, String>();
		request.put("Service", "AWSECommerceService");
		request.put("AssociateTag", ASSOCIATE_TAG);
		request.put("Operation", "ItemLookup");
		request.put("ItemId", asin);
		String requestUrl = helper.sign(request);
		return fetchURL(requestUrl);
	}

	private String fetchURL(String requestUrl) {
		String url = null;
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(requestUrl);
			Node urlNode = doc.getElementsByTagName("DetailPageURL").item(0);
			url = urlNode.getTextContent();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return url;
	}

	//	public static void main(String[] args) {
	//		Map<String, Double> minPriceMap = new HashMap<>();
	//		minPriceMap.put("Used-LikeNew", 6.8);
	//		minPriceMap.put("Used-Good", 15.2);
	//		UsedPriceCrawler crawler = new UsedPriceCrawler(minPriceMap, "0812984250");
	//		System.out.println(crawler.getBestPriceMap());
	//	}
}
