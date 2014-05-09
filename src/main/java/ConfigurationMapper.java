package main.java;

import java.io.File;
import java.net.URL;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import main.java.plugins.StoreDOMPlugin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.crawljax.browser.EmbeddedBrowser.BrowserType;
import com.crawljax.core.configuration.BrowserConfiguration;
import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.core.configuration.CrawljaxConfiguration.CrawljaxConfigurationBuilder;
import com.crawljax.plugins.crawloverview.CrawlOverview;

/**
 * Maps key-value entries to a Crawljax-configuration
 *
 */
public class ConfigurationMapper {
	
	public static CrawljaxConfiguration convert(URL website, File outputDir, Map<String,String> args) {

		CrawljaxConfigurationBuilder builder = CrawljaxConfiguration.builderFor(website.toString());
		builder.setOutputDirectory(outputDir);

		// Add plugins
		builder.addPlugin(new CrawlOverview());
		builder.addPlugin(new StoreDOMPlugin());
		builder.setBrowserConfig(new BrowserConfiguration(BrowserType.FIREFOX, 1));
		
		// arguments
		for( Entry<String, String> entry : args.entrySet()) {
			convertArgument(builder, entry.getKey(), entry.getValue());
		}
		return builder.build();
	}
	
	private static void convertArgument(CrawljaxConfigurationBuilder builder,String key,String value) {
		assert builder != null;
		assert key != null;
		assert value != null;
		
		// Standard settings
		if(key.equalsIgnoreCase("d") || key.equalsIgnoreCase("depth")) {
			builder.setMaximumDepth(Integer.parseInt(value));
		} else if(key.equalsIgnoreCase("s") || key.equalsIgnoreCase("maxstates")) {
			builder.setMaximumStates(Integer.parseInt(value));
		} else if(key.equalsIgnoreCase("t") || key.equalsIgnoreCase("timeout")) {
			builder.setMaximumRunTime(Long.parseLong(value), TimeUnit.MINUTES);
		} else if(key.equalsIgnoreCase("a") || key.equalsIgnoreCase("crawlHiddenAnchors")) {
			builder.crawlRules().crawlHiddenAnchors(true);
		} else if(key.equalsIgnoreCase("waitAfterReload")) {
			builder.crawlRules().waitAfterReloadUrl(Long.parseLong(value), TimeUnit.MILLISECONDS);
		} else if(key.equalsIgnoreCase("waitAfterEvent")) {
			builder.crawlRules().waitAfterEvent(Long.parseLong(value), TimeUnit.MILLISECONDS);
		} else if(key.equalsIgnoreCase("b") || key.equalsIgnoreCase("browser")) {
			for (BrowserType b : BrowserType.values()) {
				if (b.name().equalsIgnoreCase(value)) {
					builder.setBrowserConfig(new BrowserConfiguration(b, 1));
				}
			}
		}
		// TODO browsertype and parallel
	}
}
