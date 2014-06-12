package main.java.analysis;

import java.util.Collection;

import com.google.common.collect.ImmutableList;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import main.java.distributed.results.WebsiteResult;

@Slf4j
/**
 * Data model of an analysis, which contains the tested websites, metrics and results.
 */
public class Analysis {

	@Getter
	private String title = "";

	@Getter
	private final Collection<WebsiteResult> benchmarkWebsites;

	@Getter
	private Collection<WebsiteResult> testWebsitesResults;

	@Getter
	private ImmutableList<Statistic> statistics;

	@Getter
	private ImmutableList<Metric> metrics;

	@Getter
	private float score = 0;

	/**
	 * 
	 * @param title
	 * @param benchmarkWebsites
	 * @param metrics
	 */
	public Analysis(String title, Collection<WebsiteResult> benchmarkWebsites,
	        ImmutableList<Metric> metrics) {
		if (title != null && title != "")
			this.title = title;
		if (benchmarkWebsites == null || benchmarkWebsites.isEmpty()) {
			throw new AnalysisException(
			        "Error while creating analysisReport, benchmarkWebsites should not be empty.");
		}
		if (metrics == null) {
			this.metrics = new ImmutableList.Builder<Metric>().build();
			log.warn("Metrics should not be null (has been converted to an empty list though)");
		} else {
			this.metrics = metrics;
		}
		this.benchmarkWebsites = benchmarkWebsites;
	}

	/**
	 * Runs all installed metrics against benchmarks and the test-results.
	 * 
	 * @param testWebsitesResults
	 *            the recrawled websiteResults, equivalent to the benchmarks.
	 */
	public void runAnalysis(Collection<WebsiteResult> testWebsitesResults) {
		this.testWebsitesResults = testWebsitesResults;
		if (testWebsitesResults == null || testWebsitesResults.size() != benchmarkWebsites.size()) {
			throw new AnalysisException(
			        "Error while creating analysisReport, testWebsitesResults should not be empty.");
		}
		runMetrics(testWebsitesResults);
	}

	private ImmutableList<Statistic> runMetrics(Collection<WebsiteResult> websiteResults) {
		ImmutableList.Builder<Statistic> resultBuilder = ImmutableList.builder();
		for (Metric metric : metrics) {
			log.info("Running metric: " + metric.getMetricName());
			try {
				// ignore/abort metrics, which throw any exception.
				Collection<Statistic> result = metric.apply(benchmarkWebsites, websiteResults);
				resultBuilder.addAll(result);
			} catch (Exception e) {
				log.error("Error occured while applying metric {}: {}", metric.getMetricName(),
				        e.getMessage());
			}
		}
		statistics = resultBuilder.build();
		return statistics;
	}

	/**
	 * Checks if the current analysis has a metric.
	 * @param classname the class of the metric.
	 * @return true if the analysis contains the metric, otherwise false.
	 */
	public boolean hasMetric(Class<?> classname) {
		for (Metric metric : metrics) {
			if (metric.getClass().equals(classname))
				return true;
		}
		return false;
	}
}