package main.java;

import java.io.File;
import java.net.URL;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import main.java.crawljax.plugins.StoreDOMPlugin;

import com.crawljax.browser.EmbeddedBrowser.BrowserType;
import com.crawljax.core.configuration.BrowserConfiguration;
import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.core.configuration.CrawljaxConfiguration.CrawljaxConfigurationBuilder;
import com.crawljax.core.state.duplicatedetection.*;
import com.crawljax.plugins.crawloverview.CrawlOverview;

/**
 * Maps key-value entries to a Crawljax-configuration
 */
@Slf4j
public class ConfigurationMapper {
	
	/**
	 * Sets up the crawljax-configuration for a given website, outputDir and additional args
	 * @param website the website to be crawled
	 * @param outputDir the output-folder
	 * @param args the additional args
	 * @return a crawljaxConfiguration for website, outputDir and additional args.
	 */
	public CrawljaxConfiguration convert(URL website, File outputDir, Map<String,String> args) {

		CrawljaxConfigurationBuilder builder = CrawljaxConfiguration.builderFor(website.toString());
		builder.setOutputDirectory(outputDir);

		// Add plugins
		builder.addPlugin(new CrawlOverview());
		builder.addPlugin(new StoreDOMPlugin());
		builder.setBrowserConfig(new BrowserConfiguration(BrowserType.FIREFOX, 1));
		
		// arguments
		for( Entry<String, String> entry : args.entrySet()) {
			try {
			log.info("Configuration pair: {} = {}", entry.getKey(), entry.getValue());
				convertArgument(builder, entry.getKey(), entry.getValue());
			} catch (Exception e) {
				log.error("Failed to map pair {} = {}. ({})",entry.getKey(), entry.getValue(), e.getMessage());
			} 
		}
		return builder.build();
	}
	
	/**
	 * Converts a key=value representation to the relevant setting in Crawljax
	 * @param builder the Crawljax-builder to be changed
	 * @param key string key
	 * @param value string value
	 */
	private void convertArgument(@NonNull CrawljaxConfigurationBuilder builder,@NonNull String key,@NonNull String value) 
			throws InstantiationException, IllegalAccessException, ClassNotFoundException {	
		
		// Standard settings
		if(key.equalsIgnoreCase("d") || key.equalsIgnoreCase("depth")) {
			builder.setMaximumDepth(Integer.parseInt(value));
		} else if(key.equalsIgnoreCase("s") || key.equalsIgnoreCase("maxstates")) {
			builder.setMaximumStates(Integer.parseInt(value));
		} else if(key.equalsIgnoreCase("t") || key.equalsIgnoreCase("timeout")) {
			builder.setMaximumRunTime(Long.parseLong(value), TimeUnit.MINUTES);
		} else if(key.equalsIgnoreCase("threshold")) {
			builder.setThresholdNearDuplicateDetection(Integer.parseInt(value));
		} else if(key.equalsIgnoreCase("a") || key.equalsIgnoreCase("crawlHiddenAnchors")) {
			builder.crawlRules().crawlHiddenAnchors(true);
		} else if(key.equalsIgnoreCase("waitAfterReload")) {
			builder.crawlRules().waitAfterReloadUrl(Long.parseLong(value), TimeUnit.MILLISECONDS);
		} else if(key.equalsIgnoreCase("waitAfterEvent")) {
			builder.crawlRules().waitAfterEvent(Long.parseLong(value), TimeUnit.MILLISECONDS);
		} else if(key.equalsIgnoreCase("feature")) {
			String[] parts = value.split(";");
			assert parts.length >= 3;
			if(parts[0].equalsIgnoreCase("FeatureShingles")) {
				Integer index = Integer.valueOf(parts[2]);
				FeatureSizeType fst = FeatureSizeType.values()[index];
				FeatureShingles ft = new FeatureShingles(Integer.valueOf(parts[1]), fst);
				NearDuplicateDetectionSingleton.addFeature(ft);
				log.info("Feature added: {}", ft);
			}
		} else if(key.equalsIgnoreCase("b") || key.equalsIgnoreCase("browser")) {
			for (BrowserType b : BrowserType.values()) {
				if (b.name().equalsIgnoreCase(value)) {
					builder.setBrowserConfig(new BrowserConfiguration(b, 1));
				}
			}
		} else {
			log.warn("Undefined key in configuration: {}", key);
		}
	}
}
