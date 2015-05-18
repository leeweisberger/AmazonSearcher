package code;

import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class AmazonPageNavigator {
	private static final String DISABLED_LABEL = "li[class=a-disabled a-last]";
	private static final String NEXT = "Next";
	public static final String URL_SECOND_PART_NEW = "/ref=olp_tab_new?ie=UTF8&condition=new";
	public static final String URL_SECOND_PART_USED = "/ref=olp_tab_used?ie=UTF8&condition=used&startIndex=";
	public static final String URL_FIRST_PART = "http://www.amazon.com/gp/offer-listing/";
	private int pageOffset;
	private String asin;
	
	/**
	 * Instantiates a new amazon page navigator.
	 *
	 * @param asin the asin that we will be navigating with
	 */
	public AmazonPageNavigator(String asin){
		pageOffset=0;
		this.asin=asin;
	}
	
	/**
	 * Navigate to the new price page for the asin.
	 *
	 * @return the Document that represents the new price page
	 */
	public Document navigateToNewPricePage(){
		Document doc=null;
		try {
			doc = Jsoup.connect(URL_FIRST_PART + asin +URL_SECOND_PART_NEW).timeout(30000).userAgent("Mozilla/17.0").get();
		} catch (IOException e) {
			System.err.println("caught error and could not navigate to new price page");
			return doc;
		}	
		return doc;
	}
	
	/**
	 * Navigate to the next used price page. The first time it is called, this method returns the Document of the first used price page
	 *
	 * @return the Document that represents the next used price page
	 */
	public  Document navigateToNextUsedPricePage() {
		Document doc=null;
		try {
			doc = Jsoup.connect(URL_FIRST_PART + asin +URL_SECOND_PART_USED + pageOffset).timeout(30000).userAgent("Mozilla/17.0").get();
		} catch (IOException e) {
			System.err.println("caught error and could not navigate to next used price page");
			return doc;
		}	
		pageOffset+=10;
		return doc;
		
	}
	
	/**
	 * Checks if we are on the last used price page.
	 *
	 * @return true, if is on last used price page
	 */
	public boolean isOnLastUsedPricePage(){
		Document doc=null;
		try{
			doc = Jsoup.connect(URL_FIRST_PART + asin +URL_SECOND_PART_USED + pageOffset).timeout(30000).userAgent("Mozilla/17.0").get();
		}
		catch(IOException e){
			return true;
		}
		Elements disabledContent = doc.select(DISABLED_LABEL);
		for(Element content : disabledContent){
			if(content.text().contains(NEXT))
				return true;
		}
		return false;
	}
	
	/**
	 * Gets the url of the new price page for the given asin.
	 *
	 * @return the url
	 */
	public String getURL(){
		return URL_FIRST_PART + asin +URL_SECOND_PART_NEW;
	}
}
