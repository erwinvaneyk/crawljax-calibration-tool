package main.java.analysis;

import java.util.Collection;
import java.util.Map;

import main.java.distributed.results.WebsiteResult;

/**
 * Metrics can be added to an analysis (using the factory). A metric is supposed to measure some aspect
 * of the benchmark and test crawl. 
 */
public interface IMetric {
	
	/**
	 * @return metric name
	 */
	public String getMetricName();

	/**
	 * TODO: change result into more flexible structure.
	 * @param benchmarkWebsites the websiteResults of the benchmark crawl
	 * @param testWebsitesResults the websiteResults of the test crawl
	 * @return a map containing as key the section/title and as values the results of the metric.
	 */
	public Map<String, Object> apply(Collection<WebsiteResult> benchmarkWebsites, Collection<WebsiteResult> testWebsitesResults);
}