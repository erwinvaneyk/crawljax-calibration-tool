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

import com.crawljax.core.state.duplicatedetection.FeatureShingles;
import com.crawljax.core.state.duplicatedetection.FeatureShinglesException;
import com.crawljax.core.state.duplicatedetection.FeatureType;
import com.crawljax.core.state.duplicatedetection.NearDuplicateDetection;
import com.crawljax.core.state.duplicatedetection.NearDuplicateDetectionCrawlHash32;
import com.crawljax.core.state.duplicatedetection.Type;

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
	public static final String TOTAL_STATES = "Total states";
	
	@Getter private int threshold = 1;
	@Getter private float score;
	@Getter private final String metricName = "State Analysis";

	public Map<String, Object> apply(Collection<WebsiteResult> bw, Collection<WebsiteResult> tr) {
		ConcurrentHashMap<String, Object> result = new ConcurrentHashMap<String, Object>();
		int missedStates = 0;
		int duplicateStates = 0;
		int totalStates = 0;
		
		for(WebsiteResult benchmarkWebsite : bw) {
			ConcurrentHashMap<String, String> duplicates = this.retrieveDuplicates(benchmarkWebsite.getId());
			WebsiteResult testedResult = retrieveByUrl(tr, benchmarkWebsite.getWorkTask().getURL());
			List<StateResult> benchmarkStates = new ArrayList<StateResult>(benchmarkWebsite.getStateResults());
			List<StateResult> testedStates = new ArrayList<StateResult>(testedResult.getStateResults());
			List<StateResult> remove = new ArrayList<StateResult>();
			for(StateResult testedState : testedStates) {
				StateResult benchmarkState = retrieveStateByHash(benchmarkStates, testedState,threshold);
				if(benchmarkState != null) {
					remove.add(testedState);
					if(!removeState(benchmarkStates, duplicates, benchmarkState.getStateId())) {
						log.error("Failed to remove {} ", benchmarkState.getStateId());
					}
					log.debug("benchmarkstates: {}", benchmarkStates);
				}
			}
			if(!testedStates.removeAll(remove))
				log.error("Failed to removeAll {} ", remove);
			benchmarkStates = removeDuplicatesFromCollection(benchmarkStates);
			result.put("(" + benchmarkWebsite.getId() + ") " + MISSED_STATES, benchmarkStates);
			result.put("(" +  benchmarkWebsite.getId() + ") " + DUPLICATE_STATES, testedStates);
			duplicateStates += testedStates.size();
			missedStates += benchmarkStates.size();
			totalStates += benchmarkWebsite.getStateResults().size() + testedResult.getStateResults().size();
		}
		result.put("# " + TOTAL_STATES,"\t" + totalStates);
		result.put("# " + MISSED_STATES, missedStates);
		result.put("# " + DUPLICATE_STATES, duplicateStates);

		score = ((float) totalStates - (float) missedStates - (float) duplicateStates)/ (float) totalStates;
		return result;
	}
	
	private List<StateResult> removeDuplicatesFromCollection(List<StateResult> states) {
		ArrayList<StateResult> temp = new ArrayList<StateResult>(states);
		if(states != null && !states.isEmpty()) {
			StateResult[] arr = states.toArray(new StateResult[0]);
			ConcurrentHashMap<String, String> duplicates = this.retrieveDuplicates(states.get(0).getWebsiteResult().getId());
			for(int i = 0; i < arr.length; i++) {
				this.removeState(temp, duplicates, arr[i].getStateId());
				temp.add(arr[i]);
			}
		}
		return temp;
	}
	
	private WebsiteResult retrieveByUrl(Collection<WebsiteResult> testWebsitesResults, URL keyword) {
		for(WebsiteResult cw : testWebsitesResults) {
			if(cw.getWorkTask().getURL().equals(keyword)) {
				return cw;
			}
		}
		return null;
	}
	
	private StateResult retrieveStateByHash(Collection<StateResult> tr, StateResult hash, int threshold) {
		StateResult result = null;
		int minDistance = Integer.MAX_VALUE;
		List<FeatureType> ft = new ArrayList<FeatureType>();
		ft.add(new FeatureShingles(1, Type.CHARS));
		NearDuplicateDetection npd = new NearDuplicateDetectionCrawlHash32(threshold,ft);
		for(StateResult state : tr) {
			try {
				int distance = npd.getDistance(
						npd.generateHash(hash.getDom()), 
						npd.generateHash(state.getDom()));
				if(distance <= threshold && distance < minDistance)
					result = state; minDistance = distance;
			} catch (FeatureShinglesException e) {
				e.printStackTrace();
			}
		}
		return result;		
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
	
	private boolean removeState(List<StateResult> states, ConcurrentHashMap<String,String> duplicates, String removedState) {
		boolean removed = false;
		for(StateResult benchmark : states) {
			log.debug("Comparing {} to {}", removedState, benchmark.getStateId());
			if(benchmark.getStateId().equals(removedState)) {
				states.remove(benchmark);
				removed = true; break;
			}
		}
		if(removed) {
			log.debug("Removed {}", removedState);
			for(Entry<String, String> duplicate : duplicates.entrySet()) {
				log.debug("Comparing {} to duplicate-entry {}", removedState, duplicate.toString());
				if(duplicate.getKey().equals(removedState) ) {
					duplicates.remove(duplicate.getKey());
					removeState(states, duplicates, duplicate.getValue());
				} else if(duplicate.getValue().equals(removedState)) {
					duplicates.remove(duplicate.getKey());
					removeState(states, duplicates, duplicate.getKey());
				}
			}
		}
		return removed;
	}
}
