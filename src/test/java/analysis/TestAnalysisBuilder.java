package test.java.analysis;

import static org.junit.Assert.*;

import java.util.List;

import main.java.TestingSuiteModule;
import main.java.analysis.AnalysisException;
import main.java.analysis.AnalysisBuilderImpl;
import main.java.analysis.Analysis;
import main.java.distributed.ConnectionManagerOrmImpl;
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
	private static ConnectionManagerOrmImpl connMgr;
	private static Injector injector;

	@BeforeClass
	public static void benchmarkWebsite() throws Exception {
		connMgr = new ConnectionManagerOrmImpl();
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
		AnalysisBuilderImpl af = injector.getInstance(AnalysisBuilderImpl.class);
		Analysis res = af.getAnalysis("testGetAnalysis", new int[]{benchmarkWebsite.getId()});
		assertNotNull(res);
		assertNotNull(res.getTitle());
		assertNotNull(res.getBenchmarkWebsites());
		assertEquals(res.getBenchmarkWebsites().size(),1);
		assertTrue(res.getBenchmarkWebsites().contains(benchmarkWebsite));
	}
	
	@Test
	public void testGetAnalysisTitleNull()  {
		AnalysisBuilderImpl af = injector.getInstance(AnalysisBuilderImpl.class);
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
		AnalysisBuilderImpl af = injector.getInstance(AnalysisBuilderImpl.class);
		af.getAnalysis("TestGetAnalysisIDsNull", null);
	}
	
	@Test(expected=AnalysisException.class)
	public void testGetAnalysisBothNull()  {
		AnalysisBuilderImpl af = injector.getInstance(AnalysisBuilderImpl.class);
		af.getAnalysis("TestGetAnalysisBothNull", null);
	}
	
	@Test(expected=AnalysisException.class)
	public void testGetAnalysisIDsEmpty()  {
		AnalysisBuilderImpl af = injector.getInstance(AnalysisBuilderImpl.class);
		af.getAnalysis("testGetAnalysisIDsEmpty", new int[]{});
	}

	@Test
	public void testRetrieveWebsiteResultsById()  {
		AnalysisBuilderImpl af = injector.getInstance(AnalysisBuilderImpl.class);
		List<WebsiteResult> results = af.retrieveWebsiteResultsById(new int[]{benchmarkWebsite.getId()});
		assertNotNull(results);
		assertEquals(results.size(), 1);
		assertTrue(results.contains(benchmarkWebsite));
	}

	@Test(expected=AnalysisException.class)
	public void testRetrieveWebsiteResultsByIdNull()  {
		AnalysisBuilderImpl af = injector.getInstance(AnalysisBuilderImpl.class);
		af.retrieveWebsiteResultsById(null);
	}
	
	@Test(expected=AnalysisException.class)
	public void testRetrieveWebsiteResultsByIdEmpty()  {
		AnalysisBuilderImpl af = injector.getInstance(AnalysisBuilderImpl.class);
		af.retrieveWebsiteResultsById(new int[]{});
	}
}
