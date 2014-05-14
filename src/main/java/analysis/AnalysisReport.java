package main.java.analysis;

import java.net.URL;
import java.util.Collection;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import main.java.distributed.results.StateResult;
import main.java.distributed.results.WebsiteResult;

@Slf4j
@Data public class AnalysisReport {
	
	// Setup
	private final String title;
	
	private final Collection<WebsiteResult> benchmarkWebsites;
	
	private boolean analysed = false;
	
	// Metrics
	private float accuracy;
	
	private Collection<StateResult> failedStatesMissed;
	
	private Collection<StateResult> failedStatesDuplicates;
	
	private double speedDifference;
	
	public void runAnalysis(Collection<WebsiteResult> testWebsitesResults) {
		log.info("tested websiteResults:\t" + testWebsitesResults.toString());
		log.info("benchmarked websitesResults:\t " + benchmarkWebsites.toString());
		accuracy = accuracyAnalysis(benchmarkWebsites, testWebsitesResults);
		speedDifference = speedAnalysis(benchmarkWebsites, testWebsitesResults);
		analysed = true;
	}
	
	private float accuracyAnalysis(Collection<WebsiteResult> benchmarkWebsites, Collection<WebsiteResult> testWebsitesResults) {
		// compare results
		float result = 0;
		for(WebsiteResult baseWebsite : benchmarkWebsites) {
			WebsiteResult crawledWebsite = retrieveByUrl(testWebsitesResults, baseWebsite.getWorkTask().getURL());
			
			// check which states are present
			Collection<StateResult> baseStates = baseWebsite.getStateResults();
			Collection<StateResult> crawledStates = crawledWebsite.getStateResults();
			result += (float) crawledStates.size() / (float) baseStates.size();
			log.info("Accuracy of " + baseWebsite.getWorkTask().getURL().toString() + ": " + (float) crawledStates.size() / (float) baseStates.size()
					+ " (" + crawledStates.size() + " / " + baseStates.size() + ")");
		}		
		return result / (float) benchmarkWebsites.size();
	}
	
	private long speedAnalysis(Collection<WebsiteResult> benchmarkWebsites, Collection<WebsiteResult> testWebsitesResults) {
		long benchmarkDuration = 0;
		long testDuration = 0;
		for(WebsiteResult baseWebsite : benchmarkWebsites) {
			benchmarkDuration += baseWebsite.getDuration();
		}
		for(WebsiteResult testWebsite : testWebsitesResults) {
			testDuration += testWebsite.getDuration();
		}
		return testDuration - benchmarkDuration;
		
	}
	
	private WebsiteResult retrieveByUrl(Collection<WebsiteResult> testWebsitesResults, URL keyword) {
		for(WebsiteResult cw : testWebsitesResults) {
			if(cw.getWorkTask().getURL().equals(keyword)) {
				return cw;
			}
		}
		return null;
	}
}
