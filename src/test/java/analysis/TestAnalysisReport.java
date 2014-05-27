package test.java.analysis;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

import main.java.analysis.AnalysisException;
import main.java.analysis.Analysis;
import main.java.distributed.ConnectionManagerORM;
import main.java.distributed.results.WebsiteResult;

import org.junit.Test;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;

public class TestAnalysisReport {

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
	
	@Test(expected=AnalysisException.class)
	public void testAnalysisReportWebsiteResultsEmpty() throws AnalysisException {
		new Analysis("WebsiteResultsEmpty", new ArrayList<WebsiteResult>(), null);
	}
	
	@Test(expected=AnalysisException.class)
	public void testAnalysisReportWebsiteResultsNull() throws AnalysisException {
		new Analysis("WebsiteResultsEmpty", null, null);
	}

	@Test
	public void testRunAnalysisSame() throws AnalysisException, SQLException {
		ConnectionManagerORM connMgr = new ConnectionManagerORM();
		Dao<WebsiteResult,String> dao = DaoManager.createDao(connMgr.getConnectionORM(), WebsiteResult.class);
		WebsiteResult benchmarkWebsite = dao.queryForId(String.valueOf(TestAnalysisFactory.benchmarkWebsiteID));
		Collection<WebsiteResult> input = new ArrayList<WebsiteResult>();
		input.add(benchmarkWebsite);
		Analysis result = new Analysis("testRunAnalysisSame", input, null);
		result.runAnalysis(input);
		// temporary
		assertEquals(result.getTestWebsitesResults(), input);
		assertEquals(result.getBenchmarkWebsites(), input);
		connMgr.closeConnection();
	}

	@Test(expected=AnalysisException.class)
	public void testRunAnalysisMissingWebsites() throws AnalysisException {
		WebsiteResult wr = mock(WebsiteResult.class);
		Collection<WebsiteResult> input = new ArrayList<WebsiteResult>();
		input.add(wr);
		Analysis result = new Analysis("testRunAnalysisEmpty", input, null);
		result.runAnalysis(new ArrayList<WebsiteResult>());
	}

	@Test(expected=AnalysisException.class)
	public void testRunAnalysisNull() throws AnalysisException {
		Collection<WebsiteResult> input = new ArrayList<WebsiteResult>();
		input.add(mock(WebsiteResult.class));
		Analysis result = new Analysis("testRunAnalysisEmpty", input, null);
		result.runAnalysis(null);
	}
}
