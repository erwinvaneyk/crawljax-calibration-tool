package test.java.analysis;

import static org.junit.Assert.*;

import java.util.List;

import main.java.analysis.AnalysisException;
import main.java.analysis.AnalysisFactory;
import main.java.analysis.Analysis;
import main.java.distributed.ConnectionManagerORM;
import main.java.distributed.results.WebsiteResult;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;

public class TestAnalysisFactory {
	
	public final static String BenchmarkUrl = "http://demo.crawljax.com"; 
	public static WebsiteResult benchmarkWebsite;
	public static int benchmarkWebsiteID = 1;
	private static ConnectionManagerORM connMgr;

	@BeforeClass
	public static void benchmarkWebsite() throws Exception {
		connMgr = new ConnectionManagerORM();
		Dao<WebsiteResult,String> dao = DaoManager.createDao(connMgr.getConnectionORM(), WebsiteResult.class);
		benchmarkWebsite = dao.queryForId(String.valueOf(benchmarkWebsiteID));
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		connMgr.closeConnection();
	}

	@Test
	public void testGetAnalysisValid() throws AnalysisException {
		AnalysisFactory af = new AnalysisFactory();
		Analysis res = af.getAnalysis("testGetAnalysis", new int[]{benchmarkWebsite.getId()});
		assertNotNull(res);
		assertNotNull(res.getTitle());
		assertNotNull(res.getBenchmarkWebsites());
		assertEquals(res.getBenchmarkWebsites().size(),1);
		assertTrue(res.getBenchmarkWebsites().contains(benchmarkWebsite));
	}
	
	@Test
	public void testGetAnalysisTitleNull() throws AnalysisException {
		AnalysisFactory af = new AnalysisFactory();
		Analysis res = af.getAnalysis(null, new int[]{benchmarkWebsite.getId()});
		assertNotNull(res);
		assertNotNull(res.getTitle());
		assertEquals(res.getTitle(), "");
		assertNotNull(res.getBenchmarkWebsites());
		assertEquals(res.getBenchmarkWebsites().size(),1);
		assertTrue(res.getBenchmarkWebsites().contains(benchmarkWebsite));
	}
	
	@Test(expected=AnalysisException.class)
	public void testGetAnalysisIDsNull() throws AnalysisException {
		AnalysisFactory af = new AnalysisFactory();
		af.getAnalysis("TestGetAnalysisIDsNull", null);
	}
	
	@Test(expected=AnalysisException.class)
	public void testGetAnalysisBothNull() throws AnalysisException {
		AnalysisFactory af = new AnalysisFactory();
		af.getAnalysis("TestGetAnalysisBothNull", null);
	}
	
	@Test(expected=AnalysisException.class)
	public void testGetAnalysisIDsEmpty() throws AnalysisException {
		AnalysisFactory af = new AnalysisFactory();
		af.getAnalysis("testGetAnalysisIDsEmpty", new int[]{});
	}

	@Test
	public void testRetrieveWebsiteResultsById() throws AnalysisException {
		AnalysisFactory af = new AnalysisFactory();
		List<WebsiteResult> results = af.retrieveWebsiteResultsById(new int[]{benchmarkWebsite.getId()});
		assertNotNull(results);
		assertEquals(results.size(), 1);
		assertTrue(results.contains(benchmarkWebsite));
	}

	@Test(expected=AnalysisException.class)
	public void testRetrieveWebsiteResultsByIdNull() throws AnalysisException {
		AnalysisFactory af = new AnalysisFactory();
		af.retrieveWebsiteResultsById(null);
	}
	
	@Test(expected=AnalysisException.class)
	public void testRetrieveWebsiteResultsByIdEmpty() throws AnalysisException {
		AnalysisFactory af = new AnalysisFactory();
		af.retrieveWebsiteResultsById(new int[]{});
	}
}
