package tests;

import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;

import code.AmazonPageNavigator;

public class AmazonPageNavigatorTester {
	private static final String SAMPLE_ASIN = "0136094945";
	AmazonPageNavigator pageNavigator;

	@Before
	public void initSearcher() {
		pageNavigator = new AmazonPageNavigator(SAMPLE_ASIN);
	}
	@Test
	public void navigateToNewPricePageTest(){
		assertNotNull(pageNavigator.navigateToNewPricePage());
	}

	@Test
	public void navigateToUsedPricePageTest(){
		assertNotNull(pageNavigator.navigateToNextUsedPricePage());
	}
}
