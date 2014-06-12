package test.java.analysis;

import static org.junit.Assert.*;


import main.java.analysis.AnalysisException;
import main.java.analysis.AnalysisBuilderImpl;
import main.java.distributed.DatabaseUtils;
import main.java.distributed.results.WebsiteResult;
import main.java.distributed.workload.WorkloadDao;

import org.junit.Test;
import com.j256.ormlite.dao.Dao;
import static org.mockito.Mockito.*;

public class TestAnalysisBuilderImpl {
	
	public final static String BenchmarkUrl = "http://demo.crawljax.com"; 
	public static WebsiteResult benchmarkWebsite;
	public static int benchmarkWebsiteID = 1;

	
	@Test(expected=AnalysisException.class)
	public void testGetAnalysisIDsNull()  {
		Dao<WebsiteResult, String> websiteDao = mock(Dao.class);
		WorkloadDao wl = mock(WorkloadDao.class);
		DatabaseUtils du = mock(DatabaseUtils.class);
		AnalysisBuilderImpl af = new AnalysisBuilderImpl(websiteDao, wl, du);
		af.getAnalysis("TestGetAnalysisIDsNull", null);
	}
	
	@Test(expected=AnalysisException.class)
	public void testGetAnalysisBothNull()  {
		Dao<WebsiteResult, String> websiteDao = mock(Dao.class);
		WorkloadDao wl = mock(WorkloadDao.class);
		DatabaseUtils du = mock(DatabaseUtils.class);
		AnalysisBuilderImpl af = new AnalysisBuilderImpl(websiteDao, wl, du);
		af.getAnalysis("TestGetAnalysisBothNull", null);
	}
	
	@Test(expected=AnalysisException.class)
	public void testGetAnalysisIDsEmpty()  {
		Dao<WebsiteResult, String> websiteDao = mock(Dao.class);
		WorkloadDao wl = mock(WorkloadDao.class);
		DatabaseUtils du = mock(DatabaseUtils.class);
		AnalysisBuilderImpl af = new AnalysisBuilderImpl(websiteDao, wl, du);
		af.getAnalysis("testGetAnalysisIDsEmpty", new int[]{});
	}

	@Test(expected=AnalysisException.class)
	public void testRetrieveWebsiteResultsByIdNull()  {
		Dao<WebsiteResult, String> websiteDao = mock(Dao.class);
		WorkloadDao wl = mock(WorkloadDao.class);
		DatabaseUtils du = mock(DatabaseUtils.class);
		AnalysisBuilderImpl af = new AnalysisBuilderImpl(websiteDao, wl, du);
		af.retrieveWebsiteResultsById(null);
	}
	
	@Test(expected=AnalysisException.class)
	public void testRetrieveWebsiteResultsByIdEmpty()  {
		Dao<WebsiteResult, String> websiteDao = mock(Dao.class);
		WorkloadDao wl = mock(WorkloadDao.class);
		DatabaseUtils du = mock(DatabaseUtils.class);
		AnalysisBuilderImpl af = new AnalysisBuilderImpl(websiteDao, wl, du);
		af.retrieveWebsiteResultsById(new int[]{});
	}
}
