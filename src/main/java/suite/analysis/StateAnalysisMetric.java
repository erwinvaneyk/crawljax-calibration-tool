package suite.analysis;

import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import suite.distributed.DatabaseUtils;
import suite.distributed.results.StateResult;
import suite.distributed.results.WebsiteResult;

import com.crawljax.core.state.duplicatedetection.*;
import com.google.inject.Inject;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
/**
 * This metric measures how many duplicate states and missed states the recrawled result contains.
 */
public class StateAnalysisMetric implements Metric {

	@Getter
	private final String metricName = "State Analysis Metric";

	@Getter
	@Setter
	NearDuplicateDetection nearDuplicateDetection;

	public static final String MISSED_STATES = "Missed states";
	public static final String DUPLICATE_STATES = "Duplicate states";
	public static final String TOTAL_BENCHMARK_STATES = "Total benchmark states";
	public static final String TOTAL_TESTED_STATES = "Total tested states";
	public static final String TOTAL_BENCHMARK_UNIQUE_STATES = "Total unique benchmark states";
	public static final String MISSED_UNIQUE_STATES = "Missed unique states";

	private DatabaseUtils databaseUtils;

	@Inject
	public StateAnalysisMetric(DatabaseUtils databaseUtils, NearDuplicateDetection npd) {
		// Configure a NearestDuplicateDetection for comparing the states
		this.nearDuplicateDetection = npd;
		this.databaseUtils = databaseUtils;
	}

	/**
	 * Apply will go through the StateResults of each WebsiteResult, trying to match each
	 * StateResult of the benchmark-results to one of the
	 */
	public Collection<Statistic> apply(Collection<WebsiteResult> bw, Collection<WebsiteResult> tr) {
		// Initialize needed variables
		Collection<Statistic> result = new ArrayList<Statistic>();
		int missedStates = 0, duplicateStates = 0, missedUniqueStates = 0, totalBenchmarkStates =
		        0, totalTestedStates = 0, totalUniqueBenchmarkStates = 0;

		// Loop through all websites in the benchmark-list
		for (WebsiteResult benchmarkWebsite : bw) {
			// Find relevant tested website, get StateResults, remove similar states
			WebsiteResult testedWebsite =
			        retrieveByUrl(tr, benchmarkWebsite.getWorkTask().getURL());
			List<StateResult> benchmarkStates =
			        new ArrayList<StateResult>(benchmarkWebsite.getStateResults());
			List<StateResult> testedStates =
			        new ArrayList<StateResult>(testedWebsite.getStateResults());
			removeSimilarStatesFromLists(benchmarkStates, testedStates);

			// Store the results
			List<StateResult> benchmarkMissedUniqueStates =
			        removeDuplicatesFromCollection(benchmarkStates, benchmarkWebsite.getId());
			duplicateStates += testedStates.size();
			missedStates += benchmarkStates.size();
			missedUniqueStates += benchmarkMissedUniqueStates.size();
			totalBenchmarkStates += benchmarkWebsite.getStateResults().size();
			totalTestedStates += testedWebsite.getStateResults().size();
			totalUniqueBenchmarkStates +=
			        removeDuplicatesFromCollection(
			                new ArrayList<StateResult>(benchmarkWebsite.getStateResults()),
			                benchmarkWebsite.getId()).size();
			result.add(new Statistic(
			        "(" + benchmarkWebsite.getId() + ") " + MISSED_UNIQUE_STATES,
			        String.valueOf(benchmarkMissedUniqueStates.size()),
			        benchmarkMissedUniqueStates));
			result.add(new Statistic("(" + benchmarkWebsite.getId() + ") " + DUPLICATE_STATES,
			        String.valueOf(testedStates.size()), testedStates));
		}
		// Store the results of all the websites
		result.add(new Statistic(TOTAL_BENCHMARK_STATES, String.valueOf(totalBenchmarkStates)));
		result.add(new Statistic(MISSED_STATES, String.valueOf(missedStates)));
		result.add(new Statistic(MISSED_UNIQUE_STATES, String.valueOf(missedUniqueStates)));
		result.add(new Statistic(TOTAL_TESTED_STATES, String.valueOf(totalTestedStates)));
		result.add(new Statistic(TOTAL_BENCHMARK_UNIQUE_STATES, String
		        .valueOf(totalUniqueBenchmarkStates)));
		result.add(new Statistic(DUPLICATE_STATES, String.valueOf(duplicateStates)));
		return result;
	}

	/**
	 * Removes all states, present in both lists, from the lists. Also deleting any duplicates in
	 * benchmarkStates.
	 * 
	 * @param benchmarkStates
	 *            states from the benchmark-website
	 * @param testedStates
	 *            states from the tested-website
	 */
	private void removeSimilarStatesFromLists(@NonNull List<StateResult> benchmarkStates,
	        @NonNull List<StateResult> testedStates) {
		assert !benchmarkStates.isEmpty();
		// Retrieve the duplicate-table
		ConcurrentHashMap<String, String> duplicates = null;
		try {
			duplicates =
			        databaseUtils.retrieveDuplicatesMap(benchmarkStates.get(0).getWebsiteResult()
			                .getId());
		} catch (SQLException e) {
			log.error(
			        "SQL error while retrieving the duplicate-map: {}. Assuming that duplicate-map does not exist.",
			        e.getMessage());
		}
		for (Iterator<StateResult> iterator = testedStates.iterator(); iterator.hasNext();) {
			// Find the similar/equal state in the benchmark-list, under a threshold.
			StateResult testState = iterator.next();
			StateResult benchmarkState =
			        retrieveNearestState(benchmarkStates, testState);
			if (benchmarkState != null) {
				// If found remove the state and all duplicates from the benchmark-list,
				if (!removeStateAndDuplicates(benchmarkStates, duplicates,
				        benchmarkState.getStateId())) {
					log.error("Failed to remove {} ", benchmarkState.getStateId());
				}
				iterator.remove();
			} else {
				log.warn("State {} could not be matched to benchmark-state -> duplicate",
				        testState);
			}
		}
	}

	/**
	 * Removes all duplicates in a StateResults-list, using the duplicates-mapping of the
	 * websiteResult-id.
	 * 
	 * @param states
	 *            The original list with any duplicates.
	 * @return A new list containing the stateResults without any duplicates
	 */
	private List<StateResult> removeDuplicatesFromCollection(@NonNull List<StateResult> states,
	        int websiteResultId) {
		ArrayList<StateResult> temp = new ArrayList<StateResult>(states);
		// get the duplicates-mapping relevant for this list, using the websiteResult-id.
		try {
			ConcurrentHashMap<String, String> duplicates =
			        databaseUtils.retrieveDuplicatesMap(websiteResultId);
			for (StateResult current : states) {
				if (temp.contains(current)) {
					this.removeStateAndDuplicates(temp, duplicates, current.getStateId());
					temp.add(current);
				}
			}
		} catch (SQLException e) {
			log.error(
			        "SQL error while retrieving the duplicate-map: {}. Assuming that duplicate-map does not exist.",
			        e.getMessage());
		}
		return temp;
	}

	/**
	 * Retrieves WebsiteResult, given a URL.
	 * 
	 * @param websites
	 *            the collection to be searched in.
	 * @param url
	 *            the url for which the WebsiteResult needs to be found.
	 * @return WebsiteResult with the given url, or else null.
	 */
	private WebsiteResult retrieveByUrl(Collection<WebsiteResult> websites, URL url) {
		for (WebsiteResult cw : websites) {
			if (cw.getWorkTask().getURL().equals(url))
				return cw;
		}
		return null;
	}

	/**
	 * For a given state, find the nearest state in the collection states, under a given threshold.
	 * 
	 * @param states
	 *            collection to be searched in.
	 * @param source
	 *            the reference state
	 * @param threshold
	 *            the upper boundary for the distance to the nearest state in states.
	 * @return StateResult of the state nearest to source. Else if no State was found under the
	 *         threshold, return null.
	 */
	private StateResult retrieveNearestState(Collection<StateResult> states,
	        @NonNull StateResult source) {
		StateResult result = null;
		int minDistance = Integer.MAX_VALUE;
		for (StateResult state : states) {
			try {
				// For each state, calculate the distance from the source to the state.
				double distance =
				        nearDuplicateDetection.generateFingerprint(source.getDom()).getDistance(
				                nearDuplicateDetection.generateFingerprint(state.getDom()));
				// If the distance is better than the previous distance, hold current state.
				if (distance <= nearDuplicateDetection.getDefaultThreshold() && distance < minDistance) {
					result = state;
					minDistance = (int) distance;
				}
			} catch (FeatureException e) {
				log.error("Error while retrieve nearest state: {}", e.getMessage());
			}
		}
		return result;
	}

	/**
	 * Removes relevant state for a given id
	 * 
	 * @param states
	 *            collection to be searched in.
	 * @param stateId
	 *            the id of the state to be removed.
	 * @return true when state was deleted, else false.
	 */
	private boolean removeStateById(List<StateResult> states, String stateId) {
		boolean removed = false;
		for (StateResult benchmark : states) {
			if (benchmark.getStateId().equals(stateId)) {
				states.remove(benchmark);
				removed = true;
				break;
			}
		}
		return removed;
	}

	/**
	 * Recursively removes the state and all its duplicates from the list.
	 * 
	 * @param states
	 *            the list in which the states need to be removed.
	 * @param duplicates
	 *            a duplicate-mapping, in which all relevant tuples will also be removed.
	 * @param stateId
	 *            the stateID of the state to be removed with all its duplicates
	 * @return true when state was deleted, else false.
	 */
	private boolean removeStateAndDuplicates(List<StateResult> states,
	        ConcurrentHashMap<String, String> duplicates, String stateId) {
		// Remove the state
		boolean removed = removeStateById(states, stateId);
		if (removed) {
			for (Entry<String, String> duplicate : duplicates.entrySet()) {
				// given an entry/tuple, remove the entry from the map (to prevent infinite
				// recursions)
				// and recursively remove the key (if the value was the current stateId) or else the
				// value.
				if (duplicate.getKey().equals(stateId)) {
					duplicates.remove(duplicate.getKey());
					removeStateAndDuplicates(states, duplicates, duplicate.getValue());
				} else if (duplicate.getValue().equals(stateId)) {
					duplicates.remove(duplicate.getKey());
					removeStateAndDuplicates(states, duplicates, duplicate.getKey());
				}
			}
		}
		return removed;
	}
}
