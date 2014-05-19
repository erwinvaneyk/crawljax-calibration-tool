package main.java.analysis;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import main.java.distributed.results.WebsiteResult;

/**
 * This metric measures the speed difference between two crawls.
 */
public class SpeedMetric implements IMetric {

	public String getMetricName() {
		return "Speed difference metric";
	}

	public Map<String, Object> apply(
			Collection<WebsiteResult> benchmarkWebsites,
			Collection<WebsiteResult> testWebsitesResults) {
		long benchmarkDuration = 0;
		long testDuration = 0;
		for(WebsiteResult baseWebsite : benchmarkWebsites) {
			benchmarkDuration += baseWebsite.getDuration();
		}
		for(WebsiteResult testWebsite : testWebsitesResults) {
			testDuration += testWebsite.getDuration();
		}
		Map<String, Object> map = new HashMap<String, Object>();
		long difference = testDuration - benchmarkDuration;
		String result = (difference >= 0) ? "+" + String.valueOf(difference) : String.valueOf(difference);
		map.put("Speed diff", result);
		return map;
		
	}

}
