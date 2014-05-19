package main.java.analysis;

import java.util.Collection;
import java.util.Map;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import main.java.distributed.results.WebsiteResult;

@Slf4j
/**
 * Data model of an analysis, which contains the tested websites, metrics and results.
 */
public class Analysis {
	
	// Setup
	@Getter private String title = "";
	
	@Getter private final Collection<WebsiteResult> benchmarkWebsites;
	
	@Getter private Collection<WebsiteResult> testWebsitesResults;
	
	// Metrics
	@Getter private ImmutableMap<String, Object> statistics;
	
	@Getter private ImmutableList<IMetric> metrics;
	
	@Getter private float score;
	
	public Analysis(String title,  Collection<WebsiteResult> benchmarkWebsites, ImmutableList<IMetric> metrics) throws AnalysisException {
		if (title != null && title != "") this.title = title;
		if(benchmarkWebsites == null || benchmarkWebsites.isEmpty()) {
			throw new AnalysisException("Error while creating analysisReport, benchmarkWebsites should not be empty.");
		}
		this.metrics = metrics;
		this.benchmarkWebsites = benchmarkWebsites;
	}
	
	/**
	 * Runs all installed metrics against benchmarks and the test-results.
	 * @param testWebsitesResults the recrawled websiteResults, equivalent to the benchmarks.
	 * @throws AnalysisException 
	 */
	public void runAnalysis(Collection<WebsiteResult> testWebsitesResults) throws AnalysisException {
		assert benchmarkWebsites != null && !benchmarkWebsites.isEmpty();
		if(testWebsitesResults == null || testWebsitesResults.size() != benchmarkWebsites.size()) {
			throw new AnalysisException("Error while creating analysisReport, testWebsitesResults should not be empty.");
		}
		this.testWebsitesResults = testWebsitesResults;
		// Run metric tests
		Builder<String, Object> resultBuilder = ImmutableMap.builder();
		int succesfulMetrics = 0;
		for(IMetric metric : metrics) {
			log.info("Running metric: " + metric.getMetricName());
			try {
				Map<String,Object> result = metric.apply(benchmarkWebsites, testWebsitesResults);
				resultBuilder.putAll(result);
				score = metric.getScore();
				succesfulMetrics++;
			} catch(Exception e) {
				log.error("Error occured while applying metric {}: {}", metric.getMetricName(), e.getMessage());
			}
		}
		score = score / (float) succesfulMetrics;
		statistics = resultBuilder.build();
	}
}
