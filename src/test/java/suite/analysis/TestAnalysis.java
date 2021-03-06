package suite.analysis;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Collection;

import org.junit.Test;

import suite.distributed.results.StateResult;
import suite.distributed.results.WebsiteResult;
import suite.distributed.workload.WorkTask;

public class TestAnalysis {

	@Test
	public void testAnalysisReport() throws AnalysisException {
		WebsiteResult wr = mock(WebsiteResult.class);
		Collection<WebsiteResult> input = new ArrayList<WebsiteResult>();
		input.add(wr);
		String title = "testAnalysisReport";
		Analysis result = new Analysis(title, input, null);
		assertEquals(result.getBenchmarkWebsites(), input);
		assertEquals(result.getTitle(), title);
	}

	@Test
	public void testAnalysisReportTitleNull() throws AnalysisException {
		Collection<WebsiteResult> input = new ArrayList<WebsiteResult>();
		input.add(mock(WebsiteResult.class));
		String title = null;
		Analysis result = new Analysis(title, input, null);
		assertEquals(result.getBenchmarkWebsites(), input);
		assertNotNull(result.getTitle());
	}

	@Test
	public void testAnalysisReportTitleEmpty() throws AnalysisException {
		WebsiteResult wr = mock(WebsiteResult.class);
		Collection<WebsiteResult> input = new ArrayList<WebsiteResult>();
		input.add(wr);
		String title = "";
		Analysis result = new Analysis(title, input, null);
		assertEquals(result.getBenchmarkWebsites(), input);
		assertNotNull(result.getTitle());
	}

	@Test(expected = AnalysisException.class)
	public void testAnalysisReportWebsiteResultsEmpty() throws AnalysisException {
		new Analysis("WebsiteResultsEmpty", new ArrayList<WebsiteResult>(), null);
	}

	@Test(expected = AnalysisException.class)
	public void testAnalysisReportWebsiteResultsNull() throws AnalysisException {
		new Analysis("WebsiteResultsEmpty", null, null);
	}

	@Test
	public void testRunAnalysisSame() throws AnalysisException {

		WorkTask workTask = new WorkTask(4, "mocked.nl");
		WebsiteResult benchmarkWebsite = new WebsiteResult("MOCK JSON results", 42);
		benchmarkWebsite.setId(3);
		benchmarkWebsite.setWorkTask(workTask);
		Collection<StateResult> stateResults = new ArrayList<StateResult>();
		StateResult stateResult =
		        new StateResult(benchmarkWebsite, "state1", "MOCK DOM", "STRIPPED MOCK DOM",
		                "01010101", null);
		stateResults.add(stateResult);
		benchmarkWebsite.setStateResults(stateResults);
		Collection<WebsiteResult> input = new ArrayList<WebsiteResult>();

		input.add(benchmarkWebsite);
		Analysis result = new Analysis("testRunAnalysisSame", input, null);
		result.runAnalysis(input);
		// temporary
		assertEquals(result.getTestWebsitesResults(), input);
		assertEquals(result.getBenchmarkWebsites(), input);
	}

	@Test(expected = AnalysisException.class)
	public void testRunAnalysisMissingWebsites() throws AnalysisException {
		WebsiteResult wr = mock(WebsiteResult.class);
		Collection<WebsiteResult> input = new ArrayList<WebsiteResult>();
		input.add(wr);
		Analysis result = new Analysis("testRunAnalysisEmpty", input, null);
		result.runAnalysis(new ArrayList<WebsiteResult>());
	}

	@Test(expected = AnalysisException.class)
	public void testRunAnalysisNull() throws AnalysisException {
		Collection<WebsiteResult> input = new ArrayList<WebsiteResult>();
		input.add(mock(WebsiteResult.class));
		Analysis result = new Analysis("testRunAnalysisEmpty", input, null);
		result.runAnalysis(null);
	}
}
