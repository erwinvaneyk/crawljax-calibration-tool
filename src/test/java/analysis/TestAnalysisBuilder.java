package test.java.analysis;

import static org.junit.Assert.*;

import java.util.List;

import main.java.TestingSuiteModule;
import main.java.analysis.AnalysisException;
import main.java.analysis.AnalysisBuilder;
import main.java.analysis.Analysis;
import main.java.distributed.ConnectionManagerORM;
import main.java.distributed.results.WebsiteResult;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;

public class TestAnalysisBuilder {
	
	public final static String BenchmarkUrl = "http://demo.crawljax.com"; 
	public static WebsiteResult benchmarkWebsite;
	public static int benchmarkWebsiteID = 1;
	private static ConnectionManagerORM connMgr;
	private static Injector injector;

	@BeforeClass
	public static void benchmarkWebsite() throws Exception {
		connMgr = new ConnectionManagerORM();
		Dao<WebsiteResult,String> dao = DaoManager.createDao(connMgr.getConnectionORM(), WebsiteResult.class);
		benchmarkWebsite = dao.queryForId(String.valueOf(benchmarkWebsiteID));
		injector = Guice.createInjector(new TestingSuiteModule());
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		connMgr.closeConnection();
	}

	@Test
	public void testGetAnalysisValid()  {
		AnalysisBuilder af = injector.getInstance(AnalysisBuilder.class);
		Analysis res = af.getAnalysis("testGetAnalysis", new int[]{benchmarkWebsite.getId()});
		assertNotNull(res);
		assertNotNull(res.getTitle());
		assertNotNull(res.getBenchmarkWebsites());
		assertEquals(res.getBenchmarkWebsites().size(),1);
		assertTrue(res.getBenchmarkWebsites().contains(benchmarkWebsite));
	}
	
	@Test
	public void testGetAnalysisTitleNull()  {
		AnalysisBuilder af = injector.getInstance(AnalysisBuilder.class);
		Analysis res = af.getAnalysis(null, new int[]{benchmarkWebsite.getId()});
		assertNotNull(res);
		assertNotNull(res.getTitle());
		assertEquals(res.getTitle(), "");
		assertNotNull(res.getBenchmarkWebsites());
		assertEquals(res.getBenchmarkWebsites().size(),1);
		assertTrue(res.getBenchmarkWebsites().contains(benchmarkWebsite));
	}
	
	@Test(expected=AnalysisException.class)
	public void testGetAnalysisIDsNull()  {
		AnalysisBuilder af = injector.getInstance(AnalysisBuilder.class);
		af.getAnalysis("TestGetAnalysisIDsNull", null);
	}
	
	@Test(expected=AnalysisException.class)
	public void testGetAnalysisBothNull()  {
		AnalysisBuilder af = injector.getInstance(AnalysisBuilder.class);
		af.getAnalysis("TestGetAnalysisBothNull", null);
	}
	
	@Test(expected=AnalysisException.class)
	public void testGetAnalysisIDsEmpty()  {
		AnalysisBuilder af = injector.getInstance(AnalysisBuilder.class);
		af.getAnalysis("testGetAnalysisIDsEmpty", new int[]{});
	}

	@Test
	public void testRetrieveWebsiteResultsById()  {
		AnalysisBuilder af = injector.getInstance(AnalysisBuilder.class);
		List<WebsiteResult> results = af.retrieveWebsiteResultsById(new int[]{benchmarkWebsite.getId()});
		assertNotNull(results);
		assertEquals(results.size(), 1);
		assertTrue(results.contains(benchmarkWebsite));
	}

	@Test(expected=AnalysisException.class)
	public void testRetrieveWebsiteResultsByIdNull()  {
		AnalysisBuilder af = injector.getInstance(AnalysisBuilder.class);
		af.retrieveWebsiteResultsById(null);
	}
	
	@Test(expected=AnalysisException.class)
	public void testRetrieveWebsiteResultsByIdEmpty()  {
		AnalysisBuilder af = injector.getInstance(AnalysisBuilder.class);
		af.retrieveWebsiteResultsById(new int[]{});
	}
}
