/**
 * 
 */
package suite.analysis;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.Test;

import suite.distributed.results.StateResult;
import suite.distributed.results.WebsiteResult;
import suite.distributed.workload.WorkTask;

public class TestSpeedMetric {

	/**
	 * Test method for {@link suite.analysis.SpeedMetric#getMetricName()}.
	 */
	@Test
	public void testGetMetricName() {
		SpeedMetric metric = new SpeedMetric();
		assertNotNull(metric.getName());
	}

	/**
	 * Test method for
	 * {@link suite.analysis.SpeedMetric#apply(java.util.Collection, java.util.Collection)}.
	 */
	@Test
	public void testApply() {
		SpeedMetric metric = new SpeedMetric();
		WebsiteResult bw1a = getMockedWebsiteResult(1, 3, "http://www.test1.com/", 100.0F);
		WebsiteResult bw1b = getMockedWebsiteResult(1, 3, "http://www.test1.com/", 70.0F);
		WebsiteResult bw2a = getMockedWebsiteResult(2, 5, "http://www.test2.com/", 100.0F);
		WebsiteResult bw2b = getMockedWebsiteResult(2, 5, "http://www.test2.com/", 110.0F);
		List<WebsiteResult> benchmarkWebsites = new ArrayList<WebsiteResult>();
		List<WebsiteResult> testWebsitesResults = new ArrayList<WebsiteResult>();
		benchmarkWebsites.add(bw1a);
		benchmarkWebsites.add(bw2a);
		testWebsitesResults.add(bw1b);
		testWebsitesResults.add(bw2b);
		Collection<Statistic> results = metric.apply(benchmarkWebsites, testWebsitesResults);
		for (Statistic stat : results) {
			assertEquals(SpeedMetric.SPEED_INCREASE, stat.getName());
			assertEquals(((-1 * (180F - 200F)) / 200F * 100) + "%", stat.getValue());
		}
	}

	private StateResult getMockedStateResult(String id, WebsiteResult parent) {
		StateResult stateResult = mock(StateResult.class);
		when(stateResult.getDom()).thenReturn("mock DOM");
		when(stateResult.getStrippedDom()).thenReturn("mock Stripped DOM");
		when(stateResult.getStateId()).thenReturn(id);
		when(stateResult.getWebsiteResult()).thenReturn(parent);
		return stateResult;
	}

	private WebsiteResult getMockedWebsiteResult(int websiteId, int states, String url,
	        float duration) {
		WebsiteResult websiteResult = new WebsiteResult("mock json-results", duration);
		websiteResult.setId(websiteId);
		websiteResult.setWorkTask(new WorkTask(websiteId, url));
		Collection<StateResult> stateResults = new ArrayList<StateResult>();
		for (int i = 0; i < states; i++) {
			stateResults.add(getMockedStateResult("state" + i, websiteResult));
		}
		websiteResult.setStateResults(stateResults);
		return websiteResult;
	}
}
