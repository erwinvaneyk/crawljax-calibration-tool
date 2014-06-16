package suite.analysis;

import java.util.ArrayList;
import java.util.Collection;

import suite.distributed.results.WebsiteResult;

/**
 * This metric measures the speed difference between two crawls.
 */
public class SpeedMetric implements Metric {

	public static final String SPEED_INCREASE = "Speed increase";

	private float benchmarkDuration;
	private float testDuration;

	public String getMetricName() {
		return "Speed difference metric";
	}

	public Collection<Statistic> apply(
	        Collection<WebsiteResult> benchmarkWebsites,
	        Collection<WebsiteResult> testWebsitesResults) {
		for (WebsiteResult baseWebsite : benchmarkWebsites) {
			benchmarkDuration += baseWebsite.getDuration();
		}
		for (WebsiteResult testWebsite : testWebsitesResults) {
			testDuration += testWebsite.getDuration();
		}
		float difference = testDuration - benchmarkDuration;

		float res = ((difference * -1) / benchmarkDuration) * 100;
		String result = String.valueOf(res);
		Collection<Statistic> ret = new ArrayList<Statistic>();
		ret.add(new Statistic(SPEED_INCREASE, result + "%"));
		return ret;

	}
}