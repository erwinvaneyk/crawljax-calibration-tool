package main.java.analysis;

import java.util.Collection;

import main.java.distributed.results.WebsiteResult;

/**
 * Metrics can be added to an analysis (using the factory). A metric is supposed to measure some
 * aspect of the benchmark and test crawl.
 */
public interface Metric {

	/**
	 * @return metric name
	 */
	public String getMetricName();

	/**
	 * @param benchmarkWebsites
	 *            the websiteResults of the benchmark crawl
	 * @param testWebsitesResults
	 *            the websiteResults of the test crawl
	 * @return a map containing as key the section/title and as values the results of the metric.
	 */
	public Collection<Statistic> apply(Collection<WebsiteResult> benchmarkWebsites,
	        Collection<WebsiteResult> testWebsitesResults);

	/**
	 * Calculates the score of the test-run, in the context of the metric.
	 * 
	 * @return a value between 0 - 1 indicating the score, where 1 is perfect.
	 */
	public float getScore();
}
