package main.java.analysis;

import com.google.common.collect.ImmutableList;

/**
 * A factory/builder for generating a analysis, for a given set of websiteResults.
 */
public interface AnalysisBuilder {

	/**
	 * Adds a metric to the metric pool
	 * 
	 * @param metric
	 *            a metric to be us
	 */
	public void addMetric(Metric metric);

	/**
	 * Gets the immutable list of metrics.
	 * 
	 * @return an immutable list of metrics
	 */
	public ImmutableList<Metric> getMetrics();

	/**
	 * Basic factory for constructing Analyses. Retrieves benchmark-results by id, re-crawls the
	 * websites and generates a report. Finally it removes all websiteResults from the DB.
	 * 
	 * @param title
	 *            The (unique) title of the analysis
	 * @param websiteids
	 *            The ids of websiteResults used as benchmarks
	 * @return A completed analysisReport
	 */
	public Analysis getAnalysis(String title, int[] websiteids);

}
