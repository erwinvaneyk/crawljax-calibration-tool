package main.java.analysis;

import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import com.crawljax.core.state.duplicatedetection.*;

import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import main.java.distributed.ConnectionManager;
import main.java.distributed.results.StateResult;
import main.java.distributed.results.WebsiteResult;

@Slf4j
/**
 * This metric measures how many duplicate states and missed states the recrawled result contains.
 */
public class StateAnalysisMetric implements IMetric {

	@Getter private final String metricName = "State Analysis Metric";
	
	@Getter private float score = 1; // stub

	public static final String MISSED_STATES 					= "Missed states";
	public static final String DUPLICATE_STATES 				= "Duplicate states";
	public static final String TOTAL_BENCHMARK_STATES 			= "Total benchmark states";
	public static final String TOTAL_TESTED_STATES 				= "Total tested states";
	public static final String TOTAL_BENCHMARK_UNIQUE_STATES 	= "Total unique benchmark states";
	public static final String MISSED_UNIQUE_STATES 			= "Missed unique states";
	
	/**
	 * The threshold is used for retrieveNearestState, to indicate the max difference between two 'similar' states.
	 */
	private int thresholdNearestState = 1;

	/**
	 * Apply will go through the StateResults of each WebsiteResult, trying to match each StateResult of the 
	 * benchmark-results to one of the 
	 */
	public Map<String, Object> apply(Collection<WebsiteResult> bw, Collection<WebsiteResult> tr) {
		// Initialize needed variables
		ConcurrentHashMap<String, Object> result = new ConcurrentHashMap<String, Object>();
		int missedStates = 0, duplicateStates = 0, missedUniqueStates = 0, 
				totalBenchmarkStates = 0, totalTestedStates = 0, totalUniqueBenchmarkStates = 0;
		
		// Loop through all websites in the benchmark-list
		for(WebsiteResult benchmarkWebsite : bw) {
			// Find relevant tested website, get StateResults, remove similar states
			WebsiteResult testedWebsite = retrieveByUrl(tr, benchmarkWebsite.getWorkTask().getURL());
			List<StateResult> benchmarkStates = new ArrayList<StateResult>(benchmarkWebsite.getStateResults());
			List<StateResult> testedStates = new ArrayList<StateResult>(testedWebsite.getStateResults());
			removeSimilarStatesFromLists(benchmarkStates, testedStates);
			
			// Store the results
			List<StateResult> benchmarkMissedUniqueStates = removeDuplicatesFromCollection(benchmarkStates, benchmarkWebsite.getId());
			duplicateStates += testedStates.size();
			missedStates += benchmarkStates.size();
			missedUniqueStates += benchmarkMissedUniqueStates.size(); 
			totalBenchmarkStates += benchmarkWebsite.getStateResults().size();
			totalTestedStates += testedWebsite.getStateResults().size();
			totalUniqueBenchmarkStates += removeDuplicatesFromCollection(
					new ArrayList<StateResult>(benchmarkWebsite.getStateResults()), benchmarkWebsite.getId()).size();
			result.put("(" + benchmarkWebsite.getId() + ") " + MISSED_UNIQUE_STATES, benchmarkMissedUniqueStates);
			result.put("(" +  benchmarkWebsite.getId() + ") " + DUPLICATE_STATES, testedStates);
		}
		// Store the results of all the websites
		result.put(TOTAL_BENCHMARK_STATES,totalBenchmarkStates);
		result.put(MISSED_STATES, missedStates);
		result.put(MISSED_UNIQUE_STATES, missedUniqueStates);
		result.put(TOTAL_TESTED_STATES, totalTestedStates);
		result.put(TOTAL_BENCHMARK_UNIQUE_STATES, totalUniqueBenchmarkStates);
		result.put(DUPLICATE_STATES, duplicateStates);
		return result;
	}
	
	/**
	 * Removes all states, present in both lists, from the lists. Also deleting any duplicates in benchmarkStates.
	 * @param benchmarkStates states from the benchmark-website
	 * @param testedStates states from the tested-website
	 */
	private void removeSimilarStatesFromLists(@NonNull List<StateResult> benchmarkStates, @NonNull List<StateResult> testedStates) {
		assert !benchmarkStates.isEmpty();
		// Retrieve the duplicate-table
		ConcurrentHashMap<String, String> duplicates = retrieveDuplicatesMap(benchmarkStates.get(0).getWebsiteResult().getId());
		for(Iterator<StateResult> iterator = testedStates.iterator(); iterator.hasNext();) {
			// Find the similar/equal state in the benchmark-list, under a threshold.
			StateResult testState = iterator.next();
			StateResult benchmarkState = retrieveNearestState(benchmarkStates, testState, thresholdNearestState);
			if(benchmarkState != null) {
				// If found remove the state and all duplicates from the benchmark-list, 
				if(!removeStateAndDuplicates(benchmarkStates, duplicates, benchmarkState.getStateId())) {
					log.error("Failed to remove {} ", benchmarkState.getStateId());
				}
				iterator.remove();
			} else {
				log.warn("State {} could not be matched to benchmark-state -> duplicate", benchmarkState);
			}
		}
	}
	
	/**
	 * Removes all duplicates in a StateResults-list, using the duplicates-mapping of the websiteResult-id.
	 * @param states The original list with any duplicates.
	 * @return A new list containing the stateResults without any duplicates
	 */
	private List<StateResult> removeDuplicatesFromCollection(@NonNull List<StateResult> states, int websiteResultId) {
		ArrayList<StateResult> temp = new ArrayList<StateResult>(states);
		// get the duplicates-mapping relevant for this list, using the websiteResult-id.
		ConcurrentHashMap<String, String> duplicates = retrieveDuplicatesMap(websiteResultId);
		for(StateResult current : states) {
			if(temp.contains(current)) {
				this.removeStateAndDuplicates(temp, duplicates, current.getStateId());
				temp.add(current);
			}
		}
		return temp;
	}
	
	/**
	 * Retrieves WebsiteResult, given a URL.
	 * @param websites the collection to be searched in.
	 * @param url the url for which the WebsiteResult needs to be found.
	 * @return WebsiteResult with the given url, or else null.
	 */
	private WebsiteResult retrieveByUrl(Collection<WebsiteResult> websites, URL url) {
		for(WebsiteResult cw : websites) {
			if(cw.getWorkTask().getURL().equals(url))
				return cw;
		}
		return null;
	}
	
	/**
	 * For a given state, find the nearest state in the collection states, under a given threshold.
	 * @param states collection to be searched in.
	 * @param source the reference state
	 * @param threshold the upper boundary for the distance to the nearest state in states.
	 * @return StateResult of the state nearest to source. Else if no State was found under the threshold, return null.
	 */
	private StateResult retrieveNearestState(Collection<StateResult> states, @NonNull StateResult source, int threshold) {
		StateResult result = null;
		int minDistance = Integer.MAX_VALUE;
		// Configure a NearestDuplicateDetection for comparing the states 
		List<FeatureType> ft = new ArrayList<FeatureType>();
		ft.add(new FeatureShingles(1, FeatureSizeType.CHARS));
		NearDuplicateDetection npd = new NearDuplicateDetectionCrawlHash32(threshold,ft);
		
		for(StateResult state : states) {
			try {
				// For each state, calculate the distance from the source to the state.
				int distance = npd.getDistance(
						npd.generateHash(source.getDom()), 
						npd.generateHash(state.getDom()));
				// If the distance is better than the previous distance, hold current state. 
				if(distance <= threshold && distance < minDistance) {
					result = state; 
					minDistance = distance;
				}
			} catch (FeatureShinglesException e) {
				log.error("Error while retrieve nearest state: {}", e.getMessage());
			}
		}
		return result;		
	}
	
	/**
	 * TODO: could be moved to a database-utils class, non?
	 * Retrieves the duplicate-mapping for a given websiteResultID.
	 * @param websiteResultId the websiteResultID for which the mapping should be retrieved
	 * @return map with tuples defining duplicates, using a format <WebsiteResultID, WebsiteResultID>,
	 * 			if an error occurd or nothing was found, return an empty map.
	 */
	private ConcurrentHashMap<String, String> retrieveDuplicatesMap(int websiteResultId) {
		ConcurrentHashMap<String, String> stateIds = new ConcurrentHashMap<String, String>();
		try {
			// Retrieve the duplicate mapping from the database.
			// TODO hide database-details
			Connection conn = new ConnectionManager().getConnection();
			ResultSet res = conn.createStatement().executeQuery("SELECT * FROM  benchmarkSite WHERE websiteId = " + websiteResultId);
			while (res.next()) {
				stateIds.put(res.getString("stateIdFirst"),res.getString("stateIdSecond"));
			}
		} catch (SQLException e) {
			log.error("SQL exception while retrieving duplicates: " + e.getMessage());
		}
		return stateIds;
	}
	
	/**
	 * Removes relevant state for a given id
	 * @param states collection to be searched in.
	 * @param stateId the id of the state to be removed.
	 * @return true when state was deleted, else false. 
	 */
	private boolean removeStateById(List<StateResult> states, String stateId) {
		boolean removed = false;
		for(StateResult benchmark : states) {
			if(benchmark.getStateId().equals(stateId)) {
				states.remove(benchmark);
				removed = true; 
				break;
			}
		}
		return removed;
	}
	
	/**
	 * Recursively removes the state and all its duplicates from the list.
	 * @param states the list in which the states need to be removed.
	 * @param duplicates a duplicate-mapping, in which all relevant tuples will also be removed.
	 * @param stateId the stateID of the state to be removed with all its duplicates
	 * @return true when state was deleted, else false.
	 */
	private boolean removeStateAndDuplicates(List<StateResult> states, ConcurrentHashMap<String,String> duplicates, String stateId) {
		// Remove the state
		boolean removed = removeStateById(states, stateId);
		if(removed) {
			for(Entry<String, String> duplicate : duplicates.entrySet()) {
				// given an entry/tuple, remove the entry from the map (to prevent infinite recursions) 
				// and recursively remove the key (if the value was the current stateId) or else the value.
				if(duplicate.getKey().equals(stateId) ) {
					duplicates.remove(duplicate.getKey());
					removeStateAndDuplicates(states, duplicates, duplicate.getValue());
				} else if(duplicate.getValue().equals(stateId)) {
					duplicates.remove(duplicate.getKey());
					removeStateAndDuplicates(states, duplicates, duplicate.getKey());
				}
			}
		}
		return removed;
	}
}
