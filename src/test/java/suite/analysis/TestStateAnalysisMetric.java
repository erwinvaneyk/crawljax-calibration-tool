package suite.analysis;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.Test;

import suite.TestingSuiteModule;
import suite.distributed.results.StateResult;
import suite.distributed.results.WebsiteResult;
import suite.distributed.workload.WorkTask;

import com.crawljax.core.state.duplicatedetection.Fingerprint;
import com.crawljax.core.state.duplicatedetection.NearDuplicateDetection;
import com.google.inject.Guice;

public class TestStateAnalysisMetric {

	public StateAnalysisMetric getStateAnalysisMetric() {
		return Guice.createInjector(new TestingSuiteModule(this.getClass().toString()))
		        .getInstance(
		                StateAnalysisMetric.class);
	}

	@Test
	public void testApplyEmpty() {
		// mock
		List<WebsiteResult> benchmarkWebsites = new ArrayList<WebsiteResult>();
		List<WebsiteResult> testedWebsites = new ArrayList<WebsiteResult>();
		// run method
		StateAnalysisMetric sam = getStateAnalysisMetric();
		assertNotNull(sam.apply(benchmarkWebsites, testedWebsites));
	}

	@Test
	public void testGetMetricName() {
		StateAnalysisMetric sam = getStateAnalysisMetric();
		assertNotNull(sam.getName());
	}

	private StateResult getMockedStateResult(String id, WebsiteResult parent) {
		StateResult stateResult = mock(StateResult.class);
		when(stateResult.getDom()).thenReturn("mock DOM");
		when(stateResult.getStrippedDom()).thenReturn("mock Stripped DOM");
		when(stateResult.getStateId()).thenReturn(id);
		when(stateResult.getWebsiteResult()).thenReturn(parent);
		return stateResult;
	}

	private WebsiteResult getMockedWebsiteResult(int websiteId, int states, String url) {
		WebsiteResult websiteResult = new WebsiteResult("mock json-results", 42.0F);
		websiteResult.setId(websiteId);
		websiteResult.setWorkTask(new WorkTask(websiteId, url));
		Collection<StateResult> stateResults = new ArrayList<StateResult>();
		for (int i = 0; i < states; i++) {
			stateResults.add(getMockedStateResult("state" + i, websiteResult));
		}
		websiteResult.setStateResults(stateResults);
		return websiteResult;
	}

	@Test
	public void testApply() {
		// Mock everything!
		List<WebsiteResult> testedWebsites = new ArrayList<WebsiteResult>();
		List<WebsiteResult> benchmarkedWebsites = new ArrayList<WebsiteResult>();
		testedWebsites.add(getMockedWebsiteResult(1, 2, "http://mock.mock"));
		benchmarkedWebsites.add(getMockedWebsiteResult(2, 2, "http://mock.mock"));
		NearDuplicateDetection ndd = mock(NearDuplicateDetection.class);
		Fingerprint finger = mock(Fingerprint.class);
		when(finger.getDistance(any(Fingerprint.class))).thenReturn(0.0, 1.0, 42.0);
		when(ndd.generateFingerprint(anyString())).thenReturn(finger);
		// Execute method
		StateAnalysisMetric sam = getStateAnalysisMetric();
		sam.setNearDuplicateDetection(ndd);
		Collection<Statistic> results = sam.apply(benchmarkedWebsites, testedWebsites);
		for (Statistic stat : results) {
			if (stat.getName().equals(StateAnalysisMetric.TOTAL_BENCHMARK_STATES)) {
				int value = Integer.valueOf(stat.getValue());
				assertEquals(2, value);
			}
			if (stat.getName().equals(StateAnalysisMetric.TOTAL_BENCHMARK_UNIQUE_STATES)) {
				int value = Integer.valueOf(stat.getValue());
				assertEquals(2, value);
			}
			if (stat.getName().equals(StateAnalysisMetric.TOTAL_TESTED_STATES)) {
				int value = Integer.valueOf(stat.getValue());
				assertEquals(2, value);
			}
			if (stat.getName().equals(StateAnalysisMetric.MISSED_STATES)) {
				int value = Integer.valueOf(stat.getValue());
				assertEquals(1, value);
			}
			if (stat.getName().equals(StateAnalysisMetric.DUPLICATE_STATES)) {
				int value = Integer.valueOf(stat.getValue());
				assertEquals(1, value);
			}
			if (stat.getName().equals(StateAnalysisMetric.MISSED_UNIQUE_STATES)) {
				int value = Integer.valueOf(stat.getValue());
				assertEquals(1, value);
			}
		}
	}

	private Statistic getStatistic(Collection<Statistic> stats, String name) {
		for (Statistic stat : stats) {
			if (stat.getName().equals(name))
				return stat;
		}
		return null;
	}

	@Test
	public void testApplyInverted() {
		// / Mock everything!
		List<WebsiteResult> testedWebsites = new ArrayList<WebsiteResult>();
		List<WebsiteResult> benchmarkedWebsites = new ArrayList<WebsiteResult>();
		testedWebsites.add(getMockedWebsiteResult(1, 3, "http://mock.mock"));
		benchmarkedWebsites.add(getMockedWebsiteResult(2, 3, "http://mock.mock"));
		NearDuplicateDetection ndd = mock(NearDuplicateDetection.class);
		Fingerprint finger = mock(Fingerprint.class);
		when(finger.getDistance(any(Fingerprint.class))).thenReturn(0.0, 1.0, 42.0);
		when(ndd.generateFingerprint(anyString())).thenReturn(finger);
		// Execute method
		StateAnalysisMetric sam = getStateAnalysisMetric();
		sam.setNearDuplicateDetection(ndd);

		Collection<Statistic> results = sam.apply(benchmarkedWebsites, testedWebsites);

		ndd = mock(NearDuplicateDetection.class);
		finger = mock(Fingerprint.class);
		when(finger.getDistance(any(Fingerprint.class))).thenReturn(0.0, 1.0, 42.0);
		when(ndd.generateFingerprint(anyString())).thenReturn(finger);
		sam.setNearDuplicateDetection(ndd);
		Collection<Statistic> resultsInv = sam.apply(testedWebsites, benchmarkedWebsites);

		assertEquals(
		        getStatistic(results, StateAnalysisMetric.TOTAL_BENCHMARK_STATES).getValue(),
		        getStatistic(resultsInv, StateAnalysisMetric.TOTAL_BENCHMARK_STATES).getValue());
		assertEquals(getStatistic(results, StateAnalysisMetric.TOTAL_BENCHMARK_UNIQUE_STATES)
		        .getValue(),
		        getStatistic(resultsInv, StateAnalysisMetric.TOTAL_BENCHMARK_UNIQUE_STATES)
		                .getValue());
		assertEquals(getStatistic(results, StateAnalysisMetric.TOTAL_TESTED_STATES).getValue(),
		        getStatistic(resultsInv, StateAnalysisMetric.TOTAL_TESTED_STATES).getValue());
		assertEquals(getStatistic(results, StateAnalysisMetric.DUPLICATE_STATES).getValue(),
		        getStatistic(resultsInv, StateAnalysisMetric.MISSED_STATES).getValue());
	}

	@Test(expected = NullPointerException.class)
	public void testApplyNull1() {
		List<WebsiteResult> testedWebsites = new ArrayList<WebsiteResult>();
		// run method
		StateAnalysisMetric sam = getStateAnalysisMetric();
		sam.apply(null, testedWebsites);
	}

	@Test(expected = NullPointerException.class)
	public void testApplyNullBoth() {
		// run method
		StateAnalysisMetric sam = getStateAnalysisMetric();
		sam.apply(null, null);
	}

}
