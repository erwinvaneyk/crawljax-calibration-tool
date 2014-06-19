/**
 * 
 */
package suite.crawljax;

import static org.junit.Assert.*;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import suite.crawljax.ConfigurationMapper;

import com.crawljax.core.configuration.CrawljaxConfiguration;

public class ConfigurationMapperTest {

	/**
	 * Test method for
	 * {@link suite.crawljax.ConfigurationMapper#convert(java.net.URL, java.io.File, java.util.Map)}
	 * .
	 * 
	 * @throws URISyntaxException
	 */
	@Test
	public void testConvert() throws MalformedURLException, URISyntaxException {
		ConfigurationMapper cm = new ConfigurationMapper();
		URL url = new URL("http://www.test.com");
		File outputDir = new File("/test/");
		Map<String, String> args = new HashMap<String, String>();
		args.put("depth", "5");
		args.put("maxstates", "4");
		args.put("timeout", "10");
		args.put("threshold", "0.5");
		args.put("crawlHiddenAnchors", "true");
		args.put("waitAfterReload", "4000");
		args.put("waitAfterEvent", "4000");
		args.put("feature", "FeatureShingles;1;1");
		args.put("browser", "chrome");
		args.put("undefined", "undefined");
		CrawljaxConfiguration result = cm.convert(url, outputDir, args);
		assertNotNull(result.getBrowserConfig());
		assertEquals(result.getMaximumDepth(), 5);
		assertEquals(result.getMaximumStates(), 4);
		assertEquals(result.getMaximumRuntime(), 600000);
		assertEquals(result.getOutputDir(), outputDir);
		assertEquals(result.getUrl(), url.toURI());
		assertEquals(result.getNearDuplicateDetectionFactory().getDefaultThreshold(), 0.5, 0.01);
		assertEquals(result.getCrawlRules().getWaitAfterEvent(), 4000);
		assertEquals(result.getCrawlRules().getWaitAfterReloadUrl(), 4000);
		assertEquals(result.getNearDuplicateDetectionFactory().getFeatures().size(), 1);
	}

}
