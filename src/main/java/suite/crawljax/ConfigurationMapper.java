package suite.crawljax;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import suite.crawljax.plugins.StoreDomPlugin;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import com.crawljax.browser.EmbeddedBrowser.BrowserType;
import com.crawljax.core.configuration.BrowserConfiguration;
import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.core.configuration.CrawljaxConfiguration.CrawljaxConfigurationBuilder;
import com.crawljax.core.state.NDDStateVertexFactory;
import com.crawljax.core.state.duplicatedetection.*;
import com.crawljax.plugins.crawloverview.CrawlOverview;
import com.google.common.collect.ImmutableList;

/**
 * Maps key-value entries to a Crawljax-configuration
 */
@Slf4j
public class ConfigurationMapper {

	private static final int FEATURE_SYNTAX_SIZE = 3;
	private static final int FEATURE_INDEX_TYPE = 0;
	private static final int FEATURE_INDEX_SIZE = 1;
	private static final int FEATURE_INDEX_SUBTYPE = 2;
	
	private double threshold = -1;
	private List<FeatureType> features;
	private String ndd = "";

	/**
	 * Sets up the crawljax-configuration for a given website, outputDir and additional args
	 * 
	 * @param website
	 *            the website to be crawled
	 * @param outputDir
	 *            the output-folder
	 * @param args
	 *            the additional args
	 * @return a crawljaxConfiguration for website, outputDir and additional args.
	 */
	public CrawljaxConfiguration convert(URL website, File outputDir, Map<String, String> args) {

		features = new ArrayList<FeatureType>();
		CrawljaxConfigurationBuilder builder =
		        CrawljaxConfiguration.builderFor(website.toString());
		builder.setOutputDirectory(outputDir);

		// Add plugins
		builder.addPlugin(new CrawlOverview());
		builder.addPlugin(new StoreDomPlugin());
		builder.setBrowserConfig(new BrowserConfiguration(BrowserType.FIREFOX, 1));
		builder.setStateVertexFactory(new NDDStateVertexFactory());

		// arguments
		for (Entry<String, String> entry : args.entrySet()) {
			try {
				log.info("Configuration pair: {} = {}", entry.getKey(), entry.getValue());
				convertArgument(builder, entry.getKey(), entry.getValue());
			} catch (Exception e) {
				log.error("Failed to map pair {} = {}. ({})", entry.getKey(), entry.getValue(),
				        e.getMessage());
			}
		}
		if(!features.isEmpty() && threshold >= 0)
			builder.setNearDuplicateDetectionFactory(buildNearDuplicateDetectionFactory());	
		return builder.build();
	}

	private NearDuplicateDetection buildNearDuplicateDetectionFactory() {
		if(threshold < 0 || features == null)
			throw new RuntimeException("Failed to convert settings to valid NDD. Not all parameters where provided.");
		log.info("Build NDD using parametes: {} and {}", threshold, features);
		if(ndd.equalsIgnoreCase("broder")) {
			return new NearDuplicateDetectionBroder(threshold,ImmutableList.copyOf(features));
		} else {
			return new NearDuplicateDetectionCrawlhash(threshold,ImmutableList.copyOf(features));
		}
    }

	/**
	 * Converts a key=value representation to the relevant setting in Crawljax
	 * 
	 * @param builder
	 *            the Crawljax-builder to be changed
	 * @param key
	 *            string key
	 * @param value
	 *            string value
	 */
	private void convertArgument(@NonNull CrawljaxConfigurationBuilder builder,
	        @NonNull String key, @NonNull String value) {

		// Standard settings
		if (key.equalsIgnoreCase("d") || key.equalsIgnoreCase("depth")) {
			builder.setMaximumDepth(Integer.parseInt(value));
		} else if (key.equalsIgnoreCase("s") || key.equalsIgnoreCase("maxstates")) {
			builder.setMaximumStates(Integer.parseInt(value));
		} else if (key.equalsIgnoreCase("t") || key.equalsIgnoreCase("timeout")) {
			builder.setMaximumRunTime(Long.parseLong(value), TimeUnit.MINUTES);
		} else if (key.equalsIgnoreCase("threshold")) {
			threshold = Double.parseDouble(value);
		} else if (key.equalsIgnoreCase("ndd") || key.equalsIgnoreCase("nearduplicatedetection")) {
			ndd = value;
		} else if (key.equalsIgnoreCase("a") || key.equalsIgnoreCase("crawlHiddenAnchors")) {
			builder.crawlRules().crawlHiddenAnchors(true);
		} else if (key.equalsIgnoreCase("waitAfterReload")) {
			builder.crawlRules().waitAfterReloadUrl(Long.parseLong(value), TimeUnit.MILLISECONDS);
		} else if (key.equalsIgnoreCase("waitAfterEvent")) {
			builder.crawlRules().waitAfterEvent(Long.parseLong(value), TimeUnit.MILLISECONDS);
		} else if (key.equalsIgnoreCase("feature")) {
			addFeature(value);
		} else if (key.equalsIgnoreCase("b") || key.equalsIgnoreCase("browser")) {
			for (BrowserType b : BrowserType.values()) {
				if (b.name().equalsIgnoreCase(value)) {
					builder.setBrowserConfig(new BrowserConfiguration(b, 1));
				}
			}
		} else {
			log.warn("Undefined key in configuration: {}", key);
		}
	}
	
	private void addFeature(String feature) {
		String[] parts = feature.split(";");
		if(parts.length < FEATURE_SYNTAX_SIZE) {
			log.warn("Invalid syntax for feature {}. Syntax should be: FeatureType;size;type");
			return;
		}
		if (parts[FEATURE_INDEX_TYPE].equalsIgnoreCase("FeatureShingles")) {
			int index = Integer.valueOf(parts[FEATURE_INDEX_SUBTYPE]);
			FeatureShingles.SizeType fst = FeatureShingles.SizeType.values()[index];
			FeatureShingles ft = new FeatureShingles(Integer.valueOf(parts[FEATURE_INDEX_SIZE]), fst);
			features.add(ft);
			log.info("Feature added: {}", ft);
		}		
	}
}
