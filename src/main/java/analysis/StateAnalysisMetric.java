package main.java.analysis;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.crawljax.core.state.duplicatedetection.NearDuplicateDetection;
import com.crawljax.core.state.duplicatedetection.NearDuplicateDetectionCrawlHash32;

import lombok.extern.slf4j.Slf4j;
import main.java.distributed.results.StateResult;
import main.java.distributed.results.WebsiteResult;

@Slf4j
/**
 * This metric measures how many duplicate states and missed states the recrawled result has.
 */
public class StateAnalysisMetric implements IMetric {
	
	public static final String MISSED_STATES = "Missed states";
	public static final String DUPLICATE_STATES = "Duplicate states";

	public String getMetricName() {
		return "State Analysis";
	}

	public Map<String, Object> apply(Collection<WebsiteResult> bw, Collection<WebsiteResult> tr) {
		Map<String, Object> result = new HashMap<String, Object>();
		result.put(MISSED_STATES, "");
		result.put(DUPLICATE_STATES, "");
		for(WebsiteResult benchmarkWebsite : bw) {
			WebsiteResult testedResult = retrieveByUrl(tr, benchmarkWebsite.getWorkTask().getURL());
			// match each state
			Collection<StateResult> benchmarkStates = new ArrayList<StateResult>(benchmarkWebsite.getStateResults());
			Collection<StateResult> testedStates = new ArrayList<StateResult>(testedResult.getStateResults());
			Collection<StateResult> temp = new ArrayList<StateResult>();
			for(StateResult benchmarkState : benchmarkStates) {
				StateResult testedState = retrieveStateByHash(testedStates, benchmarkState.getStrippedDomHash(), 4);
				if(testedState != null) {
					temp.add(benchmarkState);
					if(!testedStates.remove(testedState))
						log.error("Failed to remove {} ", testedState.getStateId());
				}
			}
			if(!benchmarkStates.removeAll(temp))
				log.error("Failed to removeAll {} ", temp);
			result.put("# " + MISSED_STATES, benchmarkStates.size());
			result.put("# " + DUPLICATE_STATES, testedStates.size());
			result.put(MISSED_STATES, benchmarkStates);
			result.put(DUPLICATE_STATES, testedStates);
		}
		return result;
	}
	
	private WebsiteResult retrieveByUrl(Collection<WebsiteResult> testWebsitesResults, URL keyword) {
		for(WebsiteResult cw : testWebsitesResults) {
			if(cw.getWorkTask().getURL().equals(keyword)) {
				return cw;
			}
		}
		return null;
	}
	
	private StateResult retrieveStateByHash(Collection<StateResult> tr, int hash, int threshold) {
		for(StateResult state : tr) {
			NearDuplicateDetection npd = new NearDuplicateDetectionCrawlHash32(threshold, null);
			if(npd.isNearDuplicateHash(hash, state.getStrippedDomHash()))
				return state;
		}
		return null;		
	}

}
