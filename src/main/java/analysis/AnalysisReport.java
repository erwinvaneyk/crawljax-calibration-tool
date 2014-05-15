package main.java.analysis;

import java.net.URL;
import java.util.Collection;

import com.crawljax.core.state.duplicatedetection.NearDuplicateDetection;
import com.crawljax.core.state.duplicatedetection.NearDuplicateDetectionCrawlHash32;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import main.java.distributed.results.StateResult;
import main.java.distributed.results.WebsiteResult;

@Slf4j
@Data public class AnalysisReport {
	
	// Setup
	private final String title;
	
	private final Collection<WebsiteResult> benchmarkWebsites;
	
	// Metrics
	private float accuracy;
	
	private Collection<StateResult> failedStatesMissed;
	
	private Collection<StateResult> failedStatesDuplicates;
	
	private double speedDifference;
	
	public void runAnalysis(Collection<WebsiteResult> testWebsitesResults) {
		log.info("tested websiteResults (" + testWebsitesResults.size() + "):\t" + testWebsitesResults.toString());
		log.info("benchmarked websitesResults("+benchmarkWebsites.size()+"):\t " + benchmarkWebsites.toString());
		accuracy = accuracyAnalysis(benchmarkWebsites, testWebsitesResults);
		speedDifference = speedAnalysis(benchmarkWebsites, testWebsitesResults);
		log.info("Missed websiteResults (" + failedStatesMissed.size() + "):\t" + failedStatesMissed.toString());
		log.info("Redundant websitesResults("+failedStatesDuplicates.size()+"):\t " + failedStatesDuplicates.toString());
		stateAnalysis(benchmarkWebsites, testWebsitesResults);
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
	
	// missed states = in Benchmark, not in testresults
	// Redundant states = not in Benchmark, in testresults
	private void stateAnalysis(Collection<WebsiteResult> bw, Collection<WebsiteResult> tr) {
		for(WebsiteResult benchmarkWebsite : bw) {
			WebsiteResult testedResult = retrieveByUrl(tr, benchmarkWebsite.getWorkTask().getURL());
			for(StateResult benchmarkState : benchmarkWebsite.getStateResults()) {
				StateResult testedState = retrieveStateByHash(testedResult, benchmarkState.getStrippedDomHash(), 4);
				if(testedState != null) {
					testedResult.getStateResults().remove(testedState);
					benchmarkWebsite.getStateResults().remove(benchmarkState);
				}
			}
			// metrics
			failedStatesMissed.addAll(benchmarkWebsite.getStateResults());
			failedStatesDuplicates.addAll(testedResult.getStateResults());
		}
	}
	
	private StateResult retrieveStateByHash(WebsiteResult tr, int hash, int threshold) {
		for(StateResult state : tr.getStateResults()) {
			NearDuplicateDetection npd = new NearDuplicateDetectionCrawlHash32(threshold, null);
			npd.isNearDuplicateHash(hash, state.getStrippedDomHash());
		}
		return null;		
	}
}
