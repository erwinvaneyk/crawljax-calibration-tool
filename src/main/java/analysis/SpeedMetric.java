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

	float benchmarkDuration;
	float testDuration;
	
	public String getMetricName() {
		return "Speed difference metric";
	}

	public Map<String, Object> apply(
			Collection<WebsiteResult> benchmarkWebsites,
			Collection<WebsiteResult> testWebsitesResults) {
		for(WebsiteResult baseWebsite : benchmarkWebsites) {
			benchmarkDuration += baseWebsite.getDuration();
		}
		for(WebsiteResult testWebsite : testWebsitesResults) {
			testDuration += testWebsite.getDuration();
		}
		Map<String, Object> map = new HashMap<String, Object>();
		float difference = testDuration - benchmarkDuration;
		
		float res = ((difference*-1)/benchmarkDuration) * 100;
		DecimalFormat df = new DecimalFormat("#.##");
		res = Float.valueOf(df.format(res));
		String result = String.valueOf(res);
		map.put("Speed increese: ", result+"%");
		return map;
		
	}

	public float getScore() {
		float score = (benchmarkDuration - testDuration)/benchmarkDuration;
		if (score < 0) {
			return 0;
		} else if (score > 0.5) {
			return 1;
		} else {
			return (float) 0.5;
		}
	}

}
