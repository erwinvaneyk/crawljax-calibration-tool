package main.java.analysis;

import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import com.crawljax.core.state.duplicatedetection.NearDuplicateDetection;
import com.crawljax.core.state.duplicatedetection.NearDuplicateDetectionCrawlHash32;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import main.java.distributed.ConnectionManager;
import main.java.distributed.results.StateResult;
import main.java.distributed.results.WebsiteResult;

@Slf4j
/**
 * This metric measures how many duplicate states and missed states the recrawled result has.
 */
public class StateAnalysisMetric implements IMetric {
	
	public static final String MISSED_STATES = "Missed states";
	public static final String DUPLICATE_STATES = "Duplicate states";
	
	@Getter private int threshold;
	
	public StateAnalysisMetric(int threshold) {
		this.threshold = threshold;
	}

	public String getMetricName() {
		return "State Analysis";
	}

	public Map<String, Object> apply(Collection<WebsiteResult> bw, Collection<WebsiteResult> tr) {
		ConcurrentHashMap<String, Object> result = new ConcurrentHashMap<String, Object>();
		result.put(MISSED_STATES, "");
		result.put(DUPLICATE_STATES, "");
		for(WebsiteResult benchmarkWebsite : bw) {
			ConcurrentHashMap<String, String> duplicates = this.retrieveDuplicates(benchmarkWebsite.getId());
			WebsiteResult testedResult = retrieveByUrl(tr, benchmarkWebsite.getWorkTask().getURL());
			// match each state
			List<StateResult> benchmarkStates = new ArrayList<StateResult>(benchmarkWebsite.getStateResults());
			List<StateResult> testedStates = new ArrayList<StateResult>(testedResult.getStateResults());
			List<StateResult> remove = new ArrayList<StateResult>();
			for(StateResult testedState : testedStates) {
				StateResult benchmarkState = retrieveStateByHash(benchmarkStates, testedState.getStrippedDomHash(),threshold);
				log.warn("{} == {}", testedState.getStateId(), benchmarkState.getStateId());
				if(benchmarkState != null) {
					remove.add(testedState);
					if(!removeState(benchmarkStates, duplicates, benchmarkState.getStateId())) {
						log.error("Failed to remove {} ", benchmarkState.getStateId());
					}
					log.warn(benchmarkStates.toString());
				}
			}
			if(!testedStates.removeAll(remove));
				log.error("Failed to remove {} ", remove);
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
	
	private ConcurrentHashMap<String, String> retrieveDuplicates(int websiteResultId) {
		ConcurrentHashMap<String, String> stateIds = new ConcurrentHashMap<String, String>();
		try {
			Connection conn = new ConnectionManager().getConnection();
			ResultSet res = conn.createStatement().executeQuery("SELECT * FROM  benchmarkSite WHERE websiteId = " + websiteResultId);
			while (res.next()) {
				stateIds.put(res.getString("stateIdFirst"),res.getString("stateIdSecond"));
			}
		} catch (SQLException e) {
			log.error("SQL exception in retrieveDuplicates: " + e.getMessage());
		}
		return stateIds;
	}
	
	private boolean removeState(List<StateResult> benchmarkStates, ConcurrentHashMap<String,String> duplicates, String removedState) {
		boolean removed = false;
		for(StateResult benchmark : benchmarkStates) {
			log.debug("Comparing {} to {}", removedState, benchmark.getStateId());
			if(benchmark.getStateId().equals(removedState)) {
				benchmarkStates.remove(benchmark);
				removed = true; break;
			}
		}
		if(removed) {
			log.debug("Removed {}", removedState);
			for(Entry<String, String> duplicate : duplicates.entrySet()) {
				log.debug("Comparing {} to duplicate-entry {}", removedState, duplicate.toString());
				if(duplicate.getKey().equals(removedState) ) {
					duplicates.remove(duplicate.getKey());
					removeState(benchmarkStates, duplicates, duplicate.getValue());
				} else if(duplicate.getValue().equals(removedState)) {
					duplicates.remove(duplicate.getKey());
					removeState(benchmarkStates, duplicates, duplicate.getKey());
				}
			}
		}
		return removed;
	}

}
