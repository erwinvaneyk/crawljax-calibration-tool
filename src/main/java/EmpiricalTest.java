package main.java;

import java.util.concurrent.TimeUnit;

import com.crawljax.core.CrawljaxRunner;
import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.core.configuration.CrawljaxConfiguration.CrawljaxConfigurationBuilder;

public class EmpiricalTest {
	
	private static final long WAIT_TIME_AFTER_EVENT = 200;
	private static final long WAIT_TIME_AFTER_RELOAD = 20;
	private static String[] urls = {	"http://www.messi.com/",
										"http://www.rogerfederer.com/",
										"http://www.fcbarcelona.com/"};
	
	public static void main(String[] args) {
		for(int i=0; i<urls.length; i++) {
			CrawljaxConfigurationBuilder builder = CrawljaxConfiguration.builderFor(urls[i]);
			builder.crawlRules().clickDefaultElements();
			
			// Set timeouts
			builder.crawlRules().waitAfterReloadUrl(WAIT_TIME_AFTER_RELOAD, TimeUnit.MILLISECONDS);
			builder.crawlRules().waitAfterEvent(WAIT_TIME_AFTER_EVENT, TimeUnit.MILLISECONDS);

					
			CrawljaxRunner crawljax = new CrawljaxRunner(builder.build());
			crawljax.call();
		}
	}
}