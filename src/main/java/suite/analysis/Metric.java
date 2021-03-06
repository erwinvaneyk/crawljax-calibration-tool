package suite.analysis;

import java.util.Collection;

import suite.distributed.results.WebsiteResult;

/**
 * Metrics can be added to an analysis (using the factory). A metric is supposed to measure some
 * aspect of the benchmark and test crawl.
 */
public interface Metric {

	/**
	 * @return metric name
	 */
	String getName();

	/**
	 * @param benchmarkWebsites
	 *            the websiteResults of the benchmark crawl
	 * @param testWebsitesResults
	 *            the websiteResults of the test crawl
	 * @return a map containing as key the section/title and as values the results of the metric.
	 */
	Collection<Statistic> apply(Collection<WebsiteResult> benchmarkWebsites,
	        Collection<WebsiteResult> testWebsitesResults);
}
