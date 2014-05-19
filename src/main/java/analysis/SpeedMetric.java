package main.java.analysis;

import java.text.DecimalFormat;
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
		double benchmarkDuration = 0;
		double testDuration = 0;
		for(WebsiteResult baseWebsite : benchmarkWebsites) {
			benchmarkDuration += baseWebsite.getDuration();
		}
		for(WebsiteResult testWebsite : testWebsitesResults) {
			testDuration += testWebsite.getDuration();
		}
		Map<String, Object> map = new HashMap<String, Object>();
		double difference = testDuration - benchmarkDuration;
		
		double res = ((difference*-1)/benchmarkDuration) * 100;
		DecimalFormat df = new DecimalFormat("#.##");
		res = Double.valueOf(df.format(res));
		String result = String.valueOf(res);
		map.put("Speed increese: ", result+"%");
		return map;
		
	}

}
